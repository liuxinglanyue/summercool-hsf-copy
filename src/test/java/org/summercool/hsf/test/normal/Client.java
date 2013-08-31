package org.summercool.hsf.test.normal;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.summercool.hsf.future.ChannelGroupFuture;
import org.summercool.hsf.netty.channel.HsfChannelGroup;
import org.summercool.hsf.netty.service.HsfConnector;
import org.summercool.hsf.netty.service.HsfConnectorImpl;
import org.summercool.hsf.proxy.ServiceProxyFactory;
import org.summercool.hsf.test.listener.TestChannelListener;
import org.summercool.hsf.test.service.TestService;
import org.summercool.hsf.util.HsfOptions;


/**
 * @Title: Client.java
 * @Description: TODO(添加描述)
 * @date 2012-2-23 上午01:01:33
 * @version V1.0
 */
public class Client {
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		HsfConnector connector = new HsfConnectorImpl();
//		connector.setOption(HsfOptions.CHANNEL_NUM_PER_GROUP, 32);
//		connector.getListeners().add(new TestChannelListener());
		connector.setOption(HsfOptions.SYNC_INVOKE_TIMEOUT, 10000);
		connector.setOption(HsfOptions.OPEN_SERVICE_INVOKE_STATISTIC, true);
		ChannelGroupFuture groupFuture = connector.connect(new InetSocketAddress("192.168.10.7",8087));
//		List<HsfChannelGroup> group = groupFuture.getGroupList();
//		
//		System.out.println("running");
//		final TestService testService = ServiceProxyFactory.getRoundFactoryInstance(connector).wrapSyncProxy(
//				TestService.class);
//		System.out.println(testService.test("很长一段时间以来，RIM的表现的确难以让投资者、分析师甚至是自己的员工为之兴奋，而其中的一个关键因素就是该公司无力让自己的PlayBook平板电脑在市场上站稳脚跟。事实上，平板电脑近来迅猛发展，未来几年还将得到爆炸式的发展。尽管目前有消息称，RIM将在今年推出新版PlayBook平板电脑，但该产品成功的可能性仍然是微乎其微，甚至仍将继续失败，以下就是其中的10大原因。1、PlayBook第一版留下阴影要想让新版产品取得成功，那么该产品的前任也必须相当成功，但令RIM感到非常不幸的是，该公司的BlackBerry PlayBook平板电脑却一直难以畅销。那些已经购买此设备的用户并没有给予该产品高度品价。鉴于这一因素，RIM要想让新版BlackBerry PlayBook取得成功，当然是非常困难。很长一段时间以来，RIM的表现的确难以让投资者、分析师甚至是自己的员工为之兴奋，而其中的一个关键因素就是该公司无力让自己的PlayBook平板电脑在市场上站稳脚跟。事实上，平板电脑近来迅猛发展，未来几年还将得到爆炸式的发展。尽管目前有消息称，RIM将在今年推出新版PlayBook平板电脑，但该产品成功的可能性仍然是微乎其微，甚至仍将继续失败，以下就是其中的10大原因。1、PlayBook第一版留下阴影要想让新版产品取得成功，那么该产品的前任也必须相当成功，但令RIM感到非常不幸的是，该公司的BlackBerry PlayBook平板电脑却一直难以畅销。那些已经购买此设备的用户并没有给予该产品高度品价。鉴于这一因素，RIM要想让新版BlackBerry PlayBook取得成功，当然是非常困难。"));
//
//		ExecutorService executorService = Executors.newFixedThreadPool(100);
//		long begin = System.currentTimeMillis();
//		for (int i = 0; i < 0; i++) {
//			executorService.execute(new Runnable() {
//
//				@Override
//				public void run() {
//					testService.test("大家都有过复制一个大文件时，久久等待却不见结束，明明很着急却不能取消的情况吧——一旦取消，一切都要从头开始！");
//				}
//			});
//		}
//		executorService.shutdown();
//		executorService.awaitTermination(10, TimeUnit.MINUTES);
//		
//		long end = System.currentTimeMillis();
//
//		System.out.println("coust " + (end - begin));
//
//		System.out.println(group.get(0).getServiceStatistic());
	}

}
