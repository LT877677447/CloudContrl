/**
 * @author Administrator
 * 2019年4月30日 下午6:30:18 
 */
package com.kilotrees.action.task;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.dao.deviceinfodao;
import com.kilotrees.dao.vpninfodao;
import com.kilotrees.dao.task.WXActiveTaskDao;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.vpninfo;
import com.kilotrees.services.JsonActionService;
import com.kilotrees.services.phonetype_service;

public class WXActiveTaskAction implements ITaskAction {

	private final int NETWORK_TYPE_SOCKS5 = 1;
	private final int NETWORK_TYPE_VPN = 2;
	private final int NETWORK_TYPE_4G = 3;
	
	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		JSONArray adtasks = response.optJSONArray("tasks");
		JSONObject adtask = adtasks.optJSONObject(0);
		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");
		String dev_tag = response.optString("dev_tag");

		if (adtasks.length() == 0) {
			return;
		}

		String packageName = adtask.optString("packageName");
		String phoneNumber = adtask.optString("phoneNumber");
		String isOverSeas = adtask.optString("isOverSeas");
		
 
		JSONObject phoneInfo = adtask.optJSONObject("phoneInfo");
		JSONObject appInfo = adtask.optJSONObject("appInfo");
		
		int netType = NETWORK_TYPE_4G;
		
		if(netType == NETWORK_TYPE_4G) {
			phonetype_service.setPhoneInfoIsUsingWifi(phoneInfo, false);
		}
		
		if(netType == NETWORK_TYPE_VPN) {
			String devicesUsingWuJiVPN = " ";
			Boolean isUsingWuJiVPN = devicesUsingWuJiVPN.contains(dev_tag);
			if (isUsingWuJiVPN) {

				String wuJiVPNPackageName = "org.wuji";
				String wuJiVPNAPKFileName = "wuji_duli_60.apk";
				String wuJiVPNAPKurl = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/wuji_duli_60.apk";

				JSONObject install = JsonActionService.createAction_INSTALL_APP(wuJiVPNPackageName, wuJiVPNAPKFileName,
						wuJiVPNAPKurl);
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
		}
		
		if(netType == NETWORK_TYPE_SOCKS5) {
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
		}

		// prefix action
		JSONObject jsonInstall = JsonActionService.createAction_INSTALL_APP("com.vfive.romservertester",
				"RomserverTester.apk",
				ServerConfig.getStoragePrivateBaseURL() + "/phone_files/other/RomserverTester.apk");
		prefix_task_actions.put(jsonInstall);

		JSONObject enablePermissions = JsonActionService.createAction_ENABLE_PERMISSIONS_ANDROID_M(packageName, null);
		prefix_task_actions.put(enablePermissions);

		JSONObject command1 = JsonActionService.createAction_EXEC_COMMANDS("input tap 99 180");
		prefix_task_actions.put(command1);

		JSONObject sleep = JsonActionService.createAction_SLEEP(2000);
		prefix_task_actions.put(sleep);

		JSONObject command2 = JsonActionService.createAction_EXEC_COMMANDS("input keyevent 4 ");
		prefix_task_actions.put(command2);
		
		JSONObject sleepAction = JsonActionService.createAction_SLEEP(3000);
		prefix_task_actions.put(sleepAction);
		
		String zipEventFileName = "getevent.zip";
		String zipEventFilePath = "Resources/QQ/" + zipEventFileName;
		String zipEventDownloadURL = ServerConfig.getStorageBaseURL() + zipEventFilePath;
		
		JSONObject action_unzip_event = JsonActionService.createAction_UNZIP_REMOTE_FILE(packageName, zipEventFileName,
				zipEventDownloadURL);
		prefix_task_actions.put(action_unzip_event);
		
		if(netType == NETWORK_TYPE_4G) {
			
			//做之前关 wifi 做完开wifi(最后做)
			JSONObject disableWifi = JsonActionService.createAction_DISABLE_WIFI();
			prefix_task_actions.put(disableWifi);
			
			// 打开 wifi 要在上传Zip之前
			JSONObject enableWifi = JsonActionService.createAction_ENABLE_WIFI();
//			suffix_task_actions.put(enableWifi);
			int index = JsonActionService.getFirstIndexOfAction(suffix_task_actions, JsonActionService.ACTION_TYPE_ZIP_FILE);
			JsonActionService.insertAction(suffix_task_actions, enableWifi, index);
			
		}
		
		// phoneInfo
		phoneInfo.remove("Build.VERSION.SDK");
		phoneInfo.remove("Build.VERSION.SDK_INT");
		phoneInfo.remove("Screen.widthPixels");
		phoneInfo.remove("Screen.heightPixels");
		
		//appInfo
		appInfo.put("phase", "active");
		
		int accountType;
//		if(phoneNumber.startsWith("00")){
		if(isOverSeas.equals("true")){
			accountType = 2;
		}else {
			accountType = 1;
		}
		appInfo.put("AccountType", accountType);
		JSONArray array1 = new JSONArray();
		if(accountType == 2) {
		//海外
			array1.put("START_DAEMON_WECHAT_READ");  //开户刷阅读线程
			array1.put("COMPLETE_PROFILE_AvatarNickname");  //昵称
			array1.put("COMPLETE_PROFILE_AccountQRCode");  //账号
			array1.put("COMPLETE_PROFILE_SexLocationStatus"); //性别
			array1.put("CHANGE_PASSWORD"); //密码
			array1.put("POST_ONE_TIMELINE"); //发一条朋友圈
			array1.put("SCAN_ALL_TIMELINE"); //浏览所有朋友圈
		}
		if(accountType == 1) {
		//正常账号
			array1.put("POST_ONE_TIMELINE"); //发一条朋友圈
			array1.put("SCAN_ALL_TIMELINE"); //浏览所有朋友圈
		}
		appInfo.put("behaviors", array1);
		
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
			WXActiveTaskDao.SuccActive(phoneNumber);
		} else {
			//活跃失败
			WXActiveTaskDao.failActive(phoneNumber);
		}
	}

}
