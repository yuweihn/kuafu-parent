package com.yuweix.kuafu.core.io;


import com.yuweix.kuafu.core.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;


/**
 * 文件处理工具
 * @author yuwei
 */
public abstract class FileUtil extends StreamUtil {
	private static final Logger log = LoggerFactory.getLogger(FileUtil.class);


	/**
	 * 读取文件内容
	 * @param filePath
	 */
	public static String getFileContent(String filePath) {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader bf = null;
		try {
			fis = new FileInputStream(filePath);
			isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
			bf = new BufferedReader(isr);
			StringBuilder builder = new StringBuilder("");
			String line;
			do {
				line = bf.readLine();
				if (line != null) {
					if (builder.length() != 0) {
						builder.append("\n");
					}
					builder.append(line);
				}
			} while (line != null);
			return builder.toString();
		} catch (Exception ex) {
			log.error("读取文件内容失败, Error: {}", ex.getMessage(), ex);
			throw new RuntimeException(ex);
		} finally {
			if (bf != null) {
				try {
					bf.close();
				} catch (IOException ex) {
					log.error("bf.close失败, Error: {}", ex.getMessage(), ex);
				}
			}
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException ex) {
					log.error("isr.close失败, Error: {}", ex.getMessage(), ex);
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ex) {
					log.error("fis.close失败, Error: {}", ex.getMessage(), ex);
				}
			}
		}
	}

	/**
	 * 保存文件，返回存放路径+文件全名。
	 * @param content    文件内容
	 * @param rootDir    存放文件的一级目录
	 * @param subDir     存放文件的二级目录
	 * @param fileName   文件名(含扩展名)
	 * 
	 * eg.
	 * rootDir: /wx-resource
	 * subDir: /user
	 * fileName: 8149a013aef24114a8e3d10ba2ea5f1f.txt
	 * 返回：如"/user/8149a013aef24114a8e3d10ba2ea5f1f.txt"
	 */
	public static String write(byte[] content, String rootDir, String subDir, String fileName) {
		subDir = subDir == null || subDir.trim().equals("") ? "" : subDir.trim();
		String dateDir = DateUtil.formatDate(new Date(), DateUtil.PATTERN_DATE2);
		subDir = subDir + "/" + dateDir;

		write(content, rootDir + subDir + "/" + fileName);
		return subDir + "/" + fileName;
	}

	/**
	 * 保存文件
	 * @param content
	 * @param fullFileName    文件全路径
	 */
	public static void write(byte[] content, String fullFileName) {
		File file = new File(fullFileName);
		File dir = file.getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}

		OutputStream fos = null;
		OutputStream bos = null;
		try {
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(content);
		} catch (Exception ex) {
			log.error("写入文件失败, Error: {}", ex.getMessage(), ex);
			throw new RuntimeException(ex);
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException ex) {
					log.error("bos.close失败, Error: {}", ex.getMessage(), ex);
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ex) {
					log.error("fos.close失败, Error: {}", ex.getMessage(), ex);
				}
			}
		}
	}

	/**
	 * /.     ===>   /
	 * /..    ===>   /
	 * /path/.   ===>   /path
	 * /path/..  ===>   /
	 */
	public static String simplifyPath(String path) {
		if (path == null) {
			return null;
		}

		path = path.trim();
		if (path.isEmpty()) {
			return "/";
		}

		String[] parts = path.split("/");
		java.util.Deque<String> stack = new java.util.ArrayDeque<>();

		for (String part : parts) {
			if (part.isEmpty() || ".".equals(part)) {
				continue;
			}
			if ("..".equals(part)) {
				if (!stack.isEmpty()) {
					stack.pollLast();
				}
			} else {
				stack.addLast(part);
			}
		}

		StringBuilder result = new StringBuilder();
		for (String dir : stack) {
			result.append("/").append(dir);
		}

		return !result.isEmpty() ? result.toString() : "/";
	}

	public static String buildAsciiFileName(String fileName) {
		if (fileName == null) {
			return null;
		}
		fileName = fileName.trim();
		StringBuilder sb = new StringBuilder(fileName.length());
		for (int i = 0; i < fileName.length(); i++) {
			char c = fileName.charAt(i);
			sb.append(c <= 0x7F ? c : '_');
		}
		return sb.toString();
	}
}
