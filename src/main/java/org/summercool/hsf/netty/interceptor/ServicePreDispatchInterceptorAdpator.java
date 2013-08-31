package org.summercool.hsf.netty.interceptor;

import org.summercool.hsf.netty.event.EventDispatcher;
import org.summercool.hsf.pojo.RemoteServiceObject;
import org.summercool.hsf.pojo.RequestObject;

/**
 * @Description: 远程服务调用消息拦截适配类
 * @author 简道
 * @date 2012-5-17 下午1:22:52
 */
public abstract class ServicePreDispatchInterceptorAdpator implements PreDispatchInterceptor<Object> {
	@Override
	public boolean canIntercept(Object msg) {
		return ((msg instanceof RequestObject) && ((RequestObject) msg).getTarget() instanceof RemoteServiceObject)
				|| msg instanceof RemoteServiceObject;
	}

	@Override
	public boolean intercept(EventDispatcher eventDispatcher, Object msg) {
		if (msg instanceof RemoteServiceObject) {
			return innerIntercept(eventDispatcher, (RemoteServiceObject) msg);
		} else if (msg instanceof RequestObject) {
			RequestObject request = (RequestObject) msg;
			if (request.getTarget() instanceof RemoteServiceObject) {
				return innerIntercept(eventDispatcher, (RemoteServiceObject) request.getTarget());
			}
		}
		return false;
	}

	protected abstract boolean innerIntercept(EventDispatcher eventDispatcher, RemoteServiceObject msg);
}
