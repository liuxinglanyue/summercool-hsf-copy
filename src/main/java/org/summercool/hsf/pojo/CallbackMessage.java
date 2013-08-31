package org.summercool.hsf.pojo;

/**
 * @ClassName: CallbackMessage
 * @Description: 如果发送的消息实现该接口，callback方式发送异常时，getMessage()返回值将作为callback.doException的参数传入
 * @author 简道
 * @date 2012-3-6 下午3:26:33
 * 
 */
public interface CallbackMessage<T> {
	public T getMessage();
}