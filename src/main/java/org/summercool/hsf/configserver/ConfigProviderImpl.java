package org.summercool.hsf.configserver;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Executor;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.summercool.hsf.configserver.pojo.ConfigServiceInfo;
import org.summercool.hsf.configserver.pojo.ConfigServiceItemInfo;
import org.summercool.hsf.configserver.service.ConfigProviderService;
import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.listener.ChannelEventListenerAdapter;
import org.summercool.hsf.netty.listener.EventBehavior;
import org.summercool.hsf.netty.service.HsfConnectorImpl;
import org.summercool.hsf.netty.service.HsfService;
import org.summercool.hsf.proxy.ServiceProxyFactory;
import org.summercool.hsf.util.HsfContextHolder;

/**
 * @Title: ConfigProvider.java
 * @Package org.summercool.hsf.configserver
 * @Description: 配置服务提供者实现
 * @author 简道
 * @date 2012-3-22 下午8:40:04
 * @version V1.0
 */
public class ConfigProviderImpl extends HsfConnectorImpl implements ConfigProvider {
	private ConfigServiceInfo configServiceInfo;

	public ConfigProviderImpl() {
		super();
	}

	public ConfigProviderImpl(Executor bossExecutor, int workerCount) {
		super(bossExecutor, workerCount);
	}

	public ConfigProviderImpl(Executor bossExecutor, Executor workerExecutor, int workerCount) {
		super(bossExecutor, workerExecutor, workerCount);
	}

	@Override
	protected void init() {
		super.init();
		getListeners().add(new CSChannelEventListener());
	}

	@Override
	public void register(ConfigServiceInfo configServiceInfo) {
		// 向ConfigServer注册服务
		ConfigProviderService providerProxyService = ServiceProxyFactory.getBroadcastFactoryInstance(this)
				.wrapSyncProxy(ConfigProviderService.class);
		providerProxyService.register(configServiceInfo);

		for (Map.Entry<String, HashSet<ConfigServiceItemInfo>> entry : configServiceInfo.getItems().entrySet()) {
			this.configServiceInfo.addItems(entry.getValue());
		}
	}

	@Override
	public void remove(ConfigServiceInfo configServiceInfo) {
		// 向ConfigServer注册服务
		ConfigProviderService providerProxyService = ServiceProxyFactory.getBroadcastFactoryInstance(this)
				.wrapSyncProxy(ConfigProviderService.class);
		providerProxyService.remove(configServiceInfo);

		for (Map.Entry<String, HashSet<ConfigServiceItemInfo>> entry : configServiceInfo.getItems().entrySet()) {
			this.configServiceInfo.removeItems(entry.getValue());
		}
	}

	@Override
	public void setConfigService(ConfigServiceInfo configServiceInfo) {
		this.configServiceInfo = configServiceInfo;
	}

	@Override
	public ConfigServiceInfo getConfigService() {
		return configServiceInfo;
	}

	private class CSChannelEventListener extends ChannelEventListenerAdapter {
		@Override
		public EventBehavior groupCreated(ChannelHandlerContext ctx, HsfChannel channel, String groupName) {
			HsfService hsfService = HsfContextHolder.getHsfService();
			// 向ConfigServer注册服务
			ConfigProviderService providerProxyService = ServiceProxyFactory.getRoundFactoryInstance(hsfService)
					.wrapSyncProxy(ConfigProviderService.class, groupName);
			providerProxyService.register(configServiceInfo);

			return EventBehavior.Continue;
		}
	}
}
