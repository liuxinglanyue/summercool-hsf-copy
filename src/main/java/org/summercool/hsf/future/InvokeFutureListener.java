package org.summercool.hsf.future;

/**
 * @Title: InvokeFutureListener.java
 * @Package org.summercool.hsf.future
 * @Description: InvokeFuture监听器
 * @author 简道
 * @date 2011-11-17 下午3:02:29
 * @version V1.0
 */
public interface InvokeFutureListener<T> {
	void operationComplete(InvokeFuture<T> future) throws Exception;
}
