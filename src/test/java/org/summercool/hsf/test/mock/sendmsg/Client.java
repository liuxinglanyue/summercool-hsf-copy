package org.summercool.hsf.test.mock.sendmsg;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.listener.ChannelEventListenerAdapter;
import org.summercool.hsf.netty.listener.EventBehavior;
import org.summercool.hsf.netty.service.HsfConnector;
import org.summercool.hsf.netty.service.HsfConnectorImpl;
import org.summercool.hsf.proxy.ServiceProxyFactory;
import org.summercool.hsf.test.mock.service.ClientServiceImpl;
import org.summercool.hsf.test.mock.service.ServerService;
import org.summercool.hsf.util.AsyncCallback;

/**
 * @Title: Client.java
 * @Description: 测试Server和Client互发消息，及重连
 * @date 2012-2-28 上午09:07:59
 * @version V1.0
 */
public class Client {
	private static TestAsyncCallback testAsyncCallback = new TestAsyncCallback();
	private static ServerService serverService;

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		HsfConnector connector = new HsfConnectorImpl();
		connector.getListeners().add(new ConnectorChannelEventHandler());
		connector.registerService(new ClientServiceImpl());
		serverService = ServiceProxyFactory.getRoundFactoryInstance(connector).wrapSyncProxy(ServerService.class);

		connector.connect(new InetSocketAddress("192.168.1.26", 8082));

		// final ServerService serverService =
		// ServiceProxyFactory.getRoundFactoryInstance(connector).wrapAsyncCallbackProxy(
		// ServerService.class, testAsyncCallback);
	}

	public static class TestAsyncCallback extends AsyncCallback<Object> {
		public void doCallback(Object data) {
			System.out.println(data);
		};
	}

	static class ConnectorChannelEventHandler extends ChannelEventListenerAdapter {
		@Override
		public EventBehavior groupCreated(ChannelHandlerContext ctx, HsfChannel channel, String groupName) {
			//
			for (int i = 0; i < 10; i++) {
				System.out.println(serverService.callServer(" client test " + i));
			}

			return EventBehavior.Continue;
		}
	}
}
