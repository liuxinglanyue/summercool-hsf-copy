package org.summercool.hsf.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Title: GexinUtils.java
 * @Package org.summercool.hsf.util
 * @Description: 通用工具处理类
 * @author 简道
 * @date 2011-8-4 下午12:01:25
 * @version V1.0
 */
public class LangUtil {
	static Logger logger = LoggerFactory.getLogger(LangUtil.class);

	public static Boolean parseBoolean(Object value) {
		if (value != null) {
			if (value instanceof Boolean) {
				return (Boolean) value;
			} else if (value instanceof String) {
				return Boolean.valueOf((String) value);
			}
		}
		return null;
	}

	public static boolean parseBoolean(Object value, boolean defaultValue) {
		if (value != null) {
			if (value instanceof Boolean) {
				return (Boolean) value;
			} else if (value instanceof String) {
				try {
					return Boolean.valueOf((String) value);
				} catch (Exception e) {
					logger.warn("parse boolean value({}) failed.", value);
				}
			}
		}
		return defaultValue;
	}

	/**
	 * @Title: parseInt
	 * @Description: Int解析方法，可传入Integer或String值
	 * @author 简道
	 * @param value
	 *        Integer或String值
	 * @return Integer 返回类型
	 */
	public static Integer parseInt(Object value) {
		if (value != null) {
			if (value instanceof Integer) {
				return (Integer) value;
			} else if (value instanceof String) {
				return Integer.valueOf((String) value);
			}
		}
		return null;
	}

	public static Integer parseInt(Object value, Integer defaultValue) {
		if (value != null) {
			if (value instanceof Integer) {
				return (Integer) value;
			} else if (value instanceof String) {
				try {
					return Integer.valueOf((String) value);
				} catch (NumberFormatException e) {
					logger.warn("parse Integer value({}) failed.", value);
				}
			}
		}
		return defaultValue;
	}

	/**
	 * @Title: getASCIIString
	 * @Description: 获取ASCII编码字符串
	 * @author 简道
	 * @param str
	 * @return String 返回类型
	 */
	public static String getASCIIString(String str) {
		if (str != null) {
			try {
				return URLEncoder.encode(str, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error(StackTraceUtil.getStackTrace(e));
			}
		}
		return null;
	}

	/**
	 * @Title: getUTF8String
	 * @Description: 获取UTF8编码字符串
	 * @author 简道
	 * @param str
	 * @return String 返回类型
	 */
	public static String getUTF8String(String str) {
		if (str != null) {
			try {
				return URLDecoder.decode(str, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error(StackTraceUtil.getStackTrace(e));
			}
		}
		return null;
	}

	/***
	 * 
	 * @Title: parseLong
	 * @Description: long解析方法，可传入Long或String值
	 * @author 简道
	 * @param value
	 *        Integer或String值
	 * @param @return
	 * @return Long 返回类型
	 */
	public static Long parseLong(Object value) {
		if (value != null) {
			if (value instanceof Long) {
				return (Long) value;
			} else if (value instanceof String) {
				return Long.valueOf((String) value);
			}
		}
		return null;
	}
	
	public static Long parseLong(Object value, Long defaultValue) {
		if (value != null) {
			if (value instanceof Long) {
				return (Long) value;
			} else if (value instanceof String) {
				try {
					return Long.valueOf((String) value);
				} catch (NumberFormatException e) {
					logger.warn("parse Long value({}) failed.", value);
				}
			}
		}
		return defaultValue;
	}

	/**
	 * @Title: parseDouble
	 * @Description: Double解析方法，可传入Double或String值
	 * @author 简道
	 * @param value
	 *        Double或String值
	 * @return Double 返回类型
	 */
	public static Double parseDouble(Object value) {
		if (value != null) {
			if (value instanceof Double) {
				return (Double) value;
			} else if (value instanceof String) {
				return Double.valueOf((String) value);
			}
		}
		return null;
	}

	/**
	 * @Title: toString
	 * @Description: toString实现，当对象为null时直接返回null
	 * @author 简道
	 * @param value
	 * @param @return
	 * @return String 返回类型
	 */
	public static String toString(Object value) {
		if (value == null) {
			return null;
		}

		return value.toString();
	}

	/**
	 * @Title: stringEquals
	 * @Description: 验证两个字符串是否相等
	 * @author 简道
	 * @param str1
	 * @param str2
	 * @return boolean 返回类型
	 */
	public static boolean stringEquals(String str1, String str2) {
		if (str1 == null || str2 == null) {
			return false;
		}

		return str1.equals(str2);
	}
}
