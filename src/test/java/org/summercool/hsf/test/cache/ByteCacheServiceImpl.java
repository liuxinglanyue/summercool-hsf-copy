package org.summercool.hsf.test.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Title: EhcacheServiceImpl.java
 * @Package org.summercool.hsf.test.ehcache
 * @Description: TODO(添加描述)
 * @author Administrator
 * @date 2012-3-19 下午10:50:10
 * @version V1.0
 */
public class ByteCacheServiceImpl implements ByteCacheService {

	private ConcurrentHashMap<String, byte[]> cache = new ConcurrentHashMap<String, byte[]>();

	@Override
	public boolean set(String key, byte[] value) {
		System.out.println("set cache(key:" + key + ", value:" + value + ")");
		cache.put(key, value);
		return true;
	}

	@Override
	public byte[] get(String key) {
		System.out.println("get cache(key:" + key + ")");
		return cache.get(key);
	}
}
