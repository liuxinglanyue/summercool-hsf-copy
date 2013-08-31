package org.summercool.hsf.configserver;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.summercool.hsf.configserver.pojo.ConfigServiceInfo;
import org.summercool.hsf.configserver.pojo.ConfigServiceItemInfo;
import org.summercool.hsf.configserver.service.ConfigChangeNotifiedService;
import org.summercool.hsf.configserver.service.ConfigConsumerService;
import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.listener.ChannelEventListenerAdapter;
import org.summercool.hsf.netty.listener.EventBehavior;
import org.summercool.hsf.netty.service.HsfConnector;
import org.summercool.hsf.netty.service.HsfConnectorImpl;
import org.summercool.hsf.netty.service.HsfService;
import org.summercool.hsf.proxy.ServiceProxyFactory;
import org.summercool.hsf.util.AddressUtil;
import org.summercool.hsf.util.HsfContextHolder;
import org.summercool.hsf.util.StackTraceUtil;

/**
 * @Title: ConfigConsumerImpl.java
 * @Package org.summercool.hsf.configserver
 * @Description: 配置服务使用者实现
 * @author 简道
 * @date 2012-3-22 下午9:13:57
 * @version V1.0
 */
public class ConfigConsumerImpl extends HsfConnectorImpl implements ConfigConsumer {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private HsfConnector connector;
	private ConfigServiceInfo configServiceInfo;
	private Set<String> subscribeService = new HashSet<String>();

	public ConfigConsumerImpl() {
		super();
	}

	public ConfigConsumerImpl(Executor bossExecutor, int workerCount) {
		super(bossExecutor, workerCount);
	}

	public ConfigConsumerImpl(Executor bossExecutor, Executor workerExecutor, int workerCount) {
		super(bossExecutor, workerExecutor, workerCount);
	}

	@Override
	protected void initSystemListener() {
		super.initSystemListener();
		//
		getListeners().add(new CSChannelEventListener());
		registerService(new ConfigChangeNotifiedServiceImpl());
	}

	@Override
	public Set<String> subscribe(String... serviceArray) {
		if (serviceArray == null || serviceArray.length == 0) {
			return subscribeService;
		}
		for (String service : serviceArray) {
			subscribeService.add(service);
		}

		//
		ConfigConsumerService consumerProxyService = ServiceProxyFactory.getBroadcastFactoryInstance(this)
				.wrapSyncProxy(ConfigConsumerService.class);
		//
		try {
			consumerProxyService.subscribe(serviceArray);
		} catch (Exception e) {
			logger.error("subscribe to config server failed, error:{}", StackTraceUtil.getStackTrace(e));
		}
		return subscribeService;
	}

	@Override
	public Set<String> unsubscribe(String... serviceArray) {
		if (serviceArray == null || serviceArray.length == 0) {
			return subscribeService;
		}
		for (String service : serviceArray) {
			subscribeService.remove(service);
		}
		//
		ConfigConsumerService consumerProxyService = ServiceProxyFactory.getBroadcastFactoryInstance(this)
				.wrapSyncProxy(ConfigConsumerService.class);
		//
		try {
			consumerProxyService.unsubscribe(serviceArray);
		} catch (Exception e) {
			logger.error("subscribe to config server failed, error:{}", StackTraceUtil.getStackTrace(e));
		}
		return subscribeService;
	}

	@Override
	public void setSubscribeService(Set<String> subscribeService) {
		this.subscribeService = subscribeService;
	}

	@Override
	public Set<String> getSubscribeService() {
		return subscribeService;
	}

	@Override
	public ConfigServiceInfo getConfigServiceInfo() {
		return configServiceInfo;
	}

	@Override
	public void setConnector(HsfConnector connector) {
		this.connector = connector;
	}

	@Override
	public HsfConnector getConnector() {
		return connector;
	}

	private class CSChannelEventListener extends ChannelEventListenerAdapter {
		@Override
		public EventBehavior groupCreated(ChannelHandlerContext ctx, HsfChannel channel, String groupName) {
			if (subscribeService != null && subscribeService.size() > 0) {
				//
				HsfService hsfService = HsfContextHolder.getHsfService();
				ConfigConsumerService consumerProxyService = ServiceProxyFactory.getRoundFactoryInstance(hsfService)
						.wrapSyncProxy(ConfigConsumerService.class, groupName);
				//
				String[] serviceArray = new String[subscribeService.size()];
				subscribeService.toArray(serviceArray);
				try {
					consumerProxyService.subscribe(serviceArray);
				} catch (Exception e) {
					logger.error("subscribe to config server failed, error:{}", StackTraceUtil.getStackTrace(e));
				}
			}
			return EventBehavior.Continue;
		}
	}

	public final class ConfigChangeNotifiedServiceImpl implements ConfigChangeNotifiedService {

		@Override
		public void configChanged(ConfigServiceInfo configServiceInfo) {
			ConfigConsumerImpl.this.configServiceInfo = configServiceInfo;
			if (connector != null) {
				if (configServiceInfo == null) {
					return;
				}
				//
				Collection<HashSet<ConfigServiceItemInfo>> serviceConfigList = configServiceInfo.getItems().values();
				if (serviceConfigList == null) {
					return;
				}
				//
				List<SocketAddress> addressList = new ArrayList<SocketAddress>();
				for (HashSet<ConfigServiceItemInfo> itemList : serviceConfigList) {
					for (ConfigServiceItemInfo item : itemList) {
						SocketAddress[] addressArray = AddressUtil.parseAddress(item.getAddress());
						for (SocketAddress socketAddress : addressArray) {
							addressList.add(socketAddress);
						}
					}
				}

				//
				SocketAddress[] addressArray = new SocketAddress[addressList.size()];
				addressList.toArray(addressArray);
				connector.refreshIPList(true, addressArray);
			}
		}
	}
}
