package org.summercool.hsf.spring.factorybeans;

import java.lang.reflect.Constructor;
import java.net.SocketAddress;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.jboss.netty.channel.ChannelHandler;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.summercool.hsf.future.ChannelGroupFuture;
import org.summercool.hsf.netty.interceptor.PreDispatchInterceptor;
import org.summercool.hsf.netty.service.HsfConnector;
import org.summercool.hsf.netty.service.HsfConnectorImpl;
import org.summercool.hsf.util.AddressUtil;

/**
 * @Title: HsfConnectorFactoryBean
 * @Package org.summercool.hsf.spring.factorybeans
 * @Description: HsfConnector的FactoryBean实现，以集成到Spring
 * @author 简道
 * @date 2011-9-30 下午5:34:20
 * @version V1.0
 */
public class HsfConnectorFactoryBean implements FactoryBean<HsfConnector>, InitializingBean {
	private SocketAddress[] addresses;
	private Map<String, Object> options;
	private LinkedHashMap<String, ChannelHandler> handlers;
	@SuppressWarnings("rawtypes")
	private List<PreDispatchInterceptor> preDispatchInterceptors;
	private List<EventListener> listeners;
	private HsfConnector connector;
	private Executor bossExecutor;
	private Executor workerExecutor;
	private Executor eventExecutor;
	private String groupName;
	private boolean connectSync = true;
	private int workerCount = Runtime.getRuntime().availableProcessors() + 1;
	private List<Object> services;
	private Class<?> objectType = HsfConnectorImpl.class;

	public HsfConnectorFactoryBean() {
	}

	public void setServices(List<Object> services) {
		this.services = services;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void afterPropertiesSet() throws Exception {
		if (addresses == null || addresses.length == 0) {
			throw new IllegalArgumentException("addresses can not be null.");
		}

		if (bossExecutor == null) {
			connector = new HsfConnectorImpl();
		} else if (workerExecutor != null) {
			Constructor<?> ctor = objectType.getConstructor(Executor.class, Executor.class, Integer.class);
			ctor.setAccessible(true);
			connector = (HsfConnector) ctor.newInstance(bossExecutor, workerExecutor, workerCount);
		} else {
			Constructor<?> ctor = objectType.getConstructor(Executor.class, Integer.class);
			ctor.setAccessible(true);
			connector = (HsfConnector) ctor.newInstance(bossExecutor, workerCount);
		}

		if (eventExecutor != null) {
			connector.setEventExecutor(eventExecutor);
		}

		if (groupName != null) {
			connector.setGroupName(groupName);
		}

		if (options != null) {
			connector.setOptions(options);
		}

		if (handlers != null) {
			connector.setHandlers(handlers);
		}

		if (listeners != null) {
			connector.setListeners(listeners);
		}

		if (preDispatchInterceptors != null) {
			connector.setPreDispatchInterceptors(new LinkedList<PreDispatchInterceptor>(preDispatchInterceptors));
		}

		if (services != null) {
			connector.setServices(services);
		}
	}

	public ChannelGroupFuture connect() throws Exception {
		HsfConnector connector = getObject();
		return connector.connect(addresses, connectSync);
	}

	@Override
	public Class<?> getObjectType() {
		return objectType;
	}

	@Override
	public HsfConnector getObject() throws Exception {
		if (connector == null) {
			afterPropertiesSet();
		}
		return connector;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setObjectType(Class<?> objectType) {
		this.objectType = objectType;
	}

	public void setAddresses(String addressArray) {
		addresses = AddressUtil.parseAddress(addressArray);
	}

	public void setOptions(Map<String, Object> options) {
		this.options = options;
	}

	public void setHandlers(LinkedHashMap<String, ChannelHandler> handlers) {
		this.handlers = handlers;
	}

	public void setListeners(List<EventListener> listeners) {
		this.listeners = listeners;
	}

	public void setBossExecutor(Executor bossExecutor) {
		this.bossExecutor = bossExecutor;
	}

	public void setWorkerExecutor(Executor workerExecutor) {
		this.workerExecutor = workerExecutor;
	}

	public void setWorkerCount(int workerCount) {
		this.workerCount = workerCount;
	}

	public void setConnectSync(boolean connectSync) {
		this.connectSync = connectSync;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public void setEventExecutor(Executor eventExecutor) {
		this.eventExecutor = eventExecutor;
	}

	@SuppressWarnings("rawtypes")
	public void setPreDispatchInterceptors(List<PreDispatchInterceptor> preDispatchInterceptors) {
		this.preDispatchInterceptors = preDispatchInterceptors;
	}

	public void shutdown() {
		if (connector != null) {
			connector.shutdown();
		}
	}
}