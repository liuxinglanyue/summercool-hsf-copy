package org.summercool.hsf.pojo;

import java.io.Serializable;

/**
 * @Title: RemoteServiceMessage.java
 * @Package org.summercool.hsf.pojo
 * @Description: 远程通信消息包装类
 * @author 简道
 * @date 2011-9-29 下午1:26:41
 * @version V1.0
 */
public class RemoteServiceMessage implements Serializable {

	private static final long serialVersionUID = -1204495527018523321L;

	private Object clientId;

	private long seq;

	private Object target;

	public Object getClientId() {
		return clientId;
	}

	public void setClientId(Object clientId) {
		this.clientId = clientId;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RemoteServiceMessage [clientId=");
		builder.append(clientId);
		builder.append(", seq=");
		builder.append(seq);
		builder.append(", target=");
		builder.append(target);
		builder.append("]");
		return builder.toString();
	}

}
