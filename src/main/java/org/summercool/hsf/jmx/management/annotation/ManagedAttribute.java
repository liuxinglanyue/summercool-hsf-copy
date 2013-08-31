package org.summercool.hsf.jmx.management.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for an MBean attributes.<p>
 * 
 * May be applied to one or both of: <ul> <li>An MBean attribute getter: getXxx(), or boolean isXxx()</li> <li>An MBean
 * attribute setter: setXxx()</li> </ul>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ManagedAttribute {

}
