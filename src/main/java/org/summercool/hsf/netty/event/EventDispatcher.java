package org.summercool.hsf.netty.event;

import java.util.EventListener;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.summercool.hsf.exception.HsfInterceptedException;
import org.summercool.hsf.exception.HsfRemoteServiceException;
import org.summercool.hsf.future.ChannelGroupFuture;
import org.summercool.hsf.netty.channel.FlowManager;
import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.channel.HsfChannelGroup;
import org.summercool.hsf.netty.interceptor.PreDispatchInterceptor;
import org.summercool.hsf.netty.listener.ChannelEventListener;
import org.summercool.hsf.netty.listener.EventBehavior;
import org.summercool.hsf.netty.listener.ExceptionEventListener;
import org.summercool.hsf.netty.listener.MessageEventListener;
import org.summercool.hsf.netty.service.HsfService;
import org.summercool.hsf.pojo.RemoteServiceObject;
import org.summercool.hsf.pojo.RequestObject;
import org.summercool.hsf.pojo.ResponseObject;
import org.summercool.hsf.threadpool.AbortPolicyWithReport;
import org.summercool.hsf.util.EndlessLoopUtil;
import org.summercool.hsf.util.HandshakeUtil;
import org.summercool.hsf.util.HsfContextHolder;
import org.summercool.hsf.util.NamedThreadFactory;
import org.summercool.hsf.util.StackTraceUtil;

/**
 * @Title: EventDispatcher.java
 * @Package org.summercool.hsf.netty.listener
 * @Description: 事件分发类
 * @author 简道
 * @date 2011-9-27 下午2:36:57
 * @version V1.0
 */
public class EventDispatcher {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Executor executor;
	private Executor channelExecutor;
	private Executor exceptionExecutor;
	private int maximumPoolSize = 150;
	private int queueCapacity = 1000000;

	/**
	 * @Fields service : 接受事件分发的Hsf服务对象
	 */
	protected HsfService service;

	public EventDispatcher(HsfService service) {
		if (service == null) {
			throw new IllegalArgumentException("service");
		}

		this.service = service;
		this.executor = getExecutor();
		this.exceptionExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1,
				new NamedThreadFactory("ExceptionEventProcessor", true));
		this.channelExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("ChannelEventProcessor", true));
	}

	private ThreadPoolExecutor getExecutor() {
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(maximumPoolSize, maximumPoolSize, 60L,
				TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(queueCapacity), new NamedThreadFactory(
						"EventDispatcherProcessor", true), new AbortPolicyWithReport("EventDispatcherProcessor"));
		return threadPoolExecutor;
	}

	/**
	 * @Title: dispatchMessageEvent
	 * @Description: 分发消息事件
	 * @author 简道
	 * @param ctx
	 * @param channel
	 * @param e
	 * @return void 返回类型
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void dispatchMessageEvent(final ChannelHandlerContext ctx, final HsfChannel channel, final MessageEvent e) {
		Executor threadPool = executor;
		if (HandshakeUtil.isInitMsg(e.getMessage())) {
			threadPool = channelExecutor;
		} else {
			// 对非初始消息进行拦截
			if (service.getPreDispatchInterceptors() != null && service.getPreDispatchInterceptors().size() > 0) {
				try {
					for (PreDispatchInterceptor interceptor : service.getPreDispatchInterceptors()) {
						//
						if (interceptor.canIntercept(e.getMessage()) && !interceptor.intercept(this, e.getMessage())) {
							// 直接返回调用方
							returnWithException(e.getMessage(), new HsfInterceptedException("intercepted by " + interceptor.getClass()), channel);
							return;
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		//
		try {
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						// 为当前线程添加Channel
						HsfContextHolder.setChannel(channel);
						HsfContextHolder.setHsfService(service);
						HsfContextHolder.setRemoteGroupName(channel.getChannelGroup() != null ? channel.getChannelGroup().getName() : null);

						for (EventListener listener : service.getListeners()) {
							if (listener instanceof MessageEventListener) {
								EventBehavior eventBehavior = ((MessageEventListener) listener).messageReceived(ctx, channel, e);
								if (EventBehavior.Break.equals(eventBehavior)) {
									break;
								}
							}
						}
					} catch (Exception ex) {
						dispatchExceptionCaught(ctx, channel, new DefaultExceptionEvent(channel, ex));
					} finally {
						// 清除当前线程持有的Channel
						HsfContextHolder.setChannel(null);
						HsfContextHolder.setHsfService(null);
						HsfContextHolder.setRemoteGroupName(null);
					}
				}
			});
		} catch (RejectedExecutionException ex) {
			//
			if (e.getMessage() instanceof RequestObject) {
				RequestObject request = (RequestObject) e.getMessage();
				//
				returnWithException(request, ex, channel);
			}
		}
	}

	private void returnWithException(Object msg, Exception ex, Channel channel) {
		logger.info("msg {} is intrcepted", msg);
		if (msg instanceof RemoteServiceObject) {
			return;
		} else if (msg instanceof RequestObject) {
			//
			RequestObject request = (RequestObject) msg;
			ResponseObject resObj = new ResponseObject();
			resObj.setClientId(request.getClientId());
			resObj.setSeq(request.getSeq());

			//
			HsfRemoteServiceException exWrapper = new HsfRemoteServiceException(ex.getMessage(), ex);
			// 去除异常中循环嵌套引用
			EndlessLoopUtil.fixEndlessLoop(exWrapper);

			//
			resObj.setCause(exWrapper);
			//
			channel.write(resObj);
		}
	}

	public void dispatchGroupCreatedEvent(final ChannelHandlerContext ctx, final HsfChannel channel,
			final String groupName) {
		logger.warn("group {} is created.", groupName);
		//
		HsfContextHolder.setChannel(channel);
		HsfContextHolder.setHsfService(service);
		HsfContextHolder.setRemoteGroupName(groupName);
		HsfContextHolder.setInProcessingGroupCreatedEvent(true);

		try {
			for (EventListener listener : service.getListeners()) {
				if (listener instanceof ChannelEventListener) {
					ChannelEventListener channelEventListener = (ChannelEventListener) listener;
					EventBehavior eventBehavior = channelEventListener.groupCreated(ctx, channel, groupName);

					if (EventBehavior.Break.equals(eventBehavior)) {
						break;
					}
				}
			}
		} catch (Exception e) {
			dispatchExceptionCaught(ctx, channel, new DefaultExceptionEvent(channel, e));
		} finally {
			HsfContextHolder.setInProcessingGroupCreatedEvent(false);
			HsfContextHolder.setChannel(null);
			HsfContextHolder.setHsfService(null);
			HsfContextHolder.setRemoteGroupName(null);

			HsfChannelGroup group = service.getGroups().get(groupName);
			if (group != null) {
				// add group to future
				ChannelGroupFuture channelFuture = channel.getChannelGroupFuture();
				if (channelFuture != null) {
					channelFuture.addGroup(group);
				}
			}
		}
	}

	public void dispatchGroupRemovedEvent(final ChannelHandlerContext ctx, final HsfChannel channel,
			final String groupName) {
		try {
			logger.warn("group {} is removed.", groupName);
			for (EventListener listener : service.getListeners()) {
				if (listener instanceof ChannelEventListener) {
					ChannelEventListener channelEventListener = (ChannelEventListener) listener;
					EventBehavior eventBehavior = channelEventListener.groupRemoved(ctx, channel, groupName);

					if (EventBehavior.Break.equals(eventBehavior)) {
						break;
					}
				}
			}
		} catch (Exception e) {
			dispatchExceptionCaught(ctx, channel, new DefaultExceptionEvent(channel, e));
		} finally {
			service.getGroups().remove(groupName);
		}
	}

	/**
	 * @Title: dispatchChannelEvent
	 * @Description: 分发通道事件
	 * @author 简道
	 * @param ctx
	 * @param channel
	 * @param e
	 * @return void 返回类型
	 */
	public void dispatchChannelEvent(final ChannelHandlerContext ctx, final HsfChannel channel,
			final ChannelStateEvent e) {
		// 只处理通道Connected事件
		if (!ChannelState.CONNECTED.equals(e.getState())) {
			return;
		}
		//
		final boolean isConnected = e.getValue() != null;
		//
		channelExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					// 为当前线程添加Channel
					HsfContextHolder.setChannel(channel);
					HsfContextHolder.setHsfService(channel.getService());
					HsfContextHolder.setRemoteGroupName(channel.getChannelGroup() != null ? channel.getChannelGroup()
							.getName() : null);
					// 恢复流量
					restoreFlow(channel);
					//
					for (EventListener listener : service.getListeners()) {
						if (listener instanceof ChannelEventListener) {
							ChannelEventListener channelEventListener = (ChannelEventListener) listener;

							EventBehavior eventBehavior = EventBehavior.Continue;
							if (isConnected) {
								eventBehavior = channelEventListener.channelConnected(ctx, channel, e);
							} else {
								eventBehavior = channelEventListener.channelClosed(ctx, channel, e);
							}

							if (EventBehavior.Break.equals(eventBehavior)) {
								break;
							}
						}
					}
				} catch (Exception ex) {
					dispatchExceptionCaught(ctx, channel, new DefaultExceptionEvent(channel, ex));
				} finally {
					try {
						// group removed
						if (!isConnected) {
							//
							HsfChannelGroup channelGroup = channel.getChannelGroup();
							if (channelGroup != null) {
								synchronized (channelGroup) {
									channelGroup = service.getGroups().get(channelGroup.getName());
									if (channelGroup != null && channelGroup.isEmpty()) {
										dispatchGroupRemovedEvent(ctx, channel, channelGroup.getName());
									}
								}
							}
						}
					} catch (Exception e) {
						dispatchExceptionCaught(ctx, channel, new DefaultExceptionEvent(channel, e));
					}

					// 清除当前线程持有的Channel
					HsfContextHolder.setChannel(null);
					HsfContextHolder.setHsfService(null);
					HsfContextHolder.setRemoteGroupName(null);
				}
			}

			// 恢复流量
			private void restoreFlow(HsfChannel channel) {
				FlowManager flowManager = service.getFlowManager();
				if (flowManager != null) {
					flowManager.release((int) channel.getWaitingResponseNum());
				}
			}
		});
	}

	/**
	 * @Title: dispatchExceptionCaught
	 * @Description: 分发异常事件
	 * @author 简道
	 * @param ctx
	 * @param channel
	 * @param e
	 * @return void 返回类型
	 */
	public void dispatchExceptionCaught(final ChannelHandlerContext ctx, final HsfChannel channel,
			final ExceptionEvent e) {
		exceptionExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					// 为当前线程添加Channel
					HsfContextHolder.setChannel(channel);
					HsfContextHolder.setHsfService(channel.getService());
					HsfContextHolder.setRemoteGroupName(channel.getChannelGroup() != null ? channel.getChannelGroup()
							.getName() : null);

					for (EventListener listener : service.getListeners()) {
						if (listener instanceof ExceptionEventListener) {
							ExceptionEventListener exEventListener = (ExceptionEventListener) listener;

							EventBehavior eventBehavior = exEventListener.exceptionCaught(ctx, channel, e);
							if (EventBehavior.Break.equals(eventBehavior)) {
								break;
							}
						}
					}
				} catch (Exception ex) {
					logger.error("dispatchExceptionCaught on channel {} error:{}.", channel,
							StackTraceUtil.getStackTrace(ex));
				} finally {
					// 清除当前线程持有的Channel
					HsfContextHolder.setChannel(null);
					HsfContextHolder.setHsfService(null);
					HsfContextHolder.setRemoteGroupName(null);
				}
			}
		});
	}

	public void setMaximumPoolSize(int maximumPoolSize) {
		if (maximumPoolSize < Runtime.getRuntime().availableProcessors() + 1) {
			throw new IllegalArgumentException("maximumPoolSize must great than " + Runtime.getRuntime().availableProcessors());
		}

		if (this.maximumPoolSize != maximumPoolSize) {
			this.maximumPoolSize = maximumPoolSize;
			setExecutor(getExecutor());
		}
	}

	public int getMaxinumPoolSize() {
		return maximumPoolSize;
	}

	public int getQueueCapacity() {
		return queueCapacity;
	}

	public void setQueueCapacity(int queueCapacity) {
		if (queueCapacity <= 0) {
			throw new IllegalArgumentException("queueCapacity must great than 0");
		}
		if (this.queueCapacity != queueCapacity) {
			this.queueCapacity = queueCapacity;
			setExecutor(getExecutor());
		}
	}

	public HsfService getService() {
		return service;
	}

	public void setExecutor(Executor executor) {
		if (executor == null) {
			throw new NullPointerException("executor is null.");
		}
		Executor preExecutor = this.executor;
		this.executor = executor;
		//
		if (preExecutor instanceof ExecutorService) {
			List<Runnable> tasks = ((ExecutorService) preExecutor).shutdownNow();
			if (tasks != null && tasks.size() > 0) {
				for (Runnable task : tasks) {
					this.executor.execute(task);
				}
			}
		}
	}

	public int getExecutorActiveCount() {
		if (executor instanceof ThreadPoolExecutor) {
			return ((ThreadPoolExecutor) executor).getActiveCount();
		}
		return -1;
	}

	public long getExecutorCompletedTaskCount() {
		if (executor instanceof ThreadPoolExecutor) {
			return ((ThreadPoolExecutor) executor).getCompletedTaskCount();
		}
		return -1;
	}

	public int getExecutorLargestPoolSize() {
		if (executor instanceof ThreadPoolExecutor) {
			return ((ThreadPoolExecutor) executor).getLargestPoolSize();
		}
		return -1;
	}

	public int getExecutorPoolSize() {
		if (executor instanceof ThreadPoolExecutor) {
			return ((ThreadPoolExecutor) executor).getPoolSize();
		}
		return -1;
	}

	public long getExecutorTaskCount() {
		if (executor instanceof ThreadPoolExecutor) {
			return ((ThreadPoolExecutor) executor).getTaskCount();
		}
		return -1;
	}

	public int getExecutorQueueSize() {
		if (executor instanceof ThreadPoolExecutor) {
			return ((ThreadPoolExecutor) executor).getQueue().size();
		}
		return -1;
	}
}