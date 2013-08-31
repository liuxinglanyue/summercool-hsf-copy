package org.summercool.hsf.proxy.strategy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.summercool.hsf.exception.HsfOperationException;
import org.summercool.hsf.future.InvokeFuture;
import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.channel.HsfChannelGroup;
import org.summercool.hsf.netty.service.HsfService;
import org.summercool.hsf.pojo.RemoteServiceObject;
import org.summercool.hsf.util.AsyncCallback;
import org.summercool.hsf.util.AsyncType;
import org.summercool.hsf.util.ReflectionUtil;
import org.summercool.hsf.util.TLSUtil;

/**
 * @Title: AsyncServiceRequestHandler.java
 * @Package org.summercool.hsf.proxy.strategy
 * @Description: 异步请求处理
 * @author 简道
 * @date 2011-9-30 下午3:11:08
 * @version V1.0
 */
@SuppressWarnings("rawtypes")
public class AsyncSingleGroupRequestHandler implements InvocationHandler {
	HsfService service;
	String serviceName;
	AsyncType asyncType;
	AsyncCallback callback;
	String groupName;

	public AsyncSingleGroupRequestHandler(HsfService service, String serviceName, AsyncType asyncType,
			AsyncCallback callback, String groupName) {
		if (serviceName == null) {
			throw new IllegalArgumentException("serviceName can not be null.");
		} else if (service == null) {
			throw new IllegalArgumentException("service can not be null.");
		} else if (groupName == null) {
			throw new IllegalArgumentException("groupName can not be null.");
		}

		this.service = service;
		this.serviceName = serviceName;
		this.asyncType = asyncType == null ? AsyncType.Default : asyncType;
		this.callback = callback;
		this.groupName = groupName;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (!service.isAlived()) {
			throw new IllegalStateException("service is not alived.");
		}

		RemoteServiceObject remoteServiceObject = new RemoteServiceObject();
		remoteServiceObject.setMethodName(method.getName());
		remoteServiceObject.setServiceName(serviceName);
		remoteServiceObject.setArgs(args);

		Object retObj = writeAsync(remoteServiceObject, groupName);

		if (AsyncType.Future.equals(asyncType)) {
			TLSUtil.setData(InvokeFuture.class, retObj);
		}

		return ReflectionUtil.getDefaultValue(method.getReturnType());
	}

	public Object writeAsync(RemoteServiceObject message, String groupName) {

		HsfChannelGroup group = service.getGroups().get(groupName);
		if (group == null) {
			throw new HsfOperationException("HsfService group(" + groupName + ") is not existed.");
		}

		// 获取Channel，发送消息
		HsfChannel channel = group.getNextChannel();
		if (channel == null) {
			throw new HsfOperationException("channel is null.");
		}

		switch (asyncType) {
		case Callback:
			channel.writeAsync(message, callback);
			break;

		case Future:
			return channel.writeAsync(message);

		case Default:
			channel.write(message);
			break;
		}

		return null;
	}
}