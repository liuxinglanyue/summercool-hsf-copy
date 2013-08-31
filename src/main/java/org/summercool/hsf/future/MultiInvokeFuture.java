package org.summercool.hsf.future;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.summercool.hsf.netty.dispatcher.InvokeResult;

/**
 * @Title: MultiInvokeFuture.java
 * @Package org.summercool.hsf.future
 * @Description: 多次异步调用结果Future
 * @author 简道
 * @date 2011-9-29 下午3:36:03
 * @version V1.0
 */
public class MultiInvokeFuture extends InvokeFuture<InvokeResult> {
	private List<InvokeFuture<?>> futureList = new LinkedList<InvokeFuture<?>>();

	public MultiInvokeFuture() {
		result = new InvokeResult();
	}

	public void addFuture(InvokeFuture<?> future) {
		futureList.add(future);
	}

	@Override
	public InvokeResult getResult() {
		for (InvokeFuture<?> future : futureList) {
			Object retValue = future.getResult();
			if (future.getCause() != null) {
				result.put(future.getChannel().getId(), future.getCause());
			} else {
				result.put(future.getChannel().getId(), retValue);
			}
		}
		return result;
	}

	@Override
	public InvokeResult getResult(long timeout, TimeUnit unit) {
		for (InvokeFuture<?> future : futureList) {
			try {
				Object retValue = future.getResult(timeout, unit);
				if (future.getCause() != null) {
					result.put(future.getChannel().getId(), future.getCause());
				} else {
					result.put(future.getChannel().getId(), retValue);
				}
			} catch (Exception e) {
				result.put(future.getChannel().getId(), e);
			}
		}
		return result;
	}

	@Override
	public boolean isDone() {
		boolean result = true;
		for (InvokeFuture<?> future : futureList) {
			if (!(result = result && future.isDone())) {
				break;
			}
		}

		return result;
	}

	public List<InvokeFuture<?>> getFutureList() {
		return futureList;
	}
}
