package org.summercool.hsf.netty.dispatcher.async;

import java.util.Map.Entry;

import org.summercool.hsf.exception.HsfOperationException;
import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.channel.HsfChannelGroup;
import org.summercool.hsf.netty.dispatcher.InvokeResult;
import org.summercool.hsf.netty.service.HsfService;
import org.summercool.hsf.util.AsyncCallback;
import org.summercool.hsf.util.AsyncType;

/**
 * @Title: BroadcastDispatchStrategy.java
 * @Package org.summercool.hsf.netty.dispatcher.async
 * @Description: 异步广播式分发策略实现
 * @author 简道
 * @date 2011-9-29 下午2:16:17
 * @version V1.0
 */
public class AsyncBroadcastDispatchStrategy extends AsyncAbstractDispatchStrategy {

	public AsyncBroadcastDispatchStrategy(HsfService service) {
		super(service);
	}

	@Override
	public InvokeResult dispatch(Object message, AsyncType asyncType) {
		if (message == null) {
			throw new IllegalArgumentException("Message can not be null.");
		} else if (!service.isAlived()) {
			throw new IllegalStateException("service is not alived.");
		} else if (AsyncType.Callback.equals(asyncType)) {
			throw new IllegalArgumentException("only support AsyncType.None and AsyncType.Future.");
		}

		// 默认为None
		if (asyncType == null) {
			asyncType = AsyncType.Default;
		}

		InvokeResult invokeResult = new InvokeResult();

		// 广播消息
		for (Entry<String, HsfChannelGroup> entry : service.getGroups().entrySet()) {
			HsfChannelGroup group = entry.getValue();
			HsfChannel channel = group.getNextChannel();

			if (channel != null) {
				try {
					switch (asyncType) {
					case Default:
						invokeResult.put(channel.getId(), channel.write(message));
						break;

					case Future:
						invokeResult.put(channel.getId(), channel.writeAsync(message));
						break;
					}
				} catch (Exception e) {
					invokeResult.put(channel, e);
				}
			} else {
				invokeResult.put(channel, new HsfOperationException("HsfService Channel can not be null."));
			}
		}

		return invokeResult;
	}

	@Override
	public InvokeResult dispatch(Object message, AsyncCallback<?> callback) {
		if (message == null) {
			throw new IllegalArgumentException("Message can not be null.");
		} else if (!service.isAlived()) {
			throw new IllegalStateException("service is not alived.");
		}

		InvokeResult invokeResult = new InvokeResult();

		// 广播消息
		for (Entry<String, HsfChannelGroup> entry : service.getGroups().entrySet()) {
			HsfChannelGroup group = entry.getValue();
			HsfChannel channel = group.getNextChannel();

			if (channel != null) {
				try {
					channel.writeAsync(message, callback);
				} catch (Exception e) {
					invokeResult.put(channel, e);
				}
			} else {
				invokeResult.put(channel, new HsfOperationException("HsfService Channel can not be null."));
			}
		}

		return invokeResult;
	}
}
