/**
 * @author Administrator
 * 2019年4月27日 下午4:49:50 
 */
package com.kilotrees.action.task;

import java.util.Random;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.dao.deviceinfodao;
import com.kilotrees.dao.vpninfodao;
import com.kilotrees.dao.task.DouYinActiveTaskDao;
import com.kilotrees.dao.task.WXActiveTaskDao;
import com.kilotrees.model.bo.TaskBase;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.vpninfo;
import com.kilotrees.services.JsonActionService;
import com.kilotrees.services.phonetype_service;
import com.kilotrees.util.InfoGenUtil;
import com.kilotrees.util.JSONObjectUtil;

public class DouYinActiveTaskAction implements ITaskAction {
	private static Logger log = Logger.getLogger(DouYinActiveTaskAction.class);

	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		JSONArray adtasks = response.optJSONArray("tasks");
		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");
		String dev_tag = response.optString("dev_tag");

		if (adtasks.length() == 0) {
			return;
		}

		JSONObject adtask = adtasks.getJSONObject(0);
		int taskPhase = adtask.optInt("taskPhase");
		String packageName = adtask.optString("packageName");
		String autoid = adtask.optString("autoid");
		String zipFileName = "data_" + autoid + ".zip";
		String zipFilePath = "files/zips/" + packageName + "/" + zipFileName;

		if (taskPhase == TaskBase.TASK_PHASE_REMAIN) {
			// Download the remain zip file in phase remain
			String zipDownloadURL = ServerConfig.getStorageBaseURL() + zipFilePath;

			JSONObject action_UNZIP_FILE = JsonActionService.getFirstAction(prefix_task_actions, JsonActionService.ACTION_TYPE_UNZIP_FILE);
			if (action_UNZIP_FILE != null) {
				action_UNZIP_FILE.put("zip_file_name", zipFileName);
				action_UNZIP_FILE.put("zip_download_url", zipDownloadURL);
			} else {
				action_UNZIP_FILE = JsonActionService.createAction_UNZIP_REMOTE_FILE(packageName, zipFileName, zipDownloadURL);
				prefix_task_actions.put(action_UNZIP_FILE);
			}
		}

		// 1.prefix action
		JSONObject enablePermissions = JsonActionService.createAction_ENABLE_PERMISSIONS_ANDROID_M(packageName, "0,1,2");
		prefix_task_actions.put(enablePermissions);
		
		JSONObject command1 = JsonActionService.createAction_EXEC_COMMANDS("input tap 99 180");
		prefix_task_actions.put(command1);
		
		JSONObject sleep = JsonActionService.createAction_SLEEP(2000);
		prefix_task_actions.put(sleep);
		
		
		JSONObject command2 = JsonActionService.createAction_EXEC_COMMANDS("input keyevent 4 ");//返回的actions
		prefix_task_actions.put(command2);
		
		// 2. suffix_task_actions
		String zipfiles = adtask.optString("zipfiles");
		String notZipRegex = adtask.optString("unzip_regex");

		String resourceUploadURL = ServerConfig.getFileServerServlet("ResourceUpload");
		String zipUploadURL = resourceUploadURL + "?fileName=" + zipFilePath;

		JSONObject action_ZIP_FILE = JsonActionService.getFirstAction(suffix_task_actions, JsonActionService.ACTION_TYPE_ZIP_FILE);
		if (action_ZIP_FILE != null) {
			action_ZIP_FILE.put("zip_file_name", zipFileName);
			action_ZIP_FILE.put("zip_upload_url", zipUploadURL);
		} else {
			action_ZIP_FILE = JsonActionService.createAction_ZIP_FILE(packageName, zipFileName, zipUploadURL, zipfiles, notZipRegex);
			suffix_task_actions.put(action_ZIP_FILE);
		}

		// socket5
		String devicesUsingWuJiVPN = "A6001 A6002 A6003 A6004 A6005 A6006 A6007 A6008 A6009 A6010 A6011 A6012 A6013 A6014 A6015"
				   +" B6001 B6002 B6003 B6004 B6005 B6006 B6007 B6008 B6009 B6010 B6011 B6012 B6013 B6014 B6015";// 使用无极设备
		Boolean isUsingWuJiVPN = devicesUsingWuJiVPN.contains(dev_tag);
		if (!isUsingWuJiVPN) {
			// socket5
			String socket5PackageName = "org.proxydroid";
			String socket5FileName = "org.proxydroidApp.apk";
			String socket5APKurl = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk";

			JSONObject jsonInstall = JsonActionService.createAction_INSTALL_APP(socket5PackageName, socket5FileName, socket5APKurl);
			prefix_task_actions.put(jsonInstall);

			JSONObject jsonStop = JsonActionService.createAction_STOP_APP(socket5PackageName);
			prefix_task_actions.put(jsonStop);

			JSONObject jsonOpen = JsonActionService.createAction_OPEN_APP(socket5PackageName, 0);
			prefix_task_actions.put(jsonOpen);

			JSONObject jsonClose = JsonActionService.createAction_CLOSE_APP(socket5PackageName, 0);
			suffix_task_actions.put(jsonClose);

			suffix_task_actions.put(jsonStop);

		} else {
			// wuji VPN
//			String appDownloadURL = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/wuji_duli_60.apk";
//			JSONObject wujiVPN_Action = JsonActionService.createAction_INSTALL_APP("org.wuji", "wuji_duli_60.apk", appDownloadURL);
//			prefix_task_actions.put(wujiVPN_Action);

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
		}

		// phoneInfo
		JSONObject phoneInfo = adtask.optJSONObject("phoneInfo");
		phoneInfo.remove("Build.VERSION.SDK");
		phoneInfo.remove("Build.VERSION.SDK_INT");
		phonetype_service.setPhoneInfoIsUsingWifi(phoneInfo, true);

//		JSONObject newPhoneInfo = changePhoneInfo(phoneInfo);
//		adtask.put("phoneInfo", newPhoneInfo);
		
		JSONObject connectionInfo = phoneInfo.optJSONObject("Wifi.ConnectionInfo");
		String macAddress = connectionInfo.optString("MacAddress");
		JSONObject fileContentsInfo  = phoneInfo.optJSONObject("Files.Contents");
		if (fileContentsInfo == null) {
			fileContentsInfo = new JSONObject();
			phoneInfo.put("Files.Contents", fileContentsInfo);
		}
		
		String pathWlan0Address = "/sys/class/net/wlan0/address";
		String pathWlan0AddressLink = "/sys/devices/fb000000.qcom,wcnss-wlan/net/wlan0/address";
		fileContentsInfo.put(pathWlan0Address, macAddress);
		fileContentsInfo.put(pathWlan0AddressLink, macAddress);
		 
		//appinfo
		JSONObject appinfo = adtask.optJSONObject("appInfo");
		appinfo.put("phase", "active");
	}

	@SuppressWarnings("unused")
	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {
		int result = request.optInt("result");

		String phoneNumber = request.optString("phoneNumber");
		if (result == 0) {
			//活跃成功
			JSONObject appinfoJson = request.optJSONObject("appInfo");
			JSONObject phoneInfoJson = request.optJSONObject("phoneInfo");

			String autoid = request.optString("autoid");
			DouYinActiveTaskDao.SuccActive(phoneNumber);
		} else {
			//活跃失败
			DouYinActiveTaskDao.failActive(phoneNumber);
		}
	}

	private JSONObject changePhoneInfo(JSONObject phoneInfo) throws JSONException {
		JSONObject newPhoneInfo = new JSONObject();
		String key = "Build.SERIAL,Build.FINGERPRINT,Build.ID,Build.MANUFACTURER,Build.DEVICE,Build.TIME,Build.BRAND,Build.CPU_ABI,"
				+ "Build.VERSION.INCREMENTAL,Build.VERSION.RELEASE,Build.DISPLAY,Build.BRAND,Build.MODEL,Bluetooth.Address,Bluetooth.Name,"
				+ "Telephony.DeviceId,Telephony.SubscriberId,Telephony.Line1Number,Telephony.Msisdn,Telephony.NetworkOperator,Telephony.NetworkOperatorName,"
				+ "Telephony.IccSerialNumber,WebKit.UserAgent,Package.InstalledPackages";

		String keys[] = key.split(",");
		for (String str : keys) {
			Object value = phoneInfo.opt(str);
			newPhoneInfo.put(str, value);
		}

		JSONObject newSettings = new JSONObject();
		newPhoneInfo.put("Settings", newSettings);

		JSONObject SystemJson = new JSONObject();
		JSONObject SecureJson = new JSONObject();
		newSettings.put("System", SystemJson);
		newSettings.put("Secure", SecureJson);

		SystemJson.put("android_id", phoneInfo.optJSONObject("Settings").optJSONObject("System").opt("android_id"));
		SystemJson.put("android_id", phoneInfo.optJSONObject("Settings").optJSONObject("Secure").opt("android_id"));

		JSONObject ConnectionInfo = new JSONObject();
		newPhoneInfo.put("Wifi.ConnectionInfo", ConnectionInfo);
		ConnectionInfo.put("MacAddress", phoneInfo.optJSONObject("Wifi.ConnectionInfo").opt("MacAddress"));
		return newPhoneInfo;
	}

}
