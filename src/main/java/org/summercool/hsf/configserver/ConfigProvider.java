package org.summercool.hsf.configserver;

import org.summercool.hsf.configserver.pojo.ConfigServiceInfo;
import org.summercool.hsf.netty.service.HsfConnector;

/**
 * @Title: ConfigProvider.java
 * @Package org.summercool.hsf.configserver
 * @Description: 配置服务提供者
 * @author 简道
 * @date 2012-3-22 下午8:42:44
 * @version V1.0
 */
public interface ConfigProvider extends HsfConnector {
	public void setConfigService(ConfigServiceInfo configServiceInfo);

	public ConfigServiceInfo getConfigService();
	
	public void register(ConfigServiceInfo configServiceInfo);

	public void remove(ConfigServiceInfo configServiceInfo);
	
}
