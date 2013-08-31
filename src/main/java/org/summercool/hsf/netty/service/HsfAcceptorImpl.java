package org.summercool.hsf.netty.service;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.summercool.hsf.netty.handler.StateCheckChannelHandler;
import org.summercool.hsf.netty.handler.upstream.DispatchUpStreamHandler;
import org.summercool.hsf.netty.handshake.AcceptorHandshakeProcessor;
import org.summercool.hsf.netty.listener.impl.AcceptorGroupMessageEventListener;
import org.summercool.hsf.util.HsfOptions;
import org.summercool.hsf.util.LangUtil;

/**
 * @Title: HsfAcceptorImpl.java
 * @Package org.summercool.hsf.netty.service
 * @Description: HsfAcceptor实现类
 * @author 简道
 * @date 2012-2-21 上午08:31:58
 * @version V1.0
 */
public class HsfAcceptorImpl extends AbstractHsfService implements HsfAcceptor {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private HashedWheelTimer idleTimer = new HashedWheelTimer();
	private ServerBootstrap bootstrap;
	private AtomicBoolean alive = new AtomicBoolean(false);
	private AcceptorHandshakeProcessor handshakeProcessor;

	public HsfAcceptorImpl() {
		super();
	}

	public HsfAcceptorImpl(Executor bossExecutor, int workerCount) {
		super(bossExecutor, workerCount);
	}

	public HsfAcceptorImpl(Executor bossExecutor, Executor workerExecutor, int workerCount) {
		super(bossExecutor, workerExecutor, workerCount);
	}

	@Override
	protected void init() {
		super.init();
		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossExecutor, workerExecutor, workerCount));
	}

	@Override
	protected void initSystemListener() {
		super.initSystemListener();
		getListeners().add(0, new AcceptorGroupMessageEventListener(eventDispatcher));
	}

	public List<Channel> bind(SocketAddress... addressArray) {
		if (addressArray == null || addressArray.length == 0) {
			throw new IllegalArgumentException("addressArray can not be null or empty.");
		}

		// 设置Option
		Map<String, Object> options = getOptions();
		for (String key : options.keySet()) {
			bootstrap.setOption(key, options.get(key));
		}

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();

				// 注册各种自定义Handler
				LinkedHashMap<String, ChannelHandler> handlers = getHandlers();
				for (String key : handlers.keySet()) {
					pipeline.addLast(key, handlers.get(key));
				}

				// 注册链路空闲检测Handler
				Integer writeIdleTime = LangUtil.parseInt(getOption(HsfOptions.WRITE_IDLE_TIME));
				Integer readIdleTime = LangUtil.parseInt(getOption(HsfOptions.READ_IDLE_TIME));
				if (writeIdleTime == null) {
					writeIdleTime = 10;
				}
				if (readIdleTime == null) {
					// 默认为写空闲的3倍
					readIdleTime = writeIdleTime * 3;
				}

				pipeline.addLast("timeout", new IdleStateHandler(idleTimer, readIdleTime, writeIdleTime, 0));
				pipeline.addLast("idleHandler", new StateCheckChannelHandler(HsfAcceptorImpl.this));

				// 注册事件分发Handler
				pipeline.addLast("dispatchHandler", new DispatchUpStreamHandler(eventDispatcher));

				return pipeline;
			}
		});

		// 监听端口
		List<Channel> retChannels = new ArrayList<Channel>();
		for (SocketAddress address : addressArray) {
			retChannels.add(bootstrap.bind(address));

			logger.warn("Server started, listen at [{}]", address);
		}

		alive.set(true);

		return retChannels;
	}

	public List<Channel> bind(int... portArray) {
		if (portArray == null || portArray.length == 0) {
			throw new IllegalArgumentException("portArray can not be null or empty.");
		}

		SocketAddress[] addressArray = new SocketAddress[portArray.length];
		for (int i = 0; i < portArray.length; i++) {
			int port = portArray[i];
			addressArray[i] = new InetSocketAddress(port);
		}

		return bind(addressArray);
	}

	@Override
	public boolean isAlived() {
		return alive.get();
	}

	public void shutdown() {
		alive.set(false);
		super.shutdown();
		idleTimer.stop();

		if (bootstrap != null) {
			bootstrap.releaseExternalResources();
		}
	}

	public AcceptorHandshakeProcessor getHandshakeProcessor() {
		return handshakeProcessor;
	}

	public void setHandshakeProcessor(AcceptorHandshakeProcessor handshakeProcessor) {
		this.handshakeProcessor = handshakeProcessor;
	}

}
