package org.summercool.hsf.test.normal;

import org.summercool.hsf.netty.service.HsfAcceptor;
import org.summercool.hsf.netty.service.HsfAcceptorImpl;
import org.summercool.hsf.test.listener.TestChannelListener;
import org.summercool.hsf.test.service.TestServiceImpl;


/**
 * @Title: Server.java
 * @Description: TODO(添加描述)
 * @date 2012-2-23 上午12:58:53
 * @version V1.0
 */
public class Server {
	public static void main(String[] args) {
		HsfAcceptor acceptor = new HsfAcceptorImpl();
		acceptor.getListeners().add(new TestChannelListener());
		acceptor.registerService(new TestServiceImpl());
		acceptor.bind(8082);
	}
}
