/**
 * @author Administrator
 * 2019年1月18日 下午12:20:16 
 */
package com.kilotrees.action.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.services.JsonActionService;

public class DuoBaoTouTiaoTaskAction implements ITaskAction {

	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		JSONArray adtasks = response.optJSONArray("tasks");
		if (adtasks.length() == 0) {
			return;
		}

		JSONObject adtask = adtasks.optJSONObject(0);
		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");
		String dev_tag = response.optString("dev_tag");
		String packageName = adtask.optString("packageName");

		String devicesUsingWuJiVPN = "";// 使用无极的设备
		Boolean isUsingWuJiVPN = devicesUsingWuJiVPN.contains(dev_tag);

		JSONObject appinfo = adtask.optJSONObject("appInfo");
		appinfo.put("isVisitor", false);
		
		JSONObject phoneInfo = adtask.optJSONObject("phoneInfo"); 
		phoneInfo.remove("Build.VERSION.SDK");
		phoneInfo.remove("Build.VERSION.SDK_INT");

		if (isUsingWuJiVPN) {

			String appDownloadURL = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/wuji_duli_60.apk";
			JSONObject wujiVPN_Action = JsonActionService.createAction_INSTALL_APP("org.wuji", "wuji_duli_60.apk", appDownloadURL);
			prefix_task_actions.put(wujiVPN_Action);

		} else {
			// socket5
			boolean isUseOneKeySocks5 = false;

			// ----------- 一键Socks5 -----------

			if (isUseOneKeySocks5) {

				String zipFileName = "OneKeySocks5.zip";
				String zipDownloadURL = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/socks5/" + zipFileName;
				
				JSONObject command = JsonActionService.createAction_EXEC_COMMANDS(""
						+ "chmod 777 -R /data/local/rom/cache/proxy, "
						+ "chmod 777 /data/local/rom/cache/proxy/proxy.sh "
						+ "");
				JSONObject unzip = JsonActionService.createAction_UNZIP_REMOTE_FILE(packageName, zipFileName, zipDownloadURL, 1);
				JSONObject executeConnect = JsonActionService.createAction_EXEC_SCRIPT_METHOD(packageName, 90 * 1000, "android.application.util.ProxyVendor_AnHui", "connectOn");
				JSONObject executeDisconnect = JsonActionService.createAction_EXEC_SCRIPT_METHOD(packageName, 90 * 1000, "android.application.util.ProxyVendor_AnHui", "connectOff");
				
				prefix_task_actions.put(command);
				prefix_task_actions.put(unzip);
				prefix_task_actions.put(command);
				prefix_task_actions.put(executeConnect);
				suffix_task_actions.put(executeDisconnect);

				// ----------- ProxyDroid APP -----------------

			} else {

				String socks5PackageName = "org.proxydroid";
				String socks5APKFileName = "org.proxydroidApp.apk";
				String socks5APKurl = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk";

				JSONObject install = JsonActionService.createAction_INSTALL_APP(socks5PackageName, socks5APKFileName, socks5APKurl);
				JSONObject stop = JsonActionService.createAction_STOP_APP(socks5PackageName);
				JSONObject open = JsonActionService.createAction_OPEN_APP(socks5PackageName, 0);
				JSONObject close = JsonActionService.createAction_CLOSE_APP(socks5PackageName, 0);

				prefix_task_actions.put(install);
				prefix_task_actions.put(stop);
				prefix_task_actions.put(open);
				suffix_task_actions.put(close);
				suffix_task_actions.put(stop);

			}

		}

		// File currentFile = new File("D:/WebServer/config/1005_DuoBaoTouTiao.json");
		// File parent = currentFile.getParentFile();
		// if(parent != null && !parent.exists()) {
		// parent.mkdirs();
		// }
		//
		// File originFile = new
		// File("D:/WebServer/config/1005_DuoBaoTouTiao_origin.json");
		// parent = originFile.getParentFile();
		// if(parent != null && !parent.exists()) {
		// parent.mkdirs();
		// }
		//
		// if(currentFile.length() <= 0) {
		// reload();
		// }
		// fetch(currentFile, request,response);

		// ----------------------------------------------------------------------------------------------------
		/*
		 * 下面是俊哥的想法
		 */
		// String configFileName = "D:/WebServer/config/DuoBaoTouTiao_Ratio.json";
		// String contents = IFileUtil.read(configFileName);
		// JSONObject ratioJson = new JSONObject(contents);
		//
		// if (!ratioJson.has("isVisitorAmount")) {
		// JSONObject isVisitorJson = ratioJson.optJSONObject("isVisitor");
		// int isVisitorVal = isVisitorJson.optInt("true");
		// int isNotVisitorVal = isVisitorJson.optInt("false");
		//
		// float isVisitorRatio = ((float)isVisitorVal) / 100.0f;
		// float isNotVisitorRatio = ((float)isNotVisitorVal) / 100.0f;
		//
		// advtaskinfo taskInfo = advtaskinfodao.getAdvtaskbyid(1005);
		// int dayusercount = taskInfo.getDayusercount();
		//
		// int isVisitorAmount = (int)(dayusercount * isVisitorRatio);
		// int isNotVisitorAmount = (int)(dayusercount * isNotVisitorRatio);
		//
		// ratioJson.put("isVisitorAmount", isVisitorAmount);
		// ratioJson.put("isNotVisitorAmount", isNotVisitorAmount);
		//
		// IFileUtil.write(configFileName, ratioJson.toString());
		// }
		//
		// boolean isGetVisitor = new Random().nextBoolean();
		// int isVisitorAmount = ratioJson.optInt("isVisitorAmount");
		// int isNotVisitorAmount = ratioJson.optInt("isNotVisitorAmount");
		//
		// if (isGetVisitor) {
		// isVisitorAmount--;
		// } else {
		// isNotVisitorAmount--;
		// }
		//
		// if (isVisitorAmount <= 0) {
		// isGetVisitor = false;
		// }
		// if (isNotVisitorAmount <= 0) {
		// isGetVisitor = true;
		// }
		//
		// ratioJson.put("isVisitorAmount", isVisitorAmount);
		// ratioJson.put("isNotVisitorAmount", isNotVisitorAmount);
		// IFileUtil.write(configFileName, ratioJson.toString());

		// appinfo.put("isVisitor", isGetVisitor);
		// ---------------------------------------------------------------------------------------------------------------------------
	}

	/**
	 * 重新从origin放满
	 */
	private void reload() {
		try {
			String inStr = "";
			byte[] bs = new byte[512];

			File originFile = new File("D:/WebServer/config/1005_DuoBaoTouTiao_origin.json");
			if (originFile.getParentFile() != null && !originFile.getParentFile().exists()) {
				originFile.getParentFile().mkdirs();
			}
			FileInputStream in = new FileInputStream(originFile);

			File realFile = new File("D:/WebServer/config/1005_DuoBaoTouTiao.json");
			if (realFile.getParentFile() != null && !realFile.getParentFile().exists()) {
				realFile.getParentFile().mkdirs();
			}
			FileOutputStream out = new FileOutputStream(realFile);

			while (in.read(bs) != -1) {
				out.write(bs, 0, bs.length);
			}
			in.close();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void fetch(File file, JSONObject request, JSONObject response) {
		try {
			JSONArray adtasks = response.optJSONArray("tasks");
			JSONObject jsonObject = adtasks.optJSONObject(0);
			JSONObject appinfo = jsonObject.optJSONObject("appInfo");

			byte[] bs = new byte[512];
			String str = "";
			FileInputStream in = new FileInputStream(file);
			while (in.read(bs) != -1) {
				str += new String(bs, "UTF-8");
			}
			in.close();

			JSONObject json = new JSONObject(str);
			HashMap<String, Integer> map = new HashMap<>();
			map.put("true", json.getInt("true"));
			map.put("false", json.getInt("false"));

			int random = new Random().nextInt(2);
			int v = 0; // 拿到的值
			boolean isFromTrue = true; // 是否来自"true"
			// random: 1 拿 true 0拿 false
			if (random == 1) {
				v = map.get("true");
				if (v <= 0) {
					v = map.get("false");
					isFromTrue = false;
				}
			}
			if (random == 0) {
				v = map.get("false");
				isFromTrue = false;
				if (v <= 0) {
					v = map.get("true");
					isFromTrue = true;
				}
			}
			// 全部拿完了，重新放满
			if (v <= 0) {
				reload();
				handleTaskRequest(request, response);
				return;
			}
			if (isFromTrue) {
				map.put("true", --v);
				appinfo.put("isVisitor", true);
			} else {
				map.put("false", --v);
				appinfo.put("isVisitor", false);
			}
			json.put("true", map.get("true"));
			json.put("false", map.get("false"));
			FileOutputStream out = new FileOutputStream(file);
			out.write(json.toString().getBytes());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {
	}

}
