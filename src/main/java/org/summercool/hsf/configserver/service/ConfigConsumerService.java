package org.summercool.hsf.configserver.service;

import java.util.Set;

import org.summercool.hsf.annotation.RemoteServiceContract;

/**
 * @Title: ConfigConsumerService.java
 * @Package org.summercool.hsf.configserver.service
 * @Description: 配置服务消费者接口
 * @author 简道
 * @date 2012-3-22 上午11:54:45
 * @version V1.0
 */
@RemoteServiceContract
public interface ConfigConsumerService extends ConfigService {
	/**
	 * @Title: subscribe
	 * @Description: 订阅服务
	 * @author 简道
	 * @param serviceArray
	 *        服务列表
	 * @return Set<String> 返回已经订阅的列表
	 */
	public Set<String> subscribe(String... serviceArray);

	/**
	 * @Title: unsubscribe
	 * @Description: 解除订阅服务
	 * @author 简道
	 * @param serviceArray
	 *        服务列表
	 * @return Set<String> 返回已经订阅的列表
	 */
	public Set<String> unsubscribe(String... serviceArray);
}
