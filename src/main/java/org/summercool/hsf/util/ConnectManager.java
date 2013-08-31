package org.summercool.hsf.util;

import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Title: ReconnectManager.java
 * @Package org.summercool.hsf.util
 * @Description: 重连管理类
 * @author 简道
 * @date 2011-11-17 下午8:33:48
 * @version V1.0
 */
public class ConnectManager {
	private ConcurrentHashMap<SocketAddress, AtomicInteger> disconnAddressList = new ConcurrentHashMap<SocketAddress, AtomicInteger>();
	private ConcurrentHashMap<SocketAddress, String> connectedAddressList = new ConcurrentHashMap<SocketAddress, String>();
	private BlockingQueue<ConnectionInfo> reconnInfoQueue = new LinkedBlockingQueue<ConnectionInfo>(20);
	
	public Set<SocketAddress> getConnectedAddress() {
		return connectedAddressList.keySet();
	}

	public String getConnectedGroupName(SocketAddress address) {
		return connectedAddressList.get(address);
	}

	public void addConnected(SocketAddress address, String group) {
		connectedAddressList.put(address, group);
	}

	public void removeConnected(SocketAddress address) {
		connectedAddressList.remove(address);
	}

	public Set<SocketAddress> getDisconnectAddress() {
		return disconnAddressList.keySet();
	}

	public void addDisconnectAddress(SocketAddress address) {
		AtomicInteger num = disconnAddressList.putIfAbsent(address, new AtomicInteger(1));

		if (num != null) {
			num.getAndIncrement();
		}
	}

	public void countDownDisconnect(SocketAddress address) {
		AtomicInteger num = disconnAddressList.get(address);
		if (num != null) {
			if (num.decrementAndGet() < 1) {
				removeDisconnect(address);
			}
		}
	}

	public void removeDisconnect(SocketAddress address) {
		disconnAddressList.remove(address);
	}

	public Integer getDisconnectNum(SocketAddress address) {
		AtomicInteger num = disconnAddressList.get(address);
		if (num == null) {
			return 0;
		}
		return num.get();
	}
	public void logConnect(SocketAddress address) {
		ConnectionInfo connInfo = new ConnectionInfo(address.toString());
		while (!reconnInfoQueue.offer(connInfo)) {
			reconnInfoQueue.poll();
		}
	}

	public Queue<ConnectionInfo> getReconnectionInfoQueue() {
		return reconnInfoQueue;
	}

	public static class ConnectionInfo {
		private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		private Date time;
		private String address;

		public ConnectionInfo(String address) {
			this.address = address;
			this.time = new Date();
		}

		public String getTime() {
			return timeFormat.format(time);
		}

		public String getAddress() {
			return address;
		}

		@Override
		public String toString() {
			return "[" + getTime() + "," + address + "]";
		}
	}
}
