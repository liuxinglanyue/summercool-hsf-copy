package org.summercool.hsf.test.normal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.summercool.hsf.future.ChannelGroupFuture;
import org.summercool.hsf.netty.channel.HsfChannelGroup;
import org.summercool.hsf.netty.service.HsfConnector;
import org.summercool.hsf.netty.service.HsfConnectorImpl;
import org.summercool.hsf.netty.service.HsfService;
import org.summercool.hsf.proxy.ServiceProxyFactory;
import org.summercool.hsf.util.HsfOptions;

//import com.gexin.demo.ehcache.State;

/**
 * @Title: EhcacheClient.java
 * @Package org.summercool.hsf.test.normal
 * @Description: TODO(添加描述)
 * @author Administrator
 * @date 2012-3-9 下午7:45:44
 * @version V1.0
 */
public class EhcacheClient {
	private static Logger logger = LoggerFactory.getLogger(EhcacheClient.class);
	private static final Random rand = new Random();

	public static void main(String[] args) throws InterruptedException, IOException {
//		HsfConnector connector = new HsfConnectorImpl();
//		connector.setOption(HsfOptions.OPEN_SERVICE_INVOKE_STATISTIC, true);
//		connector.setOption(HsfOptions.MAX_THREAD_NUM_OF_DISPATCHER, 150);
//		ChannelGroupFuture groupFuture = connector.connect(new InetSocketAddress("192.168.11.4", 8082));
//
//		final EhcacheService ehcacheService = ServiceProxyFactory.getRoundFactoryInstance(connector).wrapSyncProxy(
//				EhcacheService.class);
//
//		ExecutorService executorService = Executors.newFixedThreadPool(100);
//		long begin = System.currentTimeMillis();
//		final AtomicLong num = new AtomicLong();
//
//		startMonitor(connector);
//
//		final State state = new State();
//		state.setFlag(String.valueOf(rand.nextInt()));
//		state.setPlatform(String.valueOf(rand.nextInt()));
//		state.setState(rand.nextInt());
//		state.setTimestamp(Calendar.getInstance().getTime());
//		state.setUuid(UUID.randomUUID().toString());
//
//		for (int i = 4000000; i < 8000000; i++) {
//			final int j = i;
//			executorService.execute(new Runnable() {
//
//				@Override
//				public void run() {
//					// //
//					ehcacheService.setState(String.valueOf(j), state);
//				}
//			});
//		}
//		executorService.shutdown();
//		executorService.awaitTermination(100, TimeUnit.MINUTES);
//
//		long end = System.currentTimeMillis();
//
//		System.out.println("coust " + (end - begin) + " num:" + num.get());
	}

	private static void startMonitor(final HsfService service) {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				long totalSent = 0L;
				long totalRev = 0L;
				for (Entry<String, HsfChannelGroup> entry : service.getGroups().entrySet()) {
					totalSent += entry.getValue().getMsgStatistic().getSentNum().get();
					totalRev += entry.getValue().getMsgStatistic().getReceivedNum().get();
				}
				logger.warn("total sent num:{}/total reveiced num:{}", totalSent, totalRev);
			}
		}, 3000L, 5000L, TimeUnit.MILLISECONDS);
	}
}
