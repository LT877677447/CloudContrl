/**
 * @author Administrator
 * 2019年2月22日 下午3:37:40 
 */
package com.kilotrees.action.task;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.model.po.ServerConfig;

public class QingChongWuTaskAction implements ITaskAction {

	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		JSONArray adtasks = response.optJSONArray("tasks");
		JSONObject adtask = adtasks.optJSONObject(0);
		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");

		try {
			if (adtasks.length() > 0) {
				String dev_tag = response.optString("dev_tag");
				// if (dev_tag.equals("AOS60007")) {
				// JSONObject jsonVPN = new JSONObject();
				// jsonVPN.put("action", "INSTALL_APP");
				// jsonVPN.put("filename", "wuji_duli_60.apk");
				// jsonVPN.put("packageName", "org.wuji");
				// jsonVPN.put("file_url", serverconfig.getStoragePrivateBaseURL() +
				// "/phone_files/update/wuji_duli_60.apk");
				// prefix_task_actions.put(jsonVPN);
				// }

				// if (dev_tag.equals("AOS60008") || dev_tag.equals("AOS60009")||
				// dev_tag.startsWith("AOS6001") ) {
				//socket5
				JSONObject jsonInstall = new JSONObject();
				jsonInstall.put("action", "INSTALL_APP");
				jsonInstall.put("filename", "org.proxydroidApp.apk");
				jsonInstall.put("packageName", "org.proxydroid");
				jsonInstall.put("file_url", ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk");
				prefix_task_actions.put(jsonInstall);

				JSONObject jsonStop = new JSONObject();
				jsonStop.put("action", "STOP_APP");
				jsonStop.put("packageName", "org.proxydroid");
				prefix_task_actions.put(jsonStop);

				JSONObject jsonOpen = new JSONObject();
				jsonOpen.put("action", "OPEN_APP");
				jsonOpen.put("packageName", "org.proxydroid");
				prefix_task_actions.put(jsonOpen);

				JSONObject jsonClose = new JSONObject();
				jsonClose.put("action", "CLOSE_APP");
				jsonClose.put("packageName", "org.proxydroid");
				suffix_task_actions.put(jsonClose);

				JSONObject jsonStop2 = new JSONObject();
				jsonStop2.put("action", "STOP_APP");
				jsonStop2.put("packageName", "org.proxydroid");
				suffix_task_actions.put(jsonStop2);
				// }
				JSONObject phoneInfo = adtask.optJSONObject("phoneInfo");
				phoneInfo.remove("Build.VERSION.SDK_INT");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {

	}

}
