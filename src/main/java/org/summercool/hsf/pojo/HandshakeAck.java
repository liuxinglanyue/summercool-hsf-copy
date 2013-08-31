package org.summercool.hsf.pojo;

import java.io.Serializable;

/**
 * @Title: HandshakeAck.java
 * @Package org.summercool.hsf.pojo
 * @Description: 握手Ack消息
 * @author 简道
 * @date 2011-11-22 下午11:11:58
 * @version V1.0
 */
public class HandshakeAck implements Serializable {
	private static final long serialVersionUID = -3005546218059286518L;

	private String groupName;

	public HandshakeAck() {

	}

	public HandshakeAck(String groupName) {
		this.groupName = groupName;
	}

	private Object attachment;

	/**
	 * @return the attachment
	 */
	public Object getAttachment() {
		return attachment;
	}

	/**
	 * @param attachment
	 *        the attachment to set
	 */
	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/*
	 * (非 Javadoc)
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("HandshakeAck [groupName=");
		builder.append(groupName);
		builder.append(", attachment=");
		builder.append(attachment);
		builder.append("]");
		return builder.toString();
	}

}
