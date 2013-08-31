package org.summercool.hsf.configserver.service;

import org.summercool.hsf.annotation.RemoteServiceContract;
import org.summercool.hsf.configserver.pojo.ConfigServiceInfo;

/**
 * @Title: ConfigProviderService.java
 * @Package org.summercool.hsf.configserver.service
 * @Description: 配置服务提供者接口
 * @author 简道
 * @date 2012-3-22 上午11:54:45
 * @version V1.0
 */
@RemoteServiceContract
public interface ConfigProviderService extends ConfigService {
	/**
	 * @Title: register
	 * @Description: 注册服务
	 * @author 简道
	 * @param configServiceInfo
	 *        设定文件
	 * @return void 返回类型
	 */
	public void register(ConfigServiceInfo configServiceInfo);

	/**
	 * @Title: register
	 * @Description: 删除服务
	 * @author 简道
	 * @param configServiceInfo
	 *        设定文件
	 * @return void 返回类型
	 */
	public void remove(ConfigServiceInfo configServiceInfo);
}
