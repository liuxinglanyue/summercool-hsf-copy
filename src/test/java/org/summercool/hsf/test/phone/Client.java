package org.summercool.hsf.test.phone;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Title: Client.java
 * @Package org.summercool.hsf.test.phone
 * @Description: TODO(添加描述)
 * @date 2012-3-24 上午12:38:30
 * @version V1.0
 */
public class Client {
	public static void main(String[] args) throws Exception {
		final PhoneConnector connector = new PhoneConnector();
		connector.connect("127.0.0.1", 8088);
		//
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < 3000; i++) {
					try {
						connector.send(PhoneConnector.SERIALIZER.serialize(CustomSerializer.encode(UUID.randomUUID()
								.toString())));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}, 5000L, 1000L, TimeUnit.MILLISECONDS);
	}
}
