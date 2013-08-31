package org.summercool.hsf.statistic;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Title: StatisticInfo.java
 * @Package org.summercool.hsf.statistic
 * @Description: 统计信息
 * @author 简道
 * @date 2012-2-27 上午08:41:12
 * @version V1.0
 */
public class StatisticInfo implements Serializable {
	private static final long serialVersionUID = 7094331845793206401L;

	private long lastestReceived;
	private long lastestSent;
	private AtomicLong receivedNum = new AtomicLong();
	private AtomicLong sentNum = new AtomicLong();

	public AtomicLong getReceivedNum() {
		return receivedNum;
	}

	public AtomicLong getSentNum() {
		return sentNum;
	}

	public long getLastestReceived() {
		return lastestReceived;
	}

	public void setLastestReceived(Long lastestReceived) {
		this.lastestReceived = lastestReceived;
	}

	public void setLastestReceivedIfLater(Long lastestReceived) {
		if (lastestReceived == null) {
			return;
		}
		if (this.lastestReceived < lastestReceived) {
			this.lastestReceived = lastestReceived;
		}
	}

	public Long getLastestSent() {
		return lastestSent;
	}

	public void setLastestSent(Long lastestSent) {
		this.lastestSent = lastestSent;
	}

	public void setLastestSentIfLater(Long lastestSent) {
		if (lastestSent == null) {
			return;
		}
		if (this.lastestSent < lastestSent) {
			this.lastestSent = lastestSent;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StatisticInfo [lastestReceived=");
		builder.append(lastestReceived);
		builder.append(", lastestSent=");
		builder.append(lastestSent);
		builder.append(", receivedNum=");
		builder.append(receivedNum);
		builder.append(", sentNum=");
		builder.append(sentNum);
		builder.append("]");
		return builder.toString();
	}

}
