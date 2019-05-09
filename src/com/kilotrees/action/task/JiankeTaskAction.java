/**
 * @author Administrator
 * 2019年1月22日 下午3:33:13 
 */
package com.kilotrees.action.task;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.util.StringUtil;

public class JiankeTaskAction implements ITaskAction {
	private static Logger log = Logger.getLogger(JiankeTaskAction.class);	
	@Override
	public void handleTaskRequest(JSONObject request,JSONObject response) throws Exception {
		String dev_tag = response.optString("dev_tag");
		if (StringUtil.isStringEmpty(dev_tag)) {
			return;
		}
		
		
		boolean isInSocks5Devices = /*dev_tag.equals("phone004") || dev_tag.equals("phone006") || dev_tag.equals("phone000") ||
				dev_tag.equals("phone002") ||*/ dev_tag.equals("A001") || dev_tag.equals("A002") || dev_tag.equals("A003")
				|| dev_tag.equals("A004")|| dev_tag.equals("A005")|| dev_tag.equals("A006")
				|| dev_tag.equals("A007")|| dev_tag.equals("A008")|| dev_tag.equals("A009")
				|| dev_tag.equals("A010")|| dev_tag.equals("A022");
		if (isInSocks5Devices){
			JSONArray adtasks = response.optJSONArray("tasks");
			try {
				JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
				JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");
				log.info("adtasks.length() : " + adtasks.length() + ",,dev_tag : " + dev_tag);
				if (adtasks.length() > 0) {
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
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	@Override
	public void handleTaskReport(JSONObject request,JSONObject response) throws Exception {
	}
}
