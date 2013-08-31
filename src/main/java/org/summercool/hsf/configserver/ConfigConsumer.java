package org.summercool.hsf.configserver;

import java.util.Set;

import org.summercool.hsf.configserver.pojo.ConfigServiceInfo;
import org.summercool.hsf.netty.service.HsfConnector;

/**
 * @Title: ConfigConsumer.java
 * @Package org.summercool.hsf.configserver
 * @Description: 配置服务使用者
 * @author 简道
 * @date 2012-3-22 下午8:42:44
 * @version V1.0
 */
public interface ConfigConsumer extends HsfConnector {
	public void setConnector(HsfConnector connector);

	public HsfConnector getConnector();

	public ConfigServiceInfo getConfigServiceInfo();

	public void setSubscribeService(Set<String> subscribeService);

	public Set<String> getSubscribeService();

	public Set<String> subscribe(String... serviceArray);

	public Set<String> unsubscribe(String... serviceArray);
}
