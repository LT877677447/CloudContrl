/**
 * @author Administrator
 * 2019年4月18日 下午9:55:48 
 */
package com.kilotrees.action.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.dao.deviceinfodao;
import com.kilotrees.dao.phonetypedao;
import com.kilotrees.dao.vpninfodao;
import com.kilotrees.dao.douYin.DouYinDao;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.phonetype;
import com.kilotrees.model.po.vpninfo;
import com.kilotrees.services.JsonActionService;
import com.kilotrees.services.phonetype_service;
import com.kilotrees.util.DouYinIssueLog;
import com.kilotrees.util.InfoGenUtil;
import com.kilotrees.util.JSONObjectUtil;

public class DouYinZhuCeTaskAction implements ITaskAction {
	private static List<JSONObject> phoneInfoList = new ArrayList<>();
	private static List<String> phoneInfoList2 = new ArrayList<>();
	private static Integer phoneInfoIndex2Take = 0;
	
	static {
		
		try {
//			List<phonetype> p2 = phonetypedao.getOPPOPhoneInfo("tb_phonetype", "oppo");
//			phoneInfoList2 = phonetypedao.getPhoneInfo("tb_DouYinAccount","phoneInfo");

//			for (int i = 0; i < p.size(); i++) {
//				phoneInfoList2.add(phoneInfo);
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		JSONArray adtasks = response.optJSONArray("tasks");
		JSONObject adtask = adtasks.optJSONObject(0);
		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");
		String dev_tag = response.optString("dev_tag");
		String packageName = adtask.optString("packageName");
		
		if (adtasks.length() == 0) {
			return;
		}
		
		//VPN
		String devicesUsingWuJiVPN = "A6001 A6002 A6003 A6004 A6005 A6006 A6007 A6008 A6009 A6010 A6011 A6012 A6013 A6014 A6015"
								   +" B6001 B6002 B6003 B6004 B6005 B6006 B6007 B6008 B6009 B6010 B6011 B6012 B6013 B6014 B6015"
								   + "D6001 D6002";// 使用无极设备
		Boolean isUsingWuJiVPN = devicesUsingWuJiVPN.contains(dev_tag);
		if (!isUsingWuJiVPN) {
			// socket5
			String socket5PackageName = "org.proxydroid";
			String socket5FileName = "org.proxydroidApp.apk";
			String socket5APKurl = ServerConfig.getStoragePrivateBaseURL()
					+ "/phone_files/update/org.proxydroidApp.apk";

			JSONObject jsonInstall = JsonActionService.createAction_INSTALL_APP(socket5PackageName, socket5FileName,
					socket5APKurl);
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
//			JSONObject wujiVPN_Action = JsonActionService.createAction_INSTALL_APP("org.wuji", "wuji_duli_60.apk",
//					appDownloadURL);
//			prefix_task_actions.put(wujiVPN_Action);
			
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
		
		//prefix action
		JSONObject enablePermissions = JsonActionService.createAction_ENABLE_PERMISSIONS_ANDROID_M(packageName, "0,1,2");
		prefix_task_actions.put(enablePermissions);
		
		JSONObject command = JsonActionService.createAction_EXEC_COMMANDS("input keyevent 4 ");//返回的actions
		prefix_task_actions.put(command);
		
		JSONObject sleepAction = JsonActionService.createAction_SLEEP(4000);
		prefix_task_actions.put(sleepAction);
		
		JSONArray dirArray = new JSONArray();
		dirArray.put("/sdcard/Android/data/");
		dirArray.put("/sdcard/Android/");
		dirArray.put("/sdcard/amap/");
		dirArray.put("/sdcard/bytedance/");
		dirArray.put("/sdcard/aweme_monitor/");
		dirArray.put("/sdcard/monitor/");
		JSONObject jsonClearDirs = JsonActionService.createAction_CLEAR_DIRS(dirArray);
		prefix_task_actions.put(jsonClearDirs);
		
//		JSONObject jsonInstall = JsonActionService.createAction_INSTALL_APP("com.vfive.romservertester",
//				"RomserverTester.apk",
//				ServerConfig.getStoragePrivateBaseURL() + "/phone_files/other/RomserverTester.apk");
//		prefix_task_actions.put(jsonInstall);
		
		
		//phoneInfo
//		String s = phoneInfoList2.get(phoneInfoIndex2Take);
//		JSONObject j = new JSONObject(s);
//		phoneInfoIndex2Take++;
//		phoneInfoIndex2Take = phoneInfoIndex2Take % phoneInfoList2.size();
//		adtask.put("phoneInfo", j);
		
		JSONObject phoneInfo = adtask.optJSONObject("phoneInfo"); 
		phoneInfo.remove("Build.VERSION.SDK");
		phoneInfo.remove("Build.VERSION.SDK_INT");
		phonetype_service.setPhoneInfoIsUsingWifi(phoneInfo, true);
		phoneInfo.remove("Screen.widthPixels");
		phoneInfo.remove("Screen.heightPixels");
		
		//appinfo
		JSONObject appinfo = adtask.optJSONObject("appInfo");
		appinfo.put("phase", "zhuce");
		
		DouYinIssueLog.log(dev_tag, adtask.optJSONObject("phoneInfo").toString(), new Date());
	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {
		int result_code = request.optInt("result");

		JSONObject appinfoJson = request.optJSONObject("appInfo");
		JSONObject phoneInfoJson = request.optJSONObject("phoneInfo");
		
		String phoneNumber = appinfoJson.optString("phoneNumber");
		String pass = appinfoJson.optString("password");
		String comment = "注册成功|result:"+result_code;
		String appinfo = appinfoJson.toString();
		String phoneInfo = phoneInfoJson.toString();
		String autoid = request.optLong("autoid")+"";
		int status = 1;
		if (result_code == 0 || result_code == 10) {
			DouYinDao.newAccountSuccess(autoid,phoneNumber, pass, new Date(), appinfo, phoneInfo,status,comment);
		}else {
			//抖音注册失败,logSubType = 1 
			String info = "注册失败|result:"+result_code;
			DouYinDao.RegistLog_1(phoneNumber, pass, info, appinfo, phoneInfo,  new Date());
		}
	}

	private JSONObject changePhoneInfo(JSONObject phoneInfo) throws JSONException {
		JSONObject newPhoneInfo = new JSONObject();
		String key = "Build.SERIAL,Build.FINGERPRINT,Build.ID,Build.MANUFACTURER,Build.DEVICE,Build.TIME,Build.BRAND,Build.CPU_ABI,"
				+ "Build.VERSION.INCREMENTAL,Build.VERSION.RELEASE,Build.DISPLAY,Build.BRAND,Build.MODEL,Bluetooth.Address,Bluetooth.Name,"
				+ "Telephony.DeviceId,Telephony.SubscriberId,Telephony.Line1Number,Telephony.Msisdn,Telephony.NetworkOperator,Telephony.NetworkOperatorName,"
				+ "Telephony.IccSerialNumber,WebKit.UserAgent,Package.InstalledPackages";
		
		String keys[] = key.split(",");
		for(String str:keys) {
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
