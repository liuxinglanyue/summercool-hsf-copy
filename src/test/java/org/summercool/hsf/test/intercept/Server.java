package org.summercool.hsf.test.intercept;

import java.util.LinkedList;

import org.summercool.hsf.netty.event.EventDispatcher;
import org.summercool.hsf.netty.interceptor.PreDispatchInterceptor;
import org.summercool.hsf.netty.interceptor.ServicePreDispatchInterceptorAdpator;
import org.summercool.hsf.netty.service.HsfAcceptor;
import org.summercool.hsf.netty.service.HsfAcceptorImpl;
import org.summercool.hsf.pojo.RemoteServiceMessage;
import org.summercool.hsf.pojo.RemoteServiceObject;
import org.summercool.hsf.test.service.TestServiceImpl;


/**
 * @Title: Server.java
 * @Description: TODO(添加描述)
 * @date 2012-2-23 上午12:58:53
 * @version V1.0
 */
public class Server {
	public static void main(String[] args) {
		HsfAcceptor acceptor = new HsfAcceptorImpl();
		LinkedList<PreDispatchInterceptor> interceptors = new LinkedList<PreDispatchInterceptor>();
		interceptors.add(new ServicePreDispatchInterceptorAdpator() {
			
			@Override
			protected boolean innerIntercept(EventDispatcher eventDispatcher, RemoteServiceObject msg) {
				return false;
			}
		});
		acceptor.setPreDispatchInterceptors(interceptors);
		acceptor.registerService(new TestServiceImpl());
		
		acceptor.bind(8082);
	}
}
