package com.yuweix.kuafu.core.csv;


import com.yuweix.kuafu.core.ActionUtil;
import com.yuweix.kuafu.core.DateUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author yuwei
 */
public abstract class CsvUtil {
	private static final Logger log = LoggerFactory.getLogger(CsvUtil.class);

	private static final String CSV_CONTENT_TYPE = "text/csv";


	public static<T> void export(List<T> dataList, String fileName, HttpServletResponse resp) {
		export(dataList, fileName, CSV_CONTENT_TYPE, resp);
	}
	public static<T> void export(List<T> dataList, String fileName, String contentType, HttpServletResponse resp) {
		byte[] bytes = export(dataList);
		ActionUtil.download(bytes, fileName, contentType, null, resp);
	}

	/**
	 * 导出数据
	 * @param dataList 数据
	 */
	public static<T> byte[] export(List<T> dataList) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			export(dataList, out);
			return out.toByteArray();
		} catch (IOException ex) {
			log.error("export失败, Error: {}", ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 导出数据到输出流
	 * @param dataList 数据
	 */
	public static<T> void export(List<T> dataList, OutputStream out) {
		log.info("list size: {}", dataList == null ? 0 : dataList.size());
		if (dataList == null || dataList.size() <= 0) {
			return;
		}
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		try {
			//追加BOM标识，防止Excel打开时乱码
			out.write(0xef);
			out.write(0xbb);
			out.write(0xbf);

			osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
			bw = new BufferedWriter(osw);

			/**
			 * 输出头部
			 **/
			List<String> headList = getOutputHeadList(dataList.get(0));
			for (String head: headList) {
				bw.append(head).append(",");
			}
			bw.newLine();

			List<String> keyList = getOutputKeyList(dataList.get(0));
			/**
			 * 输出数据部分
			 **/
			for (T t: dataList) {
				List<Object> dList = getOutputDataList(keyList, t);
				for (Object d: dList) {
					bw.append(convertToString(d)).append(",");
				}
				bw.newLine();
			}
		} catch (Exception ex) {
			log.error("导出数据时发生异常, Error: {}", ex.getMessage(), ex);
			throw new RuntimeException(ex);
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException ex) {
					log.error("bw.close失败, Error: {}", ex.getMessage(), ex);
				}
			}
			if (osw != null) {
				try {
					osw.close();
				} catch (IOException ex) {
					log.error("osw.close失败, Error: {}", ex.getMessage(), ex);
				}
			}
		}
	}

	private static<T> List<String> getOutputHeadList(T t) {
		List<String> list = new ArrayList<>();
		if (Map.class.isAssignableFrom(t.getClass())) {
			Map<?, ?> map = (Map<?, ?>) t;
			for (Object key : map.keySet()) {
				list.add(key.toString());
			}
		} else {
			Field[] fields = t.getClass().getDeclaredFields();
			if (fields != null && fields.length > 0) {
				for (Field field: fields) {
					CsvKey csvKeyAno = field.getAnnotation(CsvKey.class);
					if (csvKeyAno != null) {
						list.add(csvKeyAno.value() == null || "".equals(csvKeyAno.value().trim()) ? field.getName() : csvKeyAno.value());
					}
				}
			}
		}
		return list;
	}

	private static<T> List<String> getOutputKeyList(T t) {
		List<String> list = new ArrayList<>();
		if (Map.class.isAssignableFrom(t.getClass())) {
			Map<?, ?> map = (Map<?, ?>) t;
			for (Object key : map.keySet()) {
				list.add(key.toString());
			}
		} else {
			Field[] fields = t.getClass().getDeclaredFields();
			if (fields != null && fields.length > 0) {
				for (Field field: fields) {
					CsvKey csvKeyAno = field.getAnnotation(CsvKey.class);
					if (csvKeyAno != null) {
						list.add(field.getName());
					}
				}
			}
		}
		return list;
	}

	private static<T> List<Object> getOutputDataList(List<String> keyList, T t) {
		Assert.notEmpty(keyList, "[keyList] must not be empty.");
		List<Object> list = new ArrayList<>();

		if (Map.class.isAssignableFrom(t.getClass())) {
			Map<?, ?> map = (Map<?, ?>) t;
			for (String key: keyList) {
				list.add(map.get(key));
			}
		} else {
			for (String key: keyList) {
				PropertyDescriptor pd = null;
				try {
					pd = new PropertyDescriptor(key, t.getClass());
					Method getMethod = pd.getReadMethod();
					Object o = getMethod.invoke(t);
					list.add(o);
				} catch (Exception ex) {
					log.error("getOutputDataList获取属性值失败, Error: {}", ex.getMessage(), ex);
				}
			}
		}
		return list;
	}

	private static String convertToString(Object value) {
		if (value == null) {
			return "";
		}
		if (value instanceof Date) {
			return DateUtil.formatDate((Date) value, "yyyy-MM-dd HH:mm:ss");
		}
		return value.toString();
	}
}
