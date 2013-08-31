package org.summercool.hsf.pojo;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @Title: RemoteServiceObject.java
 * @Package com.jiandao.hsf.pojo
 * @Description: 远程服务调用承载实体
 * @author 简道
 * @date 2011-9-13 下午3:33:13
 * @version V1.0
 */
public class RemoteServiceObject implements Serializable {
	private static final long serialVersionUID = 7767339732445044550L;

	/**
	 * @Fields serviceName : 服务名称
	 */
	private String serviceName;

	/**
	 * @Fields methodName : 方法名称
	 */
	private String methodName;

	/**
	 * @Fields args : 方法参数
	 */
	private Object[] args;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	/*
	 * (非 Javadoc)
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RemoteServiceObject [serviceName=");
		builder.append(serviceName);
		builder.append(", methodName=");
		builder.append(methodName);
		builder.append(", args=");
		builder.append(Arrays.toString(args));
		builder.append("]");
		return builder.toString();
	}

}
