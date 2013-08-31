package org.summercool.hsf.netty.channel;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class FlowManagerImpl implements FlowManager {
	private int threshold = 2000000;
	private Semaphore available = new Semaphore(threshold, false);

	@Override
	public void acquire() throws InterruptedException {
		available.acquire();
	}

	@Override
	public void acquire(int permits) throws InterruptedException {
		available.acquire(permits);
	}

	@Override
	public boolean acquire(int permits, int timeout) {
		try {
			return available.tryAcquire(permits, timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}

	@Override
	public void release() {
		release(1);
	}

	@Override
	public void release(int permits) {
		available.release(permits);
	}

	@Override
	public int getAvailable() {
		return available.availablePermits();
	}

	@Override
	public void setThreshold(int newThreshold) {
		if (newThreshold <= 0) {
			throw new IllegalArgumentException("threshold must great than 0.");
		}

		available = new Semaphore(newThreshold, false);
	}

	@Override
	public int getThreshold() {
		return threshold;
	}
}
