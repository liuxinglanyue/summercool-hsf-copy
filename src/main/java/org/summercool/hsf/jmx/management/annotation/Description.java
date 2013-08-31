package org.summercool.hsf.jmx.management.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds a description to the feature being annotation. Note that when applied to getter/setter ({@link ManagedAttribute}
 * ) methods the description should describe the attribute, not the getter/setter method, and therefore the
 * {@link Description} annotation should be added to only one, since a description annotation added to both getter and
 * setter methods would cause competing attribute descriptions which may result in indeterminate behavior or cause an
 * error during introspection.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER })
public @interface Description {
	/**
	 * @return The descriptive text of the MBean feature being annotated ({@link MBean}, {@link ManagedAttribute},
	 *         {@link ManagedOperation} or {@link Parameter})
	 */
	String value();
}
