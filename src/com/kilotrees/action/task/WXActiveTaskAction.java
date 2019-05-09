/**
 * @author Administrator
 * 2019年4月30日 下午6:30:18 
 */
package com.kilotrees.action.task;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.dao.deviceinfodao;
import com.kilotrees.dao.vpninfodao;
import com.kilotrees.dao.task.WXActiveTaskDao;
import com.kilotrees.model.bo.TaskBase;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.vpninfo;
import com.kilotrees.services.JsonActionService;

public class WXActiveTaskAction implements ITaskAction {
	private static Logger log = Logger.getLogger(WXActiveTaskAction.class);

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

		// VPN
		String devicesUsingWuJiVPN = "";// 使用无极设备
		Boolean isUsingWuJiVPN = devicesUsingWuJiVPN.contains(dev_tag);
		if (!isUsingWuJiVPN) {
			// socket5
			String socket5PackageName = "org.proxydroid";
			String socket5FileName = "org.proxydroidApp.apk";
			String socket5APKurl = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk";
			
			//测试用局域网，注释掉下面
//			JSONObject jsonInstall = JsonActionService.createAction_INSTALL_APP(socket5PackageName, socket5FileName, socket5APKurl);
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

		// prefix action
		JSONObject jsonInstall = JsonActionService.createAction_INSTALL_APP("com.vfive.romservertester", "RomserverTester.apk",
				ServerConfig.getStoragePrivateBaseURL() + "/phone_files/other/RomserverTester.apk");
		prefix_task_actions.put(jsonInstall);

		JSONObject enablePermissions = JsonActionService.createAction_ENABLE_PERMISSIONS_ANDROID_M(packageName, null);
		prefix_task_actions.put(enablePermissions);

		JSONObject command1 = JsonActionService.createAction_EXEC_COMMANDS("input tap 99 180");
		prefix_task_actions.put(command1);

		JSONObject sleep = JsonActionService.createAction_SLEEP(2000);
		prefix_task_actions.put(sleep);

		JSONObject command2 = JsonActionService.createAction_EXEC_COMMANDS("input keyevent 4 ");// 返回的actions
		prefix_task_actions.put(command2);

		JSONObject sleepAction = JsonActionService.createAction_SLEEP(3000);
		prefix_task_actions.put(sleepAction);

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

		// phoneInfo
		JSONObject phoneInfo = adtask.optJSONObject("phoneInfo");
		phoneInfo.remove("Build.VERSION.SDK");
		phoneInfo.remove("Build.VERSION.SDK_INT");
		phoneInfo.remove("Screen.widthPixels");
		phoneInfo.remove("Screen.heightPixels");
		
		//appinfo
//		JSONObject appinfo = adtask.optJSONObject("appInfo");
//		appinfo.put("phase", "active");
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
