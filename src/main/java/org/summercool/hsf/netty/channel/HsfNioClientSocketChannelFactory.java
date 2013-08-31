package org.summercool.hsf.netty.channel;

import java.util.concurrent.Executor;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.socket.SocketChannel;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import org.summercool.hsf.future.ChannelGroupFuture;
import org.summercool.hsf.future.ChannelGroupFutureHolder;
import org.summercool.hsf.util.HsfConstants;
import org.summercool.hsf.util.TLSUtil;

/**
 * @Title: HsfNioClientSocketChannelFactory.java
 * @Package org.summercool.hsf.netty.channel
 * @Description: 
 * @author 简道
 * @date 2012-2-23 上午12:03:56
 * @version V1.0
 */
public class HsfNioClientSocketChannelFactory extends NioClientSocketChannelFactory {

	public HsfNioClientSocketChannelFactory(Executor bossExecutor, Executor workerExecutor) {
		super(bossExecutor, workerExecutor);
	}

	public HsfNioClientSocketChannelFactory(Executor bossExecutor, Executor workerExecutor, int workerCount) {
		super(bossExecutor, workerExecutor, workerCount);
	}

	@Override
	public SocketChannel newChannel(ChannelPipeline pipeline) {
		ChannelGroupFuture channelFuture = (ChannelGroupFuture) TLSUtil.remove(HsfConstants.KEY_CURRENT_CHANNEL_FUTURE);
		SocketChannel channel = super.newChannel(pipeline);
		if (channelFuture != null) {
			ChannelGroupFutureHolder.put(channel.getId(), channelFuture);
		}

		return channel;
	}
}
