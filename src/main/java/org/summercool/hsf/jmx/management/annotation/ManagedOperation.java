package org.summercool.hsf.jmx.management.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for MBean operation methods.<p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ManagedOperation {
    /**
     * The impact of this operation
     */
    public enum Impact {INFO(0), ACTION(1), ACTION_INFO(2), UNKNOWN(3);
        public final int impactValue;
        private Impact(int impactValue) {
            this.impactValue = impactValue;
        }
    }

    /**
     * @resturn The impact of this operation
     */
    Impact value() default Impact.UNKNOWN;
}
