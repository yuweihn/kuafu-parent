package com.yuweix.kuafu.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author yuwei
 */
public abstract class StrUtil {
	private static final Logger log = LoggerFactory.getLogger(StrUtil.class);

	public static final String[] CHAR_ARRAY = {"A", "B", "C", "D", "E", "F", "G"
			, "H", "I", "J", "K", "L", "M", "N"
			, "O", "P", "Q", "R", "S", "T"
			, "U", "V", "W", "X", "Y", "Z"
			, "a", "b", "b", "d", "e", "f", "g"
			, "h", "i", "j", "k", "l", "m", "n"
			, "o", "p", "q", "r", "s", "t"
			, "u", "v", "w", "x", "y", "z"
			, "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

	public static final String EMAIL_REG = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
	public static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REG);

	public static final Pattern MOBILE_PATTERN = Pattern.compile("^[1][3-9]\\d{9}$");

	public static final Pattern PHONE_PATTERN = Pattern.compile("^[\\d]{5,20}$");

	public static final String IP_REG = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
	public static final Pattern IP_PATTERN = Pattern.compile(IP_REG);


	public static void trim(String[] strs) {
		if (null == strs) {
			return;
		}
		String str;
		for (int i = 0; i < strs.length; i++) {
			str = strs[i];
			if (null != str) {
				strs[i] = trim(str);
			}
		}
	}
	public static String trim(CharSequence str) {
		return (null == str) ? null : trim(str, 0);
	}
	public static String trim(CharSequence str, int mode) {
		return trim(str, mode, StrUtil::isBlankChar);
	}

	public static String trim(CharSequence str, int mode, Predicate<Character> predicate) {
		if (str == null) {
			return null;
		}
		int length = str.length();
		int start = 0;
		int end = length;
		if (mode <= 0) {
			while ((start < end) && (predicate.test(str.charAt(start)))) {
				start++;
			}
		}
		if (mode >= 0) {
			while ((start < end) && (predicate.test(str.charAt(end - 1)))) {
				end--;
			}
		}
		String result;
		if ((start > 0) || (end < length)) {
			result = str.toString().substring(start, end);
		} else {
			result = str.toString();
		}
		return result;
	}

	public static boolean isBlankChar(char c) {
		return isBlankChar((int) c);
	}
	/**
	 * 是否空白符
	 * 空白符包括空格、制表符、全角空格和不间断空格
	 */
	public static boolean isBlankChar(int c) {
		return Character.isWhitespace(c)
				|| Character.isSpaceChar(c)
				|| c == '\ufeff'
				|| c == '\u202a'
				|| c == '\u0000'
				|| c == '\u3164'
				|| c == '\u2800';
	}

	/**
	 * 按指定长度截取字符串(一个汉字相当于两个字符)，超过的以省略号代替
	 * @param str
	 * @param len
	 * @return
	 */
	public static String cut(String str, int len) {
		if (str == null || str.length() <= 0 || len <= 0) {
			return "";
		}

		StringBuilder builder = new StringBuilder("");
		int count = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			count += isChineseChar(c) ? 2 : 1;

			if (count > len) {
				builder.append("......");
				break;
			}
			builder.append(c);
		}

		return builder.toString();
	}

	/**
	 * 是否是中文字符
	 * @param c
	 * @return
	 */
	public static boolean isChineseChar(char c) {
		return Character.isLetter(c) && c > 255;
	}

	/**
	 * 取得指定长度的随机码
	 * @param length
	 * @return
	 */
	public static String getRandCode(int length) {
		Random random = new SecureRandom();
		StringBuilder builder = new StringBuilder("");
		for (int i = 0; i < length; i++) {
			builder.append(CHAR_ARRAY[random.nextInt(CHAR_ARRAY.length)]);
		}
		return builder.toString();
	}

	/**
	 * 是否是Email
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email) {
		if (email == null || "".equals(email.trim())) {
			return false;
		}
		return EMAIL_PATTERN.matcher(email.trim()).find();
	}

	/**
	 * 是否是手机号码
	 * @param mobile
	 * @return
	 */
	public static boolean isMobile(String mobile) {
		if (mobile == null || "".equals(mobile.trim())) {
			return false;
		}
		return MOBILE_PATTERN.matcher(mobile.trim()).find();
	}

	/**
	 * 是否是电话号码
	 * @param phone
	 * @return
	 */
	public static boolean isPhone(String phone) {
		if (phone == null || "".equals(phone.trim())) {
			return false;
		}
		return PHONE_PATTERN.matcher(phone.trim()).find();
	}

	public static boolean isIp(String ip) {
		if (ip == null || "".equals(ip.trim())) {
			return false;
		}
		return IP_PATTERN.matcher(ip.trim()).matches();
	}

	/**
	 * 以纯文本形式显示时的过滤逻辑
	 * @param str
	 * @return
	 */
	public static String escape(String str) {
		if (str == null || str.length() <= 0) {
			return "";
		}

		return str.trim().replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "\\\"")
				.replace("\r\n", "<br/>")
				.replace("\n", "<br/>");
	}

	/**
	 * 需要显示html标签时的过滤逻辑
	 * @param str
	 * @return
	 */
	public static String escape2(String str) {
		if (str == null || str.length() <= 0) {
			return "";
		}

		return str.trim().replace("\"", "\\\"")
				.replace("\r\n", "<br/>")
				.replace("\n", "<br/>");
	}

	public static<T> String join(T[] arr, String separator) {
		if (arr == null || arr.length <= 0) {
			return null;
		}

		StringBuilder builder = new StringBuilder("");
		for (T t: arr) {
			builder.append(separator).append(t.toString());
		}
		builder.delete(0, separator.length());
		return builder.toString();
	}

	/**
	 * 下划线转驼峰
	 * @param str
	 * @return
	 */
	public static String toCamel(String str) {
		Pattern pattern = Pattern.compile("_(\\w)");
		Matcher matcher = pattern.matcher(str);
		StringBuffer buf = new StringBuffer(str);
		if (!matcher.find()) {
			return buf.toString();
		}
		buf = new StringBuffer();
		matcher.appendReplacement(buf, matcher.group(1).toUpperCase());
		matcher.appendTail(buf);
		String res = toCamel(buf.toString());
		res = res.substring(0, 1).toUpperCase() + res.substring(1);
		return res;
	}

	/**
	 * 驼峰转下划线
	 * @param str
	 * @return
	 */
	public static String toUnderline(String str) {
		Pattern pattern = Pattern.compile("[A-Z]");
		Matcher matcher = pattern.matcher(str);
		StringBuffer buf = new StringBuffer(str);
		if (!matcher.find()) {
			return buf.toString();
		}
		buf = new StringBuffer();
		matcher.appendReplacement(buf, "_" + matcher.group(0).toLowerCase());
		matcher.appendTail(buf);
		String res = toUnderline(buf.toString());
		if (res.startsWith("_")) {
			res = res.substring(1);
		}
		return res;
	}

	/**
	 * 将源集合中的所有字符串，按指定符号分拆
	 * eg.    ["abc,ef", "123,45"] ===>>> ["abc", "ef", "123", "45"]
	 */
	public static List<String> split(List<String> sources, String regex) {
		List<String> targets = new ArrayList<>();
		if (sources == null || sources.size() <= 0) {
			return targets;
		}
		for (String src: sources) {
			if (src == null) {
				continue;
			}
			if (src.contains(regex)) {
				String[] arr = src.split(regex);
				for (String s: arr) {
					String s0 = s.trim();
					if (!"".equals(s0)) {
						targets.add(s0);
					}
				}
			} else {
				targets.add(src);
			}
		}
		return targets;
	}
}
