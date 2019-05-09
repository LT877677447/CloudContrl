package com.kilotrees.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.model.po.ServerConfig;
/**
 * 生成配置json文件
 * @author Administrator
 *
 */
public class configJson {
	static JSONObject jsonConfig;
	
	static String getJsonPath() {
		return ServerConfig.contextRealPath + "/servconfig.json";
	}
	
	static void loadJsonFile() throws IOException, JSONException
	{
		String jsonFile = getJsonPath();
		File f = new File(jsonFile);
		if(f.exists())
		{
			FileInputStream fins = new FileInputStream(f);
			byte[] buf = new byte[(int) f.length()];
			fins.read(buf);
			fins.close();
			String s = new String(buf,"utf-8");
			jsonConfig = new JSONObject(s);
		}
		else
			jsonConfig = new JSONObject();
	}
	
	static void saveJsonFile() throws IOException
	{
		String jsonFile = getJsonPath();
		String sJson = jsonConfig.toString();
		FileOutputStream fos = new FileOutputStream(jsonFile);
		byte[] buf = sJson.getBytes("utf-8");
		fos.write(buf);
		fos.close();
	}
	/**
	 * 添加易码验证码平台
	 * @throws JSONException 
	 * @throws IOException 
	 */
	static void setyimaSmsPlatform() throws IOException, JSONException
	{
		if(jsonConfig == null)
			loadJsonFile();
		String id = "yima";
		JSONObject jsonYima = new JSONObject();
		//登录帐号和地址
		jsonYima.put("loginurl", "http://api.fxhyd.cn/UserInterface.aspx?action=login");
		jsonYima.put("username", "qswl168");
		jsonYima.put("pass", "dasheng01");
		//取手机号地址
		jsonYima.put("getmobileurl", "http://api.fxhyd.cn/UserInterface.aspx?action=getmobile");
		//发短信地址
		jsonYima.put("sendsmsurl", "http://api.fxhyd.cn/UserInterface.aspx?action=sendsms");
		//取短信内容地址
		jsonYima.put("getsmsurl", "http://api.fxhyd.cn/UserInterface.aspx?action=getsms");
		jsonConfig.put("smspf_" + id, jsonYima);
		saveJsonFile();
		
	}
	
	
	public static void main(String[] argv) throws IOException, JSONException
	{
		setyimaSmsPlatform();
		System.out.println("end");
	}
}
