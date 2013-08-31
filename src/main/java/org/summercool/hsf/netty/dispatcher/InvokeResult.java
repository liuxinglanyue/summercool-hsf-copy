package org.summercool.hsf.netty.dispatcher;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.summercool.hsf.netty.channel.HsfChannelGroup;

/**
 * @Title: InvokeResult.java
 * @Package org.summercool.hsf.netty.dispatcher
 * @Description: 调用结果结构
 * @author 简道
 * @date 2011-9-29 下午3:08:50
 * @version V1.0
 */
public class InvokeResult implements Serializable {
	private static final long serialVersionUID = 592379485137249222L;

	private Map<Object, Object> map = new LinkedHashMap<Object, Object>();

	public Object put(Object key, Object value) {
		Object preValue = map.get(key);

		map.put(key, value);

		return preValue;
	}
	
	public Object put(HsfChannelGroup group, Object value) {
		return put(group.getName()	, value);
	}

	public Object remove(Object key) {
		return map.remove(key);
	}

	public Object get(Object key) throws Throwable {
		Object value = map.get(key);
		if (value != null && value instanceof Throwable) {
			throw (Throwable) value;
		}

		return value;
	}

	public Object getFirstValue() throws Throwable {
		if (map.size() == 0) {
			return null;
		}

		return get(map.keySet().iterator().next());
	}

	public Set<Object> keySet() {
		return map.keySet();
	}
	
	public int size(){
		return map.size();
	}
}
