package org.summercool.hsf.test.callback;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.service.HsfConnector;
import org.summercool.hsf.netty.service.HsfConnectorImpl;
import org.summercool.hsf.proxy.ServiceProxyFactory;
import org.summercool.hsf.test.service.TestService;
import org.summercool.hsf.util.AsyncCallback;
import org.summercool.hsf.util.CallbackRegister;
import org.summercool.hsf.util.HsfOptions;
import org.summercool.hsf.util.StackTraceUtil;

public class Client {
	private static TestAsyncCallback testAsyncCallback = new TestAsyncCallback();

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		HsfConnector connector = new HsfConnectorImpl();
		// 打开异步Callback调用方式的消息参数保存开关(默认关闭)，打开后，在Callback的doCallback回调方法中可以获取到发送的消息内容
		connector.setOption(HsfOptions.HOLD_CALLBACK_PARAM, true);
		//
		connector.connect(new InetSocketAddress("192.168.1.52", 8082));

		// 两种使用方式
		test1(connector);
		//
		test2(connector);
	}

	private static void test1(HsfConnector connector) {
		final TestService testService = ServiceProxyFactory.getRoundFactoryInstance(connector).wrapAsyncCallbackProxy(
				TestService.class, testAsyncCallback);

		for (int i = 0; i < 10; i++) {
			try {
				//
				testService.test("Hello world");
				// 注意，以上两个行为必须保证在同一个Thread中被执行，因为注册数据是通过ThreadLocal实现的
			} catch (Exception e) {
				System.err.println(StackTraceUtil.getStackTrace(e));
			}
		}
	}

	private static void test2(HsfConnector connector) {
		for (int i = 0; i < 10; i++) {
			final int num = i;
			try {
				final TestService testService = ServiceProxyFactory.getRoundFactoryInstance(connector)
						.wrapAsyncCallbackProxy(TestService.class, new AsyncCallback<String>() {
							@Override
							public void doCallback(String data) {
								System.out.println("num:" + num + " return " + data);
							}
						});
				//
				testService.test("Hello world");
				// 注意，以上两个行为必须保证在同一个Thread中被执行，因为注册数据是通过ThreadLocal实现的
			} catch (Exception e) {
				System.err.println(StackTraceUtil.getStackTrace(e));
			}
		}
	}

	public static class TestAsyncCallback extends AsyncCallback<Object> {
		public void doCallback(Object data) {
			System.out.println("received " + data + " sent msg:" + CallbackRegister.getCallbackParam());
		}

		@Override
		public void doExceptionCaught(Throwable ex, HsfChannel channel, Object param) {
			System.out.println(param);
			//
			super.doExceptionCaught(ex, channel, param);
		}
	}
}
