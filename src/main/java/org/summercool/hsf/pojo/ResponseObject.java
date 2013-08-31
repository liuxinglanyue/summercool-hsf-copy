package org.summercool.hsf.pojo;

/**
 * @ClassName: ResponseObject
 * @Description: 远程服务调用反馈实体
 * @author 简道
 * @date 2011-9-29 下午1:23:53
 */
public class ResponseObject extends RemoteServiceMessage {
	private static final long serialVersionUID = -5367323290694750103L;

	private Throwable cause;

	private String causeMessage;

	public Throwable getCause() {
		return cause;
	}

	public void setCause(Throwable cause) {
		this.cause = cause;
	}

	public String getCauseMessage() {
		return causeMessage;
	}

	public void setCauseMessage(String causeMessage) {
		this.causeMessage = causeMessage;
	}
}
