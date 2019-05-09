package com.kilotrees.action.task;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.model.po.ServerConfig;

public class MaoPuXiaoShuoFreeTaskAction implements ITaskAction {

	@Override
	public void handleTaskRequest(JSONObject request,JSONObject response) throws Exception {
		JSONArray adtasks = response.optJSONArray("tasks");
		try {
			JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
			JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");

			if (adtasks.length() > 0) {
				JSONObject jsonOpen = new JSONObject();
				jsonOpen.put("action", "OPEN_APP");
				jsonOpen.put("filename", "org.proxydroidApp.apk");
				jsonOpen.put("packageName", "org.proxydroid");
				jsonOpen.put("file_url", ServerConfig.getStorageBaseURL() + "/files/apks/org.proxydroidApp.apk");
				prefix_task_actions.put(jsonOpen);

				JSONObject jsonClose = new JSONObject();
				jsonClose.put("action", "CLOSE_APP");
				jsonClose.put("filename", "org.proxydroidApp.apk");
				jsonClose.put("packageName", "org.proxydroid");
				jsonClose.put("file_url", ServerConfig.getStorageBaseURL() + "/files/apks/org.proxydroidApp.apk");
				suffix_task_actions.put(jsonClose);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void handleTaskReport(JSONObject request,JSONObject response) throws Exception {
	}

}
