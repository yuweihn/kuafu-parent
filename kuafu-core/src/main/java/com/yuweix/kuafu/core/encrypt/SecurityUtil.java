package com.yuweix.kuafu.core.encrypt;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;


/**
 * 加密工具
 * @author yuwei
 */
public abstract class SecurityUtil {
	private static final Logger log = LoggerFactory.getLogger(SecurityUtil.class);

	private static final String SECURITY_KEY = "sfdfyu8**((^$$$SDSDhHJlSDDsdsvcx234ex,,,cjv.xckv...";
	private static final char[] HEX_DIGIT = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final int AES_KEY_LENGTH = 16;
	private static final int IV_LENGTH = 16;


	public static String getMd5(String str) {
		return getMd5(str, UTF_8.name());
	}
	public static String getMd5(String str, String charset) {
		return getSecurityByAlgor(Algor.MD5.getCode(), str, charset);
	}

	public static String getSha1(String str) {
		return getSha1(str, UTF_8.name());
	}
	public static String getSha1(String str, String charset) {
		return getSecurityByAlgor(Algor.SHA1.getCode(), str, charset);
	}

	public static String getSha256(String str) {
		return getSha256(str, UTF_8.name());
	}
	public static String getSha256(String str, String charset) {
		return getSecurityByAlgor(Algor.SHA256.getCode(), str, charset);
	}

	public static String getSecurityByAlgor(String algor, String str, String charset) {
		try {
			byte[] tmp = str.getBytes(charset == null ? UTF_8.name() : charset);
			MessageDigest mdTemp = MessageDigest.getInstance(algor);
			mdTemp.update(tmp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char[] arr = new char[j << 1];
			int k = 0;
			for (byte byte0 : md) {
				arr[k++] = HEX_DIGIT[byte0 >>> 4 & 0xf];
				arr[k++] = HEX_DIGIT[byte0 & 0xf];
			}
			String val = new String(arr).toLowerCase();
			log.debug(val);
			return val;
		} catch (Exception ex) {
			log.error("getSecurityByAlgor>>>Error: {}", ex.getMessage(), ex);
			return null;
		}
	}


	public static String encode(String str) {
		if (str == null) {
			return null;
		}
		String str0 = encrypt(str);

		String ori = str0 + "," + getMd5(SECURITY_KEY);
		String secr = getMd5(ori);
		return encrypt(str0 + "," + secr);
	}

	public static String decode(String str) {
		if (str == null) {
			return null;
		}

		try {
			String[] arr = Objects.requireNonNull(decrypt(str)).split(",");
			if (arr.length != 2) {
				return null;
			}
			if (arr[1].equals(getMd5(arr[0] + "," + getMd5(SECURITY_KEY)))) {
				return decrypt(arr[0]);
			} else {
				return null;
			}
		} catch (Exception ex) {
			log.error("decode>>>Error: {}", ex.getMessage(), ex);
			return null;
		}
	}

	private static String encrypt(String word) {
		try {
			byte[] keyBytes = buildAESKey(getMd5(SECURITY_KEY));
			byte[] iv = Arrays.copyOf(keyBytes, IV_LENGTH);
			SecretKey key = new SecretKeySpec(keyBytes, "AES");

			Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
			return bytesToHexStr(cipher.doFinal(word.getBytes(UTF_8)));
		} catch (Exception ex) {
			log.error("encrypt>>>Error: {}", ex.getMessage(), ex);
			return null;
		}
	}

	private static String decrypt(String word) {
		try {
			byte[] input = hexStrToBytes(word);
			if (input == null) {
				return null;
			}
			byte[] keyBytes = buildAESKey(getMd5(SECURITY_KEY));
			byte[] iv = Arrays.copyOf(keyBytes, IV_LENGTH);
			SecretKey key = new SecretKeySpec(keyBytes, "AES");

			Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
			return new String(cipher.doFinal(input), UTF_8);
		} catch (Exception ex) {
			log.error("decrypt>>>Error: {}", ex.getMessage(), ex);
			return null;
		}
	}

	private static byte[] buildAESKey(String md5Hex) {
		byte[] raw = md5Hex.getBytes(UTF_8);
		byte[] key = new byte[AES_KEY_LENGTH];
		System.arraycopy(raw, 0, key, 0, Math.min(raw.length, AES_KEY_LENGTH));
		return key;
	}

	/**
	 * 十六进制字符串转byte数组
	 * @param value
	 * @return
	 */
	public static byte[] hexStrToBytes(String value) {
		if (value == null || value.isEmpty() || value.length() % 2 != 0) {
			return null;
		}
		byte[] result = new byte[value.length() / 2];
		for (int i = 0; i < result.length; i++) {
			int high = Character.digit(value.charAt(i * 2), 16);
			int low = Character.digit(value.charAt(i * 2 + 1), 16);
			if (high == -1 || low == -1) {
				return null;
			}
			result[i] = (byte) ((high << 4) | low);
		}
		return result;
	}

	/**
	 * byte数组转十六进制字符串
	 * @param value
	 * @return
	 */
	public static String bytesToHexStr(byte[] value) {
		if (value == null) {
			return "";
		}
		StringBuilder builder = new StringBuilder(value.length * 2);
		for (byte b : value) {
			String hex = Integer.toHexString(b & 0xFF);
			if (hex.length() == 1) {
				builder.append('0');
			}
			builder.append(hex);
		}
		return builder.toString();
	}
}
