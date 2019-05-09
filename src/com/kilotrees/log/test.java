package com.kilotrees.log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class test {
	
	static void createRomIni() throws JSONException, UnsupportedEncodingException, IOException
	{
		JSONObject jso = new JSONObject();
		jso.put("dev_tag", "pone68");
		jso.put("server_url", "http://192.168.3.116:9090/zfyuncontrol");
		JSONObject jsowifi = new JSONObject();
		jsowifi.put("ip", "192.168.3.155");
		jso.put("wifi", jsowifi);
		jso.put("server_params", new JSONObject());
		
		String s = jso.toString();
		System.out.println(s);
		FileOutputStream fos = new FileOutputStream("D:/workspace/CloudCtrlDev/bin/rom.json");
		fos.write(s.getBytes("utf-8"));
		fos.close();
		
	}
	
	static void test2()
	{
		JSONObject taskState = new JSONObject();
		JSONArray adsState = new JSONArray();
		try {
			for (int i = 0; i < 3;i++) {
				JSONObject jsoAd = new JSONObject();
				jsoAd.put("adv_id", i);
				jsoAd.put("result", -1);
				jsoAd.put("isend", false);
				adsState.put(jsoAd);
			}

			taskState.put("ads_state", adsState);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			
		}
		System.out.println(taskState.toString());
	}
	
	public static String getChannelId(String fileName)
	{
		String channelId = "";
		String s = fileName;
		int pos1 = s.indexOf("-");
		int pos2 = s.lastIndexOf("-");
		if(pos1 == -1 || pos1 == pos2)
			return "";
		
		channelId = s.substring(pos1 + 1,pos2);
		return channelId;
	}
	public static void main(String[] argv) throws UnsupportedEncodingException, JSONException, IOException
	{
		createRomIni();
		//System.out.println(getChannelId("abc-3-2.apk"));
		//test2();
		ArrayList<String> ls = new ArrayList<String>();
		ls.add("abc");
		
		String[] s = new String[ls.size()];
		ls.toArray(s);
		System.out.println("s.length:" + s.length + ",s=" + s[0]);
//		
			JSONObject jso = new JSONObject();
			JSONArray array = new JSONArray();
			jso.put("dir", "/data/local/rom/rom.json");
			array.put(jso);
			jso = new JSONObject();
			jso.put("dir", "/data/local/rom/server.json");
			array.put(jso);	
			java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
			String[] params = new String[4];
			params[0] = "zipbackup";
			params[1] = "/data/data/com.baidu.BaiduMap/testsu.zip";
			params[2] = array.toString();
			params[2] = encoder.encodeToString(params[2].getBytes("utf-8"));
			params[3] = "null1";
			
			System.out.println(params[2]);
			String cmd = "";
			for(int i = 0; i < params.length; i++){
				cmd += params[i];
				if(i != params.length -1)
					cmd += " ";
			}
			
			System.out.println(cmd);
	}
	
}
