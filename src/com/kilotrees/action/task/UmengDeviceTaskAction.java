package com.kilotrees.action.task;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.dao.deviceinfodao;
import com.kilotrees.dao.vpninfodao;
import com.kilotrees.model.bo.TaskBase;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.vpninfo;
import com.kilotrees.services.JsonActionService;
import com.kilotrees.util.JSONObjectUtil;

public class UmengDeviceTaskAction implements ITaskAction {

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
		
		String devicesUsingWuJiVPN = "phone001|A6001";// 使用无极的设备
		Boolean isUsingWuJiVPN = devicesUsingWuJiVPN.contains(dev_tag);
		
		if (isUsingWuJiVPN) {
			
			// ----------- 无极VPN ----------- 
			String wuJiVPNPackageName = "org.wuji";
			String wuJiVPNAPKFileName = "wuji_duli_60.apk";
			String wuJiVPNAPKurl = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/wuji_duli_60.apk";
			
			JSONObject install = JsonActionService.createAction_INSTALL_APP(wuJiVPNPackageName, wuJiVPNAPKFileName, wuJiVPNAPKurl);
			JSONObject stop = JsonActionService.createAction_STOP_APP(wuJiVPNPackageName);
			JSONObject open = JsonActionService.createAction_OPEN_APP(wuJiVPNPackageName, 0);
			JSONObject close = JsonActionService.createAction_CLOSE_APP(wuJiVPNPackageName, 0);
			
			int vpnid = deviceinfodao.getDeviceInfo(dev_tag).getVpnid();
			vpninfo vpnInfo = vpninfodao.getVpnById(vpnid);
			open.put("vpnAccount", vpnInfo.getAccount());
			open.put("vpnPassword", vpnInfo.getPass());
			
			prefix_task_actions.put(install);
			prefix_task_actions.put(stop);
			prefix_task_actions.put(open);
			suffix_task_actions.put(close);
			suffix_task_actions.put(stop);
			
		} else {
			// socket5
			boolean isUseOneKeySocks5 = true;
			
			// ----------- 一键Socks5 ----------- 
			if (isUseOneKeySocks5) {
				
				String zipFileName = "OneKeySocks5.zip";
				String zipDownloadURL = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/socks5/" + zipFileName;
				
				JSONObject command = JsonActionService.createAction_EXEC_COMMANDS(""
						+ "chmod 777 -R /data/local/rom/cache/proxy, "
						+ "chmod 777 /data/local/rom/cache/proxy/proxy.sh "
						+ "");
				JSONObject unzip = JsonActionService.createAction_UNZIP_REMOTE_FILE(packageName, zipFileName, zipDownloadURL, 2);
				JSONObject executeConnect = JsonActionService.createAction_EXEC_SCRIPT_METHOD(packageName, 90 * 1000, "android.application.util.ProxyVendorAnHui", "connectOn");
				JSONObject executeDisconnect = JsonActionService.createAction_EXEC_SCRIPT_METHOD(packageName, 90 * 1000, "android.application.util.ProxyVendorAnHui", "connectOff");
				
				prefix_task_actions.put(command);
				prefix_task_actions.put(unzip);
				prefix_task_actions.put(command);
				prefix_task_actions.put(executeConnect);
				suffix_task_actions.put(executeDisconnect);
				
			//	----------- ProxyDroid APP -----------------
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
		
		
		// ----------- 只留下关键的设备信息及以不下载留存文件，看看友盟后台是否还有留存的统计 -----------------
		JSONObject phoneInfo = adtask.optJSONObject("phoneInfo"); 
		phoneInfo.remove("Build.VERSION.SDK");
		phoneInfo.remove("Build.VERSION.SDK_INT");
		
		boolean isInRemainPhase = adtask.optInt("taskPhase") == TaskBase.TASK_PHASE_REMAIN;
		if (isInRemainPhase) {
			
			// 不下载留存文件, 删除第一个 UNZIP Action, 第一个是留在的 UNZIP
			for (int i = 0; i < prefix_task_actions.length(); i++) {
				JSONObject actionJson = prefix_task_actions.optJSONObject(i);
				if (actionJson.optString("action").equals(JsonActionService.ACTION_TYPE_UNZIP_FILE)) {
					prefix_task_actions.remove(i);
					break;
				}
			}
			// 设备信息只留下 deviceId 及 MAC, Build. SystemProperties.
			JSONObject newPhoneInfo = new JSONObject();
			newPhoneInfo.put("Telephony.DeviceId", phoneInfo.opt("Telephony.DeviceId"));
			newPhoneInfo.put("Wifi.ConnectionInfo", phoneInfo.optJSONObject("Wifi.ConnectionInfo"));
			newPhoneInfo.put("Files.Contents", phoneInfo.optJSONObject("Files.Contents"));
			
			JSONObject buildJson = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfo, "Build.");
			JSONObject propertiesJson = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfo, "SystemProperties.");
			
			JSONObjectUtil.mergeJSONObject(newPhoneInfo, buildJson);
			JSONObjectUtil.mergeJSONObject(newPhoneInfo, propertiesJson);
			
			adtask.put("phoneInfo", newPhoneInfo);
		}
		
	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {
		
	}

}
