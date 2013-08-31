package org.summercool.hsf.configserver.service.impl;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.summercool.hsf.configserver.ConfigManager;
import org.summercool.hsf.configserver.pojo.ConfigServiceInfo;
import org.summercool.hsf.configserver.pojo.ConfigServiceItemInfo;
import org.summercool.hsf.configserver.service.ConfigChangeNotifiedService;
import org.summercool.hsf.configserver.service.ConfigConsumerService;
import org.summercool.hsf.netty.service.HsfService;
import org.summercool.hsf.proxy.ServiceProxyFactory;
import org.summercool.hsf.util.HsfContextHolder;
import org.summercool.hsf.util.StackTraceUtil;

/**
 * @Title: ConfigConsumerServiceImpl.java
 * @Package org.summercool.hsf.configserver.service.impl
 * @Description: 配置服务消费者实现类
 * @author 简道
 * @date 2012-3-22 下午2:40:23
 * @version V1.0
 */
public class ConfigConsumerServiceImpl extends ConfigServiceImpl implements ConfigConsumerService {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public Set<String> subscribe(String... serviceArray) {
		//
		ConfigManager configManager = ConfigManager.getConfigManager();
		String groupName = HsfContextHolder.getRemoteGroupName();
		//
		logger.warn("{} subscribe service;{}", groupName, serviceArray);
		configManager.subscribe(groupName, serviceArray);
		//
		Set<String> subscribeSet = configManager.getSubscribeServices(groupName);
		// 通知
		notifyChanged(groupName);

		return subscribeSet;
	}

	@Override
	public Set<String> unsubscribe(String... serviceArray) {
		//
		ConfigManager configManager = ConfigManager.getConfigManager();
		String groupName = HsfContextHolder.getRemoteGroupName();
		//
		logger.warn("{} unsubscribe service;{}", groupName, serviceArray);
		configManager.unsubscribe(groupName, serviceArray);
		//
		Set<String> subscribeSet = configManager.getSubscribeServices(groupName);
		// 通知
		notifyChanged(groupName);
		//
		return subscribeSet;
	}

	private void notifyChanged(String groupName) {
		HsfService hsfService = HsfContextHolder.getHsfService();
		ConfigManager configManager = ConfigManager.getConfigManager();
		//
		ConfigServiceInfo configServiceInfo = new ConfigServiceInfo();
		Set<String> subscribeSet = configManager.getSubscribeServices(groupName);
		//
		if (subscribeSet != null) {
			for (String service : subscribeSet) {
				HashSet<ConfigServiceItemInfo> serviceSet = configManager.getConfigMap().get(service);
				if (serviceSet != null) {
					configServiceInfo.addItems(serviceSet);
				}
			}
		}
		//
		ConfigChangeNotifiedService proxyService = ServiceProxyFactory.getRoundFactoryInstance(hsfService)
				.wrapAsyncProxy(ConfigChangeNotifiedService.class, groupName);
		//
		try {
			proxyService.configChanged(configServiceInfo);
			//
			logger.info("notify group {} service;{}", groupName, configServiceInfo);
		} catch (Exception e) {
			logger.error("notify failed group {} service;{}, error:{}", new Object[] { groupName, configServiceInfo,
					StackTraceUtil.getStackTrace(e) });
		}
	}
}
