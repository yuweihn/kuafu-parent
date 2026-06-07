package com.yuweix.kuafu.core.serialize.hex;


import com.yuweix.kuafu.core.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


/**
 * 十六进制序列化工具
 * @author yuwei
 */
public class HexSerializer implements Serializer {
	private static final Logger log = LoggerFactory.getLogger(HexSerializer.class);


	/**
	 * 序列化
	 */
	@Override
	public <T>String serialize(T t) {
		if (t == null) {
			return null;
		}
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(t);
			return toHexString(baos.toByteArray());
		} catch (Exception ex) {
			log.error("Error on serialize: {}", ex.getMessage(), ex);
			return null;
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException ex) {
					log.error("oos.close失败, Error: {}", ex.getMessage(), ex);
				}
			}
			if (baos != null) {
				try {
					baos.close();
				} catch (IOException ex) {
					log.error("baos.close失败, Error: {}", ex.getMessage(), ex);
				}
			}
		}
	}

	/**
	 * 反序列化
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T>T deserialize(String str) {
		if (str == null) {
			return null;
		}
		byte[] bt = toByteArray(str);
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;
		try {
			bais = new ByteArrayInputStream(bt);
			ois = new ObjectInputStream(bais);
			return (T) ois.readObject();
		} catch (Exception ex) {
			log.error("Error on deserialize: {}", ex.getMessage(), ex);
			return null;
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException ex) {
					log.error("ois.close失败, Error: {}", ex.getMessage(), ex);
				}
			}
			if (bais != null) {
				try {
					bais.close();
				} catch (IOException ex) {
					log.error("bais.close失败, Error: {}", ex.getMessage(), ex);
				}
			}
		}
	}


	private static String toHexString(byte[] byteArray) {
		if (byteArray == null || byteArray.length < 1) {
			return null;
		}

		final StringBuilder hexString = new StringBuilder();
		for (byte b : byteArray) {
			if ((b & 0xff) < 0x10) {
				hexString.append("0");
			}
			hexString.append(Integer.toHexString(0xFF & b));
		}
		return hexString.toString().toLowerCase();
	}

	private static byte[] toByteArray(String hexString) {
		if (hexString == null || "".equals(hexString)) {
			return null;
		}

		hexString = hexString.toLowerCase();
		final byte[] byteArray = new byte[hexString.length() / 2];
		int k = 0;
		for (int i = 0; i < byteArray.length; i++) {
			byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
			byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
			byteArray[i] = (byte) (high << 4 | low);
			k += 2;
		}
		return byteArray;
	}
}
