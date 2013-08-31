package org.summercool.hsf.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Title: EndlessLoopUtil.java
 * @Package org.summercool.hsf.util
 * @Description: 无限循环帮助类
 * @author 简道
 * @date 2011-10-8 下午1:56:35
 * @version V1.0
 */
public class EndlessLoopUtil {
	static Logger logger = LoggerFactory.getLogger(EndlessLoopUtil.class);

	/**
	 * @Title: fixEndlessLoop
	 * @Description: 解决指定对象循环嵌套引用
	 * @author 简道
	 * @param obj
	 *        待检测的对象
	 * @return boolean 返回类型
	 */
	public static boolean fixEndlessLoop(Object obj) {
		if (obj == null) {
			return false;
		}

		boolean fixed = false;
		HashSet<Object> chain = new HashSet<Object>();

		chain.add(obj);

		if (fixEndlessLoop(obj, chain)) {
			fixed = true;
		}

		return fixed;
	}

	static boolean fixEndlessLoop(Object obj, HashSet<Object> chain) {
		List<Field> fields = ReflectionUtil.getFields(obj.getClass());

		for (Field fd : fields) {
			if (Modifier.isStatic(fd.getModifiers()) || Modifier.isFinal(fd.getModifiers())) {
				continue;
			}

			fd.setAccessible(true);
			Object temp = null;

			try {
				temp = fd.get(obj);
			} catch (Exception e) {
			}

			if (temp != null) {
				Class<?> clazz = temp.getClass();
				if (String.class.equals(clazz) || int.class.equals(clazz) || Integer.class.equals(clazz)
						|| short.class.equals(clazz) || Short.class.equals(clazz) || long.class.equals(clazz)
						|| Long.class.equals(clazz) || byte.class.equals(clazz) || Byte.class.equals(clazz)
						|| char.class.equals(clazz) || Character.class.equals(clazz) || double.class.equals(clazz)
						|| Double.class.equals(clazz) || float.class.equals(clazz) || Float.class.equals(clazz)
						|| boolean.class.equals(clazz) || Boolean.class.equals(clazz)
						|| Date.class.isAssignableFrom(clazz)) {
					continue;
				}

				if (!chain.contains(temp)) {
					chain.add(temp);
				} else {
					try {
						fd.set(obj, ReflectionUtil.getDefaultValue(clazz));
					} catch (Exception e) {
					}
					return true;
				}

				return fixEndlessLoop(temp, chain);
			}
		}

		return false;
	}

	public static void main(String[] args) {
		RuntimeException ex = new RuntimeException();
		RuntimeException ex1 = new RuntimeException(ex);
		System.out.println(fixEndlessLoop(ex1));
	}
}
