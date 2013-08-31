package org.summercool.hsf.netty.service;

import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.jboss.netty.channel.ChannelHandler;
import org.summercool.hsf.netty.channel.FlowManager;
import org.summercool.hsf.netty.channel.HsfChannel;
import org.summercool.hsf.netty.channel.HsfChannelGroup;
import org.summercool.hsf.netty.event.EventDispatcher;
import org.summercool.hsf.netty.interceptor.PreDispatchInterceptor;
import org.summercool.hsf.pojo.ServiceEntry;
import org.summercool.hsf.util.ConcurrentArrayListHashMap;

/**
 * @Title: HsfService.java
 * @Package org.summercool.hsf.netty.service
 * @Description: Hsf服务接口
 * @author 简道
 * @date 2011-9-23 下午12:17:54
 * @version V1.0
 */
@SuppressWarnings("rawtypes")
public interface HsfService {
	/**
	 * @Title: isAlived
	 * @Description: 是否处于活动状态
	 * @author 简道
	 * @return boolean 返回类型
	 */
	boolean isAlived();

	/**
	 * @Title: getOptions
	 * @Description: 获取Options选项设置
	 * @author 简道
	 * @return Map<String,Object> 返回类型
	 */
	Map<String, Object> getOptions();

	Object getOption(String opName);

	/**
	 * @Title: setOptions
	 * @Description: 设置Options选项
	 * @author 简道
	 * @param options
	 *        选项参数
	 * @return void 返回类型
	 */
	void setOptions(Map<String, Object> options);

	void setOption(String opName, Object opValue);

	/**
	 * @Title: getHandlers
	 * @Description: 获取自定义Handler
	 * @author 简道
	 * @return LinkedHashMap<String,ChannelHandler> 返回类型
	 */
	LinkedHashMap<String, ChannelHandler> getHandlers();

	/**
	 * @Title: setHandlers
	 * @Description: 设置自定义Handler
	 * @author 简道
	 * @param handlers
	 *        Handler集合
	 * @return void 返回类型
	 */
	void setHandlers(LinkedHashMap<String, ChannelHandler> handlers);

	/**
	 * @Title: getListeners
	 * @Description: 获取监听器集合</br>{@link org.summercool.hsf.netty.listener.ChannelEventListener},
	 *               {@link org.summercool.hsf.netty.listener.MessageEventListener},
	 *               {@link org.summercool.hsf.netty.listener.ExceptionEventListener}
	 * @author 简道
	 * @return List<EventListener> 返回类型
	 */
	List<EventListener> getListeners();

	/**
	 * @Title: getListeners
	 * @Description: 设置监听器集合，
	 * @author 简道
	 * @param listeners
	 *        监听器集合</br>{@link org.summercool.hsf.netty.listener.ChannelEventListener},
	 *        {@link org.summercool.hsf.netty.listener.MessageEventListener},
	 *        {@link org.summercool.hsf.netty.listener.ExceptionEventListener}
	 * @return List<EventListener> 返回类型
	 */
	void setListeners(List<EventListener> listeners);

	/**
	 * @Title: getGroups
	 * @Description: 获取已经建立连接的Channel组集合
	 * @author 简道
	 * @return Map<String, HsfChannelGroup> 返回类型
	 */
	ConcurrentArrayListHashMap<String, HsfChannelGroup> getGroups();

	/**
	 * @Title: getChannels
	 * @Description:
	 * @author 简道
	 * @param id
	 * @return Map<Integer, HsfChannel> 返回类型
	 */
	Map<Integer, HsfChannel> getChannels();

	/**
	 * @Title: shutdown
	 * @Description: 关闭服务
	 * @author 简道
	 * @return void 返回类型
	 */
	void shutdown();

	/**
	 * @Title: getGroupName
	 * @Description: 获取组名称
	 * @author 简道
	 * @return String 返回类型
	 */
	String getGroupName();

	/**
	 * @Title: setGroupName
	 * @Description: 设置组名称
	 * @author 简道
	 * @param groupName
	 *        组名称
	 * @return void 返回类型
	 */
	void setGroupName(String groupName);

	/**
	 * @Title: getServices
	 * @Description: 获取注册的服务集合
	 * @author 简道
	 * @return LinkedHashMap<String,ServiceEntry> 返回类型
	 */
	LinkedHashMap<String, ServiceEntry> getServices();

	/**
	 * @Title: setServices
	 * @Description: 注册服务
	 * @author 简道
	 * @param services
	 *        服务集合
	 * @return void 返回类型
	 */
	void setServices(List<Object> services);

	/**
	 * @Title: registerService
	 * @Description: 注册服务
	 * @author 简道
	 * @param serviceInterface
	 *        服务接口
	 * @param service
	 *        服务对象
	 * @return void 返回类型
	 */
	void registerService(Class<?> serviceInterface, Object service);

	/**
	 * @Title: registerService
	 * @Description: 注册服务
	 * @author 简道
	 * @param name
	 *        服务名称
	 * @param service
	 *        服务对象
	 * @return void 返回类型
	 */
	void registerService(String name, Object service);

	/**
	 * @Title: registerService
	 * @Description: 注册拥有RemoteServiceContract注解的服务
	 * @author 简道
	 * @param service
	 * @return void 返回类型
	 */
	void registerService(Object service);

	FlowManager getFlowManager();

	void setFlowManager(FlowManager flowManager);

	EventDispatcher getEventDispatcher();

	void setEventExecutor(Executor executor);

	LinkedList<PreDispatchInterceptor> getPreDispatchInterceptors();

	void setPreDispatchInterceptors(LinkedList<PreDispatchInterceptor> preDispatchInterceptors);
}
