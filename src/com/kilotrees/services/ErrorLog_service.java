package com.kilotrees.services;

import org.apache.log4j.Logger;

public class ErrorLog_service {
	private static Logger log = Logger.getLogger(ErrorLog_service.class);
	private static ErrorLog_service inst;
	
	public static ErrorLog_service getInstance() {
		synchronized (ErrorLog_service.class) {
			if(inst == null) {
				inst = new ErrorLog_service();
			}
		}
		return inst;
	}
	
	public static boolean system_errlog(String slog)
	{	
//		try {
//			devpostmsgdao.system_errlog(slog);
//			return true;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return false;
	}
	
	public static boolean system_errlog(Exception exception)
	{	
//		try {
//			devpostmsgdao.system_errlog(exception);
//			return true;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return false;
	}
	
	
	
}
