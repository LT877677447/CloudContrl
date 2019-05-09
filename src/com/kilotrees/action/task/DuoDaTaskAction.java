/**
 * @author Administrator
 * 2019年4月17日 下午3:30:47 
 */
package com.kilotrees.action.task;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.services.JsonActionService;

public class DuoDaTaskAction implements ITaskAction{

	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		JSONArray adtasks = response.optJSONArray("tasks");
		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");
		String dev_tag = response.optString("dev_tag");

		if (adtasks.length() == 0) {
			return;
		}
		
		JSONObject adtask = adtasks.optJSONObject(0);
		String devicesUsingWuJiVPN = "B6001 B6002 B6003 B6004";// 使用无极设备
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
			String appDownloadURL = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/wuji_duli_60.apk";
			JSONObject wujiVPN_Action = JsonActionService.createAction_INSTALL_APP("org.wuji", "wuji_duli_60.apk",
					appDownloadURL);
			prefix_task_actions.put(wujiVPN_Action);
		}
		
		JSONObject phoneInfo = adtask.optJSONObject("phoneInfo"); 
		phoneInfo.remove("Build.VERSION.SDK");
		phoneInfo.remove("Build.VERSION.SDK_INT");
		
	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {
		
	}

}
