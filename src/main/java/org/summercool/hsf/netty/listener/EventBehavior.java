package org.summercool.hsf.netty.listener;
/**
 * @Title: EventBehavior.java
 * @Package org.summercool.hsf.netty.listener
 * @Description: 事件传递行为
 * @author 简道
 * @date 2011-9-27 下午5:42:48
 * @version V1.0
 */
public enum EventBehavior {
	/**
	 * @Fields Continue : 继续传递事件
	 */
	Continue,
	/**
	 * @Fields Break : 停止传递事件
	 */
	Break;
}