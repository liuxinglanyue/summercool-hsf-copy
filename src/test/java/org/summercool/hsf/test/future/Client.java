package org.summercool.hsf.test.future;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.summercool.hsf.future.ChannelGroupFuture;
import org.summercool.hsf.future.InvokeFuture;
import org.summercool.hsf.netty.channel.HsfChannelGroup;
import org.summercool.hsf.netty.service.HsfConnector;
import org.summercool.hsf.netty.service.HsfConnectorImpl;
import org.summercool.hsf.proxy.ServiceProxyFactory;
import org.summercool.hsf.test.listener.TestChannelListener;
import org.summercool.hsf.test.service.TestService;
import org.summercool.hsf.util.AsyncFutureInvoker;


public class Client {
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		HsfConnector connector = new HsfConnectorImpl();
		connector.getListeners().add(new TestChannelListener());
		
		ChannelGroupFuture groupFuture = connector.connect(new InetSocketAddress("localhost", 8082));
		List<HsfChannelGroup> group = groupFuture.getGroupList();

		final TestService testService = ServiceProxyFactory.getRoundFactoryInstance(connector).wrapAsyncFutureProxy(
				TestService.class);
		AsyncFutureInvoker<TestService> futureInvoker = new AsyncFutureInvoker<TestService>(testService) {
			@Override
			protected void invokeService() {
				this.service.test("future invoker");
			}
		};
		InvokeFuture future = futureInvoker.invoke();
		System.out.println(future.getResult());
	}
}
