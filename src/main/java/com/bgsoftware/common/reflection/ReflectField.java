package com.bgsoftware.common.reflection;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class ReflectField<T> {

    private static final ReflectField<Integer> MODIFIERS = new ReflectField<>(Field.class, int.class, "modifiers");

    private static Unsafe UNSAFE = null;

    private static final long INVALID_FIELD_OFFSET = -1;

    private final FieldWrapper<T> fieldWrapper;

    public ReflectField(ClassInfo classInfo, Class<?> returnType, String... fieldNames) {
        this(classInfo.findClass(), returnType, fieldNames);
    }

    public ReflectField(ClassInfo classInfo, Class<?> returnType, int modifiers, int fieldOrder) {
        this(classInfo.findClass(), returnType, modifiers, fieldOrder);
    }

    public ReflectField(Class<?> clazz, Class<?> returnType, String... fieldNames) {
        this(getFieldByName(clazz, returnType, fieldNames));
    }

    public ReflectField(Class<?> clazz, Class<?> returnType, int modifiers, int fieldOrder) {
        this(getFieldByIndex(clazz, returnType, modifiers, fieldOrder));
    }

    private ReflectField(Field field) {
        if (field == null) {
            this.fieldWrapper = null;
        } else if (!Modifier.isFinal(field.getModifiers())) {
            this.fieldWrapper = new RegularFieldWrapper<>(field);
        } else if (Modifier.isStatic(field.getModifiers())) {
            this.fieldWrapper = new StaticFinalFieldWrapper<>(field);
        } else {
            this.fieldWrapper = new FinalFieldWrapper<>(field);
        }
    }

    public ReflectField<T> removeFinal() {
        if (isValid())
            this.fieldWrapper.removeFinal();

        return this;
    }

    public T get(Object instance) {
        return get(instance, null);
    }

    public T get(Object instance, T def) {
        Object result = null;

        try {
            if (isValid())
                result = this.fieldWrapper.get(instance);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //noinspection unchecked
        return result == null ? def : (T) result;
    }

    public void set(Object instance, T value) {
        try {
            if (isValid()) {
                this.fieldWrapper.set(instance, value);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isValid() {
        return this.fieldWrapper != null;
    }

    private static Field getFieldByName(Class<?> clazz, Class<?> returnType, String... fieldNames) {
        Field field = null;

        if (clazz != null) {
            for (String fieldName : fieldNames) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                    if (returnType == null || returnType.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return field;
    }

    private static Field getFieldByIndex(Class<?> clazz, Class<?> returnType, int modifiers, int fieldOrder) {
        if (clazz != null) {
            int similarFields = 0;

            for (Field field : clazz.getDeclaredFields()) {
                // Check for modifiers.
                if (modifiers != field.getModifiers())
                    continue;

                // Check for similar return type.
                if (returnType != null && !returnType.isAssignableFrom(field.getType()))
                    continue;

                if (++similarFields == fieldOrder) {
                    field.setAccessible(true);
                    return field;
                }
            }
        }

        return null;
    }

    private static Unsafe getUnsafe() {
        if (UNSAFE == null) {
            UNSAFE = new ReflectField<Unsafe>(Unsafe.class, Unsafe.class, "theUnsafe").get(null);
        }

        return UNSAFE;
    }

    private static abstract class FieldWrapper<T> {

        protected final Field field;

        FieldWrapper(Field field) {
            this.field = field;
        }

        abstract void set(Object object, T value) throws IllegalAccessException;

        abstract boolean isValid();

        abstract void removeFinal();

        Object get(Object object) throws IllegalAccessException {
            return field.get(object);
        }

    }

    private static final class StaticFinalFieldWrapper<T> extends FieldWrapper<T> {

        private long fieldOffset = INVALID_FIELD_OFFSET;
        private Object fieldBase = null;
        private boolean calledRemoveFinal = false;

        StaticFinalFieldWrapper(Field field) {
            super(field);
        }

        @Override
        void set(Object object, T value) {
            if (isValid())
                getUnsafe().putObject(this.fieldBase, this.fieldOffset, value);
            else if(!this.calledRemoveFinal)
                new IllegalStateException("Trying to set final-field without calling #removeFinal first").printStackTrace();
        }

        @Override
        boolean isValid() {
            return this.fieldOffset != INVALID_FIELD_OFFSET && this.fieldBase != null;
        }

        @Override
        void removeFinal() {
            this.fieldOffset = getUnsafe().staticFieldOffset(field);
            this.fieldBase = getUnsafe().staticFieldBase(field);
            this.calledRemoveFinal = true;
        }

    }

    private static final class FinalFieldWrapper<T> extends FieldWrapper<T> {

        private long fieldOffset = INVALID_FIELD_OFFSET;
        private boolean calledRemoveFinal = false;

        FinalFieldWrapper(Field field) {
            super(field);
        }

        @Override
        void set(Object object, T value) {
            if (isValid())
                getUnsafe().putObject(object, this.fieldOffset, value);
            else if(!this.calledRemoveFinal)
                new IllegalStateException("Trying to set final-field without calling #removeFinal first").printStackTrace();
        }

        @Override
        boolean isValid() {
            return this.fieldOffset != INVALID_FIELD_OFFSET;
        }

        @Override
        void removeFinal() {
            this.fieldOffset = getUnsafe().objectFieldOffset(field);
            this.calledRemoveFinal = true;
        }

    }

    private static final class RegularFieldWrapper<T> extends FieldWrapper<T> {

        RegularFieldWrapper(Field field) {
            super(field);
        }

        @Override
        void set(Object object, T value) throws IllegalAccessException {
            if (isValid())
                field.set(object, value);
        }

        @Override
        boolean isValid() {
            return true;
        }

        @Override
        void removeFinal() {
            MODIFIERS.set(field, MODIFIERS.get(field) & ~Modifier.FINAL);
        }

    }

}
