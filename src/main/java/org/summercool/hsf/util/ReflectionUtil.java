package org.summercool.hsf.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.reflect.MethodUtils;
import org.summercool.hsf.exception.HsfOperationException;

/**
 * @Title: ReflectionUtil.java
 * @Package org.summercool.hsf.util
 * @Description: Reflection相关辅助类
 * @author 简道
 * @date 2011-9-17 上午9:28:03
 * @version V1.0
 */
public class ReflectionUtil {
	/**
	 * @Title: invoke
	 * @Description: 方法调用
	 * @author 简道
	 * @param object
	 *        调用者
	 * @param methodName
	 *        方法名
	 * @param args
	 *        参数
	 * @return Object 返回类型
	 */
	public static Object invoke(Object object, String methodName, Object... args) {
		if (object == null) {
			throw new IllegalArgumentException("object can not be null.");
		}
		if (methodName == null || "".equals(methodName)) {
			throw new IllegalArgumentException("methodName can not be null or empty.");
		}

		// 获取参数类型
		Class<?>[] parameterTypes;
		if (args == null || args.length == 0) {
			parameterTypes = new Class[0];
		} else {
			parameterTypes = new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				if (args[i] != null) {
					parameterTypes[i] = args[i].getClass();
				}
			}
		}

		try {
			Method method = MethodUtils.getMatchingAccessibleMethod(object.getClass(), methodName, parameterTypes);

			if (method == null) {
				throw new NoSuchMethodException("class " + object.getClass() + " has no method with name " + methodName
						+ " matchs parameters:" + args);
			}

			return method.invoke(object, args);
		} catch (Exception e) {
			throw new HsfOperationException(e.getMessage(), e);
		}
	}

	/**
	 * @Title: invoke
	 * @Description: 方法调用
	 * @author 简道
	 * @param object
	 *        调用者
	 * @param method
	 *        方法
	 * @param args
	 *        参数
	 * @return Object 返回类型
	 */
	public static Object invoke(Object object, Method method, Object... args) {
		try {
			return method.invoke(object, args);
		} catch (Exception e) {
			throw new HsfOperationException(e.getMessage(), e);
		}
	}

	/**
	 * @Title: getDefaultValue
	 * @Description: 获取指定类型默认值
	 * @author 简道
	 * @param clazz
	 *        类型
	 * @return Object 返回类型
	 */
	public static Object getDefaultValue(Class<?> clazz) {
		if (clazz.isPrimitive()) {
			if (byte.class.equals(clazz)) {
				return (byte) 0;
			} else if (short.class.equals(clazz)) {
				return (short) 0;
			} else if (int.class.equals(clazz)) {
				return 0;
			} else if (long.class.equals(clazz)) {
				return 0L;
			} else if (float.class.equals(clazz)) {
				return 0F;
			} else if (double.class.equals(clazz)) {
				return 0D;
			} else if (char.class.equals(clazz)) {
				return (char) 0;
			} else if (boolean.class.equals(clazz)) {
				return false;
			}
		}

		return null;
	}

	/**
	 * @Title: isMatch
	 * @Description: 验证参数与类型是否匹配
	 * @author 简道
	 * @param params
	 *        参数集合
	 * @param paramTypes
	 *        参数类型集合
	 * @return boolean 返回类型
	 */
	public static boolean isMatch(Object[] params, Class<?>... paramTypes) {
		if (params == null) {
			return paramTypes == null;
		} else if (paramTypes == null) {
			return false;
		}

		if (params.length == paramTypes.length) {
			for (int i = 0; i < params.length; i++) {
				if (!isMatch(params[i], paramTypes[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * @Title: validate
	 * @Description: 验证参数与类型是否匹配
	 * @author 简道
	 * @param param
	 *        参数
	 * @param paramType
	 *        参数类型
	 * @return boolean 返回类型
	 */
	public static boolean isMatch(Object param, Class<?> paramType) {
		if (param == null) {
			if (paramType != null) {
				return !paramType.isPrimitive();
			}
			return true;
		}

		if (paramType == null) {
			throw new NullPointerException("paramType can't be null.");
		}

		boolean match = paramType.isInstance(param);

		if (!match) {
			if (param.getClass().isArray()) {
				return isMatch((Object[]) param, new Class<?>[] { paramType });
			} else if (byte.class.equals(paramType)) {
				match = Byte.class.isInstance(param);
			} else if (short.class.equals(paramType)) {
				match = Short.class.isInstance(param);
			} else if (int.class.equals(paramType)) {
				match = Integer.class.isInstance(param);
			} else if (long.class.equals(paramType)) {
				match = Long.class.isInstance(param);
			} else if (float.class.equals(paramType)) {
				match = Float.class.isInstance(param);
			} else if (double.class.equals(paramType)) {
				match = Double.class.isInstance(param);
			} else if (char.class.equals(paramType)) {
				match = Character.class.isInstance(param);
			} else if (boolean.class.equals(paramType)) {
				match = Boolean.class.isInstance(param);
			}
		}
		return match;
	}

	/**
	 * @Title: getField
	 * @Description: 获取指定名称的Field
	 * @author 简道
	 * @param clazz
	 * @param fdName
	 * @throws NoSuchFieldException
	 *         设定文件
	 * @return Field 返回类型
	 */
	public static Field getField(Class<?> clazz, String fdName) throws NoSuchFieldException {
		Class<?> cls = clazz;
		while (cls != null) {
			try {
				Field fd = cls.getDeclaredField(fdName);
				return fd;
			} catch (Throwable t) {
			} finally {
				cls = cls.getSuperclass();
			}
		}

		throw new NoSuchFieldException(fdName);
	}

	/**
	 * @Title: getFields
	 * @Description: 获取所有字段
	 * @author 简道
	 * @param clazz
	 * @return List<Field> 返回类型
	 */
	public static List<Field> getFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<Field>();

		Class<?> cls = clazz;
		while (cls != null) {
			try {
				Field[] fdArray = cls.getDeclaredFields();
				for (Field field : fdArray) {
					fields.add(field);
				}
			} catch (Throwable t) {
			} finally {
				cls = cls.getSuperclass();
			}
		}

		return fields;
	}
}
