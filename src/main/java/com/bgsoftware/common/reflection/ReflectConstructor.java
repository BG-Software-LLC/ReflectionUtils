package com.bgsoftware.common.reflection;

import java.lang.reflect.Constructor;

public final class ReflectConstructor<T> {

    private final Constructor<?> constructor;

    public ReflectConstructor(Class<?> clazz, ClassInfo... parameterTypes) {
        this(clazz, ClassInfo.findClasses(parameterTypes));
    }

    public ReflectConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        this.constructor = getConstructor(clazz, parameterTypes);
    }

    public T newInstance(Object... args) {
        Object result = null;

        try {
            if (isValid())
                result = constructor.newInstance(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //noinspection unchecked
        return result == null ? null : (T) result;
    }

    public boolean isValid() {
        return constructor != null;
    }

    private static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        Constructor<?> constructor = null;

        if (clazz != null) {
            try {
                constructor = clazz.getDeclaredConstructor(parameterTypes);
                constructor.setAccessible(true);
            } catch (Exception ignored) {
            }
        }

        return constructor;
    }

}
