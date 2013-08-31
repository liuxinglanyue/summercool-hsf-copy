package org.summercool.hsf.jmx.management.helper;

import static java.lang.String.format;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.summercool.hsf.jmx.management.annotation.Description;
import org.summercool.hsf.jmx.management.annotation.MBean;
import org.summercool.hsf.jmx.management.annotation.MBean.AutomaticType;
import org.summercool.hsf.jmx.management.annotation.ManagedAttribute;
import org.summercool.hsf.jmx.management.annotation.ManagedOperation;
import org.summercool.hsf.jmx.management.annotation.ManagedOperation.Impact;
import org.summercool.hsf.jmx.management.annotation.Parameter;
import org.summercool.hsf.jmx.management.exception.ManagementException;
import org.summercool.hsf.jmx.util.Objects;

/**
 * A DynamicMBean that can introspect an annotated POJO bean and expose it as a DynamicMBean
 * 
 */
public class IntrospectedDynamicMBean implements DynamicMBean, MBeanRegistration {
	private final Object mbean;
	private final Class<?> mbeanClass;
	private final MBeanRegistration registrationDelegate;
	private final Map<String, PropertyDescriptor> propertyDescriptors;
	private final Map<String, Method> operationMethods;
	private final MBeanInfo mbeanInfo;

	/**
	 * Constructs a Dynamic MBean by introspecting a POJO MBean {@code annotatedMBean}. If {@code mbean} implements
	 * {@link MBeanRegistration}, it will receive callbacks to that interface's methods
	 * 
	 * @param mbean
	 *        a POJO MBean annotated with {@link MBean}, that should be exposed as a {@link DynamicMBean}
	 * @throws ManagementException
	 *         if an exception occurs during the introspection of {@code mbean}
	 * @throws IllegalArgumentException
	 *         if {@code mbean} is not annotated with {@link MBean}
	 */
	public IntrospectedDynamicMBean(Object mbean) throws ManagementException {
		this.mbean = mbean;
		this.mbeanClass = mbean.getClass();
		if (!mbeanClass.isAnnotationPresent(MBean.class)) {
			throw new IllegalArgumentException(format("MBean %s is not annotated with @%s", mbeanClass,
					MBean.class.getName()));
		}
		registrationDelegate = (MBeanRegistration) ((mbean instanceof MBeanRegistration) ? mbean
				: new MBeanRegistrationBase());
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(mbeanClass);
			propertyDescriptors = createPropertyDescriptors(beanInfo);
			operationMethods = createOperationMethods(beanInfo);
			mbeanInfo = createMbeanInfo(mbeanClass, propertyDescriptors, operationMethods);
		} catch (IntrospectionException e) {
			throw new ManagementException(e);
		} catch (java.beans.IntrospectionException e) {
			throw new ManagementException(e);
		}
	}

	/**
	 * @param attribute
	 *        the attribute whose value is requested
	 * @return the reflected value of attribute
	 */
	// @Override commented out for JDK 5 compatibility
	public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(attribute);
		if (propertyDescriptor == null) {
			throw new AttributeNotFoundException(attribute);
		}
		Method getter = propertyDescriptor.getReadMethod();
		if (getter == null) {
			throw new AttributeNotFoundException(format("Getter method for attribute %s of %s", attribute, mbeanClass));
		}
		try {
			if (!getter.isAccessible()) {
				getter.setAccessible(true);
			}
			return getter.invoke(mbean);
		} catch (Exception e) {
			throw new RuntimeException(format("Unable to obtain value of attribute %s of %s", attribute, mbeanClass));
		}
	}

	/**
	 * @param attributeNames
	 *        the attribute names whose values are requested
	 * @return an attribute list describing each of attributeNames
	 */
	// @Override commented out for JDK 5 compatibility
	public AttributeList getAttributes(String[] attributeNames) {
		AttributeList attributes = new AttributeList(attributeNames.length);
		for (String attributeName : attributeNames) {
			try {
				Attribute attribute = new Attribute(attributeName, getAttribute(attributeName));
				attributes.add(attribute);
			} catch (Exception e) {
				// Must be a mistake that the signature doesn't allow throwing exceptions
				throw new IllegalArgumentException(e);
			}
		}
		return attributes;
	}

	/**
	 * @param attribute
	 *        the attribute for which to update the value
	 */
	// @Override commented out for JDK 5 compatibility
	public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException {
		String name = attribute.getName();
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(name);
		if (propertyDescriptor == null) {
			throw new AttributeNotFoundException(name);
		}
		Method setter = propertyDescriptor.getWriteMethod();
		if (setter == null) {
			throw new AttributeNotFoundException(format("setter method for attribute %s of %s", name, mbeanClass));
		}
		Object value = attribute.getValue();
		try {
			if (!setter.isAccessible()) {
				setter.setAccessible(true);
			}
			setter.invoke(mbean, value);
		} catch (IllegalArgumentException e) {
			throw new InvalidAttributeValueException(String.format("attribute %s, value = (%s)%s, expected (%s)", name,
					value.getClass().getName(), value, setter.getParameterTypes()[0].getName()));
		} catch (IllegalAccessException e) {
			throw new ReflectionException(e, format("attribute %s of %s, value = (%s)%s", name, mbeanClass, value
					.getClass().getName(), value));
		} catch (InvocationTargetException e) {
			throw new MBeanException(e, format("attribute %s of %s, value = (%s)%s", name, mbeanClass, value.getClass()
					.getName(), value));
		}
	}

	/**
	 * @param attributes
	 *        a list of attributes for which to update the value
	 */
	// @Override commented out for JDK 5 compatibility
	public AttributeList setAttributes(AttributeList attributes) {
		for (Object object : attributes) {
			Attribute attribute = (Attribute) object;
			try {
				setAttribute(attribute);
			} catch (Exception e) {
				// Must be a mistake that the signature doesn't allow throwing exceptions
				throw new IllegalArgumentException(e);
			}
		}
		// It seems like an API mistake that we have to return the attributes
		return attributes;
	}

	// @Override commented out for JDK 5 compatibility
	public MBeanInfo getMBeanInfo() {
		return mbeanInfo;
	}

	// @Override commented out for JDK 5 compatibility
	public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException,
			ReflectionException {
		Method method = operationMethods.get(actionName);
		// TODO verify that the right signature is picked to avoid throwing an IllegalArgumentException
		try {
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
			return method.invoke(mbean, params);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

	}

	/**
	 * @param mbeanClass
	 *        the class that declares properties and operations
	 * @param propertyDescriptors
	 *        descriptors for all beans that are explicitly or implicitly annotated as attributes
	 * @param mbean
	 *        the annotated POJO MBean
	 * @return an MBeanInfo created by introspecting the {@code mbean}
	 * @throws IntrospectionException
	 * @throws javax.management.IntrospectionException
	 * @throws ManagementException
	 */
	private static MBeanInfo createMbeanInfo(Class<?> mbeanClass, Map<String, PropertyDescriptor> propertyDescriptors,
			Map<String, Method> operationMethods) throws IntrospectionException, ManagementException {
		String description = description(mbeanClass);
		final MBeanAttributeInfo[] attributeInfo = createAttributeInfo(propertyDescriptors);
		final MBeanConstructorInfo[] constructorInfo = createConstructorInfo();
		final MBeanOperationInfo[] operationInfo = createOperationInfo(operationMethods);
		final MBeanNotificationInfo[] notificationInfo = createNotificationInfo();
		return new MBeanInfo(mbeanClass.getName(), description, attributeInfo, constructorInfo, operationInfo,
				notificationInfo);
	}

	/**
	 * TODO should this be implemented?
	 * 
	 * @return null
	 */
	private static MBeanNotificationInfo[] createNotificationInfo() {
		return null;
	}

	/**
	 * TODO: Consider allowing multiple matches for each (overloaded) method name
	 * 
	 * @return The methods that constitute the operations
	 * @throws ManagementException
	 *         if multiple Operation annotations exist on identically named (overloaded) methods
	 */
	private static Map<String, Method> createOperationMethods(BeanInfo beanInfo) throws ManagementException {
		Set<Method> allAccessors = allAccessors(beanInfo);
		Map<String, Method> operationMethods = new HashMap<String, Method>();
		for (MethodDescriptor descriptor : beanInfo.getMethodDescriptors()) {
			Method method = descriptor.getMethod();
			ManagedOperation operationAnnotation = method.getAnnotation(ManagedOperation.class);
			if (operationAnnotation != null && allAccessors.contains(method)) {
				throw new ManagementException(String.format("Accessor method %s is annotated as an @%s", method,
						ManagedOperation.class.getName()));
			}
			/*
			 * consider the Method an automatic operation if it satisfies all of: 1. its class is annotated with
			 * @MBean(automatic=OPERATION) 2. it is a public instance (non static) method 3. it is NOT considered a bean
			 * accessor (getter/setter)
			 */
			boolean isAutomatic = isAutomatic(method.getDeclaringClass(), AutomaticType.OPERATION);
			boolean autoOperation = (isAutomatic && isPublicInstance(method) && !allAccessors.contains(method));
			if (operationAnnotation != null || autoOperation) {
				// This method is an operation
				Method old = operationMethods.put(method.getName(), method);
				if (old != null) {
					throw new ManagementException(format("Multiple Operation annotations for operation %s of %s",
							method.getName(), old.getDeclaringClass()));
				}
			}
		}
		return operationMethods;
	}

	private static boolean isPublicInstance(Method method) {
		int mod = method.getModifiers();
		return Modifier.isPublic(mod) && !Modifier.isStatic(mod);
	}

	private static Set<Method> allAccessors(BeanInfo beanInfo) {
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		Set<Method> accessors = new HashSet<Method>(propertyDescriptors.length * 2);
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			addNotNull(accessors, propertyDescriptor.getReadMethod());
			addNotNull(accessors, propertyDescriptor.getWriteMethod());
		}
		return accessors;
	}

	public static <T> void addNotNull(Collection<T> collection, T element) {
		if (element != null) {
			collection.add(element);
		}
	}

	/**
	 * @return an MBeanOPerationInfo array that describes the {@link ManagedOperation} annotated methods of the
	 *         operationMethods
	 * @throws ManagementException
	 */
	private static MBeanOperationInfo[] createOperationInfo(Map<String, Method> operationMethods)
			throws ManagementException {
		MBeanOperationInfo[] operationInfos = new MBeanOperationInfo[operationMethods.size()];
		int operationIndex = 0;
		// Iterate in method name order
		for (String methodName : sortedKeys(operationMethods)) {
			Method method = operationMethods.get(methodName);
			ManagedOperation annotation = method.getAnnotation(ManagedOperation.class);
			// add description and names to parameters
			MBeanParameterInfo[] signature = createParameterInfo(method);
			// add description and parameter info to operation method
			Impact impact = annotation == null ? Impact.UNKNOWN : annotation.value();
			int impactValue = impact.impactValue;
			String description = description(method);
			MBeanOperationInfo opInfo = new MBeanOperationInfo(method.getName(), description, signature, method
					.getReturnType().getName(), impactValue, null);
			operationInfos[operationIndex++] = opInfo;
		}
		return operationInfos;
	}

	/**
	 * 
	 * @param method
	 *        an operation or attribute getter/setter method
	 * @param autoType
	 *        the type of auto annotation to check for
	 * @return true if {@code method}'s declaring class is annotated with {@link MBean} that includes {@code autoType}
	 *         in its {@link MBean#automatic()} attribute
	 */
	private static boolean isAutomatic(Class<?> clazz, AutomaticType autoType) {
		MBean annotation = clazz.getAnnotation(MBean.class);
		if (annotation == null) {
			return false;
		}
		AutomaticType[] values = annotation.automatic();
		// believe me, this is the fastest way of doing a contains() on this array
		for (AutomaticType value : values) {
			if (value == autoType) {
				return true;
			}
		}
		return false;
	}

	protected static MBeanParameterInfo[] createParameterInfo(Method method) {
		MBeanParameterInfo[] parameters = new MBeanParameterInfo[method.getParameterTypes().length];
		for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex++) {
			final String pType = method.getParameterTypes()[parameterIndex].getName();
			// locate parameter annotation
			Parameter parameter = getParameterAnnotation(method, parameterIndex, Parameter.class);
			Description description = getParameterAnnotation(method, parameterIndex, Description.class);
			final String pName = (parameter != null) ? parameter.value() : "p" + (parameterIndex + 1); // 1 .. n
			final String pDesc = (description != null) ? description.value() : null;
			parameters[parameterIndex] = new MBeanParameterInfo(pName, pType, pDesc);
		}
		return parameters;
	}

	/**
	 * TODO should this be implemented?
	 * 
	 * @return null
	 */
	private static MBeanConstructorInfo[] createConstructorInfo() {
		return null;
	}

	/**
	 * @return all properties where getter or setter is annotated with {@link ManagedAttribute}
	 * @throws ManagementException
	 */
	private static Map<String, PropertyDescriptor> createPropertyDescriptors(BeanInfo beanInfo)
			throws ManagementException {
		Map<String, PropertyDescriptor> properties = new HashMap<String, PropertyDescriptor>();
		for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
			ManagedAttribute getterAnnotation = getAnnotation(property.getReadMethod(), ManagedAttribute.class);
			ManagedAttribute setterAnnotation = getAnnotation(property.getWriteMethod(), ManagedAttribute.class);
			if (isAutomatic(property) || getterAnnotation != null || setterAnnotation != null) {
				properties.put(property.getName(), property);
			}
		}
		return properties;
	}

	/**
	 * 
	 * @param propertyDescriptors
	 *        property descriptors that are known to have at least one {@link ManagedAttribute} annotation on its getter
	 *        or setter method
	 * @return MBean attributeInfo instances with getter/setter methods and description according to annotations
	 * @throws ManagementException
	 * @throws IntrospectionException
	 */
	private static MBeanAttributeInfo[] createAttributeInfo(Map<String, PropertyDescriptor> propertyDescriptors)
			throws ManagementException, IntrospectionException {
		MBeanAttributeInfo[] infos = new MBeanAttributeInfo[propertyDescriptors.size()];
		int i = 0;
		// iterate over properties that are known to have ManagedAttribute annotations, sorted by name
		for (String propertyName : sortedKeys(propertyDescriptors)) {
			PropertyDescriptor property = propertyDescriptors.get(propertyName);
			boolean isAutomatic = isAutomatic(property);
			Method readMethod = property.getReadMethod();
			Method writeMethod = property.getWriteMethod();
			boolean readable = isAutomatic || (null != getAnnotation(readMethod, ManagedAttribute.class));
			boolean writable = isAutomatic || (null != getAnnotation(writeMethod, ManagedAttribute.class));
			Description descriptionAnnotation = getSingleAnnotation(property, Description.class, readMethod,
					writeMethod);
			String description = (descriptionAnnotation != null) ? descriptionAnnotation.value() : null;
			MBeanAttributeInfo info = new MBeanAttributeInfo(property.getName(), description, readable ? readMethod
					: null, writable ? writeMethod : null);
			infos[i++] = info;
		}
		return infos;
	}

	/**
	 * 
	 * @param property
	 * @return true if the declaring class is marked {@link MBean#automatic()} with {@link AutomaticType#OPERATION}
	 */
	private static boolean isAutomatic(PropertyDescriptor property) {
		Method accessor = Objects.firstNotNull(property.getReadMethod(), property.getWriteMethod());
		boolean isAutomatic = isAutomatic(accessor.getDeclaringClass(), AutomaticType.ATTRIBUTE);
		return isAutomatic;
	}

	/**
	 * 
	 * @param <T>
	 * @param property
	 *        The property to which entities belong
	 * @param annotationClass
	 *        Annotation type
	 * @param entities
	 *        A number of {@code Method}'s or {@code null}'s
	 * @return The one (and only) annotation of type {@code annotationClass} that appears on {@code methods}, or null if
	 *         none of the entities are annotated with annotationClass
	 * @throws ManagementException
	 *         if more than one of the entities are annotated with annotationClass
	 */
	private static <T extends Annotation> T getSingleAnnotation(PropertyDescriptor property, Class<T> annotationClass,
			AccessibleObject... entities) throws ManagementException {
		T result = null;
		for (AccessibleObject entity : entities) {
			if (entity != null) {
				T annotation = entity.getAnnotation(annotationClass);
				if (annotation != null) {
					if (result != null) {
						throw new ManagementException(String.format("Multiple %s annotations found for property %s",
								annotationClass.getName(), property.getName()));
					}
					result = annotation;
				}
			}
		}
		return result;
	}

	/**
	 * Find an annotation for a parameter on a method.
	 * 
	 * @param <A>
	 *        The annotation.
	 * @param method
	 *        The method.
	 * @param index
	 *        The index (0 .. n-1) of the parameter in the parameters list
	 * @param annotationClass
	 *        The annotation class
	 * @return The annotation, or null
	 */
	private static <A extends Annotation> A getParameterAnnotation(Method method, int index, Class<A> annotationClass) {
		for (Annotation a : method.getParameterAnnotations()[index]) {
			if (annotationClass.isInstance(a)) {
				return annotationClass.cast(a);
			}
		}
		return null;
	}

	/**
	 * Null safe annotation checker
	 * 
	 * @param <A>
	 * @param element
	 *        element or null
	 * @param annotationClass
	 * @return the annotation, if element is not null and the annotation is present. Otherwise null
	 */
	private static <A extends Annotation> A getAnnotation(AnnotatedElement element, Class<A> annotationClass) {
		return (element != null) ? element.getAnnotation(annotationClass) : null;
	}

	private static String description(AnnotatedElement element) {
		Description annotation = element.getAnnotation(Description.class);
		String explicitValue = (annotation != null) ? annotation.value() : null;
		if (explicitValue != null && !explicitValue.isEmpty()) {
			return explicitValue;
		} else {
			return generatedDescription(element);
		}
	}

	private static String generatedDescription(AnnotatedElement element) {
		if (element instanceof Method) {
			Method method = (Method) element;
			return method.getName() + "() of " + method.getDeclaringClass().getSimpleName();
		} else if (element instanceof Class) {
			return "class " + ((Class<?>) element).getName();

		}
		return element.toString();
	}

	/**
	 * @param map
	 * @return a list of the keys in map, sorted
	 */
	private static List<String> sortedKeys(Map<String, ?> map) {
		List<String> keys = new ArrayList<String>(map.keySet());
		Collections.sort(keys);
		return keys;
	}

	public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
		return registrationDelegate.preRegister(server, name);
	}

	public void postRegister(Boolean registrationDone) {
		registrationDelegate.postRegister(registrationDone);
	}

	public void postDeregister() {
		registrationDelegate.postDeregister();
	}

	public void preDeregister() throws Exception {
		registrationDelegate.preDeregister();
	}
}
