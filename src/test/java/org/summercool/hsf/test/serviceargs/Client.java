package org.summercool.hsf.test.serviceargs;

import java.util.ArrayList;

import org.summercool.hsf.netty.service.HsfConnector;
import org.summercool.hsf.netty.service.HsfConnectorImpl;
import org.summercool.hsf.proxy.ServiceProxyFactory;
import org.summercool.hsf.util.AddressUtil;

/**
 * @Description: TODO
 * @author Kolor
 * @date 2012-5-8 下午6:10:55
 */
public class Client {
	public static void main(String[] args) {
		HsfConnector connector = new HsfConnectorImpl();
		connector.connect(AddressUtil.parseAddress("127.0.0.1:8088"));

		ArgService service = ServiceProxyFactory.getRoundFactoryInstance(connector).wrapSyncProxy(ArgService.class);
		System.out.println(service.testArgs(new ArrayList<String>(), new ArrayList<String>(), null, null, true));
	}
}
