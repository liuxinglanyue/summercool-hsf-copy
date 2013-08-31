package org.summercool.hsf.test.reconnect;

import org.summercool.hsf.netty.service.HsfAcceptor;
import org.summercool.hsf.netty.service.HsfAcceptorImpl;

/**
 * @Title: Server.java
 * @Description: TODO(添加描述)
 * @date 2012-2-23 上午12:58:53
 * @version V1.0
 */
public class Server {
	public static void main(String[] args) {
		final HsfAcceptor acceptor = new HsfAcceptorImpl();
		acceptor.bind(8082);
	}
}
