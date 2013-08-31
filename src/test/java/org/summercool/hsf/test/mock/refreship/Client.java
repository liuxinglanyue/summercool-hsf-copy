package org.summercool.hsf.test.mock.refreship;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.listener.ChannelEventListenerAdapter;
import org.summercool.hsf.netty.listener.EventBehavior;
import org.summercool.hsf.netty.service.HsfConnector;
import org.summercool.hsf.netty.service.HsfConnectorImpl;
import org.summercool.hsf.proxy.ServiceProxyFactory;
import org.summercool.hsf.test.mock.service.ClientServiceImpl;
import org.summercool.hsf.test.mock.service.ServerService;
import org.summercool.hsf.util.AddressUtil;
import org.summercool.hsf.util.AsyncCallback;
import org.summercool.hsf.util.StackTraceUtil;

/**
 * @Title: Client.java
 * @Description: 测试Server和Client互发消息，及重连
 * @date 2012-2-28 上午09:07:59
 * @version V1.0
 */
public class Client {
	private static TestAsyncCallback testAsyncCallback = new TestAsyncCallback();
	private static ServerService serverService;
	private static HsfConnector connector;

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		connector = new HsfConnectorImpl();
		connector.getListeners().add(new ConnectorChannelEventHandler());
		connector.registerService(new ClientServiceImpl());
		serverService = ServiceProxyFactory.getRoundFactoryInstance(connector).wrapSyncProxy(ServerService.class);

		connector.connect(new InetSocketAddress("192.168.1.2", 8082));

		int period = 10000;
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				refreshIp();
			}
		}, period, period, TimeUnit.MILLISECONDS);

		// final ServerService serverService =
		// ServiceProxyFactory.getRoundFactoryInstance(connector).wrapAsyncCallbackProxy(
		// ServerService.class, testAsyncCallback);
	}

	protected static void refreshIp() {
		String file = "d:/ip.txt";
		FileReader fi = null;
		BufferedReader bs = null;

		try {
			fi = new FileReader(file);
			bs = new BufferedReader(fi);
			String ch = null;

			StringBuilder sb = new StringBuilder();
			while ((ch = bs.readLine()) != null) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(ch);
			}
			
			// refresh iplist
			SocketAddress[] ipArray = AddressUtil.parseAddress(sb.toString());
			System.out.println("refresh ip list:" + sb.toString());
			connector.refreshIPList(ipArray);
		} catch (Exception e) {
			System.err.println(StackTraceUtil.getStackTrace(e));
		} finally {
			try {
				if (fi != null) {
					fi.close();
				}
				if (bs != null) {
					bs.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
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
