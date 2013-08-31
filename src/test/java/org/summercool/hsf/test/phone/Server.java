package org.summercool.hsf.test.phone;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.handler.downstream.SerializeDownstreamHandler;
import org.summercool.hsf.netty.handler.upstream.DeserializeUpstreamHandler;
import org.summercool.hsf.netty.listener.EventBehavior;
import org.summercool.hsf.netty.listener.MessageEventListener;
import org.summercool.hsf.netty.service.HsfAcceptor;
import org.summercool.hsf.netty.service.HsfAcceptorImpl;
import org.summercool.hsf.serializer.Serializer;
import org.summercool.hsf.util.HandshakeUtil;
import org.summercool.hsf.util.HsfOptions;

/**
 * @Title: Server.java
 * @Package org.summercool.hsf.test.phone
 * @date 2012-3-19 下午10:48:42
 * @version V1.0
 */
public class Server {
	public static void main(String[] args) {
		HsfAcceptor acceptor = new HsfAcceptorImpl();
		acceptor.getListeners().add(new ServerMsgListener());
		acceptor.setOption(HsfOptions.HANDSHAKE_TIMEOUT, 6000000);
		acceptor.setOption(HsfOptions.READ_IDLE_TIME, 6000000);
		// acceptor.setOption(HsfOptions.WRITE_IDLE_TIME, 6000000);
		// clear
		acceptor.getHandlers().clear();
		//
		Serializer serializer = new CustomSerializer();

		acceptor.getHandlers().put("encoder", new CustomHsfEncoder(serializer));
		acceptor.getHandlers().put("decode", new CustomHsfDecoder(serializer));

		acceptor.bind(8088);
	}

	private static class ServerMsgListener implements MessageEventListener {

		private PhoneService service = new PhoneServiceImpl();

		@Override
		public EventBehavior messageReceived(ChannelHandlerContext ctx, HsfChannel channel, MessageEvent e) {
			if (!HandshakeUtil.isInitMsg(e.getMessage())) {
				byte[] msg = (byte[]) e.getMessage();
				byte[] retValue = service.doExecute(msg);
				//
				channel.write(retValue);
			}

			return EventBehavior.Continue;
		}

	}
}
