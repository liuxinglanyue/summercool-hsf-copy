package org.summercool.hsf.util;

/**
 * @Title: AsyncType.java
 * @Package org.summercool.hsf.proxy
 * @Description: 异步方式
 * @author 简道
 * @date 2011-9-30 下午3:12:10
 * @version V1.0
 */
public enum AsyncType {
	/**
	 * @Fields Default : 普通异步方式
	 */
	Default,
	/**
	 * @Fields Future : Future方式
	 */
	Future,
	/**
	 * @Fields Callback : Callback方式
	 */
	Callback,
	/**
	 * @Fields Sync : 同步方式
	 */
	Sync
}
