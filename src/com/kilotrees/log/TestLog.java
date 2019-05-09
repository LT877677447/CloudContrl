package com.kilotrees.log;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class TestLog {
	private static Logger log = Logger.getLogger(TestLog.class);
	Timer t;
	static void testfun()
	{
		try{
			//String s = null;
			//int i = s.length();
			Thread.sleep(1000*12);
		}catch(Exception e)
		{
			log.error(e);
		}
	}
	
	void cancelTimer()
	{
		t.cancel();
	}
	
	void setTimer()
	{
		log.info("设置定时器");
        Calendar date = Calendar.getInstance();
        //设置时间为 xx-xx-xx 00:00:00
        date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE), 0, 0, 0);
        //一天的毫秒数
        long daySpan = 2 * 1000;
        //得到定时器实例
        t = new Timer();
        //使用匿名内方式进行方法覆盖
        t.schedule(new TimerTask() {
            public void run() {
            	newDayBegin(true);
                }
            
        }, date.getTime(), daySpan); //daySpan是一天的毫秒数，也是执行间隔
        
	}
	
	void newDayBegin(boolean b)
	{
		log.info("newDayBegin...");
		log.error("newDayBegin...");
	}
	
	
	static void test()
	{
		JSONObject jso = new JSONObject();
		try {
			jso.put("tag", "test_tag123");
			jso.put("phonetype", "华为mate9");
			JSONObject jso1 = null;//new JSONObject("{a:1}");
			jso.put("server_params", jso1);
			System.out.println("--" + jso.toString());
			jso1 = new JSONObject("{b:2}");
			jso.put("server_params", jso1);
			jso1 = new JSONObject();
			jso.put("test", jso1);
			System.out.println(jso.toString());
			System.out.println("jso:len=" + jso.length());
			
			
			System.out.println(jso1.toString() + ":len=" + jso1.length());
//			Map<String, String> map1 = new HashMap<String, String>();
//	        map1.put("name", "Alexia");
//	        map1.put("sex", "female");
//	        map1.put("age", "23");
//	 
//	        Map<String, String> map2 = new HashMap<String, String>();
//	        map2.put("name", "Edward");
//	        map2.put("sex", "male");
//	        map2.put("age", "24");
//	 
//	        // 将Map转换为JSONArray数据
//	        JSONArray jArray = new JSONArray();
//	        jArray.put(map1);
//	        jArray.put(map2);
//	       //定义JSON
//	        JSONObject jObject=new JSONObject();
//	        jObject.put("List", jArray);
//	        jObject.put("Count","1200");
//	         
//	        System.out.println(jObject.toString());
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	static void test1() throws IOException
	{
		Properties pps = new Properties();
		FileInputStream ins = new FileInputStream("d:/1.ini");
		pps.load(ins);
		ins.close();
		String tag = pps.getProperty("tag");
		String url = pps.getProperty("serverurl");
		log.info("tag=" + tag);
		log.info("url=" + url);
		pps.setProperty("serverurl", "http://113");
		OutputStream out = new FileOutputStream("d:/1.ini");
		pps.store(out, "server setting");
		out.close();
	}
	
	public static void main(String[] argv) throws InterruptedException, IOException
	{
		Calendar dateNow = Calendar.getInstance();
		int curHour = dateNow.get(Calendar.HOUR_OF_DAY);
		System.out.println("curHour=" + curHour);
		System.out.println("curMin=" + Calendar.getInstance().get(Calendar.MINUTE));
	//	test();
	//	test1();
		//log.info("",curHour);
//		TestLog app = new TestLog();
//		app.setTimer();
//		
//		
//		Calendar date = Calendar.getInstance();
//		log.info(date.getTime());
//        //设置时间为 xx-xx-xx 00:00:00
//        //date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE), 0, 0, 0);
//        date.add(Calendar.DAY_OF_MONTH, 1);
//        log.info(date.getTime());
//		log.debug("main end");
//		
//		Thread.sleep(1000*10);
//		app.cancelTimer();
		
	}
}
