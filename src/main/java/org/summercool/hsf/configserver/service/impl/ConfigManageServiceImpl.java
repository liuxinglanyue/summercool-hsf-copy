package org.summercool.hsf.configserver.service.impl;

import org.summercool.hsf.configserver.ConfigManager;
import org.summercool.hsf.configserver.service.ConfigManageService;

/**
 * @Title: ConfigManageServiceImpl.java
 * @Package org.summercool.hsf.configserver.service.impl
 * @Description: 配置服务管理实现类
 * @author 简道
 * @date 2012-3-22 下午2:42:35
 * @version V1.0
 */
public class ConfigManageServiceImpl extends ConfigProviderServiceImpl implements ConfigManageService {

	@Override
	public void clear() {
		//
		ConfigManager configManager = ConfigManager.getConfigManager();
		configManager.clear();
	}

}
