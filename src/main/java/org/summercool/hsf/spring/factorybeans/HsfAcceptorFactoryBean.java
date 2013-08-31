package org.summercool.hsf.spring.factorybeans;

import java.lang.reflect.Constructor;
import java.net.SocketAddress;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.summercool.hsf.netty.interceptor.PreDispatchInterceptor;
import org.summercool.hsf.netty.service.HsfAcceptor;
import org.summercool.hsf.netty.service.HsfAcceptorImpl;
import org.summercool.hsf.util.AddressUtil;

/**
 * @Title: HsfAcceptorFactoryBean
 * @Package org.summercool.hsf.spring.factorybeans
 * @Description: HsfAcceptor的FactoryBean实现，以集成到Spring
 * @author 简道
 * @date 2011-9-30 下午5:34:20
 * @version V1.0
 */
public class HsfAcceptorFactoryBean implements FactoryBean<HsfAcceptor>, InitializingBean {
	private SocketAddress[] addresses;
	private Map<String, Object> options;
	private LinkedHashMap<String, ChannelHandler> handlers;
	@SuppressWarnings("rawtypes")
	private List<PreDispatchInterceptor> preDispatchInterceptors;
	private List<EventListener> listeners;
	private HsfAcceptor acceptor;
	private Executor bossExecutor;
	private Executor workerExecutor;
	private Executor eventExecutor;
	private int workerCount = Runtime.getRuntime().availableProcessors() + 1;
	private List<Object> services;
	private String groupName;
	private Class<?> objectType = HsfAcceptorImpl.class;

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
			acceptor = (HsfAcceptor) objectType.newInstance();
		} else if (workerExecutor != null) {
			Constructor<?> ctor = objectType.getConstructor(Executor.class, Executor.class, Integer.class);
			ctor.setAccessible(true);
			acceptor = (HsfAcceptor) ctor.newInstance(bossExecutor, workerExecutor, workerCount);
		} else {
			Constructor<?> ctor = objectType.getConstructor(Executor.class, Integer.class);
			ctor.setAccessible(true);
			acceptor = (HsfAcceptor) ctor.newInstance(bossExecutor, workerCount);
		}

		if (eventExecutor != null) {
			acceptor.setEventExecutor(eventExecutor);
		}

		if (groupName != null) {
			acceptor.setGroupName(groupName);
		}

		if (options != null) {
			acceptor.setOptions(options);
		}

		if (handlers != null) {
			acceptor.setHandlers(handlers);
		}

		if (listeners != null) {
			acceptor.setListeners(listeners);
		}

		if (preDispatchInterceptors != null) {
			acceptor.setPreDispatchInterceptors(new LinkedList<PreDispatchInterceptor>(preDispatchInterceptors));
		}

		if (services != null) {
			acceptor.setServices(services);
		}
	}

	@Override
	public Class<?> getObjectType() {
		return objectType;
	}

	@Override
	public HsfAcceptor getObject() throws Exception {
		if (acceptor == null) {
			afterPropertiesSet();
		}
		return acceptor;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setObjectType(Class<?> objectType) {
		this.objectType = objectType;
	}

	public void setAddresses(String addressArray) {
		this.addresses = AddressUtil.parseAddress(addressArray);
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

	public void setWorkerCount(int workerCount) {
		this.workerCount = workerCount;
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
		if (acceptor != null) {
			acceptor.shutdown();
		}
	}

	public List<Channel> bind() throws Exception {
		HsfAcceptor acceptor = getObject();
		return acceptor.bind(addresses);
	}
}
