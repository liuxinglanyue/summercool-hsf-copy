package org.summercool.hsf.pojo;

/**
 * @ClassName: RequestObject
 * @Description: 远程服务调用请求实体
 * @author 简道
 * @date 2011-9-29 下午1:23:16
 */
public class RequestObject extends RemoteServiceMessage {
	private static final long serialVersionUID = -931000067649405616L;

	/**
	 * @Fields needCallback : 是否远程回发消息
	 */
	private boolean needCallback = true;

	/**
	 * @return the needCallback
	 */
	public boolean isNeedCallback() {
		return needCallback;
	}

	/**
	 * @param needCallback
	 *        the needCallback to set
	 */
	public void setNeedCallback(boolean needCallback) {
		this.needCallback = needCallback;
	}

}
