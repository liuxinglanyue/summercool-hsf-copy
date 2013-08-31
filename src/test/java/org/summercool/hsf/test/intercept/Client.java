package org.summercool.hsf.test.intercept;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.service.HsfConnector;
import org.summercool.hsf.netty.service.HsfConnectorImpl;
import org.summercool.hsf.proxy.ServiceProxyFactory;
import org.summercool.hsf.test.service.TestService;
import org.summercool.hsf.util.AsyncCallback;

/**
 * @Title: Client.java
 * @Description: TODO(添加描述)
 * @date 2012-2-23 上午01:01:33
 * @version V1.0
 */
public class Client {
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		HsfConnector connector = new HsfConnectorImpl();
		connector.connect(new InetSocketAddress("192.168.1.52", 8082));

		// final TestService testService = ServiceProxyFactory.getRoundFactoryInstance(connector).wrapAsyncProxy(
		// TestService.class);
		final TestService testService = ServiceProxyFactory.getRoundFactoryInstance(connector).wrapAsyncCallbackProxy(
				TestService.class, new AsyncCallback<Object>() {
					public void doCallback(Object data) {
					};
					@Override
					public void doExceptionCaught(Throwable ex, HsfChannel channel, Object param) {
						System.out.println(ex);
						// super.doExceptionCaught(ex, channel, param);
					}
				});

		ExecutorService executorService = Executors.newFixedThreadPool(100);
		long begin = System.currentTimeMillis();
		for (int i = 0; i < 1; i++) {
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
						testService.test("大家都有过复制一个大文件时，久久等待却不见结束，明明很着急却不能取消的情况吧——一旦取消，一切都要从头开始！");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.MINUTES);

		long end = System.currentTimeMillis();

		System.out.println("coust " + (end - begin));
	}

}
