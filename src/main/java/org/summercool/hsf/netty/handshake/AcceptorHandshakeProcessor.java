package org.summercool.hsf.netty.handshake;

import java.util.Map;

import org.summercool.hsf.pojo.HandshakeFinish;
import org.summercool.hsf.pojo.HandshakeRequest;

/**
 * @Title: HandshakeProcessor.java
 * @Package org.summercool.hsf.netty.handshake
 * @Description: 服务端握手处理
 * @author 简道
 * @date 2011-11-24 下午2:18:15
 * @version V1.0
 */
public interface AcceptorHandshakeProcessor {
	/**
	 * @Title: getAckAttachment
	 * @Description: 该对象将被放入握手Ack消息包中
	 * @author 简道
	 * @return Object 返回类型
	 */
	Object getAckAttachment();

	/**
	 * @Title: getInitAttributes
	 * @Description: 该Map将在GroupCreated时，放入Group的attribute中
	 * @author 简道
	 * @return Map<String,Object> 返回类型
	 */
	Map<String, Object> getInitAttributes();

	void process(HandshakeRequest handshakeRequest);

	void process(HandshakeFinish handshakeFinish);
}
