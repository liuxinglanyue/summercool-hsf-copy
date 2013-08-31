package org.summercool.hsf.test.configserver;

import org.summercool.hsf.configserver.ConfigServer;
import org.summercool.hsf.util.HsfOptions;

/**
 * 配置服务器
 * 
 * @Title: CSServer.java
 * @Package org.summercool.hsf.test.configserver
 * @date 2012-3-22 下午8:37:21
 * @version V1.0
 */
public class CSServer {
	public static void main(String[] args) {
		ConfigServer configServer = new ConfigServer();
		configServer.setOption(HsfOptions.SYNC_INVOKE_TIMEOUT, 600000);
		configServer.setOption(HsfOptions.HANDSHAKE_TIMEOUT, 600000);
		configServer.bind(8082);
	}
}
