package org.summercool.hsf.test;

import java.net.InetSocketAddress;

import org.summercool.hsf.netty.service.HsfConnector;
import org.summercool.hsf.netty.service.HsfConnectorImpl;
import org.summercool.hsf.test.service.TestServiceImpl;

/**
 * @Description: TODO
 * @author Kolor
 * @date 2012-5-14 上午10:15:45
 */
public class MainTest {
	
	public static void main(String[] args) {
		
		HsfConnector connector = new HsfConnectorImpl();
		connector.connect(new InetSocketAddress("192.168.11.3", 8002));
		connector.registerService(new TestServiceImpl());
	}
}
