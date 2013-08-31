package org.summercool.hsf.test.configserver;

import org.summercool.hsf.configserver.ConfigProvider;
import org.summercool.hsf.configserver.ConfigProviderImpl;
import org.summercool.hsf.configserver.pojo.ConfigServiceInfo;
import org.summercool.hsf.configserver.pojo.ConfigServiceItemInfo;
import org.summercool.hsf.netty.service.HsfAcceptor;
import org.summercool.hsf.netty.service.HsfAcceptorImpl;
import org.summercool.hsf.test.service.TestServiceImpl;
import org.summercool.hsf.util.AddressUtil;
import org.summercool.hsf.util.HsfOptions;

/**
 * 配置服务提供者
 * 
 * @Title: CSProvider.java
 * @Package org.summercool.hsf.test.configserver
 * @date 2012-3-22 下午8:38:58
 * @version V1.0
 */
public class CSProvider {
	public static void main(String[] args) {
		HsfAcceptor acceptor = new HsfAcceptorImpl();
		acceptor.registerService(new TestServiceImpl());
		acceptor.bind(8090);
		//
		ConfigProvider configProvider = new ConfigProviderImpl();
		configProvider.setOption(HsfOptions.SYNC_INVOKE_TIMEOUT, 600000);
		configProvider.setOption(HsfOptions.HANDSHAKE_TIMEOUT, 600000);
		//
		ConfigServiceInfo configServiceInfo = new ConfigServiceInfo();
		configServiceInfo.addItem(new ConfigServiceItemInfo("TestService", "127.0.0.1:8090"));
		configProvider.setConfigService(configServiceInfo);
		//
		configProvider.connect(AddressUtil.parseAddress("127.0.0.1:8082"));
	}
}
