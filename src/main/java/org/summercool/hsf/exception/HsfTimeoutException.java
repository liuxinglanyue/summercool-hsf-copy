package org.summercool.hsf.exception;

/**
 * @Title: HsfTimeoutException.java
 * @Package org.summercool.hsf.exception
 * @Description: Hsf调用超时异常
 * @author 简道
 * @date 2011-9-16 下午12:12:47
 * @version V1.0
 */
public class HsfTimeoutException extends HsfRuntimeException{
	private static final long serialVersionUID = 5822623760553747361L;

	public HsfTimeoutException() {
		super();
	}

	public HsfTimeoutException(String message) {
		super(message);
	}

	public HsfTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public HsfTimeoutException(Throwable cause) {
		super(cause);
	}
}
