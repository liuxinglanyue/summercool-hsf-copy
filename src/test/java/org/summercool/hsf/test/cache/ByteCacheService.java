package org.summercool.hsf.test.cache;

import org.summercool.hsf.annotation.RemoteServiceContract;

/**
 * @Title: EhcacheService.java
 * @Package org.summercool.hsf.test.ehcache
 * @Description: TODO(添加描述)
 * @author Administrator
 * @date 2012-3-19 下午10:49:24
 * @version V1.0
 */
@RemoteServiceContract
interface ByteCacheService {
	public boolean set(String key, byte[] value);
	
	public byte[] get(String key);
}
