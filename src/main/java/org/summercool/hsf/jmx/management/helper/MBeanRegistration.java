package org.summercool.hsf.jmx.management.helper;

import java.lang.management.ManagementFactory;

import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.summercool.hsf.jmx.management.exception.ManagementException;

/**
 * 
 * This class assists in registering MBeans with an MBeanServer.<p>
 * 
 * This class, unfortunately, has a name that may cause confusion, since it doesn't implement the
 * {@link javax.management.MBeanRegistration} interface.
 * 
 */
public class MBeanRegistration {
	private final Object mBean;
	private final ObjectName mBeanObjectName;
	private final MBeanServer mBeanServer;

	/**
	 * @param mBean
	 *        an MBean instance annotated with {@link @MBean} containing an objectName attribute
	 * @throws MalformedObjectNameException
	 */
	public MBeanRegistration(Object mBean) throws MalformedObjectNameException {
		this(mBean, new ObjectNameBuilder(mBean.getClass()).build());
	}

	/**
	 * @param mBean
	 *        an MBean instance in the form of a traditional MBean (implementing a sibling *MBean interface) or an
	 *        MXBean (implementing an interface annotated with {@code @MXBean}), or an instance implementing the
	 *        DynamicMBean interface.
	 * @param mBeanObjectName
	 *        the object name with which {@code mBean} will be registered
	 */
	public MBeanRegistration(Object mBean, ObjectName mBeanObjectName) {
		this.mBean = mBean;
		this.mBeanObjectName = mBeanObjectName;
		mBeanServer = ManagementFactory.getPlatformMBeanServer();
	}

	/**
	 * Register the MXBean. If the registration fails, a WARN message is logged
	 * 
	 * @throws java.beans.IntrospectionException
	 * @throws IntrospectionException
	 * @throws NotCompliantMBeanException
	 * @throws MBeanRegistrationException
	 * @throws InstanceAlreadyExistsException
	 */
	public void register() throws ManagementException {
		try {
			DynamicMBean dynamicMBean = new IntrospectedDynamicMBean(mBean);
			mBeanServer.registerMBean(dynamicMBean, mBeanObjectName);
		} catch (Exception e) {
			throw new ManagementException(e);
		}
	}

	/**
	 * Unregister the MXBean. If the unregistration fails, a WARN message is logged
	 * 
	 * @throws InstanceNotFoundException
	 * @throws MBeanRegistrationException
	 */
	public void unregister() throws ManagementException {
		try {
			mBeanServer.unregisterMBean(mBeanObjectName);
		} catch (Exception e) {
			throw new ManagementException(e);
		}
	}

}
