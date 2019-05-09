package com.kilotrees.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONObjectUtil {

	public static JSONObject copy(JSONObject json) {
		JSONObject result = new JSONObject();
		Iterator<?> iterator = json.keys();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			Object value = json.opt(key);
			try {
				result.put(key, value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static JSONObject getNewJsonWithoutKey(JSONObject source, String keyToRemove) {
		JSONObject result = copy(source);
		result.remove(keyToRemove);
		return result;
	}

	/**
	 * 把source放到destination中
	 */
	public static void mergeJSONObject(JSONObject destination, JSONObject source) {
		Iterator<?> iteratorOne = source.keys();
		while (iteratorOne.hasNext()) {
			try {
				String name = (String) iteratorOne.next();
				Object value = source.opt(name);
				destination.put(name, value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 遍历source，把所有key以prefix开头的放到新的JSONObject中返回
	 */
	public static JSONObject optJSONWithKeyPrefix(JSONObject source, String prefix) {
		JSONObject result = new JSONObject();
		try {
			Iterator<?> iterator = source.keys();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				if (key.startsWith(prefix)) {
					Object value = source.get(key);
					result.put(key, value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void removeWithKeyPrefix(JSONObject source, String prefix) {
		try {
			Iterator<?> iterator = source.keys();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				if (key.startsWith(prefix)) {
					iterator.remove();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Object To JSON
	 */
	public interface FieldFilter {
		boolean filterAction(Object obj, Field field);
	}

	public static JSONObject objectToJSONObject(Object object, int superClassDepth, FieldFilter filter) {
		Map<?, ?> map = objectFieldNameValues(superClassDepth, object, filter);
		JSONObject json = new JSONObject(map);
		return json;
	}

	public static Map<?, ?> objectFieldNameValues(int superClassDepth, Object object, FieldFilter fieldFilter) {
		Map<String, Object> result = new HashMap<String, Object>();

		if (object == null) {
			return result;
		}

		Boolean isClass = object instanceof Class;
		Class<?> clazz = isClass ? (Class<?>) object : object.getClass();

		do {

			Field[] fields = clazz.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				try {
					Field field = fields[i];
					if (fieldFilter != null && fieldFilter.filterAction(object, field)) {
						continue;
					}
					field.setAccessible(true);

					String name = field.getName();
					Object value = field.get(Modifier.isStatic(field.getModifiers()) ? clazz : object);

					if (value == null) {
						value = "";
					}
					result.put(name, value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// get super class fields
			superClassDepth--;
			clazz = clazz.getSuperclass();

		} while (superClassDepth >= 0 && clazz != null);

		return result;
	}

	/**从JSON中拿出object直到TaskBase类的所有属性，如果有就设置
	 * @param superClassDepth object和TaskBase差的级数
	 * @param object TaskBase子类对象
	 * @param json 要设置给object的JSON对象
	 */
	public static void setJSONObjectToObject(int superClassDepth, Object object, JSONObject json) {
		if (object == null || json == null) {
			return;
		}

		Boolean isClass = object instanceof Class;
		Class<?> clazz = isClass ? (Class<?>) object : object.getClass();

		do {

			Field[] fields = clazz.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				field.setAccessible(true);

				String name = field.getName();
				Object value = json.opt(name);

				if (value != null) {
					try {
						// set the value to field
						field.set(object, value);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			// get super class fields
			superClassDepth--;
			clazz = clazz.getSuperclass();

		} while (superClassDepth >= 0 && clazz != null);

	}

}
