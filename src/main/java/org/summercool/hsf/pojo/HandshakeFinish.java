package org.summercool.hsf.pojo;

import java.io.Serializable;

/**
 * @Title: HandshakeFinish.java
 * @Package org.summercool.hsf.pojo
 * @Description: 握手完成消息
 * @author 简道
 * @date Nov 16, 2011 12:21:18 AM
 * @version V1.0
 */
public class HandshakeFinish implements Serializable {
	private static final long serialVersionUID = -7658137534030389521L;

	public HandshakeFinish() {

	}

	public HandshakeFinish(String groupName) {
		this.groupName = groupName;
	}

	private String groupName;
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

	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * @param groupName
	 *        the groupName to set
	 */
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
		builder.append("HandshakeFinish [groupName=");
		builder.append(groupName);
		builder.append(", attachment=");
		builder.append(attachment);
		builder.append("]");
		return builder.toString();
	}

}
