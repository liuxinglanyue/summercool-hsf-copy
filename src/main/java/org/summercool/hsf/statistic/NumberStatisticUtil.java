package org.summercool.hsf.statistic;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数值型统计辅助类
 * 
 * @Title: NumberStatisticUtil.java
 * @Package org.summercool.hsf.util
 * @author 简道
 * @date 2012-2-14 上午11:21:33
 * @version V1.0
 */
class NumberStatisticUtil {
	/**
	 * 用于保存各指标的统计数值
	 */
	private static ConcurrentHashMap<String, StatisticHandler> statisticMap = new ConcurrentHashMap<String, StatisticHandler>();

	/**
	 * 指标的单位统计时间（毫秒），默认为1分钟
	 */
	private static HashMap<String, Long> intervalMap = new HashMap<String, Long>();

	/**
	 * 为指标设定单位统计时间
	 * 
	 * @Title: setInterval
	 * @author 简道
	 * @param key
	 *        指标key
	 * @param interval
	 *        单位统计时间，毫秒
	 * @return void 返回类型
	 */
	public static void setInterval(String key, long interval) {
		if (interval <= 0) {
			throw new IllegalArgumentException("interval must great than 0.");
		}
		intervalMap.put(key, interval);
	}

	/**
	 * 获取指定指标，前一次统计值
	 * 
	 * @Title: getPreValue
	 * @author 简道
	 * @param key
	 *        指标key
	 * @return long 返回类型
	 */
	public static long getPreValue(String key) {
		StatisticHandler statisticHandler = statisticMap.get(key);
		if (statisticHandler == null) {
			return 0L;
		}

		return statisticHandler.getPreValue();
	}

	/**
	 * 获取指定指标，当前统计值
	 * 
	 * @Title: getValue
	 * @author 简道
	 * @param key
	 *        指标Key
	 * @return long 返回类型
	 */
	public static long getValue(String key) {
		StatisticHandler statisticHandler = statisticMap.get(key);
		if (statisticHandler == null) {
			return 0L;
		}

		return statisticHandler.getValue();
	}

	/**
	 * 为指定指标增加指定量，并返回当前统计值
	 * 
	 * @Title: addAndGet
	 * @author 简道
	 * @param key
	 *        指标Key
	 * @param delta
	 *        增量
	 * @return long 返回类型
	 */
	public static long addAndGet(String key, long delta) {
		StatisticHandler statisticHandler = statisticMap.get(key);
		//
		if (statisticHandler == null) {
			Long interval = intervalMap.get(key);
			if (interval == null) {
				statisticHandler = new StatisticHandler(key);
			} else {
				statisticHandler = new StatisticHandler(key, interval);
			}
			StatisticHandler existedHandler = statisticMap.putIfAbsent(key, statisticHandler);
			// 处理并发
			if (existedHandler != null) {
				statisticHandler = existedHandler;
			}
		}

		return statisticHandler.addValue(delta);
	}

	/**
	 * 为指定指标加1，并返回当前统计值
	 * 
	 * @Title: incrementAndGet
	 * @author 简道
	 * @param key
	 *        指标Key
	 * @return long 返回类型
	 */
	public static long incrementAndGet(String key) {
		return addAndGet(key, 1);
	}

	/**
	 * 以分钟为单位的统计算法
	 * 
	 * @ClassName: DefaultStatisticHandlerImpl
	 * @author 简道
	 * @date 2012-2-14 上午11:28:32
	 * 
	 */
	private static class StatisticHandler {
		private static final long DEFAULT_INTERVAL = 60 * 1000;
		private long interval = DEFAULT_INTERVAL;
		private long currentOffset = 1000;

		private AtomicLong preValue = new AtomicLong();
		private AtomicLong value = new AtomicLong();
		private long time;
		@SuppressWarnings("unused")
		private String name;
		private AtomicBoolean timeReset = new AtomicBoolean(false);

		public StatisticHandler(String name) {
			this.name = name;
		}

		public StatisticHandler(String name, long interval) {
			this.name = name;
			this.interval = interval;
			this.currentOffset = (long) (Double.valueOf(interval) / DEFAULT_INTERVAL * 1000);
		}

		public long getPreValue() {
			checkTime();
			return preValue.get();
		}

		@SuppressWarnings("unused")
		public long getAndResetPreValue() {
			// 此处保证与checkTime切换值的同步
			synchronized (this) {
				boolean reset = checkTime();
				long result = preValue.get();
				// 主动reset
				if (!reset) {
					// reset
					long temp = value.get();
					preValue.set(temp);
					while (!value.compareAndSet(temp, 0)) {
						temp = value.get();
						preValue.set(temp);
					}
				}

				return result;
			}
		}

		public long getValue() {
			checkTime();
			return value.get();
		}

		public long addValue(long delta) {
			checkTime();
			return value.addAndGet(delta);
		}

		private boolean checkTime() {
			long now = System.currentTimeMillis();
			if (timeReset.compareAndSet(false, true)) {
				time = now;
			}
			long offset = Math.abs(now - time);
			if (offset >= interval - currentOffset) {
				// 此处加锁后，重新做判断，以避免并发调用
				synchronized (this) {
					offset = Math.abs(now - time);
					if (offset < interval - currentOffset) {
						return false;
					}
					//
					preValue.set(value.get());
					value.set(0L);
					time = now;
					return true;
				}
			}
			return false;
		}
	}
}
