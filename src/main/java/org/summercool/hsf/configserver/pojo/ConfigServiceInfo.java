package org.summercool.hsf.configserver.pojo;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @Title: ConfigServiceInfo.java
 * @Package org.summercool.hsf.configserver.pojo
 * @Description: 配置服务信息
 * @author 简道
 * @date 2012-3-22 上午11:59:32
 * @version V1.0
 */
public class ConfigServiceInfo implements Serializable {
	private static final long serialVersionUID = -7624787378765371424L;

	private Map<String, HashSet<ConfigServiceItemInfo>> itemsMap = new HashMap<String, HashSet<ConfigServiceItemInfo>>();

	public Map<String, HashSet<ConfigServiceItemInfo>> getItems() {
		return itemsMap;
	}

	public void setItems(Collection<ConfigServiceItemInfo> items) {
		itemsMap.clear();
		addItems(items);
	}

	public void addItem(ConfigServiceItemInfo item) {
		if (item == null) {
			return;
		}
		HashSet<ConfigServiceItemInfo> list = itemsMap.get(item.getServiceName());
		if (list == null) {
			list = new HashSet<ConfigServiceItemInfo>();
			itemsMap.put(item.getServiceName(), list);
		}
		list.add(item);
	}

	public void removeItem(ConfigServiceItemInfo item) {
		if (item == null) {
			return;
		}
		HashSet<ConfigServiceItemInfo> list = itemsMap.get(item.getServiceName());
		if (list != null) {
			list.remove(item);
			if (list.size() == 0) {
				itemsMap.remove(item.getServiceName());
			}
		}
	}

	public void addItems(Collection<ConfigServiceItemInfo> items) {
		if (items == null) {
			return;
		}
		for (ConfigServiceItemInfo item : items) {
			addItem(item);
		}
	}

	public void removeItems(Collection<ConfigServiceItemInfo> items) {
		if (items == null) {
			return;
		}
		for (ConfigServiceItemInfo item : items) {
			removeItem(item);
		}
	}

	public void setItems(Map<String, HashSet<ConfigServiceItemInfo>> itemsMap) {
		this.itemsMap = itemsMap;
	}
}
