package org.summercool.hsf.test.phone;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.summercool.hsf.pojo.HandshakeAck;
import org.summercool.hsf.pojo.HandshakeFinish;
import org.summercool.hsf.pojo.HandshakeRequest;
import org.summercool.hsf.pojo.Heartbeat;
import org.summercool.hsf.serializer.Serializer;

/**
 * @Title: PhoneConnector.java
 * @Package org.summercool.hsf.test.phone
 * @Description: TODO(添加描述)
 * @author Administrator
 * @date 2012-3-23 下午11:31:09
 * @version V1.0
 */
public class PhoneConnector {
	public static final Serializer SERIALIZER = new CustomSerializer();
	private ByteBuffer receivedBuffer = ByteBuffer.allocate(512);
	private final String groupName = "java nio client test";
	//
	private Selector selector;
	private SocketChannel socketChannel = null;

	public void connect(String ip, int port) throws Exception {
		// 建立连接
		socketChannel = SocketChannel.open();
		InetSocketAddress isa = new InetSocketAddress(ip, port);
		socketChannel.connect(isa);
		socketChannel.configureBlocking(false);

		// 注册到selector
		selector = Selector.open();
		socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

		// 发送握手请求消息包
		byte[] hrBytes = SERIALIZER.serialize(new HandshakeRequest(groupName));
		send(hrBytes);

		//
		new Thread(new NioWorker()).start();

		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					send(SERIALIZER.serialize(Heartbeat.getSingleton()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 10000L, 10000L, TimeUnit.MILLISECONDS);
	}

	public void send(byte[] msg) throws Exception {
		ByteBuffer sendBuffer = ByteBuffer.allocate(msg.length);
		sendBuffer.put(msg);
		sendBuffer.flip();
		//
		socketChannel.write(sendBuffer);
	}

	/**
	 * 接收消息
	 */
	private void receive(SelectionKey key) throws Exception {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		// 读取消息
		socketChannel.read(receivedBuffer);
		receivedBuffer.flip();

		while (receivedBuffer.remaining() > 3) {
			receivedBuffer.mark();
			int length = receivedBuffer.getInt();
			//
			if (length > receivedBuffer.capacity() - 4) {
				receivedBuffer.reset();
				synchronized (receivedBuffer) {
					ByteBuffer temp = receivedBuffer;
					receivedBuffer = ByteBuffer.allocate(length + 4);
					receivedBuffer.put(temp);
				}
				return;
			} else if (receivedBuffer.remaining() < length) {
				receivedBuffer.reset();
				receivedBuffer.compact();
				return;
			}
			//
			byte[] buffer = new byte[length];
			try {
				receivedBuffer.get(buffer);
				Object recMsg = SERIALIZER.deserialize(buffer);
				if (recMsg == null) {
					continue;
				}
				if (recMsg instanceof byte[]) {
					recMsg = CustomSerializer.decode((byte[]) recMsg);
				}
				System.out.println(recMsg);
				//
				// 如果接收到Server发送的握手反馈消息，则回送握手完成消息
				if (recMsg instanceof HandshakeAck) {
					// 构造握手完成消息包
					HandshakeFinish finish = new HandshakeFinish(groupName);
					byte[] hfBytes = SERIALIZER.serialize(finish);
					// 发送
					send(hfBytes);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		receivedBuffer.compact();
	}

	private boolean checkBuffer(int length) {
		boolean result = receivedBuffer.capacity() > length;
		if (!result) {

		}
		return result;
	}

	/**
	 * 通道读写任务
	 */
	private class NioWorker implements Runnable {

		@Override
		public void run() {
			// 如果未准备好，则阻塞
			while (true) {
				try {
					if (selector.select(500) > 0) {
						Set<SelectionKey> readyKeys = selector.selectedKeys();
						Iterator<SelectionKey> it = readyKeys.iterator();
						//
						while (it.hasNext()) {
							final SelectionKey key = it.next();
							try {
								it.remove();
								// 读
								if (key.isReadable()) {
									receive(key);
								}
							} catch (Exception e) {
								e.printStackTrace();
								try {
									if (key != null) {
										key.cancel();
										key.channel().close();
									}
								} catch (Exception ex) {
									e.printStackTrace();
								}
							}
						}
					}
				} catch (IOException e) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
						e.printStackTrace();
					}
					e.printStackTrace();
				}
			}
		}
	}
}
