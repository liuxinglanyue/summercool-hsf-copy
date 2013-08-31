package org.summercool.hsf.proxy.strategy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.summercool.hsf.netty.dispatcher.InvokeResult;
import org.summercool.hsf.netty.dispatcher.sync.SyncDispatchStrategy;
import org.summercool.hsf.pojo.RemoteServiceObject;
import org.summercool.hsf.util.ReflectionUtil;

/**
 * @Title: SyncServiceRequestHandler.java
 * @Package org.summercool.hsf.proxy.strategy
 * @Description: 同步请求处理
 * @author 简道
 * @date 2011-9-30 下午3:10:10
 * @version V1.0
 */
public class SyncServiceRequestHandler implements InvocationHandler {
	String serviceName;
	SyncDispatchStrategy dispatchStrategy;

	public SyncServiceRequestHandler(String serviceName, SyncDispatchStrategy dispatchStrategy) {
		if (serviceName == null) {
			throw new IllegalArgumentException("serviceName can not be null.");
		} else if (dispatchStrategy == null) {
			throw new IllegalArgumentException("dispatchStrategy can not be null.");
		}

		this.serviceName = serviceName;
		this.dispatchStrategy = dispatchStrategy;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		RemoteServiceObject remoteServiceObject = new RemoteServiceObject();
		remoteServiceObject.setMethodName(method.getName());
		remoteServiceObject.setServiceName(serviceName);
		remoteServiceObject.setArgs(args);

		InvokeResult result = dispatchStrategy.dispatch(remoteServiceObject);

		if (result.size() > 0) {
			return result.getFirstValue();
		}

		return ReflectionUtil.getDefaultValue(method.getReturnType());
	}
}