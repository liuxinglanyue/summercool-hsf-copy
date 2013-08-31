package org.summercool.hsf.statistic;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @Title: ServiceStatisticInfo.java
 * @Package org.summercool.hsf.statistic
 * @Description: Method统计信息类
 * @author 简道
 * @date 2012-3-15 上午11:19:53
 * @version V1.0
 */
public class MethodStatisticInfo implements Serializable {
	private static final long serialVersionUID = 4216461144498413593L;
	private static final String METHOD_STATISTIC_INVOKED = "METHOD_STATISTIC_INVOKED_";
	private static final String METHOD_STATISTIC_INITIATIVE_INVOKED = "METHOD_STATISTIC_INITIATIVE_INVOKED_";

	private String methodName;
	private AtomicLong totalInvokedNum = new AtomicLong();
	private AtomicLong totalInitiativeInvokeNum = new AtomicLong();

	public MethodStatisticInfo() {

	}

	public MethodStatisticInfo(String methodName) {
		this.methodName = methodName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public long incrementInvokedNum() {
		NumberStatisticUtil.incrementAndGet(METHOD_STATISTIC_INVOKED + methodName);
		return totalInvokedNum.incrementAndGet();
	}

	public long incrementInitiativeInvokeNum() {
		NumberStatisticUtil.incrementAndGet(METHOD_STATISTIC_INITIATIVE_INVOKED + methodName);
		return totalInitiativeInvokeNum.incrementAndGet();
	}

	/**
	 * @Title: getTotalInvokedNum
	 * @Description: 获取被调用统计值总量
	 * @author 简道
	 * @return long    返回类型
	 */
	public long getTotalInvokedNum() {
		return totalInvokedNum.get();
	}

	/**
	 * @Title: getIncrementInvokedNum
	 * @Description: 获取上一分钟的增量调用统计值
	 * @author 简道
	 * @return long    返回类型
	 */
	public long getIncrementInvokedNum() {
		return NumberStatisticUtil.getPreValue(METHOD_STATISTIC_INVOKED + methodName);
	}

	/**
	 * @Title: getTotalInitiativeInvokeNum
	 * @Description: 获取主动调用统计值总量
	 * @author 简道
	 * @return long    返回类型
	 */
	public long getTotalInitiativeInvokeNum() {
		return totalInitiativeInvokeNum.get();
	}

	/**
	 * @Title: getIncrementInitiativeInvokeNum
	 * @Description: 获取上一分钟的增量主动调用统计值
	 * @author 简道
	 * @return long    返回类型
	 */
	public long getIncrementInitiativeInvokeNum() {
		return NumberStatisticUtil.getPreValue(METHOD_STATISTIC_INITIATIVE_INVOKED + methodName);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("method:").append(methodName).append("{TotalInvoked:").append(getTotalInvokedNum())
				.append(", TotalInitiativeInvoke:").append(getTotalInitiativeInvokeNum()).append(", IncrementInvoked:")
				.append(getIncrementInvokedNum()).append(", IncrementInitiativeInvoke:")
				.append(getIncrementInitiativeInvokeNum()).append("}");
		return sb.toString();
	}
}
