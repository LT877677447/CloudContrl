package com.kilotrees.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONArrayUtil {

	public static JSONArray createJSONArrayFromString(String string, String separator) {
		JSONArray results = new JSONArray();
		
		if (string != null) {
			String[] strings = string.split(separator);
			for (String str : strings) {
				results.put(str);
			}
		}

		return results;
	}
	
	
	public static void insert(JSONArray array, Object value, int atIndex) {
		try {
			int length = array.length();
			if (atIndex < length) {
				// move the tails after atIndex
				for (int i = length - 1; i >= atIndex; i--) {
					JSONObject actionJson = array.optJSONObject(i);
					array.put(i + 1, actionJson);
				}
				array.put(atIndex, value);
			} else {
				// just add to tail
				array.put(value);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public static void copy(JSONArray destination, JSONArray source) {
		for (int i = 0; i < source.length(); i++) {
			Object obj = source.opt(i);
			destination.put(obj);
		}
	}

}
