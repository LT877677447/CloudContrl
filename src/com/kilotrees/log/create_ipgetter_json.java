package com.kilotrees.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.model.po.ServerConfig;

//生成ip代理配置jso,给IPGetter.java使用
public class create_ipgetter_json {
	static JSONObject jsonTaskExt;
	static int version = 1;
	
	static String getJsonPath() {
		return ServerConfig.contextRealPath + "/files/extjson/ipgetter.json";
	}

	static void loadJsonFile() throws IOException, JSONException {
		String jsonFile = getJsonPath();
		File f = new File(jsonFile);
		if (f.exists()) {
			FileInputStream fins = new FileInputStream(f);
			byte[] buf = new byte[(int) f.length()];
			fins.read(buf);
			fins.close();
			String s = new String(buf, "utf-8");
			jsonTaskExt = new JSONObject(s);
		} else
			jsonTaskExt = new JSONObject();
	}

	static void saveJsonFile() throws IOException {
		String jsonFile = getJsonPath();
		String sJson = jsonTaskExt.toString();
		FileOutputStream fos = new FileOutputStream(jsonFile);
		byte[] buf = sJson.getBytes("utf-8");
		fos.write(buf);
		fos.close();
	}

	// 加入的广告信息，用分号分开,第一个是advid,第二是重复使用率，第三可选是限制使用的ip代理id，
	static String[] advinfo_table = { /*"10,30"*/ };

	static void creatJson() throws IOException, JSONException {
		loadJsonFile();
		jsonTaskExt.put("version", version);
		// 最大缓存ip数
		jsonTaskExt.put("max_ipcache_cont", 60);

		JSONArray jsoArray = new JSONArray();

		// 加入使用的advid
		for (String info : advinfo_table) {
			String[] s = info.split(",");
			JSONObject jso = new JSONObject();
			// advid
			jso.put("adv_id", s[0]);
			// ip重复使用率
			jso.put("reuse", s[1]);
			// 使用代理，如果指定，表示此广告只使这个代理取得的 ip，一般为空，表示全部ip服务器取得的ip都可以使用
			if (s.length > 2)
				jso.put("proxyid", s[2]);

			jsoArray.put(jso);
		}
		jsonTaskExt.put("advids", jsoArray);

		// 加入代理服务器,可能有多个
		jsoArray = new JSONArray();
		// 加入第一个代理
		JSONObject jso = new JSONObject();
		jso.put("id", "niumowang");
		jso.put("name", "牛魔王");
		jso.put("url", "http://192.168.3.116:9090/zfyuncontrol/testgetips?type=1&count=10");
		// 如果要关停这个代理，设为true
		jso.put("stop", true);
		jsoArray.put(jso);
		// 加入第二个代理
		jso = new JSONObject();
		jso.put("id", "sunwukong");
		jso.put("name", "孙悟空");
		jso.put("url", "http://www.feilongip.com/Tools/proxyIP.ashx?Type=294e1679504e0ebdd5e2027191365d06&Order=73457&qty=20");
		// 如果要关停这个代理，设为true
		jso.put("stop", true);
		jsoArray.put(jso);

		// 加入第三个代理
		jso = new JSONObject();
		jso.put("id", "test1");
		jso.put("name", "自有ip库");
		jso.put("url", "http://192.168.3.116:5010/fetch/?count=5");
		// 如果要关停这个代理，设为true
		jso.put("stop", true);
		jsoArray.put(jso);
		jsonTaskExt.put("proxyipurls", jsoArray);

		saveJsonFile();

	}

	public static void main(String[] argv) throws IOException, JSONException {
		creatJson();
		System.out.println("end");
	}
}
