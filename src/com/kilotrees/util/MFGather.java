package com.kilotrees.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import com.kilotrees.dao.connectionmgr;

public class MFGather {
	
	public String officalSite = "mofangapp.com:3002";
	public String username = "@lin888888";
	public String password = "1902213295";
	
	public String loginURL = "http://v1-login.v1.mofangapp.com/1/login";
	public String allTasksURL = "http://lin888888.v1.mofangapp.com/1/classes/Task";
	public String taskDetailURL = "http://lin888888.v1.mofangapp.com/1/classes/RetentionBak";
	
	public JSONObject loginResponse = null;
	public JSONObject allTasksResponse = null;
	
	public MFGather() {
		
	}
	
	public JSONObject login() {
		try {
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("password", password);
			jsonObject.put("username", username);
			
			jsonObject.put("_ApplicationId", "5btyhy0xy1rDHjriDePtLARpbaopMSIw");
			jsonObject.put("_ClientVersion", "js0.0.1");
			jsonObject.put("_method", "GET");
			
			JSONObject response = IHTTPUtil.post(loginURL, getHeaders(), jsonObject.toString(), 0);
			
			return response;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject tasks() {
		try {
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("count", 1);
			jsonObject.put("limit", 1000);
			jsonObject.put("keys", "twologincount,twologin,finalretencount,taskid,taskname,apkname,newcount,retentioncount,finaltaskresult,taskresult,taskcount,phones,offline,state,retensetting,note,jiaoben,apk,taskCreatedAt,taskObjectId");
			jsonObject.put("order", "-createdAt");
			jsonObject.put("skip", 0);
			
			jsonObject.put("_ApplicationId", loginResponse.optString("appid"));
			jsonObject.put("_ClientVersion", "js0.0.1");
			jsonObject.put("_method", "GET");
			jsonObject.put("_InstallationId", "0bcadd0c-54b5-2aaf-786f-41f4069390cd");
			jsonObject.put("_MasterKey", loginResponse.optString("restid"));
			
			JSONObject whereJsonObject = new JSONObject();
			whereJsonObject.put("page", 1);
			
			jsonObject.put("where", whereJsonObject);
			
			JSONObject response = IHTTPUtil.post(allTasksURL, getHeaders(), jsonObject.toString(), 0);
			return response;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject detail(String objectId) {
		try {
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("count", 1);
			jsonObject.put("limit", 1000);
			jsonObject.put("order", "-createdAt");
			jsonObject.put("skip", 0);
			
			jsonObject.put("_ApplicationId", loginResponse.optString("appid"));
			jsonObject.put("_ClientVersion", "js0.0.1");
			jsonObject.put("_method", "GET");
			jsonObject.put("_InstallationId", "0bcadd0c-54b5-2aaf-786f-41f4069390cd");
			jsonObject.put("_MasterKey", loginResponse.optString("restid"));
			
			
			JSONObject whereJsonObject = new JSONObject();
			jsonObject.put("where", whereJsonObject);
			
			JSONObject taskJsonObject = new JSONObject();
			taskJsonObject.put("className", "Task");
			taskJsonObject.put("objectId", objectId);
			taskJsonObject.put("__type", "Pointer");
			whereJsonObject.put("task", taskJsonObject);
			
			
			JSONObject response = IHTTPUtil.post(taskDetailURL, getHeaders(), jsonObject.toString(), 0);
			
			return response;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	private static Map<String, Object> getHeaders() {
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Content-Type", "text/plain");
		headers.put("Origin", "http://mofangapp.com:3002");
		headers.put("Referer", "http://mofangapp.com:3002");
		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
		return headers;
	}
	
	public static void main(String[] args) {
		MFGather main = new MFGather();
		main.loginResponse = main.login();
		
		main.allTasksResponse = main.tasks();
		//YmixFNUGyl
		JSONArray tasks = main.allTasksResponse.optJSONArray("results");
		
		//464=yiyuan;625=oppo;626=vivo;627=huawei;628=xiaomi;629=qq
		String desTask = "464 625 626 627 628 629";
		for(int i = 0; i < tasks.length(); i++) {
			JSONObject result = tasks.optJSONObject(i);
			String taskObjectId = result.optString("objectId");
			int taskid = result.optInt("taskid");
			
			if(desTask.contains(""+taskid)) {
				
				int sup_objectNo;
				String sup_objectId;
				int twologincount;
				int newcount;
				int dayreten;
				String objectId;
				Date date_origin = null;
				Date date_grab = new Date();
				
				sup_objectNo = taskid;
				sup_objectId = taskObjectId;
				
				JSONObject json = main.detail(taskObjectId);
				JSONArray details = json.optJSONArray("results");
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				
				for(int i2=0;i2<details.length();i2++) {
					JSONObject detail = details.optJSONObject(i2);
					twologincount = detail.optInt("twologincount");
					newcount = detail.optInt("newcount");
					dayreten = detail.optInt("dayreten");
					objectId = detail.optString("objectId");
					
					String temp_date_origin = detail.optString("createdAt");//2019-04-25T16:00:30.564Z
					try {
						temp_date_origin = temp_date_origin.substring(0, 10)+" "+temp_date_origin.substring(11,13)+":"+temp_date_origin.substring(14, 16)+":"+temp_date_origin.substring(17,19)+"."+temp_date_origin.substring(20,23);
						date_origin = format.parse(temp_date_origin);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					newMoFang(sup_objectNo, sup_objectId, twologincount, newcount, dayreten, objectId, date_origin, date_grab);
				}
			}
		}
	}
	
	@Test
	public void t1() {
		String str = "2019-04-25T16:00:30.564Z";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		str = str.substring(0, 10)+" "+str.substring(11,13)+":"+str.substring(14, 16)+":"+str.substring(17,19)+":"+str.substring(20,23);
		System.out.println(str);
		
	}
	
	public static void newMoFang(int sup_objectNo,String sup_objectId,int twologincount,int newcount,int dayreten,String objectId,Date date_origin,Date date_grab) {
		String sql = "insert into [yun].[dbo].[tb_mofang](sup_objectNo,sup_objectId,twologincount,newcount,dayreten,objectId,date_origin,date_grab) values(?,?,?,?,?,?,?,?)";
		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement st = null;
		try {
			st = connection.prepareStatement(sql);
			st.setInt(1, sup_objectNo);
			st.setString(2, sup_objectId);
			st.setInt(3, twologincount);
			st.setInt(4, newcount);
			st.setInt(5, dayreten);
			st.setString(6, objectId);
			st.setTimestamp(7, new Timestamp(date_origin.getTime()));
			st.setTimestamp(8, new Timestamp(date_grab.getTime()));
			st.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			connectionmgr.getInstance().closeConnection(connection,st);
		}
		
	}
	
}
