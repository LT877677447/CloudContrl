/**
 * @author Administrator
 * 2019年2月27日 下午6:29:24 
 */
package com.kilotrees.action.task;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.model.po.ServerConfig;

public class QiMaoTaskAction implements ITaskAction {

	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		JSONArray adtasks = response.optJSONArray("tasks");
		JSONObject adtask = adtasks.optJSONObject(0);
		String dev_tag = response.optString("dev_tag");
		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");
		
//		JSONArray deletePaths = new JSONArray();
//		deletePaths.put("/data/local/rom/monitor/com.kmxs.reader_monitor.json");
//		JSONObject action_delete =JsonActionService.createAction_DELETE_FILES("com.kmxs.reader", deletePaths);
//		JsonActionService.insertAction(prefix_task_actions, action_delete, 0);
		
		//wuji
		JSONObject jsonVPN = new JSONObject();
		jsonVPN.put("action", "INSTALL_APP");
		jsonVPN.put("filename", "wuji_duli_20.apk");
		jsonVPN.put("packageName", "org.wuji");
		jsonVPN.put("file_url",
				ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/wuji_duli_20.apk");
		prefix_task_actions.put(jsonVPN);
		
		
		//socket5
//		JSONObject jsonInstall = new JSONObject();
//		jsonInstall.put("action", "INSTALL_APP");
//		jsonInstall.put("filename", "org.proxydroidApp.apk");
//		jsonInstall.put("packageName", "org.proxydroid");
//		jsonInstall.put("file_url", serverconfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk");
//		prefix_task_actions.put(jsonInstall);
//
//		JSONObject jsonStop = new JSONObject();
//		jsonStop.put("action", "STOP_APP");
//		jsonStop.put("filename", "org.proxydroidApp.apk");
//		jsonStop.put("packageName", "org.proxydroid");
//		jsonStop.put("file_url", serverconfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk");
//		prefix_task_actions.put(jsonStop);
//
//		JSONObject jsonOpen = new JSONObject();
//		jsonOpen.put("action", "OPEN_APP");
//		jsonOpen.put("filename", "org.proxydroidApp.apk");
//		jsonOpen.put("packageName", "org.proxydroid");
//		jsonOpen.put("file_url", serverconfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk");
//		prefix_task_actions.put(jsonOpen);
//
//		JSONObject jsonClose = new JSONObject();
//		jsonClose.put("action", "CLOSE_APP");
//		jsonClose.put("filename", "org.proxydroidApp.apk");
//		jsonClose.put("packageName", "org.proxydroid");
//		jsonClose.put("file_url", serverconfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk");
//		suffix_task_actions.put(jsonClose);
//
//		JSONObject jsonStop2 = new JSONObject();
//		jsonStop2.put("action", "STOP_APP");
//		jsonStop2.put("filename", "org.proxydroidApp.apk");
//		jsonStop2.put("packageName", "org.proxydroid");
//		jsonStop2.put("file_url", serverconfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk");
//		suffix_task_actions.put(jsonStop2);
		
		
		
		
	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {

	}

}
