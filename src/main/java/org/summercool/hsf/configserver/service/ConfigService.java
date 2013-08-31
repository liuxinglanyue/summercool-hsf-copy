package org.summercool.hsf.configserver.service;

import java.util.Set;

import org.summercool.hsf.annotation.RemoteServiceContract;
import org.summercool.hsf.configserver.pojo.ConfigServiceInfo;
import org.summercool.hsf.configserver.pojo.ConfigServiceItemInfo;

/**
 * @Title: ConfigService.java
 * @Package org.summercool.hsf.configserver.service
 * @Description: 配置服务接口
 * @author 简道
 * @date 2012-3-22 上午11:54:45
 * @version V1.0
 */
@RemoteServiceContract
public interface ConfigService {
	public ConfigServiceInfo getConfigService();

	public Set<ConfigServiceItemInfo> getConfigService(String serviceName);
}
