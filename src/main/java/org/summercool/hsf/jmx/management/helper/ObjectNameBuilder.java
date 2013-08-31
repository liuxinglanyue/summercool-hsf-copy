package org.summercool.hsf.jmx.management.helper;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.summercool.hsf.jmx.management.annotation.MBean;

public class ObjectNameBuilder {
    private static final String KEY_NAME = "name";
    private static final String KEY_TYPE = "type";
    private static final String KEY_APPLICATION = "application";

    private ObjectName objectName;
    private String domain;
    private Map<String, String> properties = new LinkedHashMap<String, String>();

    public ObjectNameBuilder(Class<?> mBeanClass) throws MalformedObjectNameException {
        MBean annotation = mBeanClass.getAnnotation(MBean.class);
        if (annotation == null) {
            throw new IllegalArgumentException(mBeanClass + " is not annotated with " + MBean.class);
        }
        this.objectName = ObjectName.getInstance(annotation.objectName());
    }

    public ObjectNameBuilder(String domain) {
        this.domain = domain;
    }

    public ObjectNameBuilder(String domain, Map<String, String> properties) {
        this.domain = domain;
        this.properties.putAll(properties);
    }

    public ObjectNameBuilder(ObjectName objectName) {
        this.objectName = objectName;
    }
    private void normalize() {
        if (objectName != null) {
            this.domain = objectName.getDomain();
            this.properties.putAll(objectName.getKeyPropertyList());
            this.objectName = null;
        }
    }

    public ObjectNameBuilder withName(String name) {
        return withProperty(KEY_NAME, name);
    }

    public ObjectNameBuilder withType(String type) {
        return withProperty(KEY_TYPE, type);
    }

    public ObjectNameBuilder withApplication(String app) {
        return withProperty(KEY_APPLICATION, app);
    }

    public ObjectNameBuilder withProperty(String key, String value) {
        normalize();
        properties.put(key, value);
        return this;
    }

    public ObjectNameBuilder withDomain(String domain) {
        normalize();
        this.domain = domain;
        return this;
    }
    public ObjectName build() throws MalformedObjectNameException {
        return (objectName != null) ? objectName
                : ObjectName.getInstance(domain, new Hashtable<String, String>(properties));
    }
}
