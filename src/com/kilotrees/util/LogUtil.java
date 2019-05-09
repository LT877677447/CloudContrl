package com.kilotrees.util;

public class LogUtil {
	public static synchronized String log(Throwable e) {
		String s = "::exception begin---\r\n" + e.toString() + "\r\n";
		s += log(e.getStackTrace());
		s += "exception end---\r\n";
		return s;
	}

	private static String log(StackTraceElement[] stack) {
		// synchronized (thLog.class) {
		int i = 0;
		String str = "";
		while (i < stack.length) {
			try {
				StackTraceElement s = stack[i];
				str += "[" + s.getLineNumber() + "]" + s.getFileName() + ":" + s.toString();
				str += "\r\n";
				i++;
			} catch (Exception e) {
				e.printStackTrace();
			}
			// }
			// log("Exception******************************\r\n");
		}
		return str;
	}
}
