package com.kilotrees.util;

import java.util.Random;

public class StringUtil {

	public static boolean isStringEmpty(String str) {
		if (str == null || str.length() == 0) {
			return true;
		}
		return false;
	}

	public static String randomString(int length) {
		String str = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		char[] chars = str.toCharArray();
		StringBuilder sb = new StringBuilder(length);
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			char c = chars[random.nextInt(chars.length)];
			sb.append(c);
		}
		return sb.toString();
	}
	
	public static String randomNumber(int length) {
		String str = "0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(str.length());
			sb.append(str.charAt(number));
		}
		return sb.toString();
	}

	/*
	 * 格式化JSON
	 */
	public static String formatJsonString(String string) {
		int level = 0;
		StringBuffer jsonFormatString = new StringBuffer();
		for (int index = 0; index < string.length(); index++) {
			char c = string.charAt(index);
			if (level > 0 && '\n' == jsonFormatString.charAt(jsonFormatString.length() - 1)) {
				jsonFormatString.append(getJsonElementLevelString(level));
			}
			// 遇到"{"和"["要增加空格和换行，遇到"}"和"]"要减少空格，以对应，遇到","要换行
			switch (c) {
			case '{':
			case '[':
				jsonFormatString.append(c + "\n");
				level++;
				break;
			case ',':
				jsonFormatString.append(c + "\n");
				break;
			case '}':
			case ']':
				jsonFormatString.append("\n");
				level--;
				jsonFormatString.append(getJsonElementLevelString(level));
				jsonFormatString.append(c);
				break;
			default:
				jsonFormatString.append(c);
				break;
			}
		}
		return jsonFormatString.toString();
	}

	private static String getJsonElementLevelString(int level) {
		StringBuffer levelString = new StringBuffer();
		for (int levelI = 0; levelI < level; levelI++) {
			levelString.append("\t");
		}
		return levelString.toString();
	}
}
