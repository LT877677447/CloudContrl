/**
 * @author Administrator
 * 2019年4月23日 下午5:30:03 
 */
package com.kilotrees.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.kilotrees.util.MFGather;

public class FetchMoFangData_Service {
	private static Logger log = Logger.getLogger(FetchMoFangData_Service.class);

	private static FetchMoFangData_Service instance;

	Timer timer;

	private FetchMoFangData_Service() {
	}

	public static FetchMoFangData_Service getInstance() {
		if (instance != null) {
			return instance;
		}
		synchronized (serverinit_service.class) {
			instance = new FetchMoFangData_Service();
		}
		return instance;
	}

	void init() {
		newDayBegin();
		setTimer();
	}

	void newDayBegin() {
		Thread t = new TInitCheckServer();
		t.start();
	}

	class TInitCheckServer extends Thread {
		public void run() {
			main_service.getInstance().addNewThread(this);
			try {
				int cc = 0;
				while (main_service.getInstance().isThreadWork()) {
					new MFGather().main(null);
					
					break;
				}
			} catch (Exception e) {
				ErrorLog_service.system_errlog(e);
				log.error(e.getMessage(), e);
			}
			main_service.getInstance().removeThread(this);
		}
	}

	void setTimer() {
		log.info("设置定时器");
		Calendar date = Calendar.getInstance();
		// 设置时间为 xx-xx-xx 00:00:00
		date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE), 0, 0, 0);
		// 第二天才执行
		date.add(Calendar.DAY_OF_MONTH, 1);
		// 一天的毫秒数
		long daySpan = 24 * 60 * 60 * 1000;
		// 得到定时器实例
		timer = new Timer();
		// 使用匿名内方式进行方法覆盖
		timer.schedule(new TimerTask() {
			public void run() {
				newDayBegin();
			}
		}, date.getTime(), daySpan); // daySpan是一天的毫秒数，也是执行间隔

	}
	
	String fetch(String urlInfo,String charset) throws Exception {
		String str = null;
		 //读取目的网页URL地址，获取网页源码  
        URL url = new URL(urlInfo);  
        HttpURLConnection httpUrl = (HttpURLConnection)url.openConnection();  
        InputStream is = httpUrl.getInputStream();  
        BufferedReader br = new BufferedReader(new InputStreamReader(is,charset));  
        StringBuilder sb = new StringBuilder();  
        String line;  
        while ((line = br.readLine()) != null) {  
            sb.append(line);  
        }  
        br.close();  
        is.close();  
        str = sb.toString();
		return str;
	}
	
	public static String getBody(HttpServletRequest request) throws IOException {  
	    String body = null;  
	    StringBuilder stringBuilder = new StringBuilder();  
	    BufferedReader bufferedReader = null;  
	    try {  
	        InputStream inputStream = request.getInputStream();  
	        if (inputStream != null) {  
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));  
	            char[] charBuffer = new char[128];  
	            int bytesRead = -1;  
	            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {  
	                stringBuilder.append(charBuffer, 0, bytesRead);  
	            }  
	        } else {  
	            stringBuilder.append("");  
	        }  
	    } catch (IOException ex) {  
	        throw ex;  
	    } finally {  
	        if (bufferedReader != null) {  
	            try {  
	                bufferedReader.close();  
	            } catch (IOException ex) {  
	                throw ex;  
	            }  
	        }  
	    }  
	  
	    body = stringBuilder.toString();  
	    return body;  
	} 
	
}
