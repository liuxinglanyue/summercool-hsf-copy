package org.summercool.hsf.netty.channel;

/**
 * @ClassName: FlowManager
 * @Description: 流量管理接口
 * @author 简道
 * @date 2012-3-6 下午3:44:59
 *
 */
public interface FlowManager {

	public abstract void acquire() throws InterruptedException;

	public abstract void acquire(int permits) throws InterruptedException;

	public abstract boolean acquire(int permits, int timeout);

	public abstract void release();

	public abstract void release(int permits);

	public abstract int getAvailable();

	public abstract void setThreshold(int newThreshold);

	public abstract int getThreshold();

}