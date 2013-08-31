package org.summercool.hsf.util;

import java.util.HashMap;

/**
 * @ClassName: TLSUtil
 * @Description: 线程本地存储(TLS)辅助类
 * @author 简道
 * @date 2011-9-29 下午1:29:21
 * 
 */
public class TLSUtil {
	public static final String HSF_GROUP = "HSF_GROUP";
	
	private static ThreadLocal<HashMap<Object, Object>> threadLocal = new ThreadLocal<HashMap<Object, Object>>();

	/**
	 * @Title: setData
	 * @Description: 存储对象
	 * @author 简道
	 * @param key
	 * @param value
	 * @return void 返回类型
	 */
	public static void setData(Object key, Object value) {
		HashMap<Object, Object> map = getMap();
		map.put(key, value);
	}

	/**
	 * @Title: getData
	 * @Description: 获取对象
	 * @author 简道
	 * @param key
	 * @return Object 返回类型
	 */
	public static Object getData(Object key) {
		HashMap<Object, Object> map = getMap();
		return map.get(key);
	}

	/**
	 * @Title: remove
	 * @Description: 删除对象
	 * @author 简道
	 * @param key
	 * @return Object 返回类型
	 */
	public static Object remove(Object key) {
		HashMap<Object, Object> map = threadLocal.get();
		if (map != null) {
			return map.remove(key);
		}
		return null;
	}

	private static HashMap<Object, Object> getMap() {
		HashMap<Object, Object> map = threadLocal.get();
		if (map == null) {
			map = new HashMap<Object, Object>();
			threadLocal.set(map);
		}
		return map;
	}
}
