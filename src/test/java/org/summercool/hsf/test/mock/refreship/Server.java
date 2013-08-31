package org.summercool.hsf.test.mock.refreship;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.listener.ChannelEventListenerAdapter;
import org.summercool.hsf.netty.listener.EventBehavior;
import org.summercool.hsf.netty.service.HsfAcceptor;
import org.summercool.hsf.netty.service.HsfAcceptorImpl;
import org.summercool.hsf.proxy.ServiceProxyFactory;
import org.summercool.hsf.test.mock.service.ClientService;
import org.summercool.hsf.test.mock.service.ServerServiceImpl;

/**
 * @Title: Server.java
 * @Package org.summercool.hsf.future
 * @Description: TODO(添加描述)
 * @date 2012-2-28 上午09:07:30
 * @version V1.0
 */
public class Server {
	private static ClientService clientService;

	public static void main(String[] args) {
		HsfAcceptor acceptor = new HsfAcceptorImpl();
		acceptor.getListeners().add(new AcceptorChannelEventHandler());
		acceptor.registerService(new ServerServiceImpl());
		//
		clientService = ServiceProxyFactory.getRoundFactoryInstance(acceptor).wrapSyncProxy(ClientService.class);
		//
		acceptor.bind(8082);
	}

	static class AcceptorChannelEventHandler extends ChannelEventListenerAdapter {
		@Override
		public EventBehavior groupCreated(ChannelHandlerContext ctx, HsfChannel channel, String groupName) {
			//
			for (int i = 0; i < 10; i++) {
				System.out.println(clientService.callClient(" server test " + i));
			}

			return EventBehavior.Continue;
		}
	}
}
