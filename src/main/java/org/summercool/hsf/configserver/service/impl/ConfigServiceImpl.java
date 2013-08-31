package org.summercool.hsf.configserver.service.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.summercool.hsf.configserver.ConfigManager;
import org.summercool.hsf.configserver.pojo.ConfigServiceInfo;
import org.summercool.hsf.configserver.pojo.ConfigServiceItemInfo;
import org.summercool.hsf.configserver.service.ConfigService;

/**
 * @Title: ConfigServiceImpl.java
 * @Package org.summercool.hsf.configserver.service.impl
 * @Description: 配置服务
 * @author 简道
 * @date 2012-3-22 下午1:35:13
 * @version V1.0
 */
public class ConfigServiceImpl implements ConfigService {

	@Override
	public ConfigServiceInfo getConfigService() {
		//
		ConfigManager configManager = ConfigManager.getConfigManager();
		ConcurrentHashMap<String, HashSet<ConfigServiceItemInfo>> configMap = configManager.getConfigMap();
		//
		ConfigServiceInfo configServiceInfo = new ConfigServiceInfo();
		configServiceInfo.setItems(configMap);
		return configServiceInfo;
	}

	@Override
	public Set<ConfigServiceItemInfo> getConfigService(String serviceName) {
		//
		ConfigManager configManager = ConfigManager.getConfigManager();
		ConcurrentHashMap<String, HashSet<ConfigServiceItemInfo>> configMap = configManager.getConfigMap();
		//
		return configMap.get(serviceName);
	}

}
