package com.kilotrees.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectUtil {
	
    public static Object getFieldValue(Object obj, String fieldName) {
        Boolean isClass = obj instanceof Class;
        Class<?> clazz = isClass ? (Class<?>) obj : obj.getClass();

        Field field = null;
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }

        try {
            // clazz now may be super class now
            Object value = field.get(Modifier.isStatic(field.getModifiers()) ? clazz : obj);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
