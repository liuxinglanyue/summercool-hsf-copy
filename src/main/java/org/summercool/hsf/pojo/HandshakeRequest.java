package org.summercool.hsf.pojo;

import java.io.Serializable;

/**
 * @Title: HandshakeRequest.java
 * @Package org.summercool.hsf.pojo
 * @Description: 握手请求消息
 * @author 简道
 * @date 2011-11-22 上午3:15:04
 * @version V1.0
 */
public class HandshakeRequest implements Serializable {
	private static final long serialVersionUID = -6955284581415918936L;

	public HandshakeRequest() {

	}

	public HandshakeRequest(String groupName) {
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
		builder.append("HandshakeRequest [groupName=");
		builder.append(groupName);
		builder.append(", attachment=");
		builder.append(attachment);
		builder.append("]");
		return builder.toString();
	}

}
