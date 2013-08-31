package org.summercool.hsf.netty.dispatcher.sync;

import java.util.concurrent.atomic.AtomicLong;

import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.channel.HsfChannelGroup;
import org.summercool.hsf.netty.dispatcher.InvokeResult;
import org.summercool.hsf.netty.service.HsfService;
import org.summercool.hsf.util.ConcurrentArrayListHashMap;
import org.summercool.hsf.util.HsfContextHolder;
import org.summercool.hsf.util.StackTraceUtil;

/**
 * @Title: SyncRoundDispatchStrategy.java
 * @Package org.summercool.hsf.netty.dispatcher.sync
 * @Description: 同步环式分发策略实现
 * @author 简道
 * @date 2011-9-29 下午2:56:05
 * @version V1.0
 */
public class SyncRoundDispatchStrategy extends SyncAbstractDispatchStrategy {

	private static final AtomicLong STATIC_SEQ = new AtomicLong(0);
	private AtomicLong groupIndex;

	public SyncRoundDispatchStrategy(HsfService service) {
		super(service);
		groupIndex = new AtomicLong(STATIC_SEQ.getAndIncrement());
	}

	@Override
	public InvokeResult dispatch(Object message) {
		if (message == null) {
			throw new IllegalArgumentException("Message can not be null.");
		} else if (!service.isAlived()) {
			throw new IllegalStateException("service is not alived.");
		}

		HsfChannel channel = getChannel(service.getGroups());
		Object retObj = write(message, channel);

		// 构建结果
		InvokeResult invokeResult = new InvokeResult();
		invokeResult.put(((HsfChannel) channel).getChannelGroup().getName(), retObj);

		return invokeResult;
	}

	/**
	 * 
	 * @Title: getChannel
	 * @Description: 获取一个有效通道
	 * @author 简道
	 * @param groups
	 * @return HsfChannel 返回类型
	 */
	private HsfChannel getChannel(ConcurrentArrayListHashMap<String, HsfChannelGroup> groups) {
		int groupSize = 0;
		int groupCount = 0;
		long index;
		HsfChannel channel;
		Object[] groupArray;
		HsfChannelGroup hsfChannelGroup;
		//
		groupArray = groups.arrayValues();
		index = groupIndex.getAndIncrement();
		groupSize = groupArray.length;

		while (true) {
			groupCount++;

			groupArray = groups.arrayValues();
			int size = groupArray.length;
			if (size == 0) {
				return null;
			}

			int position = (int) (index++ % size);
			try {
				hsfChannelGroup = (HsfChannelGroup) groupArray[position];
				// 未准备好（还没处理完GroupCreated事件）
				if (!hsfChannelGroup.isPrepared() && !HsfContextHolder.isInProcessingGroupCreatedEvent()
						|| hsfChannelGroup.isClosed()) {
					continue;
				}

				channel = hsfChannelGroup.getNextChannel();
				return channel;
			} catch (IndexOutOfBoundsException e) {
				logger.warn(StackTraceUtil.getStackTrace(e));
			}

			if (groupCount >= groupSize * 2) {
				return null;
			}
		}

	}
}
