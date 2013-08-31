package org.summercool.hsf.test.handshake;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.summercool.hsf.future.ChannelGroupFuture;
import org.summercool.hsf.netty.handshake.ConnectorHandshakeProcessor;
import org.summercool.hsf.netty.service.HsfConnector;
import org.summercool.hsf.netty.service.HsfConnectorImpl;
import org.summercool.hsf.pojo.HandshakeAck;
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
		connector.setOption(HsfOptions.CHANNEL_NUM_PER_GROUP, 32);
		connector.setHandshakeProcessor(new ConnectorHandshakeProcessorImpl());
		ChannelGroupFuture groupFuture = connector.connect(new InetSocketAddress("192.168.1.26", 8082));

	}

	public static class ConnectorHandshakeProcessorImpl implements ConnectorHandshakeProcessor {

		@Override
		public Object getRequestAttachment() {
			return "handshake request";
		}

		@Override
		public Object getFinishAttachment() {
			return "handshake finish";
		}

		@Override
		public Map<String, Object> getInitAttributes() {
			return null;
		}

		@Override
		public void process(HandshakeAck handshakeAck) {
			System.out.println("process handshake ack " + handshakeAck);
		}

	}
}
