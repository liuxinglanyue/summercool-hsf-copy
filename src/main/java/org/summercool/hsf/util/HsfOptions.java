package org.summercool.hsf.util;

/**
 * @Title: HsfConstants.java
 * @Package org.summercool.hsf.util
 * @Description: Options constants.
 * @author 简道
 * @date 2012-2-2 下午1:58:12
 * @version V1.0
 */
public class HsfOptions {
	/**
	 * tcpNoDelay, default is true.
	 */
	public static final String TCP_NO_DELAY = "tcpNoDelay";
	/**
	 * KeepAlive, default is true.
	 */
	public static final String KEEP_ALIVE = "keepAlive";
	/**
	 * reuseAddress，default is true.
	 */
	public static final String REUSE_ADDRESS = "reuseAddress";
	/**
	 * 写空闲时间(秒), default is 10.
	 */
	public static final String WRITE_IDLE_TIME = "writeIdleTime";
	/**
	 * 读空闲时间(秒), default is 60.
	 */
	public static final String READ_IDLE_TIME = "readIdleTime";
	/**
	 * 同步调用超时时间(毫秒), default is 60000
	 */
	public static final String SYNC_INVOKE_TIMEOUT = "syncInvokeTimeout";
	/**
	 * 握手超时时间(毫秒), default is 30000
	 */
	public static final String HANDSHAKE_TIMEOUT = "handshakeTimeout";
	/**
	 * 流量限额，default is 2000000
	 */
	public static final String FLOW_LIMIT = "flowLimit";
	/**
	 * 申请流量超时时间(毫秒), default is 0
	 */
	public static final String TIMEOUT_WHEN_FLOW_EXCEEDED = "timeoutWhenFlowExceeded";
	/**
	 * 分发器的最大线程数，default is 150
	 */
	public static final String MAX_THREAD_NUM_OF_DISPATCHER = "maxThreadNumOfDispatcher";
	/**
	 * 每个Group的通道数，default is 1
	 */
	public static final String CHANNEL_NUM_PER_GROUP = "channelNumPerGroup";
	/**
	 * 重连频率(毫秒), default is.10000
	 */
	public static final String RECONNECT_INTERVAL = "reconnectInterval";
	/**
	 * 建立连接超时时间(毫秒), default is.30000
	 */
	public static final String CONNECT_TIMEOUT = "connectTimeout";
	/**
	 * 是否缓存Callback方式发送的消息，缓存后将会在发送失败时回调doException方法参数传入, default is false.
	 */
	public static final String HOLD_CALLBACK_PARAM = "holdCallbackParam";
	/**
	 * 是否开启Service调用次数统计, default is false.
	 */
	public static final String OPEN_SERVICE_INVOKE_STATISTIC = "openServiceInvokeStatistic";
	/**
	 * 事件处理线程池队列上限, default is 1000000.
	 */
	public static final String EVENT_EXECUTOR_QUEUE_CAPACITY = "eventExecutorQueueCapacity";
}
