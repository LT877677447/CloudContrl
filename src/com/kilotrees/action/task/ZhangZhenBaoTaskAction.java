/**
 * @author Administrator
 * 2019年2月22日 下午2:50:40 
 */
package com.kilotrees.action.task;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.services.JsonActionService;

public class ZhangZhenBaoTaskAction implements ITaskAction {

	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		JSONArray adtasks = response.optJSONArray("tasks");
		JSONObject adtask = adtasks.optJSONObject(0);
		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");
		String dev_tag = response.optString("dev_tag");
		
		//只让AOS6 设备做任务
		if(!dev_tag.startsWith("AOS")) {
			response.put("tasks", new JSONArray());
			response.put("prefix_actions", new JSONArray());
			response.put("suffix_actions", new JSONArray());
		}
		
		
		if (adtasks.length() > 0) {
			//socket5
			String socket5_fileName = "org.proxydroidApp.apk";
			String socket5_packageName = "org.proxydroid";
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
		}

	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {

	}

}
