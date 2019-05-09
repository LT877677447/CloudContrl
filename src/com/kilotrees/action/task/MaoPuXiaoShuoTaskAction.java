package com.kilotrees.action.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;


public class MaoPuXiaoShuoTaskAction implements ITaskAction {

	/*
	 * { "TYPE64_2018-11-14": [30, 30, 50, 50], "TYPE65_2018-11-15": [30, 30,
	 * 50, 98] }
	 */
	private static final String jsonRootDir = "D:\\WebServer\\json\\";
	private static final String jsonPath = jsonRootDir + "maopu/maopuxiaoshuo.json";
	private static final String logPath = jsonRootDir + "maopu/maopuxiaoshuo.log";

	@Override
	public void handleTaskRequest(JSONObject request,JSONObject response) throws Exception {

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

			Boolean isDoing64 = false;
			
			String key64TypePrefix = "TYPE64";
			String key65TypePrefix = "TYPE65";
			String type64Key = key64TypePrefix + "_" + dateString;
			String type65Key = key65TypePrefix + "_" + dateString;
			
			JSONArray array64Type = jsonTaskExt.optJSONArray(type64Key);
			JSONArray array65Type = jsonTaskExt.optJSONArray(type65Key);
			
			if (array64Type.length() == 0) {
				isDoing64 = false;
			}
			if (array65Type.length() == 0) {
				isDoing64 = true;
			}
			
			if (array64Type.length() != 0 && array65Type.length() != 0) {
				isDoing64 = new Random().nextInt(3) == 1;
			}
			// get the job spec by key
			JSONArray array = isDoing64 ? array64Type : array65Type ;

			// 因为数据库预设是65包，改URL 65_1_1.apk to 64_1_1.apk
			if (isDoing64) {
				// 1. modify the download apk name
				JSONArray taskactions = response.optJSONArray("prefix_actions");
				JSONObject firstTaskAction = taskactions.optJSONObject(0);
				String sixFourApkName = "P33E48QMop00064_2_1.apk";
				firstTaskAction.put("filename", sixFourApkName);

				// 2. modify the download url: last path
				String downloadURL = firstTaskAction.optString("file_url");
				int lastIndex = downloadURL.lastIndexOf("/");
				String apkName = downloadURL.substring(lastIndex + 1);
				downloadURL = downloadURL.replace(apkName, sixFourApkName);
				firstTaskAction.put("file_url", downloadURL);
			}

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

					saveLog((isDoing64 ? key64TypePrefix : key65TypePrefix) + " -> "+ dev_tag +" 消费方式：" + payment + ": " + rmb);
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
	public void handleTaskReport(JSONObject request,JSONObject response) throws Exception {
	}
	
}
