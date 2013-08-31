package org.summercool.hsf.pojo;

import java.io.Serializable;

/**
 * @Title: ServiceEntry.java
 * @Package org.summercool.hsf.pojo
 * @Description: Service配置条目
 * @author 简道
 * @date 2011-9-28 上午10:38:03
 * @version V1.0
 */
public class ServiceEntry implements Serializable {
	private static final long serialVersionUID = -3438625609796576217L;

	private String name;

	private Object service;

	public ServiceEntry() {

	}

	public ServiceEntry(String name, Object service) {
		this.name = name;
		this.service = service;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ServiceEntry)) {
			return false;
		}

		ServiceEntry other = (ServiceEntry) obj;

		if (this.name == null || other.name == null) {
			return false;
		}

		return this.name.equals(other.name);
	}

	@Override
	public int hashCode() {
		if (this.name == null) {
			return super.hashCode();
		}

		return this.name.hashCode();
	}

	public void setInterface(Class<?> serviceInterface) {
		if (serviceInterface == null) {
			throw new IllegalArgumentException("serviceInterface");
		}

		setName(serviceInterface.getSimpleName());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getService() {
		return service;
	}

	public void setService(Object service) {
		this.service = service;
	}

	@Override
	public String toString() {
		return "ServiceEntry{ name:" + name + ",service:" + service + "}";
	}
}
