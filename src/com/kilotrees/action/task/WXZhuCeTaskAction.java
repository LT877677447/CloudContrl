/**
 * @author Administrator
 * 2019年4月30日 下午3:22:53 
 */
package com.kilotrees.action.task;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.dao.deviceinfodao;
import com.kilotrees.dao.vpninfodao;
import com.kilotrees.dao.weixin.WeiXinDao;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.vpninfo;
import com.kilotrees.services.JsonActionService;
import com.kilotrees.servlets.WeixinHaiWai;

public class WXZhuCeTaskAction implements ITaskAction{

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
		String devicesUsingWuJiVPN = "B6002 ";// 使用无极设备
		Boolean isUsingWuJiVPN = devicesUsingWuJiVPN.contains(dev_tag);
		if (!isUsingWuJiVPN) {
			// socket5
			String socket5PackageName = "org.proxydroid";
			String socket5FileName = "org.proxydroidApp.apk";
			String socket5APKurl = ServerConfig.getStoragePrivateBaseURL()
					+ "/phone_files/update/org.proxydroidApp.apk";

//			JSONObject jsonInstall = JsonActionService.createAction_INSTALL_APP(socket5PackageName, socket5FileName,
//					socket5APKurl);
//			prefix_task_actions.put(jsonInstall);
//
//			JSONObject jsonStop = JsonActionService.createAction_STOP_APP(socket5PackageName);
//			prefix_task_actions.put(jsonStop);
//
//			JSONObject jsonOpen = JsonActionService.createAction_OPEN_APP(socket5PackageName, 0);
//			prefix_task_actions.put(jsonOpen);
//			
//			JSONObject jsonClose = JsonActionService.createAction_CLOSE_APP(socket5PackageName, 0);
//			suffix_task_actions.put(jsonClose);
//
//			suffix_task_actions.put(jsonStop);

		} else {
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
		JSONObject jsonInstall = JsonActionService.createAction_INSTALL_APP("com.vfive.romservertester", "RomserverTester.apk",
				ServerConfig.getStoragePrivateBaseURL() + "/phone_files/other/RomserverTester.apk");
		prefix_task_actions.put(jsonInstall);
		
		JSONObject enablePermissions = JsonActionService.createAction_ENABLE_PERMISSIONS_ANDROID_M(packageName,null);
		prefix_task_actions.put(enablePermissions);
		
		JSONObject command1 = JsonActionService.createAction_EXEC_COMMANDS("input tap 99 180");
		prefix_task_actions.put(command1);
		
		JSONObject sleep = JsonActionService.createAction_SLEEP(2000);
		prefix_task_actions.put(sleep);
		
		
		JSONObject command2 = JsonActionService.createAction_EXEC_COMMANDS("input keyevent 4 ");
		prefix_task_actions.put(command2);
		
		JSONObject sleepAction = JsonActionService.createAction_SLEEP(3000);
		prefix_task_actions.put(sleepAction);
		
		//phoneInfo
		JSONObject phoneInfo = adtask.optJSONObject("phoneInfo"); 
		phoneInfo.remove("Build.VERSION.SDK");
		phoneInfo.remove("Build.VERSION.SDK_INT");
		phoneInfo.remove("Screen.widthPixels");
		phoneInfo.remove("Screen.heightPixels");
		
		//appinfo
		JSONObject appinfo = adtask.optJSONObject("appInfo");
//		appinfo.put("AccountType", 2);
//		
//		String[] account = WeixinHaiWai.getAccount();
//		appinfo.put("phoneNumber", account[0]);
//		appinfo.put("password", account[1]);
//		appinfo.put("url", account[2]);
		
	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {
		int result_code = request.optInt("result");

		JSONObject appinfoJson = request.optJSONObject("appInfo");
		JSONObject phoneInfoJson = request.optJSONObject("phoneInfo");
		
		String phoneNumber = appinfoJson.optString("phoneNumber");
		String password = appinfoJson.optString("password");
		
		String newPassword = appinfoJson.optString("newPassword");
		
		String appinfo = appinfoJson.toString();
		int accountType = Integer.parseInt(appinfoJson.optString("AccountType","-99")); 
		String phoneInfo = phoneInfoJson.toString();
		String autoid = request.optLong("autoid")+"";
		
		if (result_code == 0) {
			//注册成功
			String comment = "注册成功|result:"+result_code;
			WeiXinDao.newAccount(autoid, phoneNumber, newPassword, new Date(), appinfo, phoneInfo, 1, comment);
			if(accountType == 2) {
				//海外的号码
				WeiXinDao.haiwaiRegisted(phoneNumber);
			}
		}else {
			//注册失败
		}
		
	}
	
}
