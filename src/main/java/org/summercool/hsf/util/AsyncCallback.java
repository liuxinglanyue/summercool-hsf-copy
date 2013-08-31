package org.summercool.hsf.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.summercool.hsf.netty.channel.HsfChannel;

/**
 * @Title: AsyncCallback.java
 * @Package org.summercool.hsf.proxy
 * @Description: 异步处理回调
 * @author 简道
 * @date 2011-9-17 下午2:56:13
 * @version V1.0
 */
public abstract class AsyncCallback<TMessage> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * @Title: doCallback
	 * @Description: 回调处理消息
	 * @author 简道
	 * @param data
	 *        消息
	 * @return void 返回类型
	 */
	public abstract void doCallback(TMessage data);

	/**
	 * @Title: doExceptionCaught
	 * @Description: 处理异常
	 * @author 简道
	 * @param ex
	 * @param channel
	 * @return void 返回类型
	 */
	public void doExceptionCaught(Throwable ex, HsfChannel channel, Object param) {
		logger.error("send msg:{} to channel({}) occurs exception:{}", new Object[] { param,
				channel.getRemoteAddress(), StackTraceUtil.getStackTrace(ex) });
	}
}