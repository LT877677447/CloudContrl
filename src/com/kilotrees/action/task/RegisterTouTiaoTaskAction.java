/**
 * @author Administrator
 * 2019年2月27日 下午4:04:19 
 */
package com.kilotrees.action.task;

import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;

public class RegisterTouTiaoTaskAction implements ITaskAction{

	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
//		JSONArray adtasks = response.optJSONArray("tasks");
//		JSONObject adtask = adtasks.optJSONObject(0);
//		String dev_tag = response.optString("dev_tag");
//		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
//		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");
//		
//		if (adtasks.length() > 0) {
//			JSONObject paraJson = new JSONObject();
//			String adv_id = adtask.optString("adv_id");
//			long autoid = 0;
//			String sAutoid = request.optString("autoid");
//			if (!StringUtil.isStringEmpty(sAutoid)) {
//				autoid = Long.parseLong(sAutoid);
//			}
//			paraJson.put("action", 1);
//			paraJson.put("adv_id", adv_id);
//			paraJson.put("autoid", autoid);
//			paraJson.put("dev_tag", dev_tag);
//			JSONObject qqJson = qqaccount_service.getInstance().handleRequest(paraJson);
//			String qqnum = qqJson.optString("qqnum");
//			String pass = qqJson.optString("pass");
//			JSONObject appInfo = adtask.optJSONObject("appInfo");
//			appInfo.put("qqnum", qqnum);
//			appInfo.put("pass", pass);
//			
//			
//			
//			
//			
//		}
		
		
		
	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	
	
}
