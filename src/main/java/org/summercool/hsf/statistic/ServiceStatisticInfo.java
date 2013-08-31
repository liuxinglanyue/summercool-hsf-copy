package org.summercool.hsf.statistic;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Title: ServiceStatisticInfo.java
 * @Package org.summercool.hsf.statistic
 * @Description: Service统计信息类
 * @author 简道
 * @date 2012-3-15 上午11:19:53
 * @version V1.0
 */
public class ServiceStatisticInfo implements Serializable {
	private static final long serialVersionUID = 4216461144498413593L;

	private String name;
	private ConcurrentHashMap<String, ServiceStatisticItem> serviceStatisticMap = new ConcurrentHashMap<String, ServiceStatisticItem>();

	public long increaseInvokedNum(String serviceName, String methodName) {
		ServiceStatisticItem serviceStatisticInfo = getServiceStatisticItem(serviceName);
		//
		return serviceStatisticInfo.increaseMethodInvokedNum(methodName);
	}

	public long increaseInitiativeInvokeNum(String serviceName, String methodName) {
		ServiceStatisticItem serviceStatisticInfo = getServiceStatisticItem(serviceName);
		//
		return serviceStatisticInfo.increaseMethodInitiativeInvokeNum(methodName);
	}

	private ServiceStatisticItem getServiceStatisticItem(String serviceName) {
		ServiceStatisticItem serviceStatisticInfo = serviceStatisticMap.get(serviceName);
		if (serviceStatisticInfo == null) {
			serviceStatisticInfo = new ServiceStatisticItem(serviceName);
			ServiceStatisticItem preValue = serviceStatisticMap.putIfAbsent(serviceName, serviceStatisticInfo);
			if (preValue != null) {
				serviceStatisticInfo = preValue;
			}
		}
		return serviceStatisticInfo;
	}

	public long getTotalInvokedNum() {
		long sum = 0;
		for (Entry<String, ServiceStatisticItem> entry : serviceStatisticMap.entrySet()) {
			sum += entry.getValue().getTotalInvokedNum();
		}
		return sum;
	}

	public long getIncrementInvokedNum() {
		long sum = 0;
		for (Entry<String, ServiceStatisticItem> entry : serviceStatisticMap.entrySet()) {
			sum += entry.getValue().getIncrementInvokedNum();
		}
		return sum;
	}

	public long getTotalInitiativeInvokeNum() {
		long sum = 0;
		for (Entry<String, ServiceStatisticItem> entry : serviceStatisticMap.entrySet()) {
			sum += entry.getValue().getTotalInitiativeInvokeNum();
		}
		return sum;
	}

	public long getIncrementInitiativeInvokeNum() {
		long sum = 0;
		for (Entry<String, ServiceStatisticItem> entry : serviceStatisticMap.entrySet()) {
			sum += entry.getValue().getIncrementInitiativeInvokeNum();
		}
		return sum;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("group:").append(name).append("{TotalInvoked:").append(getTotalInvokedNum())
				.append(", TotalInitiativeInvoke:").append(getTotalInitiativeInvokeNum()).append(", IncrementInvoked:")
				.append(getIncrementInvokedNum()).append(", IncrementInitiativeInvoke:")
				.append(getIncrementInitiativeInvokeNum()).append(", services:").append(serviceStatisticMap)
				.append("}");
		return sb.toString();
	}
}
