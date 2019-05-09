package com.kilotrees.util;

import java.util.Date;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;   
/**
 * 大点击用到的友盟基础算法
 * @author Administrator
 *
 */
public class httplinks_um {
	static int get_umuuid_b_count = 10;
	static int min_umuuid_b;
	static int max_umuuid_b;
	//static ScriptEngineManager manager;
	static ScriptEngineManager manager = new ScriptEngineManager();   
	static ScriptEngine engine = manager.getEngineByName("javascript");	
	static Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);  
	
	public static void init()
	{
		//初始化javascript引擎，在系统初始化时，加载
		create_umuuid("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36",360,640);
	}
	public static String create_cnzz_eid()
	{
		String cnzz_eid = "";
		long seed = Long.parseLong("2147483648");
		cnzz_eid += (int)Math.floor(seed * Math.random());
		
		cnzz_eid += "-";
		cnzz_eid += new java.util.Date().getTime()/1000;
		cnzz_eid += "-";
		return cnzz_eid;
	}
	
	public static String create_umuuid(String user_agent,int screen_w,int screen_h)
	{
		String umuuid = "";
		String stra = get_umuuid_a_value();		
		String strb = javascript("Math.random().toString(16).replace(\".\", \"\")");
		String strc = get_umuuid_c_value(user_agent);
		String strd = Integer.toHexString(screen_w * screen_h);
		String stre = get_umuuid_a_value();	
		//strb = strb.replace(".", "");		
		umuuid = stra + "-" + strb + "-" + strc + "-" + strd + "-" + stre;
		return umuuid;
	}
	
	static String get_umuuid_c_value(String ua)
	{		
		String script ="function a() {\n"
				+ "function a(a, b) {\n"
				+ "var c, d = 0;\n"
				+ "for (c = 0; c < b.length; c++)\n"
				+ " d |= h[c] << 8 * c;\n"
				+ "return a ^ d\n"
				+ "}\n"
				+ "var f, g, h = [], k = 0;\n"
				+ "for (f = 0; f < b.length; f++)\n"
				+ "g = b.charCodeAt(f),\n"
				+ "h.unshift(g & 255),\n"
				+ "4 <= h.length && (k = a(k, h),\n"
				+ " h = []);\n"
				+ " 0 < h.length && (k = a(k, h));\n"
				+ "  return k.toString(16)\n"
				+ "}";
		engine.put("b", ua);
		String result = "";
		try {
			engine.eval(script);
			Invocable invocable = (Invocable) engine;
			result = (String) invocable.invokeFunction("a", null);
		} catch (ScriptException | NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return result;
	}
	
	static String get_umuuid_a_value()
	{
		String result = "";
		long a = new Date().getTime();
		int b = get_umuuid_aofb_value();		
		result = Long.toHexString(a) + Integer.toHexString(b);
		return result;
	}
	
	synchronized static int get_umuuid_aofb_value()
	{		
		int b = 0;
		if(get_umuuid_b_count <= 0)
		{	
			//System.out.println("get_umuuid_b_value random min and max");
			int diff = max_umuuid_b - min_umuuid_b;
			java.util.Random rand = new java.util.Random();
			b = min_umuuid_b + rand.nextInt(diff);
			return b;
		}
		get_umuuid_b_count--;
		
		long now = new Date().getTime();
		while(new Date().getTime() - now < 1)
		{
			b++;			
		}
		if(min_umuuid_b == 0)
			min_umuuid_b = b;
		else if(min_umuuid_b > b)
			min_umuuid_b = b;
		if(max_umuuid_b == 0)
			max_umuuid_b = b;
		else if(max_umuuid_b < b)
			max_umuuid_b = b;
		return b;
	}
	
	static String javascript(String script)
	{
		String result = "";
		
		try {			
			result = (String) engine.eval(script);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		//System.out.println(result);
		return result;
	}
	
	public static void main(String[] argv)
	{
		long now = new Date().getTime();
		//System.out.println("start" + now);
		init();
		System.out.println("end1:" + (new Date().getTime() - now));
		now = new Date().getTime();
		init();
		System.out.println("end2:" + (new Date().getTime() - now));
		//for(int i = 0; i < 15; i++)
		//	System.out.println(greate_umuuid(1,1));
		
		//String r = get_umuuid_c_value("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36");
		//System.out.println(r);
	}
}
