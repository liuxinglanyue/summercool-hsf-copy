package org.summercool.hsf.test.serviceargs;

import org.summercool.hsf.netty.service.HsfAcceptor;
import org.summercool.hsf.netty.service.HsfAcceptorImpl;

/**
 * @Title: Server.java
 * @Package org.summercool.hsf.test.phone
 * @date 2012-3-19 下午10:48:42
 * @version V1.0
 */
public class Server {
	public static void main(String[] args) {
		HsfAcceptor acceptor = new HsfAcceptorImpl();
		acceptor.registerService(new ArgServiceImpl());

		acceptor.bind(8088);
	}

}
