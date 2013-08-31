package org.summercool.hsf.exception;

/**
 * @Title: HsfRemoteServiceException.java
 * @Package org.summercool.hsf.exception
 * @Description: Hsf远程调用异常
 * @author 简道
 * @date 2011-9-16 下午12:12:47
 * @version V1.0
 */
public class HsfRemoteServiceException extends RuntimeException {
	private static final long serialVersionUID = 5822623760553747361L;
	private static final int MSG_LENGTH = 4096;

	public HsfRemoteServiceException() {
		super();
	}

	public HsfRemoteServiceException(String message) {
		super(message != null && message.length() > MSG_LENGTH ? message.substring(0, MSG_LENGTH - 3) + "..." : message);
	}

	public HsfRemoteServiceException(String message, Throwable cause) {
		super(message != null && message.length() > MSG_LENGTH ? message.substring(0, MSG_LENGTH - 3) + "..." : message, cause);
	}

	public HsfRemoteServiceException(Throwable cause) {
		super(cause);
	}
}
