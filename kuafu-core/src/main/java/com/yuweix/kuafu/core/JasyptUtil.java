package com.yuweix.kuafu.core;


import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentPBEConfig;


/**
 * @author yuwei
 */
public abstract class JasyptUtil {
	/**
	 * PBE 算法
	 */
	public static final String PBE_ALGORITHMS_MD5_DES = "PBEWITHMD5ANDDES";
	public static final String PBE_ALGORITHMS_MD5_TRIPLEDES = "PBEWITHMD5ANDTRIPLEDES";
	public static final String PBE_ALGORITHMS_SHA1_DESEDE = "PBEWITHSHA1ANDDESEDE";
	public static final String PBE_ALGORITHMS_SHA1_RC2_40 = "PBEWITHSHA1ANDRC2_40";

	private JasyptUtil() {

	}

	public static String encrypt(String encryptedStr, String password) {
		return encrypt(encryptedStr, PBE_ALGORITHMS_MD5_DES, password);
	}

	public static String encrypt(String encryptedStr, String algorithm, String password) {
		// StandardPBEStringEncryptor、StandardPBEBigDecimalEncryptor、StandardPBEBigIntegerEncryptor、StandardPBEByteEncryptor
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		EnvironmentPBEConfig config = new EnvironmentPBEConfig();

		// 指定加密算法
		config.setAlgorithm(algorithm);
		// 加密盐值
		config.setPassword(password);
		//config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
		encryptor.setConfig(config);

		// 加密
		return encryptor.encrypt(encryptedStr);
	}

	public static String decrypt(String decryptStr, String password) {
		return decrypt(decryptStr, PBE_ALGORITHMS_MD5_DES, password);
	}

	public static String decrypt(String decryptStr, String algorithm, String password) {
		// StandardPBEStringEncryptor、StandardPBEBigDecimalEncryptor、StandardPBEBigIntegerEncryptor、StandardPBEByteEncryptor
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		EnvironmentPBEConfig config = new EnvironmentPBEConfig();

		// 指定解密算法：解密算法要与加密算法一一对应
		config.setAlgorithm(algorithm);
		// 加密秘钥
		config.setPassword(password);
		//config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
		encryptor.setConfig(config);

		// 解密
		return encryptor.decrypt(decryptStr);
	}
}
