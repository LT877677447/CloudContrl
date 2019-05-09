/**
 * @author Administrator
 * 2019年3月27日 下午7:02:31 
 */
package com.kilotrees.action.task;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.services.JsonActionService;

public class HuoXingZhiBoTaskAction implements ITaskAction{
	
	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		JSONArray adtasks = response.optJSONArray("tasks");
		JSONObject adtask = adtasks.optJSONObject(0);
		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");
		String strWuJi = "B6001 B6002 ";//使用无极设备
		Boolean bWuJi = false;
		String dev_tag = response.optString("dev_tag");
		
		if (adtasks.length() > 0) {
			if(!strWuJi.contains(dev_tag)) {
				// socket5 
				bWuJi = false;
				adtask.put("scriptTimeout", 600);
				String socket5_packageName = "org.proxydroid";
				String socket5_fileName = "org.proxydroidApp.apk";
				String socket5_appfileurl = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk";
				
				JSONObject jsonInstall = JsonActionService.createAction_INSTALL_APP(socket5_packageName, socket5_fileName, socket5_appfileurl);
				prefix_task_actions.put(jsonInstall);
				
				JSONObject jsonStop = JsonActionService.createAction_STOP_APP(socket5_packageName);
				prefix_task_actions.put(jsonStop);
				
				JSONObject jsonOpen = JsonActionService.createAction_OPEN_APP(socket5_packageName, 0);
				prefix_task_actions.put(jsonOpen);
				
				JSONObject jsonClose = JsonActionService.createAction_CLOSE_APP(socket5_packageName, 0);
				suffix_task_actions.put(jsonClose);
				
				suffix_task_actions.put(jsonStop);
				//end socket5
				
			}else {
				//wuji 
				bWuJi = true;
				String appDownloadURL = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/wuji_duli_60.apk";
				JSONObject wujiVPN_Action = JsonActionService.createAction_INSTALL_APP("org.wuji", "wuji_duli_60.apk", appDownloadURL);
				prefix_task_actions.put(wujiVPN_Action);
				//end 
				
			}
			
			// 处理phoneInfo
			JSONObject phoneInfo = adtask.optJSONObject("phoneInfo");
			
			phoneInfo.remove("Build.VERSION.SDK");
			phoneInfo.remove("Build.VERSION.SDK_INT");
			//end
			
		}
		
		
		
	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {
		
	}

}
