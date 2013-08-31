package org.summercool.hsf.netty.channel;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.NotImplementedException;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ServerChannel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroupFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.summercool.hsf.statistic.ServiceStatisticInfo;
import org.summercool.hsf.statistic.StatisticInfo;
import org.summercool.hsf.util.StackTraceUtil;

/**
 * @Title: RoundChannelGroup.java
 * @Package org.summercool.hsf.netty.channel
 * @Description: 逐一发送ChannelGroup实现
 * @author 简道
 * @date Nov 16, 2011 1:02:42 AM
 * @version V1.0
 */
public class RoundChannelGroup implements HsfChannelGroup {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private final ChannelFutureListener remover = new ChannelFutureListener() {
		public void operationComplete(ChannelFuture future) throws Exception {
			remove(future.getChannel());
		}
	};

	private final AtomicBoolean prepared = new AtomicBoolean();
	private final AtomicBoolean closed = new AtomicBoolean();
	private final String name;
	/**
	 * @Fields msgStatistic : 消息统计
	 */
	private final StatisticInfo msgStatistic = new StatisticInfo();
	/**
	 * @Fields heartBeatStatistic : 心跳统计
	 */
	private final StatisticInfo heartBeatStatistic = new StatisticInfo();
	/**
	 * @Fields serviceStatisticInfo : Service调用统计
	 */
	private final ServiceStatisticInfo serviceStatisticInfo = new ServiceStatisticInfo();
	private final Date createTime = Calendar.getInstance().getTime();
	private List<HsfChannel> nonServerChannels = new ArrayList<HsfChannel>();
	private List<HsfChannel> serverChannels = new ArrayList<HsfChannel>();
	private AtomicLong index = new AtomicLong();
	private ConcurrentHashMap<String, Object> attribute = new ConcurrentHashMap<String, Object>();

	public RoundChannelGroup(String name) {
		this.name = name;
		serviceStatisticInfo.setName(name);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Channel find(Integer id) {
		throw new NotImplementedException();
	}

	@Override
	public ChannelGroupFuture setInterestOps(int interestOps) {
		throw new NotImplementedException();
	}

	@Override
	public ChannelGroupFuture setReadable(boolean readable) {
		throw new NotImplementedException();
	}

	@Override
	public ChannelGroupFuture write(Object message) {
		throw new NotImplementedException();
	}

	@Override
	public ChannelGroupFuture write(Object message, SocketAddress remoteAddress) {
		throw new NotImplementedException();
	}

	@Override
	public ChannelGroupFuture disconnect() {
		throw new NotImplementedException();
	}

	@Override
	public ChannelGroupFuture unbind() {
		throw new NotImplementedException();
	}

	@Override
	public ChannelGroupFuture close() {
		return close(false);
	}

	public StatisticInfo getMsgStatistic() {
		return msgStatistic;
	}

	public StatisticInfo getHeartBeatStatistic() {
		return heartBeatStatistic;
	}

	@Override
	public ServiceStatisticInfo getServiceStatistic() {
		return serviceStatisticInfo;
	}

	@Override
	public ChannelGroupFuture close(boolean stopReconnect) {
		List<ChannelFuture> futures = new ArrayList<ChannelFuture>();

		synchronized (nonServerChannels) {
			while (nonServerChannels.size() > 0) {
				try {
					HsfChannel channel = nonServerChannels.remove(0);
					futures.add(channel.close(stopReconnect));
				} catch (Exception e) {
				}
			}
		}

		synchronized (serverChannels) {
			while (serverChannels.size() > 0) {
				try {
					HsfChannel channel = serverChannels.remove(0);
					futures.add(channel.close(stopReconnect));
				} catch (Exception e) {
				}
			}
		}

		closed.set(true);

		return new DefaultChannelGroupFuture(this, futures);
	}

	@Override
	public int size() {
		return nonServerChannels.size();
	}

	@Override
	public boolean isEmpty() {
		return nonServerChannels.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		throw new NotImplementedException();
	}

	@Override
	public Iterator<Channel> iterator() {
		throw new NotImplementedException();
	}

	@Override
	public Object[] toArray() {
		throw new NotImplementedException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new NotImplementedException();
	}

	@Override
	public boolean add(Channel channel) {
		List<HsfChannel> list = channel instanceof ServerChannel ? serverChannels : nonServerChannels;
		HsfChannel hsfChannel = (HsfChannel) channel;
		synchronized (list) {
			boolean added = list.add(hsfChannel);
			if (added) {
				hsfChannel.setChannelGroup(this);
				channel.getCloseFuture().addListener(remover);
			}
			return added;
		}
	}

	@Override
	public boolean remove(Object o) {
		if (!(o instanceof Channel) || o == null) {
			return false;
		}

		boolean success = false;
		Channel c = (Channel) o;
		List<HsfChannel> list = c instanceof ServerChannel ? serverChannels : nonServerChannels;

		synchronized (list) {
			success = list.remove(c);

			if (success) {
				c.getCloseFuture().removeListener(remover);
			}
			return success;
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new NotImplementedException();
	}

	@Override
	public boolean addAll(Collection<? extends Channel> c) {
		throw new NotImplementedException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new NotImplementedException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new NotImplementedException();
	}

	@Override
	public void clear() {
		synchronized (serverChannels) {
			serverChannels.clear();
		}
		synchronized (nonServerChannels) {
			nonServerChannels.clear();
		}
	}

	@Override
	public int compareTo(ChannelGroup o) {
		int v = getName().compareTo(o.getName());
		if (v != 0) {
			return v;
		}

		return System.identityHashCode(this) - System.identityHashCode(o);
	}

	@Override
	public HsfChannel getNextChannel() {
		while (true) {
			if (nonServerChannels.size() == 0) {
				return null;
			}

			int position = (int) (index.getAndIncrement() % nonServerChannels.size());
			try {
				HsfChannel channel = nonServerChannels.get(position);
				return channel;
			} catch (IndexOutOfBoundsException e) {
				logger.warn(StackTraceUtil.getStackTrace(e));
			}
		}
	}

	@Override
	public List<HsfChannel> getChannels() {
		return nonServerChannels;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attribute;
	}

	@Override
	public boolean isPrepared() {
		return prepared.get();
	}

	@Override
	public boolean setPrepared(boolean prepared) {
		return this.prepared.compareAndSet(!prepared, prepared);
	}

	@Override
	public boolean isClosed() {
		return closed.get();
	}

	@Override
	public Date getCreateTime() {
		return createTime;
	}

	@Override
	public String toString() {
		return "Group:" + name;
	}
}
