package com.bgsoftware.common.reflection;

import java.lang.reflect.Method;
import java.util.Arrays;

public final class ReflectMethod<T> {

    private final Method method;

    public ReflectMethod(ClassInfo classInfo, String methodName, ClassInfo... parameterTypes) {
        this(classInfo.findClass(), methodName, parameterTypes);
    }

    public ReflectMethod(ClassInfo classInfo, int methodOrder, ClassInfo... parameterTypes) {
        this(classInfo.findClass(), methodOrder, parameterTypes);
    }

    public ReflectMethod(ClassInfo classInfo, String methodName, Class<?>... parameterTypes) {
        this(classInfo.findClass(), methodName, parameterTypes);
    }

    public ReflectMethod(ClassInfo classInfo, int methodOrder, Class<?>... parameterTypes) {
        this(classInfo.findClass(), methodOrder, parameterTypes);
    }

    public ReflectMethod(Class<?> clazz, String methodName) {
        this(clazz, methodName, new Class[0]);
    }

    public ReflectMethod(Class<?> clazz, int methodOrder) {
        this(clazz, methodOrder, new Class[0]);
    }

    public ReflectMethod(Class<?> clazz, String methodName, ClassInfo... parameterTypes) {
        this(clazz, methodName, ClassInfo.findClasses(parameterTypes));
    }

    public ReflectMethod(Class<?> clazz, int methodOrder, ClassInfo... parameterTypes) {
        this(clazz, methodOrder, ClassInfo.findClasses(parameterTypes));
    }

    public ReflectMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        this(clazz, null, methodName, parameterTypes);
    }

    public ReflectMethod(Class<?> clazz, int methodOrder, Class<?>... parameterTypes) {
        this(clazz, null, methodOrder, parameterTypes);
    }

    public ReflectMethod(Class<?> clazz, Class<?> returnType, String methodName, Class<?>... parameterTypes) {
        this.method = getMethodByName(clazz, methodName, returnType, parameterTypes);
    }

    public ReflectMethod(Class<?> clazz, Class<?> returnType, int methodOrder, Class<?>... parameterTypes) {
        this.method = getMethodByIndex(clazz, methodOrder, returnType, parameterTypes);
    }

    public T invoke(Object instance, Object... args) {
        return invokeWithDef(instance, null, args);
    }

    public T invokeWithDef(Object instance, T def, Object... args) {
        Object result = null;

        try {
            if (isValid())
                result = method.invoke(instance, args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //noinspection unchecked
        return result == null ? def : (T) result;
    }

    public boolean isValid() {
        return method != null;
    }

    private static Method getMethodByName(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
        Method method = null;

        if (clazz != null) {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
                if (returnType != null && !returnType.isAssignableFrom(method.getReturnType())) {
                    method = null;
                } else {
                    method.setAccessible(true);
                }
            } catch (Exception ignored) {
            }
        }

        return method;
    }

    private static Method getMethodByIndex(Class<?> clazz, int methodOrder, Class<?> returnType, Class<?>... parameterTypes) {
        if (clazz != null) {
            int similarMethods = 0;

            for (Method method : clazz.getDeclaredMethods()) {
                // Check for similar return type.
                if (returnType != null && !method.getReturnType().equals(returnType))
                    continue;

                // Check for similar parameter types.
                if (!Arrays.equals(method.getParameterTypes(), parameterTypes))
                    continue;

                if (++similarMethods == methodOrder) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }

        return null;
    }

}
