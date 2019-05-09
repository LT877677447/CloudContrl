/**
 * @author Administrator
 * 2019年1月21日 下午6:26:22 
 */
package com.kilotrees.action.task;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.model.po.ServerConfig;

public class JinRiTouTiaoTaskAction implements ITaskAction {
	
	@Override
	public void handleTaskRequest(JSONObject reuqest,JSONObject response) throws Exception {
		// TODO Auto-generated method stub
		JSONArray adtasks = response.optJSONArray("tasks");
		try {
			JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
			JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");

			if (adtasks.length() > 0) {
				JSONObject jsonInstall = new JSONObject();
				jsonInstall.put("action", "INSTALL_APP"); 
				jsonInstall.put("filename", "jinritoutiao_707_1_1.apk"); //apk name
				jsonInstall.put("packageName", "com.ss.android.article.news");//script name
				jsonInstall.put("file_url", ServerConfig.getStorageBaseURL() + "/files/apks/jinritoutiao_707_1_1.apk");
				prefix_task_actions.put(jsonInstall);
				
//				JSONObject jsonInstall2 = new JSONObject();
//				jsonInstall2.put("action", "INSTALL_APK"); 
//				jsonInstall2.put("filename", "awaken_1_1.apk"); //apk name
//				jsonInstall2.put("packageName", "com.qianshu.awaken");//script name
//				jsonInstall2.put("file_url", serverconfig.getStorageBaseURL() + "/files/apks/awaken_1_1.apk");
//				prefix_task_actions.put(jsonInstall2);
				//appinfo  shared_package_name  "com.ss.android.article.news"
				
				JSONObject jsonUpdate = new JSONObject();
				jsonUpdate.put("action", "UPDATE_SCRIPT");
				jsonUpdate.put("filename", "com.ss.android.article.news.apk");
				jsonUpdate.put("packageName", "com.ss.android.article.news");
				jsonUpdate.put("scripturl_version", 9);
				jsonUpdate.put("scripturl", ServerConfig.getStorageBaseURL() + "/files/scripts/com.ss.android.article.news.apk");
				prefix_task_actions.put(jsonUpdate);
				
//				JSONObject jsonOpen = new JSONObject();
//				jsonOpen.put("action", "OPEN_APP");
//				jsonOpen.put("filename", "awaken_1_1.apk");
//				jsonOpen.put("packageName", "com.qianshu.awaken");
//				jsonOpen.put("file_url", serverconfig.getStorageBaseURL() + "/files/apks/awaken_1_1.apk");
//				prefix_task_actions.put(jsonOpen);

			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleTaskReport(JSONObject request,JSONObject response) throws Exception {
	}

}
