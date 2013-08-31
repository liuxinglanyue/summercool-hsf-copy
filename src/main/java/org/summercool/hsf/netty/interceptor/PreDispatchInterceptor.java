package org.summercool.hsf.netty.interceptor;

import org.summercool.hsf.netty.event.EventDispatcher;

/**
 * @Description: 进入事件分发器前的拦截
 * @author 简道
 * @date 2012-5-17 下午1:12:36
 */
public interface PreDispatchInterceptor<TMessage> {

	/**
	 * @Title: canIntercept
	 * @Description: 是否能够被拦截
	 * @author 简道
	 * @param msg
	 * @return boolean
	 */
	public boolean canIntercept(Object msg);

	/**
	 * @Title: intercept
	 * @Description: 拦截处理
	 * @param msg
	 * @return boolean 返回true，则继续执行下一个拦截，如果所有拦截器都返回true，则该消息将被分发处理。返回false，则直接丢弃。
	 */
	public boolean intercept(EventDispatcher eventDispatcher, TMessage msg);
}