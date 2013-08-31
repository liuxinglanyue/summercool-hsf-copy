package org.summercool.hsf.util;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.springframework.util.StringUtils;

/**
 * @Title: AddressUtil.java
 * @Package org.summercool.hsf.util
 * @Description: Address帮助类
 * @author 简道
 * @date 2011-10-8 上午10:37:20
 * @version V1.0
 */
public class AddressUtil {
	public static final String SPLIT_CHAR = ",";

	/**
	 * @Title: parseAddress
	 * @Description: 解析地址
	 * @author 简道
	 * @param addressArray
	 * @return SocketAddress[] 返回类型
	 */
	public static SocketAddress[] parseAddress(String addressArray) {
		if (!StringUtils.hasText(addressArray)) {
			throw new IllegalArgumentException("addressArray can not be null or empty.");
		}

		String[] array = addressArray.split(SPLIT_CHAR);
		SocketAddress[] addresses = new InetSocketAddress[array.length];
		for (int i = 0; i < array.length; i++) {
			String address = array[i];
			String[] parts = address.split(":");
			if (parts.length == 2) {
				addresses[i] = new InetSocketAddress(parts[0].trim(), LangUtil.parseInt(parts[1].trim()));
			} else if (parts.length == 1) {
				addresses[i] = new InetSocketAddress(LangUtil.parseInt(parts[0].trim()));
			} else {
				throw new IllegalArgumentException("address " + address + " is invalid.");
			}
		}

		return addresses;
	}

	/**
	 * @Title: parsePort
	 * @Description: 解析端口
	 * @author 简道
	 * @param portArray
	 * @return int[] 返回类型
	 */
	public static int[] parsePort(String portArray) {
		if (!StringUtils.hasText(portArray)) {
			throw new IllegalArgumentException("portArray can not be null or empty.");
		}

		String[] array = portArray.split(SPLIT_CHAR);
		int[] ports = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			ports[i] = LangUtil.parseInt(array[i].trim());
		}

		return ports;
	}
}
