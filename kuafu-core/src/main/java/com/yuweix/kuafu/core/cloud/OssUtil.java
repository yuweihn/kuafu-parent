package com.yuweix.kuafu.core.cloud;


import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.*;
import com.yuweix.kuafu.core.io.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 阿里云OSS文件工具类
 * @author yuwei
 */
public class OssUtil {
	private static final Logger log = LoggerFactory.getLogger(OssUtil.class);

	private final String endpoint;
	private final String accessKey;
	private final String accessSecret;
	private final String bucketName;

	private volatile OSSClient ossClient = null;
	private final ReentrantLock ossClientLock = new ReentrantLock();


	public OssUtil(String endpoint, String accessKey, String accessSecret, String bucketName) {
		this.endpoint = Objects.requireNonNull(endpoint, "endpoint cannot be null");
		this.accessKey = Objects.requireNonNull(accessKey, "accessKey cannot be null");
		this.accessSecret = Objects.requireNonNull(accessSecret, "accessSecret cannot be null");
		this.bucketName = Objects.requireNonNull(bucketName, "bucketName cannot be null");
	}

	private OSSClient getOssClient() {
		if (ossClient == null) {
			ossClientLock.lock();
			try {
				if (ossClient == null) {
					ossClient = new OSSClient(this.endpoint
							, new DefaultCredentialProvider(accessKey, accessSecret), null);

					if (!ossClient.doesBucketExist(bucketName)) {
						CreateBucketRequest bucketRequest = new CreateBucketRequest(bucketName);
						bucketRequest.setCannedACL(CannedAccessControlList.PublicRead);
						ossClient.createBucket(bucketRequest);
					}
				}
			} catch (Exception ex) {
				log.error("Error on getOssClient: {}", ex.getMessage(), ex);
				throw new RuntimeException("Failed to initialize OSS client", ex);
			} finally {
				ossClientLock.unlock();
			}
		}
		return ossClient;
	}

	/**
	 * 删除一个Bucket和其中的文件
	 */
	public void deleteBucket() {
		if (!getOssClient().doesBucketExist(bucketName)) {
			return;
		}

		List<String> keyList = queryBucketKeyList();
		if (keyList != null && !keyList.isEmpty()) {
			for (String key : keyList) {
				try {
					deleteFile(key);
				} catch (Exception e) {
					log.error("Failed to delete file with key: {}, error: {}", key, e.getMessage());
				}
			}
		}

		try {
			getOssClient().deleteBucket(bucketName);
		} catch (Exception e) {
			log.error("Failed to delete bucket: {}, error: {}", bucketName, e.getMessage());
			throw new RuntimeException("Failed to delete bucket", e);
		}
	}

	/**
	 * 查询Bucket下所有的key
	 */
	public List<String> queryBucketKeyList() {
		List<String> list = new ArrayList<>();
		if (!getOssClient().doesBucketExist(bucketName)) {
			return list;
		}

		try {
			ObjectListing objListing = getOssClient().listObjects(bucketName);
			List<OSSObjectSummary> summaryList = objListing.getObjectSummaries();
			if (summaryList == null || summaryList.isEmpty()) {
				return list;
			}

			for (OSSObjectSummary summary : summaryList) {
				list.add(summary.getKey());
			}
		} catch (Exception e) {
			log.error("Failed to list objects in bucket: {}, error: {}", bucketName, e.getMessage());
			throw new RuntimeException("Failed to list objects in bucket", e);
		}
		return list;
	}

	/**
	 * 上传文件至OSS
	 * @param content 文件内容
	 * @param key     对象键，如：prd/readme.txt
	 * @return 文件的访问URL
	 */
	public String uploadFile(byte[] content, String key) {
		Objects.requireNonNull(content, "content cannot be null");
		Objects.requireNonNull(key, "key cannot be null");

		log.info("OSS upload file: key[{}]", key);

		try (ByteArrayInputStream bis = new ByteArrayInputStream(content)) {
			ObjectMetadata objMeta = new ObjectMetadata();
			objMeta.setContentLength(content.length);
			getOssClient().putObject(bucketName, key, bis, objMeta);

			String url = buildFileUrl(key);
			log.info("URL: {}", url);
			return url;
		} catch (Exception e) {
			log.error("Failed to upload file to OSS with key: {}, error: {}", key, e.getMessage());
			throw new RuntimeException("Failed to upload file to OSS", e);
		}
	}

	/**
	 * 构建文件访问URL
	 */
	private String buildFileUrl(String key) {
		String protocol = getProtocol(endpoint);
		// 移除endpoint中的协议部分
		String host = endpoint.startsWith("https://") ? endpoint.substring("https://".length()) :
				(endpoint.startsWith("http://") ? endpoint.substring("http://".length()) : endpoint);
		return protocol + bucketName + "." + host + "/" + key;
	}

	private String getProtocol(String endpoint) {
		if (endpoint != null && endpoint.startsWith("https://")) {
			return "https://";
		} else {
			return "http://";
		}
	}

	/**
	 * 从OSS下载文件
	 * @param key 对象键
	 * @return 文件内容的字节数组
	 */
	public byte[] downloadFile(String key) {
		Objects.requireNonNull(key, "key cannot be null");

		OSSObject ossObj = null;
		try {
			ossObj = getOssClient().getObject(new GetObjectRequest(bucketName, key));
			if (ossObj == null || ossObj.getObjectContent() == null) {
				return null;
			}
			return StreamUtil.read(ossObj.getObjectContent());
		} catch (Exception e) {
			log.error("Failed to download file from OSS with key: {}, error: {}", key, e.getMessage());
			throw new RuntimeException("Failed to download file from OSS", e);
		} finally {
			closeQuietly(ossObj);
		}
	}

	/**
	 * 从OSS下载文件到输出流
	 * @param key 对象键
	 * @param out 输出流
	 */
	public void downloadFile(String key, OutputStream out) {
		Objects.requireNonNull(key, "key cannot be null");
		Objects.requireNonNull(out, "output stream cannot be null");

		OSSObject ossObj = null;
		try {
			ossObj = getOssClient().getObject(new GetObjectRequest(bucketName, key));
			if (ossObj == null || ossObj.getObjectContent() == null) {
				return;
			}
			StreamUtil.write(ossObj.getObjectContent(), out);
		} catch (Exception e) {
			log.error("Failed to download file from OSS to Stream with key: {}, error: {}", key, e.getMessage());
			throw new RuntimeException("Failed to download file from OSS", e);
		} finally {
			closeQuietly(ossObj);
		}
	}

	/**
	 * 安静地关闭OSS对象
	 */
	private void closeQuietly(OSSObject ossObj) {
		if (ossObj == null) {
			return;
		}
		try {
			ossObj.close();
		} catch (IOException e) {
			log.error("Failed to close OSS object: {}", e.getMessage());
		}
	}

	/**
	 * 从OSS删除文件
	 * @param key 对象键
	 */
	public void deleteFile(String key) {
		Objects.requireNonNull(key, "key cannot be null");

		try {
			getOssClient().deleteObject(bucketName, key);
		} catch (Exception e) {
			log.error("Failed to delete file from OSS with key: {}, error: {}", key, e.getMessage());
			throw new RuntimeException("Failed to delete file from OSS", e);
		}
	}

	/**
	 * 关闭OSS客户端连接
	 */
	public void shutdown() {
		if (ossClient == null) {
			return;
		}
		ossClientLock.lock();
		try {
			if (ossClient != null) {
				ossClient.shutdown();
				ossClient = null;
			}
		} finally {
			ossClientLock.unlock();
		}
	}
}
