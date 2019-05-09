package com.kilotrees.action.task;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.model.po.ServerConfig;

public class XunLeiChongZhiTaskAction implements ITaskAction {

	@Override
	public void handleTaskRequest(JSONObject request,JSONObject response) throws Exception {
		try {
			JSONArray adtasks = response.optJSONArray("tasks");
			JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");

			if (adtasks.length() > 0) {
				JSONObject jsonOpen = new JSONObject();
				jsonOpen.put("action", "INSTALL_APP");
				jsonOpen.put("filename", "qq-6.5.apk");
				jsonOpen.put("packageName", "com.tencent.mobileqq");
				jsonOpen.put("file_url", ServerConfig.getStorageBaseURL() + "/files/apks/qq-6.5.apk");
				prefix_task_actions.put(jsonOpen);
				
				JSONObject jsonClear = new JSONObject();
				jsonClear.put("action", "CLEAR_APP");
				jsonClear.put("filename", "qq-6.5.apk");
				jsonClear.put("packageName", "com.tencent.mobileqq");
				jsonClear.put("file_url", ServerConfig.getStorageBaseURL() + "/files/apks/qq-6.5.apk");
				prefix_task_actions.put(jsonClear);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public void handleTaskReport(JSONObject request,JSONObject response) throws Exception {
	}

}
