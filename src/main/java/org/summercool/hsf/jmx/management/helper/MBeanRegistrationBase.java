package org.summercool.hsf.jmx.management.helper;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Null implementation of an MBeanRegistration. Use as a dummy, or subclass at will
 * 
 */
public class MBeanRegistrationBase implements MBeanRegistration {

	public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
		return name;
	}

	public void postRegister(Boolean registrationDone) {

	}

	public void postDeregister() {

	}

	public void preDeregister() throws Exception {

	}
}
