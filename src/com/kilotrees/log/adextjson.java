package com.kilotrees.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.model.po.ServerConfig;

public class adextjson {
	static JSONObject jsonTaskExt;
	
	static String getJsonPath() {
		return ServerConfig.contextRealPath + "/files/extjson/advext_10.json";
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
			jsonTaskExt = new JSONObject(s);
		}
		else
			jsonTaskExt = new JSONObject();
	}
	
	static void saveJsonFile() throws IOException
	{
		String jsonFile = getJsonPath();
		String sJson = jsonTaskExt.toString();
		FileOutputStream fos = new FileOutputStream(jsonFile);
		byte[] buf = sJson.getBytes("utf-8");
		fos.write(buf);
		fos.close();
	}
	//版本号,有修改的话就每次加1
	static int version = 1;
	//取ip代理地址
	//static String proxyip_url = "http://d.jghttp.golangapi.com/getip?num=3&type=2&pro=&city=0&yys=0&port=11&pack=914&ts=0&ys=0&cs=0&lb=1&sb=0&pb=45&mr=0&regions=";
	static String um_vendor_id = "1274121064";
	static String vendor_title = "千树网络";
	static String baidu_vendor_id = "593ece04d6336a0105ce0b3a714d9038";
	//static String baidu_vendor_title = "千树网络";
	//sourceurl,是一个数组
//	static String[] source_urls = {
//			"http://www.sourceurl.com/1",
//			"http://www.sourceurl.com/2",
//			"http://www.sourceurl.com/3"
//	};
	//使用此json的广告id和ip重复率,重复率一般30
	//public static int[][] advids = {{7,10},{8,30}};
	
	static void creatJson() throws IOException, JSONException
	{
		//loadJsonFile();
		jsonTaskExt = new JSONObject();
		jsonTaskExt.put("version", version);
		//对于大点击同一个广告一次下发几个
		jsonTaskExt.put("fetchcount", 3);
		jsonTaskExt.put("vendor_title",vendor_title);
		if(um_vendor_id.length() > 0){
		JSONObject jsoumlink = new JSONObject();
		//jsonTaskExt.put("proxyip_url",proxyip_url);		
		jsoumlink.put("vendor_id", um_vendor_id);
		//jsoumlink.put("vendor_title",um_vendor_title);
		jsonTaskExt.put("umlink", jsoumlink);
		}
		if(baidu_vendor_id.length() > 0)
		{
			JSONObject jsobaidulink = new JSONObject();
			//jsonTaskExt.put("proxyip_url",proxyip_url);		
			jsobaidulink.put("vendor_id", baidu_vendor_id);
			//jsobaidulink.put("vendor_title",baidu_vendor_title);
			jsonTaskExt.put("baidulink", jsobaidulink);
		}
		jsonTaskExt.put("umolduserPercent", 10);
		jsonTaskExt.put("baiduolduserPercent", 10);
		//访问客户广告url的比率
		jsonTaskExt.put("visit_linksurl_percent", 0);
		jsonTaskExt.put("pvuvPercent", 130);
		jsonTaskExt.put("getipmethod", 0);
		
		
		System.out.println(jsonTaskExt.toString());
		
//		JSONArray jsoArray = new JSONArray();
//		for(String url : source_urls)
//		{
//			JSONObject jso = new JSONObject();
//			jso.put("value", url);
//			jsoArray.put(jso);
//		}
//		jsonTaskExt.put("source_urls", jsoArray);
//		jsoArray = new JSONArray();
//		for(int[] ad : advids)
//		{
//			JSONObject jso = new JSONObject();
//			jso.put("adv_id", ad[0]);
//			jso.put("reuse", ad[1]);
//			jsoArray.put(jso);
//		}
//		jsonTaskExt.put("advuses", jsoArray);
		
		//jsonTaskExt.put("filesdir", "files/task_links");
		saveJsonFile();
	}
	
	public static void main(String[] argv) throws IOException, JSONException
	{
		System.out.println("begin");
		creatJson();
		System.out.println("end");
	}
}
