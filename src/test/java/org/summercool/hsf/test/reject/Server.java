package org.summercool.hsf.test.reject;

import org.summercool.hsf.netty.service.HsfAcceptor;
import org.summercool.hsf.netty.service.HsfAcceptorImpl;
import org.summercool.hsf.test.service.TestServiceImpl;
import org.summercool.hsf.util.HsfOptions;


/**
 * @Title: Server.java
 * @Description: TODO(添加描述)
 * @date 2012-2-23 上午12:58:53
 * @version V1.0
 */
public class Server {
	public static void main(String[] args) {
		HsfAcceptor acceptor = new HsfAcceptorImpl();
		// 设置线程池队列大小为1
		acceptor.setOption(HsfOptions.EVENT_EXECUTOR_QUEUE_CAPACITY, 1);
		acceptor.registerService(new TestServiceImpl());
		
		acceptor.bind(8082);
	}
}
