package com.yuweix.kuafu.core.encrypt;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;


/**
 * AES加解密
 * @author yuwei
 */
public abstract class SecurityAesUtil {
	private static final Logger log = LoggerFactory.getLogger(SecurityAesUtil.class);

	
	/**
	 * 加密
	 * @param content 需要加密的内容
	 * @param password 消息加解密密钥
	 * @return
	 */
	public static String encode(String content, String password) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(password.getBytes());
			kgen.init(128, secureRandom);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			byte[] byteContent = content.getBytes(StandardCharsets.UTF_8);
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
			return parseByte2HexStr(cipher.doFinal(byteContent)); // 加密
		} catch (Exception ex) {
			log.error("encode>>>Error: {}", ex.getMessage(), ex);
		}
		return null;
	}

	/**解密
	 * @param content  待解密内容
	 * @param password 消息加解密密钥
	 * @return
	 */
	public static String decode(String content, String password) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(password.getBytes());
			kgen.init(128, secureRandom);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
			byte[] byteContent = parseHexStr2Byte(content);
			return byteContent == null ? null :new String(cipher.doFinal(byteContent)); // 加密
		} catch (Exception ex) {
			log.error("decode>>>Error: {}", ex.getMessage(), ex);
		}
		return null;
	}
	
	/**将二进制转换成16进制
	 * @param value
	 * @return
	 */
	private static String parseByte2HexStr(byte[] value) {
		StringBuilder builder = new StringBuilder("");
		for (int i = 0; i < value.length; i++) {
			String hex = Integer.toHexString(value[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			builder.append(hex.toLowerCase());
		}
		return builder.toString();
	}
	
	/**将16进制转换为二进制
	 * @param value
	 * @return
	 */
	private static byte[] parseHexStr2Byte(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		byte[] result = new byte[value.length() / 2];
		for (int i = 0; i < value.length() / 2; i++) {
			int high = Integer.parseInt(value.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(value.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}
}
