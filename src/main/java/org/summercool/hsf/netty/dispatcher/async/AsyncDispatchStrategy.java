package org.summercool.hsf.netty.dispatcher.async;

import org.summercool.hsf.netty.dispatcher.InvokeResult;
import org.summercool.hsf.netty.service.HsfService;
import org.summercool.hsf.util.AsyncCallback;
import org.summercool.hsf.util.AsyncType;

/**
 * @Title: AsyncDispatchStrategy.java
 * @Package org.summercool.hsf.netty.dispatcher.async
 * @Description: 异步分发策略接口
 * @author 简道
 * @date 2011-9-29 下午2:14:14
 * @version V1.0
 */
public interface AsyncDispatchStrategy {

	/**
	 * @Title: getService
	 * @Description: 获取Hsf服务
	 * @author 简道
	 * @return HsfService 返回类型
	 */
	HsfService getService();

	/**
	 * @Title: dispatch
	 * @Description: 分发消息
	 * @author 简道
	 * @param message
	 *        消息
	 * @param asyncType
	 *        异步方式
	 * @return DispatchResult<Channel, Object> 返回类型
	 */
	InvokeResult dispatch(Object message, AsyncType asyncType);

	/**
	 * @Title: dispatch
	 * @Description: 分发消息
	 * @author 简道
	 * @param message
	 *        消息
	 * @param callback
	 *        回调
	 * @return DispatchResult<Channel, Object> 返回类型
	 */
	InvokeResult dispatch(Object message, AsyncCallback<?> callback);
}
