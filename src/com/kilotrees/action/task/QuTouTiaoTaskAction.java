package com.kilotrees.action.task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;


public class QuTouTiaoTaskAction implements ITaskAction {

	@Override
	public void handleTaskRequest(JSONObject request,JSONObject response) throws Exception {
		try {
			JSONArray adtasks = response.optJSONArray("tasks");
			if (adtasks.length() > 0) {
				JSONObject jsonObject = adtasks.optJSONObject(0);
				JSONObject phoneInfo = jsonObject.optJSONObject("phoneInfo");
				phoneInfo.put("Build.VERSION.SDK_INT", 19);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void handleTaskReport(JSONObject request,JSONObject response) throws Exception {
	}

	
}
