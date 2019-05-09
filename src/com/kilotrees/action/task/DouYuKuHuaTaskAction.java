package com.kilotrees.action.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;


public class DouYuKuHuaTaskAction implements ITaskAction {

	/*
	 * { "2018-12-19": [ 6, 6, 30, 98 ],
	 */
	private static final String jsonRootDir = "D:\\WebServer\\json\\";
	private static final String jsonPath = jsonRootDir + "douyu/DouYuKuHua_pay.json";
	private static final String logPath = jsonRootDir + "douyu/DouYuKuHua_pay.log";

	@Override
	public void handleTaskRequest(JSONObject request,JSONObject response) throws Exception {
		
		//2018-12-20 在doLogic这里判断下，如果是留存就不做了
		boolean isRemain = false;
		try {
			JSONArray adtasks = response.optJSONArray("tasks");
			JSONObject jsonObject = adtasks.optJSONObject(0);
			int rid = jsonObject.optInt("rid");

			JSONObject appinfo = jsonObject.optJSONObject("appInfo");
			int remain_sep_days = appinfo.optInt("remain_sep_days");

			isRemain = rid != 0 && remain_sep_days != 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(isRemain) 
			return;
		
		try {
			int rmb = 0;

			// create json & log file if not existed
			File jsonFile = new File(jsonPath);

			if (!jsonFile.exists()) {
				jsonFile.getParentFile().mkdirs();
				jsonFile.createNewFile();
			}

			// read the json file
			FileInputStream fins = new FileInputStream(jsonFile);
			byte[] buf = new byte[(int) jsonFile.length()];
			fins.read(buf);
			fins.close();
			String jsonString = new String(buf, "utf-8");
			JSONObject jsonTaskExt = new JSONObject(jsonString);

			// get the job spec key
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String dateString = simpleDateFormat.format(new Date());

			JSONArray array = jsonTaskExt.optJSONArray(dateString);

			// if have jobs
			if (array.length() > 0) {
				rmb = array.optInt(0);

				// synchronize the json file
				array.remove(0);
				FileWriter jsonWriter = new FileWriter(jsonPath);
				jsonWriter.write(jsonTaskExt.toString());
				jsonWriter.flush();
				jsonWriter.close();
			}

			// MaoPu no need VPN
			JSONArray adtasks = response.optJSONArray("tasks");
			for (int i = 0; i < adtasks.length(); i++) {
				JSONObject object = adtasks.optJSONObject(i);

				if (rmb != 0) {
					object.put("RMB", rmb);

					String dev_tag = response.optString("dev_tag");
					Boolean isWeChat = dev_tag.equals("phone000");
					String payment = isWeChat ? "WeChat" : "Alipay";
					object.put("payment", payment);

					saveLog(" -> " + dev_tag + " 消费方式：" + payment + ": " + rmb);
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private static void saveLog(String log) {
		File logFile = new File(logPath);
		try {
			if (!logFile.exists()) {
				logFile.getParentFile().mkdirs();
				logFile.createNewFile();
			}

			// save the log
			String timeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			FileWriter logWriter = new FileWriter(logFile, true);
			String string = timeString + ": " + log + "\r\n";
			logWriter.append(string);
			logWriter.flush();
			logWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void handleTaskReport(JSONObject reqeust,JSONObject response) throws Exception {
	}

}
