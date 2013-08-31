package org.summercool.hsf.configserver.service;

import org.summercool.hsf.annotation.RemoteServiceContract;

/**
 * @Title: ConfigManageService.java
 * @Package org.summercool.hsf.configserver.service
 * @Description: 配置服务管理接口
 * @author 简道
 * @date 2012-3-22 上午11:54:45
 * @version V1.0
 */
@RemoteServiceContract
public interface ConfigManageService extends ConfigProviderService {
	public void clear();
}
