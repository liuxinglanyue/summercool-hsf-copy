package org.summercool.hsf.netty.listener.impl;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DefaultExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.summercool.hsf.exception.HsfNoSuchServiceException;
import org.summercool.hsf.exception.HsfRemoteServiceException;
import org.summercool.hsf.future.InvokeFuture;
import org.summercool.hsf.netty.channel.FlowManager;
import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.channel.HsfChannelGroup;
import org.summercool.hsf.netty.event.EventDispatcher;
import org.summercool.hsf.netty.listener.EventBehavior;
import org.summercool.hsf.netty.listener.MessageEventListener;
import org.summercool.hsf.pojo.RemoteServiceObject;
import org.summercool.hsf.pojo.RequestObject;
import org.summercool.hsf.pojo.ResponseObject;
import org.summercool.hsf.pojo.ServiceEntry;
import org.summercool.hsf.statistic.StatisticInfo;
import org.summercool.hsf.util.AsyncCallback;
import org.summercool.hsf.util.CallbackRegister;
import org.summercool.hsf.util.EndlessLoopUtil;
import org.summercool.hsf.util.HsfOptions;
import org.summercool.hsf.util.LangUtil;
import org.summercool.hsf.util.ReflectionUtil;
import org.summercool.hsf.util.StackTraceUtil;

/**
 * @Title: ServiceMessageEventListener.java
 * @Package org.summercool.hsf.netty.listener.impl
 * @Description: 远程服务调用消息处理
 * @author 简道
 * @date 2011-9-29 下午1:11:13
 * @version V1.0
 */
public class ServiceMessageEventListener implements MessageEventListener {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private EventDispatcher eventDispatcher;

	public ServiceMessageEventListener(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	public EventBehavior messageReceived(ChannelHandlerContext ctx, HsfChannel channel, MessageEvent e) {
		// 统计
		statsRev(channel, e.getMessage());
		//
		if (e.getMessage() != null) {
			if (e.getMessage() instanceof RemoteServiceObject) {
				RequestObject request = new RequestObject();
				// 设置Seq为-1，不返回结果
				request.setSeq(-1);
				request.setTarget(e.getMessage());

				// 处理请求消息
				processRequest(ctx, channel, request);
			} else if (e.getMessage() instanceof RequestObject) {

				RequestObject request = (RequestObject) e.getMessage();
				if (request.getTarget() != null) {
					if (request.getTarget() instanceof RemoteServiceObject) {
						// 处理请求消息
						processRequest(ctx, channel, request);
					}
				}
			} else if (e.getMessage() instanceof ResponseObject) {

				ResponseObject response = (ResponseObject) e.getMessage();
				// 处理反馈消息
				processResponse(ctx, response, channel);
			}
		}
		return EventBehavior.Continue;
	}

	private void statsRev(HsfChannel channel, Object msg) {
		//
		HsfChannelGroup channelGroup = channel.getChannelGroup();
		if (channelGroup != null) {
			incrementRev(channelGroup.getMsgStatistic());
		}
		//
		incrementRev(channel.getMsgStatistic());
	}

	private void incrementRev(StatisticInfo msgStatistic) {
		msgStatistic.getReceivedNum().incrementAndGet();
		msgStatistic.setLastestReceivedIfLater(System.currentTimeMillis());
	}

	private void processRequest(ChannelHandlerContext ctx, HsfChannel channel, RequestObject request) {
		if (request.getTarget() != null && request.getTarget() instanceof RemoteServiceObject) {

			RemoteServiceObject remoteServiceObj = (RemoteServiceObject) request.getTarget();
			// 根据服务名称，取出服务对象
			ServiceEntry serviceEntry = eventDispatcher.getService().getServices()
					.get(remoteServiceObj.getServiceName());
			// 构造Response消息
			ResponseObject responseObj = new ResponseObject();
			responseObj.setClientId(request.getClientId());
			responseObj.setSeq(request.getSeq());

			if (serviceEntry != null && serviceEntry.getService() != null) {
				// 取出方法名和参数
				Object service = serviceEntry.getService();
				String methodName = remoteServiceObj.getMethodName();
				Object[] args = remoteServiceObj.getArgs();

				try {
					// 调用服务
					Object retObj = ReflectionUtil.invoke(service, methodName, args);
					// 设置调用结果返回值
					responseObj.setTarget(retObj);
				} catch (Throwable ex) {
					eventDispatcher.dispatchExceptionCaught(ctx, channel, new DefaultExceptionEvent(channel, ex));
					//
					ex = new HsfRemoteServiceException(StackTraceUtil.getStackTrace(ex));

					// 去除异常中循环嵌套引用
					EndlessLoopUtil.fixEndlessLoop(ex);

					// 设置异常信息
					responseObj.setCause(ex);
				} finally {
					// 统计Service被调用次数
					HsfChannelGroup channelGroup = channel.getChannelGroup();
					if (channelGroup != null
							&& LangUtil.parseBoolean(
									eventDispatcher.getService().getOption(HsfOptions.OPEN_SERVICE_INVOKE_STATISTIC),
									false)) {
						channelGroup.getServiceStatistic().increaseInvokedNum(remoteServiceObj.getServiceName(),
								remoteServiceObj.getMethodName());
					}
				}
			} else {
				HsfNoSuchServiceException ex = new HsfNoSuchServiceException(remoteServiceObj.getServiceName());
				eventDispatcher.dispatchExceptionCaught(ctx, channel, new DefaultExceptionEvent(channel, ex));
				//
				HsfRemoteServiceException exWrapper = new HsfRemoteServiceException(ex.getMessage());

				// 去除异常中循环嵌套引用
				EndlessLoopUtil.fixEndlessLoop(exWrapper);

				// 设置异常信息
				responseObj.setCause(exWrapper);
			}

			if (request.isNeedCallback()
					&& (request.getSeq() != -1 || responseObj.getCause() != null || responseObj.getCauseMessage() != null)) {
				// 回发Response消息
				channel.write(responseObj);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void processResponse(ChannelHandlerContext ctx, ResponseObject response, HsfChannel channel) {
		// 处理Seq为-1的异常信息
		if (response.getSeq() == -1) {
			if (response.getCause() != null) {
				ctx.sendUpstream(new DefaultExceptionEvent(channel, response.getCause()));
			}
			return;
		}

		// Future方式
		InvokeFuture future = channel.getFutures().remove(response.getSeq());
		if (future != null) {
			if (response.getCause() != null) {
				future.setCause(response.getCause());
			} else {
				future.setResult(response.getTarget());
			}
			// 释放流量
			flowRelease(channel);
		}

		// Callback方式
		AsyncCallback callback = channel.getCallbacks().remove(response.getSeq());
		if (callback != null) {
			//
			Object param = channel.getCallbackParamMap().remove(response.getSeq());
			try {
				CallbackRegister.setCallbackParam(param);
				//
				if (response.getCause() != null) {
					callback.doExceptionCaught(response.getCause(), channel, null);
				} else {
					callback.doCallback(response.getTarget());
				}
			} catch (Throwable ex) {
				logger.error(StackTraceUtil.getStackTrace(ex));
			} finally {
				// 释放流量
				flowRelease(channel);
				//
				CallbackRegister.clearCallbackParam();
			}
		}
	}

	private void flowRelease(HsfChannel channel) {
		// 流量控制
		FlowManager flowManager = this.eventDispatcher.getService().getFlowManager();
		if (flowManager != null) {
			flowManager.release();
			//
			channel.decrWaitingResponseNum();
		}
	}
}