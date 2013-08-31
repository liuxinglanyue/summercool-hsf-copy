package org.summercool.hsf.configserver;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.summercool.hsf.configserver.pojo.AddressType;
import org.summercool.hsf.configserver.pojo.ConfigServiceInfo;
import org.summercool.hsf.configserver.pojo.ConfigServiceItemInfo;
import org.summercool.hsf.netty.service.HsfService;
import org.summercool.hsf.util.HsfContextHolder;

/**
 * @Title: ConfigManager.java
 * @Package org.summercool.hsf.configserver
 * @Description: 配置服务管理者
 * @author 简道
 * @date 2012-3-22 下午1:36:04
 * @version V1.0
 */
public class ConfigManager {
	private static Logger logger = LoggerFactory.getLogger(ConfigManager.class);
	private static ConcurrentHashMap<String, ConfigManager> map = new ConcurrentHashMap<String, ConfigManager>();

	private ConcurrentHashMap<String, HashSet<ConfigServiceItemInfo>> configMap = new ConcurrentHashMap<String, HashSet<ConfigServiceItemInfo>>();
	private ConcurrentHashMap<String, HashSet<String>> serviceGroupMap = new ConcurrentHashMap<String, HashSet<String>>();
	private ConcurrentHashMap<String, HashSet<String>> groupServiceMap = new ConcurrentHashMap<String, HashSet<String>>();

	public static ConfigManager getConfigManager() {
		HsfService hsfService = HsfContextHolder.getHsfService();
		if (hsfService == null) {
			throw new IllegalStateException(
					"there is not HsfService bind to current thread. can't get a configManager.");
		}
		//
		ConfigManager configManager = map.get(hsfService.getGroupName());
		if (configManager == null) {
			configManager = new ConfigManager();
			ConfigManager preValue = map.putIfAbsent(hsfService.getGroupName(), configManager);
			if (preValue != null) {
				configManager = preValue;
			}
		}
		return configManager;
	}

	public HashSet<String> getSubscribeGroups(String serviceName) {
		if (serviceName == null) {
			return null;
		}

		return serviceGroupMap.get(serviceName);
	}

	public HashSet<String> getSubscribeServices(String groupName) {
		if (groupName == null) {
			return null;
		}

		return groupServiceMap.get(groupName);
	}

	public void subscribe(String groupName, String... serviceArray) {
		if (groupName == null || serviceArray == null || serviceArray.length == 0) {
			logger.warn("subscribe with illegal arguments(groupName:{}, serviceArray:{})", groupName, serviceArray);
			return;
		}
		for (String serviceName : serviceArray) {
			HashSet<String> groupSet = getSubscribeGroupSet(serviceName);
			groupSet.add(groupName);
			//
			HashSet<String> serviceSet = getSubscribeSet(groupName);
			serviceSet.add(serviceName);
		}
	}

	public void unsubscribe(String groupName, String... serviceArray) {
		if (groupName == null || serviceArray == null || serviceArray.length == 0) {
			logger.warn("unsubscribe with illegal arguments(groupName:{}, serviceArray:{})", groupName, serviceArray);
			return;
		}
		for (String serviceName : serviceArray) {
			HashSet<String> groupSet = serviceGroupMap.get(serviceName);
			if (removeSetItem(groupSet, groupName)) {
				if (groupSet.size() == 0) {
					synchronized (groupSet) {
						if (groupSet.size() == 0) {
							serviceGroupMap.remove(serviceName);
						}
					}
				}
			}
			//
			HashSet<String> serviceSet = getSubscribeSet(groupName);
			serviceSet.remove(serviceName);
		}
	}

	private boolean removeSetItem(Set<String> set, String item) {
		if (set != null) {
			synchronized (set) {
				return set.remove(item);
			}
		}
		return false;
	}

	public void unsubscribe(String groupName) {
		if (groupName == null) {
			logger.warn("unsubscribe with illegal arguments(groupName:{})", groupName);
			return;
		}

		HashSet<String> serviceSet = groupServiceMap.get(groupName);
		if (serviceSet != null && serviceSet.size() > 0) {
			String[] serviceArray = new String[serviceSet.size()];
			serviceSet.toArray(serviceArray);
			unsubscribe(groupName, serviceArray);
		}
	}

	public ConcurrentHashMap<String, HashSet<ConfigServiceItemInfo>> getConfigMap() {
		return configMap;
	}

	public boolean update(ConfigServiceInfo configServiceInfo) {
		if (configServiceInfo == null || configServiceInfo.getItems() == null) {
			return false;
		}
		//
		for (Map.Entry<String, HashSet<ConfigServiceItemInfo>> entry : configServiceInfo.getItems().entrySet()) {
			HashSet<ConfigServiceItemInfo> set = getConfigSet(entry.getKey());
			synchronized (set) {
				String host = HsfContextHolder.getRemoteAddress().getHostName();
				for (ConfigServiceItemInfo configServiceItemInfo : entry.getValue()) {
					//
					if (AddressType.Port.equals(configServiceItemInfo.getAddressType())) {
						configServiceItemInfo.setAddress(host + ":" + configServiceItemInfo.getAddress());
						configServiceItemInfo.setAddressType(AddressType.Address);
					}
					//
					set.add(configServiceItemInfo);
				}
			}
		}

		return true;
	}

	public boolean remove(ConfigServiceInfo configServiceInfo) {
		if (configServiceInfo == null || configServiceInfo.getItems() == null) {
			return false;
		}
		//
		for (Map.Entry<String, HashSet<ConfigServiceItemInfo>> entry : configServiceInfo.getItems().entrySet()) {
			HashSet<ConfigServiceItemInfo> set = getConfigSet(entry.getKey());
			synchronized (set) {
				set.removeAll(entry.getValue());
			}
		}

		return true;
	}

	public void clear() {
		configMap.clear();
	}

	private HashSet<ConfigServiceItemInfo> getConfigSet(String serviceName) {
		HashSet<ConfigServiceItemInfo> configSet = configMap.get(serviceName);
		if (configSet == null) {
			configSet = new HashSet<ConfigServiceItemInfo>();
			HashSet<ConfigServiceItemInfo> preValue = configMap.putIfAbsent(serviceName, configSet);
			if (preValue != null) {
				configSet = preValue;
			}
		}
		return configSet;
	}

	private HashSet<String> getSubscribeGroupSet(String serviceName) {
		HashSet<String> groupSet = serviceGroupMap.get(serviceName);
		if (groupSet == null) {
			groupSet = new HashSet<String>();
			HashSet<String> preValue = serviceGroupMap.putIfAbsent(serviceName, groupSet);
			if (preValue != null) {
				groupSet = preValue;
			}
		}
		return groupSet;
	}

	private HashSet<String> getSubscribeSet(String groupName) {
		HashSet<String> serviceSet = groupServiceMap.get(groupName);
		if (serviceSet == null) {
			serviceSet = new HashSet<String>();
			HashSet<String> preValue = groupServiceMap.putIfAbsent(groupName, serviceSet);
			if (preValue != null) {
				serviceSet = preValue;
			}
		}
		return serviceSet;
	}
}
