package org.summercool.hsf.netty.channel;

import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelConfig;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.summercool.hsf.exception.HsfFlowExceededException;
import org.summercool.hsf.exception.HsfRuntimeException;
import org.summercool.hsf.future.ChannelGroupFuture;
import org.summercool.hsf.future.InvokeFuture;
import org.summercool.hsf.netty.service.HsfService;
import org.summercool.hsf.pojo.CallbackMessage;
import org.summercool.hsf.pojo.Heartbeat;
import org.summercool.hsf.pojo.RemoteServiceObject;
import org.summercool.hsf.pojo.RequestObject;
import org.summercool.hsf.pojo.ResponseObject;
import org.summercool.hsf.statistic.StatisticInfo;
import org.summercool.hsf.util.AsyncCallback;
import org.summercool.hsf.util.CallbackRegister;
import org.summercool.hsf.util.HsfOptions;
import org.summercool.hsf.util.LangUtil;
import org.summercool.hsf.util.StackTraceUtil;

/**
 * @Title: HsfChannel.java
 * @Package org.summercool.hsf.netty.channel
 * @Description: Channel包装类
 * @author 简道
 * @date Nov 16, 2011 12:47:41 AM
 * @version V1.0
 */
public class HsfChannel implements Channel {

	/**
	 * @Fields service : 拥有该Channel的Service
	 */
	private HsfService service;
	/**
	 * @Fields channel : 被包装的真实Channel
	 */
	private Channel channel;
	/**
	 * @Fields channelGroup : 该Channel所在的Group
	 */
	private HsfChannelGroup channelGroup;
	/**
	 * @Fields channelGroupFuture :
	 */
	private ChannelGroupFuture channelGroupFuture;
	/**
	 * @Fields stopReconnect : 停止重连
	 */
	private boolean stopReconnect = false;
	/**
	 * @Fields msgStatistic : 消息统计
	 */
	private final StatisticInfo msgStatistic = new StatisticInfo();
	/**
	 * @Fields heardBeatStatistic : 心跳统计
	 */
	private final StatisticInfo heartBeatStatistic = new StatisticInfo();
	/**
	 * @Fields attachment : 附件
	 */
	private Object attachment;
	/**
	 * @Fields seq : 请求序号
	 */
	private static AtomicLong seq = new AtomicLong(0);
	/**
	 * @Fields futures : 存储通道上所有等待中的{@link InvokeFuture}
	 */
	private ConcurrentHashMap<Long, InvokeFuture<?>> futures = new ConcurrentHashMap<Long, InvokeFuture<?>>();
	/**
	 * @Fields callbacks : 存储通道上所有等待中的{@link AsyncCallback}
	 */
	private ConcurrentHashMap<Long, AsyncCallback<?>> callbacks = new ConcurrentHashMap<Long, AsyncCallback<?>>();
	/**
	 * @Fields msgs : 存储通道上所有Callback调用保存的参数
	 */
	private ConcurrentHashMap<Long, Object> cbParamMap = new ConcurrentHashMap<Long, Object>();
	/**
	 * @Fields handshakeTimeout : 握手超时任务
	 */
	private volatile Timeout handshakeTimeout;
	/**
	 * 等待Response的消息数
	 */
	private AtomicLong waitingResponseNum = new AtomicLong();

	private Logger logger = LoggerFactory.getLogger(getClass());
	private volatile boolean needLock = true;
	private ReentrantLock statisticLock = new ReentrantLock();

	private final ChannelFutureListener sendMsgSuccessListener = new ChannelFutureListener() {
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			//
			if (future.isSuccess()) {
				boolean lock = needLock;
				try {
					if (lock) {
						statisticLock.lock();
					}
					//
					long now = System.currentTimeMillis();
					msgStatistic.getSentNum().incrementAndGet();
					msgStatistic.setLastestSent(now);
					if (channelGroup != null) {
						channelGroup.getMsgStatistic().getSentNum().incrementAndGet();
						channelGroup.getMsgStatistic().setLastestSent(now);
					}
				} finally {
					if (lock) {
						statisticLock.unlock();
					}
				}
			}
		}
	};
	private final ChannelFutureListener sendHeartBeatSuccessListener = new ChannelFutureListener() {
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			//
			if (future.isSuccess()) {
				boolean lock = needLock;
				try {
					if (lock) {
						statisticLock.lock();
					}
					//
					long now = System.currentTimeMillis();
					heartBeatStatistic.getSentNum().incrementAndGet();
					heartBeatStatistic.setLastestSent(now);

					if (channelGroup != null) {
						channelGroup.getHeartBeatStatistic().getSentNum().incrementAndGet();
						channelGroup.getHeartBeatStatistic().setLastestSent(now);
					}
				} finally {
					if (lock) {
						statisticLock.unlock();
					}
				}
			}
		}
	};
	private final ChannelFutureListener closeListener = new ChannelFutureListener() {
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			Timeout timeout = HsfChannel.this.handshakeTimeout;
			if (timeout != null) {
				timeout.cancel();
			}
		}
	};
	private final ChannelFutureListener doneListener = new ChannelFutureListener() {
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			// 释放流量
			HsfChannel.this.flowRelease();
		}
	};

	public HsfChannel(HsfService service, Channel channel) {
		if (service == null) {
			throw new IllegalArgumentException("service can not be null.");
		} else if (channel == null) {
			throw new IllegalArgumentException("channel can not be null.");
		}

		this.service = service;
		this.channel = channel;
		this.channel.getCloseFuture().addListener(closeListener);
	}

	public InvokeFuture<?> writeAsync(Object msg) {
		// 流量控制
		flowAcquire();
		try {
			//
			final InvokeFuture<Object> invokeFuture = new InvokeFuture<Object>();
			invokeFuture.setChannel(this);

			// 创建Request对象
			RequestObject request = new RequestObject();
			final long seq = getSeq();
			request.setSeq(seq);
			request.setTarget(msg);

			// 存储InvokeFuture
			futures.put(request.getSeq(), invokeFuture);

			// 发送Request对象
			ChannelFuture channelFuture = write(request);
			channelFuture.addListener(new ChannelFutureListener() {
				public void operationComplete(ChannelFuture future) throws Exception {
					//
					if (!future.isSuccess()) {
						// 释放流量
						flowRelease();
						//
						futures.remove(seq);
						invokeFuture.setCause(future.getCause());
					}
				}
			});

			return invokeFuture;
		} catch (Throwable e) {
			// 释放流量
			flowRelease();
			//
			throw new HsfRuntimeException(e);
		}
	}

	public void writeAsync(Object msg, final AsyncCallback<?> callback) {
		// 创建Request对象
		RequestObject request = new RequestObject();
		final long seq = getSeq();
		request.setSeq(seq);
		request.setTarget(msg);

		//
		if (callback != null) {
			// 流量控制
			flowAcquire();
			try {

				// 存储callback
				callbacks.put(seq, callback);

				//
				Object param = getCallbackMessage(msg);
				if (param != null && LangUtil.parseBoolean(service.getOption(HsfOptions.HOLD_CALLBACK_PARAM), false)) {
					// 存储参数
					cbParamMap.put(seq, param);
				}

				// 发送Request对象
				ChannelFuture channelFuture = write(request);
				channelFuture.addListener(new ChannelFutureListener() {
					public void operationComplete(ChannelFuture future) throws Exception {
						if (!future.isSuccess()) {
							callbacks.remove(seq);
							//
							Object m = cbParamMap.remove(seq);
							try {
								CallbackRegister.setCallbackParam(m);
								//
								callback.doExceptionCaught(future.getCause(), HsfChannel.this, m);
							} catch (Exception e) {
								logger.error(StackTraceUtil.getStackTrace(e));
							} finally {
								CallbackRegister.clearCallbackParam();
							}
						}
					}
				});
			} catch (Throwable e) {
				// 释放流量
				flowRelease();
			}
		} else {
			// no callback
			request.setNeedCallback(false);
			//
			write(msg);
		}
	}

	public Object writeSync(Object msg) {
		InvokeFuture<?> invokeFuture = writeAsync(msg);

		Object retObj = null;
		boolean invokeTimeout = false;
		Integer timeout = LangUtil.parseInt(service.getOption(HsfOptions.SYNC_INVOKE_TIMEOUT), 60000);
		if (invokeTimeout = (timeout != null && timeout > 0)) {
			// 等待返回，直到Response返回或超时
			retObj = invokeFuture.getResult(timeout, TimeUnit.MILLISECONDS);
		}

		if (!invokeTimeout) {
			// 一直等待，直到Response返回
			retObj = invokeFuture.getResult();
		}

		return retObj;
	}

	private void flowAcquire() {
		// 流量控制
		FlowManager flowManager = this.service.getFlowManager();
		if (flowManager != null) {
			int timeout = LangUtil.parseInt(this.service.getOption(HsfOptions.TIMEOUT_WHEN_FLOW_EXCEEDED), 0);
			if (!flowManager.acquire(1, timeout)) {
				throw new HsfFlowExceededException("flow exceeded");
			}
		}
		//
		incrWaitingResponseNum();
	}

	private void flowRelease() {
		// 流量控制
		FlowManager flowManager = this.service.getFlowManager();
		if (flowManager != null) {
			flowManager.release();
		}
		//
		decrWaitingResponseNum();
	}

	private void statsServiceInitiativeInvoke(Object msg) {
		if (msg != null && channelGroup != null) {
			RemoteServiceObject remoteServiceObj = null;
			//
			if (msg instanceof RequestObject) {
				Object targetMsg = ((RequestObject) msg).getTarget();
				if (targetMsg != null) {
					remoteServiceObj = (RemoteServiceObject) targetMsg;
				}
			} else if (msg instanceof RemoteServiceObject) {
				remoteServiceObj = (RemoteServiceObject) msg;
			}
			//
			if (remoteServiceObj != null
					&& LangUtil.parseBoolean(service.getOption(HsfOptions.OPEN_SERVICE_INVOKE_STATISTIC), false)) {
				channelGroup.getServiceStatistic().increaseInitiativeInvokeNum(remoteServiceObj.getServiceName(),
						remoteServiceObj.getMethodName());
			}
		}
	}

	public Timeout getHandshakeTimeout() {
		return handshakeTimeout;
	}

	public void setHandshakeTimeout(Timeout handshakeTimeout) {
		this.handshakeTimeout = handshakeTimeout;
	}

	public StatisticInfo getMsgStatistic() {
		return msgStatistic;
	}

	public StatisticInfo getHeartBeatStatistic() {
		return heartBeatStatistic;
	}

	private long getSeq() {
		return seq.getAndIncrement();
	}

	public ConcurrentHashMap<Long, InvokeFuture<?>> getFutures() {
		return futures;
	}

	public ConcurrentHashMap<Long, AsyncCallback<?>> getCallbacks() {
		return callbacks;
	}

	public ConcurrentHashMap<Long, Object> getCallbackParamMap() {
		return cbParamMap;
	}

	public HsfService getService() {
		return service;
	}

	public String getKey() {
		return getRemoteAddress().toString();
	}

	public Channel getChannel() {
		return channel;
	}

	public Integer getId() {
		return channel.getId();
	}

	public ChannelFactory getFactory() {
		return channel.getFactory();
	}

	public Channel getParent() {
		return channel.getParent();
	}

	public ChannelConfig getConfig() {
		return channel.getConfig();
	}

	public ChannelPipeline getPipeline() {
		return channel.getPipeline();
	}

	public boolean isOpen() {
		return channel.isOpen();
	}

	public boolean isBound() {
		return channel.isBound();
	}

	public boolean isConnected() {
		return channel.isConnected();
	}

	public SocketAddress getLocalAddress() {
		return channel.getLocalAddress();
	}

	public SocketAddress getRemoteAddress() {
		return channel.getRemoteAddress();
	}

	public ChannelFuture write(Object message) {
		ChannelFuture future = channel.write(message);

		if (message instanceof Heartbeat) {
			future.addListener(sendHeartBeatSuccessListener);
		} else {
			future.addListener(sendMsgSuccessListener);
			//
			tryFlowAcquire(message, future);
			//
			statsServiceInitiativeInvoke(message);
		}
		return future;
	}

	private void tryFlowAcquire(Object message, ChannelFuture future) {
		boolean isRequest = message instanceof RequestObject;
		boolean isResponse = message instanceof ResponseObject;
		if ((isRequest && !((RequestObject) message).isNeedCallback()) || (!isRequest && !isResponse)) {
			// 流量控制
			flowAcquire();
			future.addListener(doneListener);
		}
	}

	@SuppressWarnings("rawtypes")
	private Object getCallbackMessage(Object msg) {
		Object cbMsg = null;
		//
		if (msg instanceof RemoteServiceObject) {
			Object[] args = ((RemoteServiceObject) msg).getArgs();
			if (args == null || args.length == 0) {
				return null;
			}

			boolean holdCallbackMsg = LangUtil.parseBoolean(service.getOption(HsfOptions.HOLD_CALLBACK_PARAM), false);
			//
			List<Object> list = new ArrayList<Object>();
			for (Object arg : args) {
				//
				if (arg != null && arg instanceof CallbackMessage) {
					list.add(((CallbackMessage) arg).getMessage());
				} else {
					//
					if (holdCallbackMsg) {
						list.add(arg);
					} else {
						list.add(null);
					}
				}
			}
			cbMsg = list.toArray();
		} else if (msg instanceof CallbackMessage) {
			cbMsg = ((CallbackMessage) msg).getMessage();
		}
		return cbMsg;
	}

	public ChannelFuture write(Object message, SocketAddress remoteAddress) {
		ChannelFuture future = channel.write(message, remoteAddress);

		if (message instanceof Heartbeat) {
			future.addListener(sendHeartBeatSuccessListener);
		} else {
			future.addListener(sendMsgSuccessListener);
			//
			tryFlowAcquire(message, future);
			//
			statsServiceInitiativeInvoke(message);
		}
		return future;
	}

	public ChannelFuture bind(SocketAddress localAddress) {
		return channel.bind(localAddress);
	}

	public ChannelFuture connect(SocketAddress remoteAddress) {
		return channel.connect(remoteAddress);
	}

	public ChannelFuture disconnect() {
		return channel.disconnect();
	}

	public ChannelFuture unbind() {
		return channel.unbind();
	}

	public ChannelFuture close() {
		return innerClose();
	}

	public ChannelFuture getCloseFuture() {
		return channel.getCloseFuture();
	}

	public int getInterestOps() {
		return channel.getInterestOps();
	}

	public boolean isReadable() {
		return channel.isReadable();
	}

	public boolean isWritable() {
		return channel.isWritable();
	}

	public ChannelFuture setInterestOps(int interestOps) {
		return channel.setInterestOps(interestOps);
	}

	public ChannelFuture setReadable(boolean readable) {
		return channel.setReadable(readable);
	}

	public HsfChannelGroup getChannelGroup() {
		return channelGroup;
	}

	public void setChannelGroup(HsfChannelGroup channelGroup) {
		this.channelGroup = channelGroup;
		try {
			statisticLock.lock();
			//
			channelGroup.getMsgStatistic().getReceivedNum().addAndGet(this.getMsgStatistic().getReceivedNum().get());
			channelGroup.getMsgStatistic().setLastestReceivedIfLater(this.getMsgStatistic().getLastestReceived());
			//
			channelGroup.getMsgStatistic().getSentNum().addAndGet(this.getMsgStatistic().getSentNum().get());
			channelGroup.getMsgStatistic().setLastestSentIfLater(this.getMsgStatistic().getLastestSent());
			//
			channelGroup.getHeartBeatStatistic().getReceivedNum()
					.addAndGet(this.getHeartBeatStatistic().getReceivedNum().get());
			channelGroup.getHeartBeatStatistic().setLastestReceivedIfLater(
					this.getHeartBeatStatistic().getLastestReceived());
			//
			channelGroup.getHeartBeatStatistic().getSentNum()
					.addAndGet(this.getHeartBeatStatistic().getSentNum().get());
			channelGroup.getHeartBeatStatistic().setLastestSentIfLater(this.getHeartBeatStatistic().getLastestSent());
		} finally {
			statisticLock.unlock();
			needLock = false;
		}
	}

	public int compareTo(Channel o) {
		return channel.compareTo(o);
	}

	public boolean isStopReconnect() {
		return stopReconnect;
	}

	public ChannelFuture disconnect(boolean stopReconnect) {
		this.stopReconnect = stopReconnect;
		return channel.disconnect();
	}

	public ChannelFuture close(boolean stopReconnect) {
		this.stopReconnect = stopReconnect;
		return innerClose();
	}

	private ChannelFuture innerClose() {
		ChannelFuture channelFuture = getChannel().close();

		// cancel所有等待中的InvokeFuture
		for (InvokeFuture<?> future : futures.values()) {
			if (!future.isDone()) {
				future.setCause(new ClosedChannelException());
			}
		}
		//
		for (Map.Entry<Long, AsyncCallback<?>> entry : callbacks.entrySet()) {
			AsyncCallback<?> callback = entry.getValue();
			try {
				Object param = cbParamMap.remove(entry.getKey());
				CallbackRegister.setCallbackParam(param);
				//
				callback.doExceptionCaught(new ClosedChannelException(), this, param);
			} catch (Exception e) {
				logger.error(StackTraceUtil.getStackTrace(e));
			} finally {
				CallbackRegister.clearCallbackParam();
			}
		}

		return channelFuture;
	}

	public Object getAttachment() {
		return attachment;
	}

	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}

	public ChannelGroupFuture getChannelGroupFuture() {
		return channelGroupFuture;
	}

	public void setChannelGroupFuture(ChannelGroupFuture channelGroupFuture) {
		this.channelGroupFuture = channelGroupFuture;
	}

	public long getWaitingResponseNum() {
		return waitingResponseNum.get();
	}

	public long incrWaitingResponseNum() {
		return waitingResponseNum.incrementAndGet();
	}

	public long decrWaitingResponseNum() {
		return waitingResponseNum.decrementAndGet();
	}

	@Override
	public String toString() {
		return channel.toString();
	}
}
