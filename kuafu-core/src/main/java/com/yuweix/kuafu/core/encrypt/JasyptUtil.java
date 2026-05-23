package com.yuweix.kuafu.core.encrypt;


import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.IvGenerator;
import org.jasypt.iv.NoIvGenerator;


/**
 * @author yuwei
 */
public abstract class JasyptUtil {
    /**
     * 默认前缀和后缀
     */
    private static final String DEFAULT_PREFIX = "ENC(";
    private static final String DEFAULT_SUFFIX = ")";


    /**
     * 解密
     */
    public static String decrypt(String password, String algorithm, String encryptedText) {
        return decrypt(password, algorithm, new NoIvGenerator(), DEFAULT_PREFIX, DEFAULT_SUFFIX, encryptedText);
    }
    public static String decrypt(String password, String algorithm, IvGenerator ivGenerator, String encryptedText) {
        return decrypt(password, algorithm, ivGenerator, DEFAULT_PREFIX, DEFAULT_SUFFIX, encryptedText);
    }
    public static String decrypt(String password, String algorithm, IvGenerator ivGenerator, String prefix, String suffix
            , String encryptedText) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(password);
        encryptor.setAlgorithm(algorithm);
        encryptor.setIvGenerator(ivGenerator);

        // 去掉前缀和后缀
        String cleanText = encryptedText.replace(prefix, "").replace(suffix, "");
        return encryptor.decrypt(cleanText);
    }

    /**
     * 加密
     */
    public static String encrypt(String password, String algorithm, String plainText) {
        return encrypt(password, algorithm, new NoIvGenerator(), DEFAULT_PREFIX, DEFAULT_SUFFIX, plainText);
    }
    public static String encrypt(String password, String algorithm, IvGenerator ivGenerator, String plainText) {
        return encrypt(password, algorithm, ivGenerator, DEFAULT_PREFIX, DEFAULT_SUFFIX, plainText);
    }
    public static String encrypt(String password, String algorithm, IvGenerator ivGenerator, String prefix, String suffix
            , String plainText) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(password);
        encryptor.setAlgorithm(algorithm);
        encryptor.setIvGenerator(ivGenerator);

        return prefix + encryptor.encrypt(plainText) + suffix;
    }
}
