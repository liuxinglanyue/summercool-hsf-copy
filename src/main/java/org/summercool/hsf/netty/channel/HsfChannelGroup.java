package org.summercool.hsf.netty.channel;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.summercool.hsf.statistic.ServiceStatisticInfo;
import org.summercool.hsf.statistic.StatisticInfo;

/**
 * @Title: HsfChannelGroup.java
 * @Package org.summercool.hsf.netty.channel
 * @Description: Hsf ChannelGroup接口定义
 * @author 简道
 * @date 2011-11-17 上午10:46:48
 * @version V1.0
 */
public interface HsfChannelGroup extends ChannelGroup {
	HsfChannel getNextChannel();

	List<HsfChannel> getChannels();

	Map<String, Object> getAttributes();

	ChannelGroupFuture close(boolean stopReconnect);

	boolean isPrepared();

	boolean setPrepared(boolean prepared);

	boolean isClosed();

	/**
	 * @Title: getCreateTime
	 * @Description: 获取Group创建时间
	 * @author 简道
	 * @return Date 返回类型
	 */
	Date getCreateTime();

	StatisticInfo getMsgStatistic();

	StatisticInfo getHeartBeatStatistic();
	
	ServiceStatisticInfo getServiceStatistic();
}
