package org.summercool.hsf.netty.dispatcher.sync;

import org.summercool.hsf.netty.dispatcher.InvokeResult;
import org.summercool.hsf.netty.service.HsfService;

/**
 * @Title: SyncDispatchStrategy.java
 * @Package org.summercool.hsf.netty.dispatcher.sync
 * @Description: 分发策略接口
 * @author 简道
 * @date 2011-9-29 下午2:26:19
 * @version V1.0
 */
public interface SyncDispatchStrategy {

	HsfService getService();

	/**
	 * @Title: dispatch
	 * @Description: 分发消息
	 * @author 简道
	 * @param message
	 *        消息
	 * @param service
	 *        Hsf服务
	 * @return DispatchResult 返回类型
	 */
	InvokeResult dispatch(Object message);
}
