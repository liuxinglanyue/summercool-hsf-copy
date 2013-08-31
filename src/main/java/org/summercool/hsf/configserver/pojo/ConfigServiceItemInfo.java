package org.summercool.hsf.configserver.pojo;

import java.io.Serializable;

/**
 * @Title: ConfigServiceItemInfo.java
 * @Package org.summercool.hsf.configserver.pojo
 * @Description: 配置服务项信息
 * @author 简道
 * @date 2012-3-22 上午11:59:32
 * @version V1.0
 */
public class ConfigServiceItemInfo implements Serializable {
	private static final long serialVersionUID = -7624787378765371425L;

	private String serviceName;
	private AddressType addressType = AddressType.Address;
	private String address;

	public ConfigServiceItemInfo() {
	}

	public ConfigServiceItemInfo(String serviceName, String address) {
		this.serviceName = serviceName;
		this.address = address;
	}

	public ConfigServiceItemInfo(String serviceName, AddressType addressType, String address) {
		this.serviceName = serviceName;
		this.addressType = addressType;
		this.address = address;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ConfigServiceItemInfo)) {
			return false;
		}
		//
		ConfigServiceItemInfo other = (ConfigServiceItemInfo) obj;
		if (serviceName == null) {
			return false;
		} else if (serviceName.equals(other.getServiceName())) {
			if (address == null) {
				return false;
			} else {
				return address.equals(other.getAddress());
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (serviceName != null) {
			if (address != null) {
				return serviceName.hashCode() + address.hashCode();
			} else {
				return serviceName.hashCode();
			}
		}
		return super.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.append("ConfigItem { serviceName:").append(serviceName).append(", type:").append(addressType)
				.append(", address:").append(address).append("}").toString();
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public AddressType getAddressType() {
		return addressType;
	}

	public void setAddressType(AddressType addressType) {
		this.addressType = addressType;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
