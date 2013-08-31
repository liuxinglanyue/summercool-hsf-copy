package org.summercool.hsf.util;

import org.summercool.hsf.future.InvokeFuture;

/**
 * @Title: AsyncInvoker.java
 * @Package org.summercool.hsf.future
 * @Description: 异步调用抽象类，请使用{@link AsyncFutureInvoker}和{@link AsyncCallbackInvoker}
 * @author 简道
 * @date 2011-9-17 下午2:01:15
 * @version V1.0
 */
public abstract class AsyncFutureInvoker<TServiceInterface> {

	protected TServiceInterface service;

	public AsyncFutureInvoker(TServiceInterface service) {
		if (service == null) {
			throw new IllegalArgumentException("service can not be null.");
		}
		this.service = service;
	}

	/**
	 * @Title: invokeService
	 * @Description: 调用接口方法
	 * @author 简道
	 * @return void 返回类型
	 */
	protected abstract void invokeService();

	/**
	 * @Title: invoke
	 * @Description: 异步执行请求，并返回Future对象
	 * @author 简道
	 * @param dispatchStratege
	 * @return InvokeFuture 返回类型
	 */
	@SuppressWarnings("rawtypes")
	public final InvokeFuture invoke() {
		invokeService();

		InvokeFuture future = (InvokeFuture) TLSUtil.getData(InvokeFuture.class);
		return future;
	}
}
