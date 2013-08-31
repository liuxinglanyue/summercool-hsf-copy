package org.summercool.hsf.configserver;

import java.util.concurrent.Executor;

import org.summercool.hsf.configserver.listener.ConfigServerChannelListener;
import org.summercool.hsf.configserver.service.impl.ConfigConsumerServiceImpl;
import org.summercool.hsf.configserver.service.impl.ConfigManageServiceImpl;
import org.summercool.hsf.configserver.service.impl.ConfigProviderServiceImpl;
import org.summercool.hsf.netty.service.HsfAcceptorImpl;

/**
 * @Title: ConfigServer.java
 * @Package org.summercool.hsf.configserver
 * @Description: ConfigServer
 * @author 简道
 * @date 2012-3-22 下午8:22:09
 * @version V1.0
 */
public class ConfigServer extends HsfAcceptorImpl {
	public ConfigServer() {
		super();
	}

	public ConfigServer(Executor bossExecutor, int workerCount) {
		super(bossExecutor, workerCount);
	}

	public ConfigServer(Executor bossExecutor, Executor workerExecutor, int workerCount) {
		super(bossExecutor, workerExecutor, workerCount);
	}

	@Override
	protected void initSystemListener() {
		super.initSystemListener();
		getListeners().add(0, new ConfigServerChannelListener());
	}

	@Override
	protected void init() {
		super.init();
		// 注册配置服务
		registerService(new ConfigConsumerServiceImpl());
		registerService(new ConfigProviderServiceImpl());
		registerService(new ConfigManageServiceImpl());
	}
}
