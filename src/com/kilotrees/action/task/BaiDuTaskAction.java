/**
 * @author Administrator
 * 2019年2月22日 下午2:50:40 
 */
package com.kilotrees.action.task;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.services.JsonActionService;

public class BaiDuTaskAction implements ITaskAction {
	private static List<String> sDvices = new ArrayList<>();
	private String packageName = "com.baidu.searchbox";
	
	
	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		JSONArray adtasks = response.optJSONArray("tasks");
		JSONObject adtask = adtasks.optJSONObject(0);
		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");
		
		if (adtasks.length() > 0) {
			JSONObject JsonClear = new JSONObject();
			JsonClear.put("action", "UNINSTALL_APP");
			JsonClear.put("packageName", "com.baidu.searchbox");
			suffix_task_actions.put(JsonClear);
			
			String dev_tag = response.optString("dev_tag");
			//socket5
			JSONObject jsonInstall = new JSONObject();
			jsonInstall.put("action", "INSTALL_APP");
			jsonInstall.put("filename", "org.proxydroidApp.apk");
			jsonInstall.put("packageName", "org.proxydroid");
			jsonInstall.put("file_url", ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk");
			prefix_task_actions.put(jsonInstall);

			JSONObject jsonStop = new JSONObject();
			jsonStop.put("action", "STOP_APP");
			jsonStop.put("filename", "org.proxydroidApp.apk");
			jsonStop.put("packageName", "org.proxydroid");
			jsonStop.put("file_url", ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk");
			prefix_task_actions.put(jsonStop);

			JSONObject jsonOpen = new JSONObject();
			jsonOpen.put("action", "OPEN_APP");
			jsonOpen.put("filename", "org.proxydroidApp.apk");
			jsonOpen.put("packageName", "org.proxydroid");
			jsonOpen.put("file_url", ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk");
			prefix_task_actions.put(jsonOpen);

			JSONObject jsonClose = new JSONObject();
			jsonClose.put("action", "CLOSE_APP");
			jsonClose.put("filename", "org.proxydroidApp.apk");
			jsonClose.put("packageName", "org.proxydroid");
			jsonClose.put("file_url", ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk");
			suffix_task_actions.put(jsonClose);

			JSONObject jsonStop2 = new JSONObject();
			jsonStop2.put("action", "STOP_APP");
			jsonStop2.put("filename", "org.proxydroidApp.apk");
			jsonStop2.put("packageName", "org.proxydroid");
			jsonStop2.put("file_url", ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk");
			suffix_task_actions.put(jsonStop2);
			//end socket5
			JSONObject phoneInfo = adtask.optJSONObject("phoneInfo"); 
			phoneInfo.remove("Build.VERSION.SDK_INT");
			
			//处理每个设备第一次拿任务
			boolean deviceDone = false;
			for(String string : sDvices) {
				if(string.equals(dev_tag)) {
					deviceDone = true;
					break;
				}
				continue;	
			}
			
			if(!deviceDone) {
				JSONArray array = new JSONArray();
				array.put("/sdcard/.ccache/");
				
				JSONObject jsonClearDirs = JsonActionService.createAction_CLEAR_DIRS(array);
				prefix_task_actions.put(jsonClearDirs);
				sDvices.add(dev_tag);
			}
			//end	
			
			
		}

	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {

	}

}
