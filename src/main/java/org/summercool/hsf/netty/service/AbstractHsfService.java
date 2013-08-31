package org.summercool.hsf.netty.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang.StringUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.summercool.hsf.annotation.RemoteServiceContract;
import org.summercool.hsf.jmx.MBeanUtils;
import org.summercool.hsf.jmx.management.annotation.Description;
import org.summercool.hsf.jmx.management.annotation.MBean;
import org.summercool.hsf.jmx.management.annotation.ManagedAttribute;
import org.summercool.hsf.netty.channel.FlowManager;
import org.summercool.hsf.netty.channel.FlowManagerImpl;
import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.channel.HsfChannelGroup;
import org.summercool.hsf.netty.channel.RoundChannelGroup;
import org.summercool.hsf.netty.decoder.LengthBasedDecoder;
import org.summercool.hsf.netty.encoder.LengthBasedEncoder;
import org.summercool.hsf.netty.event.EventDispatcher;
import org.summercool.hsf.netty.handler.downstream.CompressionDownstreamHandler;
import org.summercool.hsf.netty.handler.downstream.SerializeDownstreamHandler;
import org.summercool.hsf.netty.handler.upstream.DecompressionUpstreamHandler;
import org.summercool.hsf.netty.handler.upstream.DeserializeUpstreamHandler;
import org.summercool.hsf.netty.interceptor.PreDispatchInterceptor;
import org.summercool.hsf.netty.listener.EventBehavior;
import org.summercool.hsf.netty.listener.ExceptionEventListener;
import org.summercool.hsf.netty.listener.impl.ServiceMessageEventListener;
import org.summercool.hsf.pojo.ServiceEntry;
import org.summercool.hsf.util.ConcurrentArrayListHashMap;
import org.summercool.hsf.util.HsfOptions;
import org.summercool.hsf.util.LangUtil;
import org.summercool.hsf.util.NamedThreadFactory;
import org.summercool.hsf.util.StackTraceUtil;
import org.summercool.hsf.util.UUIDUtil;

/**
 * @Title: AbstractHsfService.java
 * @Package org.summercool.hsf.netty.service
 * @Description: Hsf服务抽象实现
 * @author 简道
 * @date 2011-9-27 下午3:25:39
 * @version V1.0
 */
@MBean
@Description("AbstractHsfService")
@SuppressWarnings("rawtypes")
public abstract class AbstractHsfService implements HsfService {
	/**
	 * @Fields handlers : 存放ChannelHandler列表
	 */
	private LinkedHashMap<String, ChannelHandler> handlers = new LinkedHashMap<String, ChannelHandler>();
	/**
	 * @Fields options : 存放Option列表
	 */
	private Map<String, Object> options = new HashMap<String, Object>();
	/**
	 * @Fields channels : 存放所有在线Channel，Key为id
	 */
	private ConcurrentHashMap<Integer, HsfChannel> channels = new ConcurrentHashMap<Integer, HsfChannel>();
	/**
	 * @Fields groupChannels : 存放建立连接的Channel组列表
	 */
	private ConcurrentArrayListHashMap<String, HsfChannelGroup> groups = new ConcurrentArrayListHashMap<String, HsfChannelGroup>();
	/**
	 * @Fields eventListeners : 存放监听器列表
	 */
	private List<EventListener> eventListeners = new ArrayList<EventListener>();
	private LinkedHashMap<String, ServiceEntry> services = new LinkedHashMap<String, ServiceEntry>();
	private LinkedList<PreDispatchInterceptor> preDispatchInterceptors;

	/**
	 * @Fields eventDispatcher : 事件分发器
	 */
	protected EventDispatcher eventDispatcher = new EventDispatcher(this);
	private String groupName;
	protected Executor bossExecutor;
	protected Executor workerExecutor;
	protected int workerCount;

	//
	private FlowManager flowManager = new FlowManagerImpl();

	private static ExecutorService getCachedExecutor(String name) {
		return Executors.newCachedThreadPool(new NamedThreadFactory(name));
	}

	public AbstractHsfService() {
		this(getCachedExecutor("HSF-BOSS-PROCESSOR"), Runtime.getRuntime().availableProcessors() + 1);
	}

	public AbstractHsfService(Executor bossExecutor, int workerCount) {
		this(bossExecutor, getCachedExecutor("HSF-WORKER-PROCESSOR"), workerCount);
	}

	public AbstractHsfService(Executor bossExecutor, Executor workerExecutor, int workerCount) {
		if (bossExecutor == null) {
			throw new IllegalArgumentException("bossExecutor can not be null.");
		} else if (workerExecutor == null) {
			throw new IllegalArgumentException("workerExecutor can not be null.");
		} else if (workerCount <= 0) {
			throw new IllegalArgumentException("workerCount required > 0.");
		} else if (workerExecutor instanceof ThreadPoolExecutor
				&& ((ThreadPoolExecutor) workerExecutor).getMaximumPoolSize() < workerCount) {
			throw new IllegalArgumentException("the maximum pool size of workerExecutor required >= workerCount.");
		}

		this.bossExecutor = bossExecutor;
		this.workerExecutor = workerExecutor;
		this.workerCount = workerCount;

		init();
	}

	@Override
	public void shutdown() {
	}

	/**
	 * @Title: init
	 * @Description: 初始化系统参数
	 * @author 简道
	 * @return void 返回类型
	 */
	protected void init() {
		// 设置GroupName
		groupName = getDefaultGroupName();

		handlers.put("encode", new LengthBasedEncoder());
		handlers.put("compress", new CompressionDownstreamHandler());
		handlers.put("serialize", new SerializeDownstreamHandler());

		handlers.put("decode", new LengthBasedDecoder());
		handlers.put("decompress", new DecompressionUpstreamHandler());
		handlers.put("deserialize", new DeserializeUpstreamHandler());

		options.put(HsfOptions.TCP_NO_DELAY, true);
		options.put(HsfOptions.KEEP_ALIVE, true);
		options.put(HsfOptions.REUSE_ADDRESS, true);
		options.put(HsfOptions.WRITE_IDLE_TIME, 10);
		options.put(HsfOptions.READ_IDLE_TIME, 60);
		options.put(HsfOptions.SYNC_INVOKE_TIMEOUT, 60000);
		options.put(HsfOptions.CONNECT_TIMEOUT, 30000);
		options.put(HsfOptions.HANDSHAKE_TIMEOUT, 30000);
		options.put(HsfOptions.FLOW_LIMIT, 2000000);
		options.put(HsfOptions.TIMEOUT_WHEN_FLOW_EXCEEDED, 0);
		options.put(HsfOptions.MAX_THREAD_NUM_OF_DISPATCHER, 150);
		options.put(HsfOptions.OPEN_SERVICE_INVOKE_STATISTIC, false);
		options.put(HsfOptions.EVENT_EXECUTOR_QUEUE_CAPACITY, 1000000);
		//
		initSystemListener();
		//
		MBeanUtils.register(this);
	}

	/**
	 * @Title: initSystemListener
	 * @Description: 初始化系统监听器
	 * @author 简道
	 * @return void 返回类型
	 */
	protected void initSystemListener() {
		eventListeners.add(0, new DefaultExceptionListener());
		eventListeners.add(new ServiceMessageEventListener(eventDispatcher));
	}

	/**
	 * @Title: getDefaultGroupName
	 * @Description: 获取默认GroupName
	 * @author 简道
	 * @return String 返回类型
	 */
	protected String getDefaultGroupName() {
		String host;
		try {
			host = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			host = "UnknowHost";
		}
		return host + "@" + UUIDUtil.random();
	}

	/**
	 * @Title: newChannelGroup
	 * @Description: 创建新的HsfChannelGroup实例，默认采用RoundChannelGroup实现
	 * @author 简道
	 * @param groupName
	 * @return HsfChannelGroup 返回类型
	 */
	protected HsfChannelGroup newChannelGroup(String groupName) {
		return new RoundChannelGroup(groupName);
	}

	@ManagedAttribute
	@Description("Returns the options of hsf.")
	public Map<String, Object> getOptions() {
		return options;
	}

	@Override
	public Object getOption(String opName) {
		return options.get(opName);
	}

	public void setOptions(Map<String, Object> options) {
		if (options == null) {
			return;
		}
		for (Entry<String, Object> entry : options.entrySet()) {
			setOption(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void setOption(String opName, Object opValue) {
		if (this.options == null) {
			this.options = new LinkedHashMap<String, Object>();
		}
		this.options.put(opName, opValue);
		//
		if (HsfOptions.FLOW_LIMIT.equals(opName)) {
			Integer flowLimit = LangUtil.parseInt(opValue);
			if (flowLimit != null && this.flowManager != null) {
				this.flowManager.setThreshold(flowLimit);
			}
		} else if (HsfOptions.MAX_THREAD_NUM_OF_DISPATCHER.equals(opName)) {
			Integer maxThreadNumOfDispatcher = LangUtil.parseInt(opValue);
			if (maxThreadNumOfDispatcher != null && maxThreadNumOfDispatcher > 0) {
				eventDispatcher.setMaximumPoolSize(maxThreadNumOfDispatcher);
			}
		} else if (HsfOptions.EVENT_EXECUTOR_QUEUE_CAPACITY.equals(opName)) {
			Integer eventExecutorQueueCapacity = LangUtil.parseInt(opValue);
			if (eventExecutorQueueCapacity != null && eventExecutorQueueCapacity > 0) {
				eventDispatcher.setQueueCapacity(eventExecutorQueueCapacity);
			}
		}
	}

	public LinkedHashMap<String, ChannelHandler> getHandlers() {
		return handlers;
	}

	public void setHandlers(LinkedHashMap<String, ChannelHandler> handlers) {
		this.handlers = handlers;
	}

	public List<EventListener> getListeners() {
		return eventListeners;
	}

	public void setListeners(List<EventListener> listeners) {
		if (listeners == null) {
			listeners = new ArrayList<EventListener>();
		}
		eventListeners = listeners;
		initSystemListener();
	}

	public ConcurrentArrayListHashMap<String, HsfChannelGroup> getGroups() {
		return groups;
	}

	@ManagedAttribute
	@Description("Returns the status of hsf service.")
	public boolean isAlived() {
		return groups.size() > 0;
	}

	@Override
	public Map<Integer, HsfChannel> getChannels() {
		return channels;
	}

	@ManagedAttribute
	@Description("Returns the groupName.")
	@Override
	public String getGroupName() {
		return groupName;
	}

	@Override
	public void setGroupName(String groupName) {
		if (groupName == null) {
			throw new IllegalArgumentException("group name can not be null.");
		}
		this.groupName = groupName;
	}

	public LinkedHashMap<String, ServiceEntry> getServices() {
		return services;
	}

	@Override
	public FlowManager getFlowManager() {
		return flowManager;
	}

	@Override
	public void setFlowManager(FlowManager flowManager) {
		this.flowManager = flowManager;
		if (this.flowManager != null) {
			//
			Integer flowLimit = LangUtil.parseInt(this.getOption(HsfOptions.FLOW_LIMIT), null);
			if (flowLimit != null) {
				this.flowManager.setThreshold(flowLimit);
			}
		}
	}

	public void setServices(List<Object> services) {
		this.services.clear();

		if (services == null) {
			return;
		}

		for (Object service : services) {
			if (service instanceof ServiceEntry) {
				ServiceEntry serviceEntry = (ServiceEntry) service;
				this.services.put(serviceEntry.getName(), serviceEntry);
			} else {
				this.registerService(service);
			}
		}
	}

	public void registerService(Class<?> serviceInterface, Object service) {
		ServiceEntry serviceEntry = new ServiceEntry();
		serviceEntry.setInterface(serviceInterface);
		serviceEntry.setService(service);

		this.services.put(serviceEntry.getName(), serviceEntry);
	}

	public void registerService(String name, Object service) {
		ServiceEntry serviceEntry = new ServiceEntry(name, service);

		this.services.put(serviceEntry.getName(), serviceEntry);
	}

	@Override
	public void registerService(Object service) {
		if (service == null) {
			throw new NullPointerException("service");
		}
		//
		if (service instanceof ServiceEntry) {
			ServiceEntry entry = (ServiceEntry) service;
			this.services.put(entry.getName(), entry);
			return;
		}

		boolean isValid = false;
		Class<?>[] interfaceList = service.getClass().getInterfaces();
		if (interfaceList != null) {
			for (Class<?> clazz : interfaceList) {
				RemoteServiceContract remoteServiceContract = clazz.getAnnotation(RemoteServiceContract.class);
				if (remoteServiceContract == null) {
					continue;
				}

				String name = remoteServiceContract.value();
				if (StringUtils.isBlank(name)) {
					registerService(clazz, service);
				} else {
					registerService(name, service);
				}
				isValid = true;
			}
		}

		if (!isValid) {
			throw new IllegalArgumentException("service type '" + service.getClass()
					+ "' is not a RemoteServiceContract service. Please mark interface with @RemoteServiceContract.");
		}
	}

	public EventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}

	public void setEventExecutor(Executor executor) {
		eventDispatcher.setExecutor(executor);
	}

	@Override
	public void setPreDispatchInterceptors(LinkedList<PreDispatchInterceptor> preDispatchInterceptors) {
		this.preDispatchInterceptors = preDispatchInterceptors;
	}

	@Override
	public LinkedList<PreDispatchInterceptor> getPreDispatchInterceptors() {
		return preDispatchInterceptors;
	}

	@ManagedAttribute
	@Description("Returns the connected groups.")
	public String getConnectedGroups() {
		return groups.keySet().toString();
	}

	@ManagedAttribute
	@Description("Returns the registered services.")
	public Map<String, String> getRegisteredServices() {
		Map<String, String> map = new HashMap<String, String>();
		for (Map.Entry<String, ServiceEntry> entry : services.entrySet()) {
			map.put(entry.getKey(), LangUtil.toString(entry.getValue().getService()));
		}

		return map;
	}

	@ManagedAttribute
	@Description("Returns the approximate number of threads that are actively executing tasks.")
	public int getActiveCount4Jmx() {
		return eventDispatcher.getExecutorActiveCount();
	}

	@ManagedAttribute
	@Description("Returns the approximate total number of tasks that have completed execution. Because the states of tasks and threads may change dynamically during computation, the returned value is only an approximation, but one that does not ever decrease across successive calls.")
	public long getCompletedTaskCount4Jmx() {
		return eventDispatcher.getExecutorCompletedTaskCount();
	}

	@ManagedAttribute
	@Description("Returns the largest number of threads that have ever simultaneously been in the pool.")
	public int getLargestPoolSize4Jmx() {
		return eventDispatcher.getExecutorLargestPoolSize();
	}

	@ManagedAttribute
	@Description("Returns the current number of threads in the pool.")
	public int getPoolSize4Jmx() {
		return eventDispatcher.getExecutorPoolSize();
	}

	@ManagedAttribute
	@Description("Returns the approximate total number of tasks that have ever been scheduled for execution. Because the states of tasks and threads may change dynamically during computation, the returned value is only an approximation.")
	public long getTaskCount4Jmx() {
		return eventDispatcher.getExecutorTaskCount();
	}

	@ManagedAttribute
	@Description("Returns the current number of tasks that have ever simultaneously been in the queue.")
	public int getQueueSize4Jmx() {
		return eventDispatcher.getExecutorQueueSize();
	}

	@ManagedAttribute
	@Description("Returns the hsf message statistic info.")
	public Map<String, String> getMsgStatistic() {
		Map<String, String> map = new HashMap<String, String>();
		for (HsfChannelGroup group : groups.values()) {
			map.put(group.getName(), group.getMsgStatistic().toString());
		}

		return map;
	}

	@ManagedAttribute
	@Description("Returns the hsf heart beat statistic info.")
	public Map<String, String> getHeartbeatStatistic() {
		Map<String, String> map = new HashMap<String, String>();
		for (HsfChannelGroup group : groups.values()) {
			map.put(group.getName(), group.getHeartBeatStatistic().toString());
		}

		return map;
	}

	/**
	 * @ClassName: DefaultExceptionListener
	 * @Description: 异常监听器
	 * @author 简道
	 * @date 2011-9-29 下午12:08:12
	 * 
	 */
	protected class DefaultExceptionListener implements ExceptionEventListener {
		Logger logger = LoggerFactory.getLogger(getClass());

		public EventBehavior exceptionCaught(ChannelHandlerContext ctx, Channel channel, ExceptionEvent e) {
			if (e.getCause() != null && channel.getRemoteAddress() != null) {
				logger.error("channel({}) exception:{}", channel, StackTraceUtil.getStackTrace(e.getCause()));
			}
			return EventBehavior.Continue;
		}
	}
}