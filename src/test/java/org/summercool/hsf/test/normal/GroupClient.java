package org.summercool.hsf.test.normal;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.summercool.hsf.future.ChannelGroupFuture;
import org.summercool.hsf.netty.service.HsfConnector;
import org.summercool.hsf.netty.service.HsfConnectorImpl;
import org.summercool.hsf.util.HsfOptions;

/**
 * @Title: GroupClient.java
 * @Package org.summercool.hsf.test.normal
 * @Description: TODO(添加描述)
 * @author Administrator
 * @date 2012-3-23 下午6:56:47
 * @version V1.0
 */
public class GroupClient {
	public static void main(String[] args) {
		for (int i = 0; i < 2; i++) {
//			HsfConnector connector = new HsfConnectorImpl(Executors.newCachedThreadPool(),
//					Executors.newFixedThreadPool(2), 2);
			HsfConnector connector = new HsfConnectorImpl();
			connector.setOption(HsfOptions.CHANNEL_NUM_PER_GROUP, 3);
			connector.setOption(HsfOptions.SYNC_INVOKE_TIMEOUT, 10000);
			connector.setOption(HsfOptions.OPEN_SERVICE_INVOKE_STATISTIC, true);
			ChannelGroupFuture groupFuture = connector.connect(new InetSocketAddress("192.168.1.26", 8082));
		}
	}
}
