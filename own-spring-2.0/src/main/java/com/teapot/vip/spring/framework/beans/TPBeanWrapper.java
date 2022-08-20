package com.teapot.vip.spring.framework.beans;

public class TPBeanWrapper {

    private Object wrappedInstance;
    private Class<?> wrappedClass;

    public TPBeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    public Object getWrappedInstance() {
        return this.wrappedInstance;
    }

    public Class<?> getWrappedClass() {
        this.wrappedClass = this.wrappedInstance.getClass();
        return this.wrappedClass;
    }

}
