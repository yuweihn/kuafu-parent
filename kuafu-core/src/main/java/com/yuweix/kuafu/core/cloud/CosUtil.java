package com.yuweix.kuafu.core.cloud;


import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.yuweix.kuafu.core.io.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 腾讯云COS文件工具类
 * @author yuwei
 */
public class CosUtil implements AutoCloseable {
	private static final Logger log = LoggerFactory.getLogger(CosUtil.class);
	private String secretId;
	private String secretKey;
	private String region;
	private String endpoint;
	private String bucketName;

	private volatile COSClient cosClient = null;
	private final ReentrantLock cosClientLock = new ReentrantLock();


	public CosUtil(String secretId, String secretKey, String region, String bucketName) {
		this(secretId, secretKey, region, null, bucketName);
	}

	public CosUtil(String secretId, String secretKey, String region, String protocol, String bucketName) {
		this.secretId = Objects.requireNonNull(secretId, "secretId cannot be null");
		this.secretKey = Objects.requireNonNull(secretKey, "secretKey cannot be null");
		this.region = Objects.requireNonNull(region, "region cannot be null");

		if (protocol == null || protocol.isEmpty()) {
			protocol = "http";
		}

		this.endpoint = protocol + "://" + bucketName + ".cos." + region + ".myqcloud.com";
		this.bucketName = Objects.requireNonNull(bucketName, "bucketName cannot be null");
	}

	private COSClient getCosClient() {
		if (cosClient == null) {
			cosClientLock.lock();
			try {
				if (cosClient == null) {
					COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
					ClientConfig clientConfig = new ClientConfig(new Region(region));
					cosClient = new COSClient(cred, clientConfig);

					if (!cosClient.doesBucketExist(bucketName)) {
						CreateBucketRequest bucketRequest = new CreateBucketRequest(bucketName);
						bucketRequest.setCannedAcl(CannedAccessControlList.PublicRead);
						cosClient.createBucket(bucketRequest);
					}
				}
			} catch (Exception ex) {
				log.error("Error on getCosClient: {}", ex.getMessage(), ex);
				throw new RuntimeException("初始化COS客户端失败", ex);
			} finally {
				cosClientLock.unlock();
			}
		}
		return cosClient;
	}

	/**
	 * 删除一个Bucket和其中的文件
	 */
	public void deleteBucket() {
		if (!getCosClient().doesBucketExist(bucketName)) {
			return;
		}

		List<String> keyList = queryBucketKeyList();
		if (keyList != null && !keyList.isEmpty()) {
			for (String key : keyList) {
				try {
					deleteFile(key);
				} catch (Exception e) {
					log.error("删除文件失败, key: {}, error: {}", key, e.getMessage());
				}
			}
		}

		try {
			getCosClient().deleteBucket(bucketName);
		} catch (Exception e) {
			log.error("删除bucket失败: {}, error: {}", bucketName, e.getMessage());
			throw new RuntimeException("删除bucket失败", e);
		}
	}

	/**
	 * 查询Bucket下所有的key
	 */
	public List<String> queryBucketKeyList() {
		Objects.requireNonNull(bucketName, "bucketName cannot be null");

		List<String> list = new ArrayList<>();
		if (!getCosClient().doesBucketExist(bucketName)) {
			return list;
		}

		try {
			ObjectListing objListing = getCosClient().listObjects(bucketName);
			List<COSObjectSummary> summaryList = objListing.getObjectSummaries();
			if (summaryList == null || summaryList.isEmpty()) {
				return list;
			}

			for (COSObjectSummary summary : summaryList) {
				list.add(summary.getKey());
			}
		} catch (Exception e) {
			log.error("查询bucket对象列表失败: {}, error: {}", bucketName, e.getMessage());
			throw new RuntimeException("查询bucket对象列表失败", e);
		}
		return list;
	}

	/**
	 * 上传文件至COS
	 * @param content
	 * @param key                 如：prd/readme.txt
	 */
	public String uploadFile(byte[] content, String key) {
		Objects.requireNonNull(content, "content cannot be null");
		Objects.requireNonNull(key, "key cannot be null");
		log.info("COS upload file: key[{}]", key);
		try (ByteArrayInputStream bis = new ByteArrayInputStream(content)) {
			ObjectMetadata objMeta = new ObjectMetadata();
			objMeta.setContentLength(content.length);
			getCosClient().putObject(bucketName, key, bis, objMeta);

			String url = buildFileUrl(key);
			log.info("URL: {}", url);
			return url;
		} catch (IOException ex) {
			log.error("上传文件失败, Error: {}", ex.getMessage(), ex);
			throw new RuntimeException("上传文件到COS失败", ex);
		}
	}

	/**
	 * 构建文件访问URL
	 */
	private String buildFileUrl(String key) {
		return endpoint + "/" + key;
	}

	/**
	 * 从COS下载文件
	 * @param key
	 */
	public byte[] downloadFile(String key) {
		Objects.requireNonNull(key, "key cannot be null");
		log.info("COS下载文件: key[{}]", key);
		COSObject cosObj = null;
		try {
			cosObj = getCosClient().getObject(new GetObjectRequest(bucketName, key));
			if (cosObj == null || cosObj.getObjectContent() == null) {
				log.error("COS文件不存在或内容为空: {}", key);
				return null;
			}
			byte[] data = StreamUtil.read(cosObj.getObjectContent());
			log.info("COS下载成功, key: {}, size: {} bytes", key, data.length);
			return data;
		} catch (Exception e) {
			log.error("COS下载文件失败, key: {}, error: {}", key, e.getMessage(), e);
			throw new RuntimeException("从COS下载文件失败", e);
		} finally {
			closeQuietly(cosObj);
		}
	}
	public void downloadFile(String key, OutputStream out) {
		Objects.requireNonNull(key, "key cannot be null");
		Objects.requireNonNull(out, "output stream cannot be null");

		log.info("COS下载文件到流: key[{}]", key);

		COSObject cosObj = null;
		try {
			cosObj = getCosClient().getObject(new GetObjectRequest(bucketName, key));
			if (cosObj == null || cosObj.getObjectContent() == null) {
				log.error("COS下载文件到流, COS文件不存在或内容为空: {}", key);
				return;
			}
			InputStream in = cosObj.getObjectContent();
			StreamUtil.write(in, out);
			log.info("COS下载文件到流成功: {}", key);
		} catch (Exception e) {
			log.error("COS下载文件到流失败, key: {}, error: {}", key, e.getMessage(), e);
			throw new RuntimeException("从COS下载文件失败", e);
		} finally {
			closeQuietly(cosObj);
		}
	}

	private void closeQuietly(COSObject cosObj) {
		if (cosObj == null) {
			return;
		}
		try {
			cosObj.close();
		} catch (IOException e) {
			log.error("关闭COS对象失败: {}", e.getMessage());
		}
	}

	/**
	 * 从COS删除文件
	 * @param key
	 */
	public void deleteFile(String key) {
		Objects.requireNonNull(key, "key cannot be null");
		log.info("COS删除文件: key[{}]", key);
		try {
			getCosClient().deleteObject(bucketName, key);
			log.info("COS删除文件成功: {}", key);
		} catch (Exception ex) {
			log.error("COS删除文件失败, key: {}, error: {}", key, ex.getMessage(), ex);
			throw new RuntimeException("从COS删除文件失败", ex);
		}
	}

	public void shutdown() {
		if (cosClient == null) {
			return;
		}
		cosClientLock.lock();
		try {
			if (cosClient != null) {
				cosClient.shutdown();
				cosClient = null;
			}
		} finally {
			cosClientLock.unlock();
		}
	}

	@Override
	public void close() {
		shutdown();
	}
}
