package org.summercool.hsf.test.callback;

import org.summercool.hsf.netty.service.HsfAcceptor;
import org.summercool.hsf.netty.service.HsfAcceptorImpl;
import org.summercool.hsf.test.listener.TestChannelListener;
import org.summercool.hsf.test.service.TestServiceImpl;

public class Server {
	public static void main(String[] args) {
		HsfAcceptor acceptor = new HsfAcceptorImpl();
		acceptor.getListeners().add(new TestChannelListener());
		acceptor.registerService(new TestServiceImpl());
		acceptor.bind(8082);
	}
}
