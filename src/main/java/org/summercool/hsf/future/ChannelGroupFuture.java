package org.summercool.hsf.future;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.channel.HsfChannelGroup;

/**
 * @Title: ChannelGroupFuture.java
 * @Package org.summercool.hsf.future
 * @Description: Group Future实现
 * @author 简道
 * @date 2011-9-27 下午2:00:20
 * @version V1.0
 */
public class ChannelGroupFuture {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private final int totalNum;
	private int successNum = 0;
	private int failedNum = 0;
	private List<HsfChannelGroup> groupList = new ArrayList<HsfChannelGroup>();
	private HashSet<SocketAddress> failedList = new HashSet<SocketAddress>();
	private Semaphore semaphoreAll = new Semaphore(0);
	private Semaphore semaphoreAnyone = new Semaphore(0);
	private ReentrantReadWriteLock numLock = new ReentrantReadWriteLock();
	private List<Throwable> causes = new ArrayList<Throwable>();

	public ChannelGroupFuture(int totalNum) {
		this.totalNum = totalNum;
		if (totalNum < 0) {
			throw new IllegalArgumentException("totalNum must >=0.");
		} else if (totalNum == 0) {
			semaphoreAll.release(Integer.MAX_VALUE);
			semaphoreAnyone.release(Integer.MAX_VALUE);
		}
	}

	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	public boolean isCancelled() {
		return false;
	}

	public boolean isDone() {
		return totalNum <= successNum + failedNum;
	}

	public List<HsfChannelGroup> getGroupList() throws InterruptedException, ExecutionException {
		//
		semaphoreAll.acquire();
		return groupList;
	}

	public List<HsfChannelGroup> getGroupList(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		//
		if (!semaphoreAll.tryAcquire(timeout, unit)) {
			logger.warn("getGroupList time out.");
		}
		return groupList;
	}

	public void addGroup(HsfChannelGroup group) {
		try {
			numLock.writeLock().lock();

			HsfChannel channel = group.getNextChannel();
			if (channel != null && failedList.remove(channel.getRemoteAddress())) {
				--failedNum;
			}

			groupList.add(group);
			++successNum;
			if (isDone()) {
				semaphoreAll.release(Integer.MAX_VALUE);
			}
			semaphoreAnyone.release(Integer.MAX_VALUE - Math.abs(semaphoreAnyone.availablePermits()));
		} finally {
			numLock.writeLock().unlock();
		}
	}

	public void addFailure(SocketAddress address, Throwable cause) {
		try {
			numLock.writeLock().lock();

			boolean isConnected = false;
			for (HsfChannelGroup group : groupList) {
				HsfChannel channel = group.getNextChannel();
				if (isConnected = (channel != null && address.equals(channel.getRemoteAddress()))) {
					break;
				}
			}

			if (!isConnected && failedList.add(address)) {
				causes.add(cause);
				++failedNum;
			}
			if (isDone()) {
				semaphoreAll.release(Integer.MAX_VALUE);
				semaphoreAnyone.release(Integer.MAX_VALUE - Math.abs(semaphoreAnyone.availablePermits()));
			}
		} finally {
			numLock.writeLock().unlock();
		}
	}

	public List<Throwable> getCauses() {
		return causes;
	}

	public boolean isSuccess() {
		return successNum == totalNum;
	}

	public boolean isPartialSuccess() {
		return successNum > 0 && successNum < totalNum;
	}

	public boolean isFailed() {
		return failedNum == totalNum;
	}
}
