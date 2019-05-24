/**
 * @author Administrator
 * 2019年3月20日 下午2:54:00 
 */
package com.kilotrees.action.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.dao.phonetypedao;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.phonetype;
import com.kilotrees.services.JsonActionService;
import com.kilotrees.services.phonetype_service;
import com.kilotrees.util.FileUtil;
import com.kilotrees.util.JSONObjectUtil;
import com.kilotrees.util.StringUtil;

public class OPPOMarketTaskAction implements ITaskAction {

	private static Integer phoneInfoIndex2Take = 0;
	private static List<JSONObject> phoneInfoList = new ArrayList<>();

	private static List<String> firstDoThisJobDvices = new ArrayList<>();// 设备第一次做此任务

	private static final String RootDir = "D:\\WebServer\\json\\";
	private static final String configJsonPath = RootDir + "OPPOMarket/OPPOMarket.json";
	private static final String logPath = RootDir + "OPPOMarket/OPPOMarket.log";

	static {
			
		try {
			List<phonetype> p1 = phonetypedao.getOPPOPhoneInfo("tb_phonetype_tmp", "oppo");
			for (int i = 0; i < p1.size(); i++) {
				JSONObject phoneInfo = new JSONObject(p1.get(i).getPhone_info());
				phoneInfoList.add(phoneInfo);
			}

			List<phonetype> p2 = phonetypedao.getOPPOPhoneInfo("tb_phonetype", "oppo");
			for (int i = 0; i < p2.size(); i++) {
				JSONObject phoneInfo = new JSONObject(p2.get(i).getPhone_info());
				phoneInfoList.add(phoneInfo);
			}

			List<phonetype> p3 = phonetypedao.getOPPOPhoneInfo("tb_phonetype_tmp", "oneplus");
			for (int i = 0; i < p3.size(); i++) {
				JSONObject phoneInfo = new JSONObject(p3.get(i).getPhone_info());
				phoneInfoList.add(phoneInfo);
			}

			List<phonetype> p4 = phonetypedao.getOPPOPhoneInfo("tb_phonetype", "oneplus");
			for (int i = 0; i < p4.size(); i++) {
				JSONObject phoneInfo = new JSONObject(p4.get(i).getPhone_info());
				phoneInfoList.add(phoneInfo);
			}

			for (int i = 0; i < phoneInfoList.size(); i++) {
				JSONObject phoneInfo = phoneInfoList.get(i);
				// int sdkVersion = phoneInfo.optInt("Build.VERSION.SDK_INT");
				// if (sdkVersion < 23) {
				phoneInfo.remove("Build.VERSION.SDK");
				phoneInfo.remove("Build.VERSION.SDK_INT");
				// }
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		JSONArray adtasks = response.optJSONArray("tasks");
		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");
		String dev_tag = response.optString("dev_tag");
		
		if (adtasks.length() == 0) {
			return;
		}

		String downloadAppTag = getTodayNeedDownloadAppTag();
		if (downloadAppTag == null) {
			response.put("tasks", new JSONArray());
			response.put("prefix_actions", new JSONArray());
			response.put("suffix_actions", new JSONArray());
			return;
		}

		JSONObject adtask = adtasks.optJSONObject(0);
//		String devicesUsingWuJiVPN = "B6001 B6002 B6003 B6004 ";// 使用无极设备
		String devicesUsingWuJiVPN = "";// 使用无极设备
		Boolean isUsingWuJiVPN = devicesUsingWuJiVPN.contains(dev_tag);

		// 指定设备设备才做
		if (!dev_tag.startsWith("A6") && !dev_tag.startsWith("B6") && !dev_tag.startsWith("M")) {
			response.put("tasks", new JSONArray());
		}

		if (!isUsingWuJiVPN) {
			// socket5

			adtask.put("scriptTimeout", 600);
			String socket5PackageName = "org.proxydroid";
			String socket5FileName = "org.proxydroidApp.apk";
			String socket5APKurl = ServerConfig.getStoragePrivateBaseURL()
					+ "/phone_files/update/org.proxydroidApp.apk";

			JSONObject jsonInstall = JsonActionService.createAction_INSTALL_APP(socket5PackageName, socket5FileName,
					socket5APKurl);
			prefix_task_actions.put(jsonInstall);

			JSONObject jsonStop = JsonActionService.createAction_STOP_APP(socket5PackageName);
			prefix_task_actions.put(jsonStop);

			JSONObject jsonOpen = JsonActionService.createAction_OPEN_APP(socket5PackageName, 0);
			prefix_task_actions.put(jsonOpen);

			JSONObject jsonClose = JsonActionService.createAction_CLOSE_APP(socket5PackageName, 0);
			suffix_task_actions.put(jsonClose);

			suffix_task_actions.put(jsonStop);

		} else {

			// wuji VPN
			String appDownloadURL = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/wuji_duli_60.apk";
			JSONObject wujiVPN_Action = JsonActionService.createAction_INSTALL_APP("org.wuji", "wuji_duli_60.apk",
					appDownloadURL);
			prefix_task_actions.put(wujiVPN_Action);

		}

		// 处理phoneInfo
		JSONObject phinfo = new JSONObject();
		phinfo = phoneInfoList.get(phoneInfoIndex2Take);
		phoneInfoIndex2Take++;
		phoneInfoIndex2Take = phoneInfoIndex2Take % phoneInfoList.size();

		JSONObject staticInfo = phonetype_service.getStaticMatchingInfo(phinfo);
		JSONObjectUtil.mergeJSONObject(phinfo, staticInfo);

		JSONObject stableInfo = phonetype_service.reandomTheStablePhoneInfo(phinfo);
		JSONObjectUtil.mergeJSONObject(phinfo, stableInfo);

		JSONObject unstableInfo = phonetype_service.reandomTheUnstablePhoneInfo(phinfo);
		JSONObjectUtil.mergeJSONObject(phinfo, unstableInfo);

		phinfo.remove("Screen.widthPixels");
		phinfo.remove("Screen.heightPixels");

		adtask.put("phoneInfo", phinfo);
		// end 处理phoneInfo

		// 处理每个设备第一次拿任务
		boolean deviceAlreadyDone = false;
		for (String string : firstDoThisJobDvices) {
			if (string.equals(dev_tag)) {
				deviceAlreadyDone = true;
				break;
			}
			continue;
		}

		if (!deviceAlreadyDone) {
			JSONArray array = new JSONArray();
			array.put("/sdcard/.ccache/");
			array.put("/data/local/rom/cache/");

			JSONObject jsonClearDirs = JsonActionService.createAction_CLEAR_DIRS(array);
			prefix_task_actions.put(jsonClearDirs);
			firstDoThisJobDvices.add(dev_tag);
		}
		// end

		// downloadAppList下发下载的APP
		JSONObject appinfo = adtask.optJSONObject("appInfo");

		JSONObject downloadAppJson = getConfigJsonForDownloadAppTag(downloadAppTag);
		String downloadAppName = downloadAppJson.optString("downloadAppName");
		String downloadSearchName = downloadAppJson.optString("downloadSearchName");
		String downloadPackageName = downloadAppJson.optString("downloadPackageName");
		String downloadCachePackageId = downloadAppJson.optString("downloadCachePackageId");

		appinfo.put("downloadAppTag", downloadAppTag);
		appinfo.put("downloadAppName", downloadAppName);
		appinfo.put("downloadSearchName", downloadSearchName);
		appinfo.put("downloadPackageName", downloadPackageName);
		appinfo.put("downloadCachePackageId", downloadCachePackageId);
		// end

		String userCenterPackageName = "com.oppo.usercenter";
		String installerPackageName = "com.android.packageinstaller";
		
		JSONArray sharedPackageNames = new JSONArray();
		sharedPackageNames.put(downloadPackageName);
		sharedPackageNames.put(userCenterPackageName);
		sharedPackageNames.put(installerPackageName);
		adtask.put("shared_packages_names", sharedPackageNames);
		
		JSONObject uninstallAction = JsonActionService.createAction_UNINSTALL_APP(downloadPackageName);
		suffix_task_actions.put(uninstallAction);

		JSONObject uninstallAction_UserCenter = JsonActionService.createAction_UNINSTALL_APP(userCenterPackageName);
		prefix_task_actions.put(uninstallAction_UserCenter);

		JSONObject installAction_UserCenter = JsonActionService.createAction_INSTALL_APP(userCenterPackageName,
				"OppoUsercenter_1_1.apk", ServerConfig.getStorageBaseURL() + "/files/apks/OppoUsercenter_1_1.apk");
		prefix_task_actions.put(installAction_UserCenter);

		String scriptURLBase = ServerConfig.getStorageBaseURL() + "/files/scripts/";
		JSONObject action_UPDATE_SCRIPT_1 = JsonActionService.createAction_UPDATE_SCRIPT(userCenterPackageName,
				scriptURLBase + userCenterPackageName + ".apk", 5);
		JSONObject action_UPDATE_SCRIPT_2 = JsonActionService.createAction_UPDATE_SCRIPT(installerPackageName,
				scriptURLBase + installerPackageName + ".apk", 5);

		prefix_task_actions.put(action_UPDATE_SCRIPT_1);
		prefix_task_actions.put(action_UPDATE_SCRIPT_2);

		JSONObject stopAction_UserCenter = JsonActionService.createAction_STOP_APP(userCenterPackageName);
		prefix_task_actions.put(stopAction_UserCenter);

		JSONObject startAction_UserCenter = JsonActionService.createAction_START_APP(userCenterPackageName);
		prefix_task_actions.put(startAction_UserCenter);

		JSONObject clearAction_UserCenter = JsonActionService.createAction_CLEAR_APP(userCenterPackageName);
		suffix_task_actions.put(clearAction_UserCenter);

		suffix_task_actions.put(uninstallAction_UserCenter);
		// end

		// 是否是进详情页去下载
		appinfo.put("is_enter_detail_page_to_download", new Random().nextInt(5) == 0);

		// 安装完是否打开APP
		appinfo.put("is_open_app_after_install", new Random().nextBoolean());

		// 处理App的下载方式
		boolean is_useing_cache_to_download = new Random().nextInt(2) == 0;
		if (isUsingWuJiVPN || downloadCachePackageId.isEmpty()) {
			is_useing_cache_to_download = false;
		}
		appinfo.put("is_useing_my_own_wifi_to_download", is_useing_cache_to_download);

		if (is_useing_cache_to_download == true) {
			String packageIdCacheFileName = downloadCachePackageId + ".apk.nrdownload";
			String res_download_url = ServerConfig.getStorageBaseURL() + "/files/resources/oppoMarket/"
					+ packageIdCacheFileName;
			JSONObject res_download_action = JsonActionService.createAction_DOWNLOAD_RESOURCE(packageIdCacheFileName,
					res_download_url);
			prefix_task_actions.put(res_download_action);
			return;
		}

		if (!isUsingWuJiVPN) {

			int value = new Random().nextInt(10);
			if (value < 1) {
				// 不用自己wifi下载
				appinfo.put("is_useing_my_own_wifi_to_download", false);

			} else if (value < 8) {
				// 用自己WiFi
				appinfo.put("is_useing_my_own_wifi_to_download", true);
				appinfo.put("situation_before_download_done", 0);

			} else if (value < 9) {
				appinfo.put("is_useing_my_own_wifi_to_download", true);
				appinfo.put("situation_before_download_done", 1);
				appinfo.put("situation_before_download_percent", 97);

			} else if (value < 10) {
				appinfo.put("is_useing_my_own_wifi_to_download", true);
				appinfo.put("situation_before_download_done", 2);
				appinfo.put("situation_before_download_percent", 97);
			}

		}
		// end

	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {
		JSONObject appinfo = request.optJSONObject("appInfo");
		String downloadAppTag = appinfo.optString("downloadAppTag");
		if (StringUtil.isStringEmpty(downloadAppTag)) {
			return;
		}

		int result = request.optInt("result");
		boolean isSuccess = result == 0;
		decreaseCountForDownloadAppTag(downloadAppTag, isSuccess);
	}

	private static void decreaseCountForDownloadAppTag(String downloadAppTag, boolean isSuccess) throws Exception {
		synchronized (OPPOMarketTaskAction.class) {
			JSONObject configJson = getConfigJson();
			String dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			JSONObject todayNeedDownloadJson = configJson.optJSONObject(dateString);

			if (isSuccess) {
				int count = todayNeedDownloadJson.optInt(downloadAppTag);
				count--;
				todayNeedDownloadJson.put(downloadAppTag, count);
			}
			
			String doneKey = "___" + downloadAppTag + "_" + (isSuccess ? "success" : "failed");
			int doneCount = todayNeedDownloadJson.optInt(doneKey);
			doneCount++;
			todayNeedDownloadJson.put(doneKey, doneCount);

			String string = StringUtil.formatJsonString(configJson.toString());
			FileUtil.write(configJsonPath, string);
		}
	}

	private static String getTodayNeedDownloadAppTag() throws Exception { 
		JSONObject configJson = getConfigJson();

		String dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		JSONObject todayNeedDownloadJson = configJson.optJSONObject(dateString);

		Iterator<?> keys = todayNeedDownloadJson.keys();
		List<String> tagList = new ArrayList<>();
		while (keys.hasNext()) {
			String tag = (String) keys.next();
			if (tag.startsWith("_")) {
				continue;
			}
			int count = todayNeedDownloadJson.optInt(tag);
			if (count > 0) {
				tagList.add(tag);
			}
		}
		Collections.shuffle(tagList);
		
		String downloadAppTag = null;
		if (tagList.size() > 0) {
			downloadAppTag = tagList.get(0);
		}
		return downloadAppTag;
	}

	private static JSONObject getConfigJsonForDownloadAppTag(String downloadAppTag) throws Exception {
		JSONObject configJson = getConfigJson();
		JSONObject json = configJson.optJSONObject(downloadAppTag);
		
		if(null == json) {
			System.out.println("debug");
		}
		
		return json;
	}

	private static JSONObject getConfigJson() throws Exception {
		String string = FileUtil.read(configJsonPath);
		JSONObject json = new JSONObject(string);
		return json;
	}

}
