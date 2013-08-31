package org.summercool.hsf.statistic;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Title: ServiceStatisticItem.java
 * @Package org.summercool.hsf.statistic
 * @Description: Service统计类
 * @author 简道
 * @date 2012-3-15 上午11:23:54
 * @version V1.0
 */
public class ServiceStatisticItem implements Serializable {
	private static final long serialVersionUID = -5157965242571945119L;

	private String serviceName;
	private ConcurrentHashMap<String, MethodStatisticInfo> methodStatisticMap = new ConcurrentHashMap<String, MethodStatisticInfo>();

	public ServiceStatisticItem() {
	}

	public ServiceStatisticItem(String serviceName) {
		this.serviceName = serviceName;
	}

	public long increaseMethodInvokedNum(String methodName) {
		MethodStatisticInfo mdStatisticInfo = getMethodStatisticInfo(methodName);
		//
		return mdStatisticInfo.incrementInvokedNum();
	}

	public long increaseMethodInitiativeInvokeNum(String methodName) {
		MethodStatisticInfo mdStatisticInfo = getMethodStatisticInfo(methodName);
		//
		return mdStatisticInfo.incrementInitiativeInvokeNum();
	}

	private MethodStatisticInfo getMethodStatisticInfo(String methodName) {
		MethodStatisticInfo mdStatisticInfo = methodStatisticMap.get(methodName);
		if (mdStatisticInfo == null) {
			mdStatisticInfo = new MethodStatisticInfo(methodName);
			MethodStatisticInfo preValue = methodStatisticMap.putIfAbsent(methodName, mdStatisticInfo);
			if (preValue != null) {
				mdStatisticInfo = preValue;
			}
		}
		return mdStatisticInfo;
	}

	public long getTotalInvokedNum() {
		long sum = 0;
		for (Entry<String, MethodStatisticInfo> entry : methodStatisticMap.entrySet()) {
			sum += entry.getValue().getTotalInvokedNum();
		}
		return sum;
	}

	public long getIncrementInvokedNum() {
		long sum = 0;
		for (Entry<String, MethodStatisticInfo> entry : methodStatisticMap.entrySet()) {
			sum += entry.getValue().getIncrementInvokedNum();
		}
		return sum;
	}

	public long getTotalInitiativeInvokeNum() {
		long sum = 0;
		for (Entry<String, MethodStatisticInfo> entry : methodStatisticMap.entrySet()) {
			sum += entry.getValue().getTotalInitiativeInvokeNum();
		}
		return sum;
	}

	public long getIncrementInitiativeInvokeNum() {
		long sum = 0;
		for (Entry<String, MethodStatisticInfo> entry : methodStatisticMap.entrySet()) {
			sum += entry.getValue().getIncrementInitiativeInvokeNum();
		}
		return sum;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("service:").append(serviceName).append("{TotalInvoked:").append(getTotalInvokedNum())
				.append(", TotalInitiativeInvoke:").append(getTotalInitiativeInvokeNum()).append(", IncrementInvoked:")
				.append(getIncrementInvokedNum()).append(", IncrementInitiativeInvoke:")
				.append(getIncrementInitiativeInvokeNum()).append(", methods:").append(methodStatisticMap).append("}");
		return sb.toString();
	}
}
