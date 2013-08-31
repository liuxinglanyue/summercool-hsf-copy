package org.summercool.hsf.configserver.service;

import org.summercool.hsf.annotation.RemoteServiceContract;
import org.summercool.hsf.configserver.pojo.ConfigServiceInfo;

/**
 * @Title: ConfigChangeNotifiedService.java
 * @Package org.summercool.hsf.configserver.service
 * @Description: 配置服务变更通知服务接口
 * @author 简道
 * @date 2012-3-22 下午1:26:08
 * @version V1.0
 */
@RemoteServiceContract
public interface ConfigChangeNotifiedService {
	public void configChanged(ConfigServiceInfo configServiceInfo);
}
