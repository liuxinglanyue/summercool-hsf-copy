package org.summercool.hsf.future;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.summercool.hsf.exception.HsfRemoteServiceException;
import org.summercool.hsf.exception.HsfRuntimeException;
import org.summercool.hsf.exception.HsfTimeoutException;
import org.summercool.hsf.netty.channel.HsfChannelGroup;
import org.summercool.hsf.util.StackTraceUtil;

/**
 * @Title: InvokeFuture.java
 * @Package org.summercool.hsf.future
 * @Description: 异步调用结果Future
 * @author 简道
 * @date 2011-9-17 下午1:32:05
 * @version V1.0
 */
public class InvokeFuture<V> {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected V result;
	protected AtomicBoolean done = new AtomicBoolean(false);
	protected AtomicBoolean success = new AtomicBoolean(false);
	protected Semaphore semaphore = new Semaphore(0);
	protected Throwable cause;
	protected Channel channel;
	protected HsfChannelGroup group;
	protected Object attachment;
	protected List<InvokeFutureListener<V>> listeners = new ArrayList<InvokeFutureListener<V>>();

	public InvokeFuture() {
	}

	public void addListener(InvokeFutureListener<V> listener) {
		if (listener == null) {
			throw new NullPointerException("listener can not be null.");
		}
		//
		notifyListener(listener);
		listeners.add(listener);
	}

	private void notifyListeners() {
		if (isDone()) {
			for (InvokeFutureListener<V> listener : listeners) {
				try {
					listener.operationComplete(this);
				} catch (Exception e) {
					logger.error("call listener({}) error:{}", this.getClass(), StackTraceUtil.getStackTrace(e));
				}
			}
		}
	}

	private void notifyListener(InvokeFutureListener<V> listener) {
		if (listener == null) {
			throw new NullPointerException("listener can not be null.");
		}
		//
		if (isDone()) {
			try {
				listener.operationComplete(this);
			} catch (Exception e) {
				logger.error("call listener({}) error:{}", this.getClass(), StackTraceUtil.getStackTrace(e));
			}
		}
	}

	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	public boolean isCancelled() {
		return false;
	}

	public boolean isDone() {
		return done.get();
	}

	public V getResult() throws HsfRuntimeException {
		if (!isDone()) {
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		// check exception
		if (cause != null) {
			if (cause instanceof HsfRemoteServiceException) {
				throw ((HsfRemoteServiceException) cause);
			}
			throw new HsfRuntimeException(cause);
		}
		//
		return this.result;
	}

	public void setResult(V result) {
		this.result = result;
		done.set(true);
		success.set(true);

		semaphore.release(Integer.MAX_VALUE - semaphore.availablePermits());
		notifyListeners();
	}

	public V getResult(long timeout, TimeUnit unit) {
		if (!isDone()) {
			try {
				if (!semaphore.tryAcquire(timeout, unit)) {
					setCause(new HsfTimeoutException("time out."));
				}
			} catch (InterruptedException e) {
				throw new HsfRuntimeException(e);
			}
		}
		// check exception
		if (cause != null) {
			if (cause instanceof HsfRemoteServiceException) {
				throw ((HsfRemoteServiceException) cause);
			}
			throw new HsfRuntimeException(cause);
		}
		//
		return this.result;
	}

	public void setCause(Throwable cause) {
		this.cause = cause;
		done.set(true);
		success.set(false);
		semaphore.release(Integer.MAX_VALUE - semaphore.availablePermits());
		notifyListeners();
	}

	public boolean isSuccess() {
		return success.get();
	}

	public Throwable getCause() {
		return cause;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	/**
	 * @return the group
	 */
	public HsfChannelGroup getGroup() {
		return group;
	}

	/**
	 * @param group
	 *            the group to set
	 */
	public void setGroup(HsfChannelGroup group) {
		this.group = group;
	}

	public Object getAttachment() {
		return attachment;
	}

	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}
}
