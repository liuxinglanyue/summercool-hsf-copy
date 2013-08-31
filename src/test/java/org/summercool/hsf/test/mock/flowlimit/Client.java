package org.summercool.hsf.test.mock.flowlimit;

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
import org.summercool.hsf.util.HsfOptions;

public class Client {
	private static TestAsyncCallback testAsyncCallback = new TestAsyncCallback();
	private static ServerService serverService;

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		HsfConnector connector = new HsfConnectorImpl();
		connector.setOption(HsfOptions.FLOW_LIMIT, 1);
		connector.getListeners().add(new ConnectorChannelEventHandler());
		connector.registerService(new ClientServiceImpl());
		serverService = ServiceProxyFactory.getRoundFactoryInstance(connector).wrapSyncProxy(ServerService.class);

		connector.connect(new InetSocketAddress("192.168.1.26", 8082));

		for (int i = 0; i < 100000; i++) {
			System.out.println(serverService.callServer(i + "/" + connector.getFlowManager().getAvailable()));
		}
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
