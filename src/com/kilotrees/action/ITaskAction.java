package com.kilotrees.action;

import org.json.JSONObject;

public interface ITaskAction {

	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception;

	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception;

}
