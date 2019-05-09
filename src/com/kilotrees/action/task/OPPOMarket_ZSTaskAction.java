/**
 * @author Administrator
 * 2019年3月28日 下午10:50:30 
 */
package com.kilotrees.action.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.kilotrees.util.JSONObjectUtil;

public class OPPOMarket_ZSTaskAction implements ITaskAction{
	private static List<JSONObject> phoneInfoList = new ArrayList<>();
	private static List<String> downloadAppTagList = null;
	private static List<String> sDvices = new ArrayList<>();// 设备第一次做任务
	private static String packageName = "com.oppo.market";
	private static Integer index2Take = 0;
	private static final String jsonRootDir = "D:\\WebServer\\json\\";
	private static final String jsonPath = jsonRootDir + "OPPOMarket/OPPOMarket.json";
	private static final String logPath = jsonRootDir + "OPPOMarket/OPPOMarket.log";

	static {

		try {
			List<phonetype> p1 = phonetypedao.getOPPOPhoneInfo("tb_phonetype_tmp","oppo");
			for (int i = 0; i < p1.size(); i++) {
				JSONObject phoneInfo = new JSONObject(p1.get(i).getPhone_info());
				phoneInfoList.add(phoneInfo);
			}

			List<phonetype> p2 = phonetypedao.getOPPOPhoneInfo("tb_phonetype","oppo");
			for (int i = 0; i < p2.size(); i++) {
				JSONObject phoneInfo = new JSONObject(p2.get(i).getPhone_info());
				phoneInfoList.add(phoneInfo);
			}
			
			List<phonetype> p3 = phonetypedao.getOPPOPhoneInfo("tb_phonetype_tmp","oneplus");
			for (int i = 0; i < p3.size(); i++) {
				JSONObject phoneInfo = new JSONObject(p3.get(i).getPhone_info());
				phoneInfoList.add(phoneInfo);
			}
			
			List<phonetype> p4 = phonetypedao.getOPPOPhoneInfo("tb_phonetype","oneplus");
			for (int i = 0; i < p4.size(); i++) {
				JSONObject phoneInfo = new JSONObject(p4.get(i).getPhone_info());
				phoneInfoList.add(phoneInfo);
			}
			
			for(int i = 0; i < phoneInfoList.size(); i++) {
				JSONObject phoneInfo = phoneInfoList.get(i);
				int sdkVersion = phoneInfo.optInt("Build.VERSION.SDK_INT");
//				if (sdkVersion < 23) {
					phoneInfo.remove("Build.VERSION.SDK");
					phoneInfo.remove("Build.VERSION.SDK_INT");
//				}
			}

			if (null == downloadAppTagList) {
				initDownloadAppList();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void initDownloadAppList() throws Exception {
		JSONObject OPPOMarketJson = getJsonFromFile(jsonPath);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String sDate = dateFormat.format(new Date());

		String ZBZS = sDate + "_ChiBiZhanShen";
		String LMZG = sDate + "_LiMingZhanGe";
		// 初始化downloadAppList
		downloadAppTagList = new ArrayList<>();
		if (OPPOMarketJson.optInt(ZBZS) > 0) {
			downloadAppTagList.add("ChiBiZhanShen");
		}
		if (OPPOMarketJson.optInt(LMZG) > 0) {
			downloadAppTagList.add("LiMingZhanGe");
		}
		if (downloadAppTagList.size() < 1) {
			// 是否已经做完
			throw new RuntimeException("downloadAppList.size < 1 ----------------------------------------------请检查 D:\\WebServer\\json\\OPPOMarket/OPPOMarket.json 是否已经做完");
		}
	}

	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		JSONArray adtasks = response.optJSONArray("tasks");
		JSONObject adtask = adtasks.optJSONObject(0);
		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");
		String strWuJi = " ";//使用无极设备
		String strPhone = "phone000 phone001 phone002";//phone系列
		Boolean isUsingWuJi = false;
		
		String dev_tag = response.optString("dev_tag");

		// 指定设备才做
		if (!dev_tag.startsWith("6")) {
			response.put("tasks", new JSONArray());
			adtasks = new JSONArray();
		}
		
		if (adtasks.length() > 0) {
			if(!strWuJi.contains(dev_tag)) {
				// socket5
				isUsingWuJi = false;
				
				adtask.put("scriptTimeout", 600);
				String socket5_packageName = "org.proxydroid";
				String socket5_fileName = "org.proxydroidApp.apk";
				String socket5_appfileurl = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk";
				
				JSONObject jsonInstall = JsonActionService.createAction_INSTALL_APP(socket5_packageName, socket5_fileName, socket5_appfileurl);
				prefix_task_actions.put(jsonInstall);
				
//				JSONObject jsonStop = JsonActionService.createAction_STOP_APP(socket5_packageName);
//				prefix_task_actions.put(jsonStop);
				
				JSONObject jsonOpen = JsonActionService.createAction_OPEN_APP(socket5_packageName, 0);
				prefix_task_actions.put(jsonOpen);
				
				JSONObject jsonClose = JsonActionService.createAction_CLOSE_APP(socket5_packageName, 0);
				suffix_task_actions.put(jsonClose);
				
//				suffix_task_actions.put(jsonStop);
				
			}else {
				//wuji 
				isUsingWuJi = true;
				String appDownloadURL = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/wuji_duli_60.apk";
				JSONObject wujiVPN_Action = JsonActionService.createAction_INSTALL_APP("org.wuji", "wuji_duli_60.apk", appDownloadURL);
				prefix_task_actions.put(wujiVPN_Action);
			}

			// 处理phoneInfo
			JSONObject phinfo = new JSONObject();
			phinfo = phoneInfoList.get(index2Take);
			index2Take++;
			index2Take = index2Take % phoneInfoList.size();

			JSONObject staticInfo = phonetype_service.getStaticMatchingInfo(phinfo);
			JSONObjectUtil.mergeJSONObject(phinfo, staticInfo);

			JSONObject stableInfo = phonetype_service.reandomTheStablePhoneInfo(phinfo);
			JSONObjectUtil.mergeJSONObject(phinfo, stableInfo);

			JSONObject unstableInfo = phonetype_service.reandomTheUnstablePhoneInfo(phinfo, null, false);
			JSONObjectUtil.mergeJSONObject(phinfo, unstableInfo);
			
			phinfo.remove("Screen.widthPixels");
			phinfo.remove("Screen.heightPixels");
			adtask.put("phoneInfo", phinfo);
			// end 处理phoneInfo

			// 处理每个设备第一次拿任务
			boolean deviceDone = false;
			for (String string : sDvices) {
				if (string.equals(dev_tag)) {
					deviceDone = true;
					break;
				}
				continue;
			}
			if (!deviceDone) {
				JSONArray array = new JSONArray();
				array.put("/sdcard/.ccache/");
				array.put("/data/local/rom/cache/");

				JSONObject jsonClearDirs = JsonActionService.createAction_CLEAR_DIRS(array);
				prefix_task_actions.put(jsonClearDirs);
				sDvices.add(dev_tag);
			}
			// end

			// downloadAppList下发下载的APP
			String defaultDownloadAppTag = "LiMingZhanGe"; // 如果downloadAppList没有了，默认给这个
			String downloadAppTag = "";
  
			int size = downloadAppTagList.size();
			if (size > 0) {
				downloadAppTag = downloadAppTagList.get(new Random().nextInt(size));
			} else {
				downloadAppTag = defaultDownloadAppTag;
			}
			
//			downloadAppTag = "ZhanShenXinShiJi";
//			downloadAppTag = "YouZhongLou";
//			downloadAppTag = "ZhiHuiYinHang";
			
			JSONObject appinfo = adtask.optJSONObject("appInfo");
			if (downloadAppTag.equals("ChiBiZhanShen")) {
				appinfo.put("downloadAppName", "赤壁战神");
				appinfo.put("downloadAppTag", "ChiBiZhanShen");
				appinfo.put("downloadSearchName", "赤壁战神");
				appinfo.put("downloadCachePackageId", "20852849");
			}
			if (downloadAppTag.equals("LiMingZhanGe")) {
				appinfo.put("downloadAppName", "黎明战歌（国战手游）");
				appinfo.put("downloadAppTag", "LiMingZhanGe");
				appinfo.put("downloadSearchName", "黎明战歌（国战手游）");
				appinfo.put("downloadCachePackageId", "20768317");
			}
			// end
			
			//战神新世纪	
			if (downloadAppTag.equals("ChiBiZhanShen")) {
				//
				String zhanShenPackageName = "com.wan.cbzs.nearme.gamecenter";
				JSONObject uninstallAction = JsonActionService.createAction_UNINSTALL_APP(zhanShenPackageName);
				suffix_task_actions.put(uninstallAction);
				
				String userCenterPackageName = "com.oppo.usercenter";
				JSONObject installAction_UserCenter = JsonActionService.createAction_INSTALL_APP(userCenterPackageName, "OppoUsercenter_1_1.apk", ServerConfig.getStorageBaseURL() + "/files/apks/OppoUsercenter_1_1.apk");
				prefix_task_actions.put(installAction_UserCenter);
				
				String installerPackageName = "com.android.packageinstaller";
				
				JSONArray sharedPackageNames = new JSONArray();
				sharedPackageNames.put(installerPackageName);
				sharedPackageNames.put(zhanShenPackageName);
				sharedPackageNames.put(userCenterPackageName);
				adtask.put("shared_packages_names", sharedPackageNames);
				
				String scriptURLBase = ServerConfig.getStorageBaseURL() + "/files/scripts/";
				JSONObject action_UPDATE_SCRIPT_1 = JsonActionService.createAction_UPDATE_SCRIPT(userCenterPackageName, scriptURLBase + userCenterPackageName + ".apk", 4);
				JSONObject action_UPDATE_SCRIPT_2 = JsonActionService.createAction_UPDATE_SCRIPT(installerPackageName, scriptURLBase + installerPackageName + ".apk", 4);
				
				prefix_task_actions.put(action_UPDATE_SCRIPT_1);
				prefix_task_actions.put(action_UPDATE_SCRIPT_2);
				
				JSONObject stopAction_UserCenter = JsonActionService.createAction_STOP_APP(userCenterPackageName);
				prefix_task_actions.put(stopAction_UserCenter);
				
				JSONObject startAction_UserCenter = JsonActionService.createAction_START_APP(userCenterPackageName);
				prefix_task_actions.put(startAction_UserCenter);
				
				JSONObject clearAction_UserCenter = JsonActionService.createAction_CLEAR_APP(userCenterPackageName);
				suffix_task_actions.put(clearAction_UserCenter);
				
				JSONObject uninstallAction_UserCenter = JsonActionService.createAction_UNINSTALL_APP(userCenterPackageName);
				suffix_task_actions.put(uninstallAction_UserCenter);
			}else if(downloadAppTag.equals("LiMingZhanGe")) {
				
				String zhanShenPackageName = "com.wan.lmzg.nearme.gamecenter";
				JSONObject uninstallAction = JsonActionService.createAction_UNINSTALL_APP(zhanShenPackageName);
				suffix_task_actions.put(uninstallAction);
				
				String userCenterPackageName = "com.oppo.usercenter";
				JSONObject installAction_UserCenter = JsonActionService.createAction_INSTALL_APP(userCenterPackageName, "OppoUsercenter_1_1.apk", ServerConfig.getStorageBaseURL() + "/files/apks/OppoUsercenter_1_1.apk");
				prefix_task_actions.put(installAction_UserCenter);
				
				String installerPackageName = "com.android.packageinstaller";
				JSONArray sharedPackageNames = new JSONArray();
				sharedPackageNames.put(installerPackageName);
				sharedPackageNames.put(zhanShenPackageName);
				sharedPackageNames.put(userCenterPackageName);
				adtask.put("shared_packages_names", sharedPackageNames);
				
				String scriptURLBase = ServerConfig.getStorageBaseURL() + "/files/scripts/";
				JSONObject action_UPDATE_SCRIPT_1 = JsonActionService.createAction_UPDATE_SCRIPT(userCenterPackageName, scriptURLBase + userCenterPackageName + ".apk", 4);
				JSONObject action_UPDATE_SCRIPT_2 = JsonActionService.createAction_UPDATE_SCRIPT(installerPackageName, scriptURLBase + installerPackageName + ".apk", 4);
				
				prefix_task_actions.put(action_UPDATE_SCRIPT_1);
				prefix_task_actions.put(action_UPDATE_SCRIPT_2);
				
				//用socket5 无极再用open 
				JSONObject jsonStart = JsonActionService.createAction_START_APP("com.wan.lmzg.nearme.gamecenter");
				prefix_task_actions.put(jsonStart);
				
				JSONObject stopAction_UserCenter = JsonActionService.createAction_STOP_APP(userCenterPackageName);
				prefix_task_actions.put(stopAction_UserCenter);
				
				JSONObject startAction_UserCenter = JsonActionService.createAction_START_APP(userCenterPackageName);
				prefix_task_actions.put(startAction_UserCenter);
				
				JSONObject clearAction_UserCenter = JsonActionService.createAction_CLEAR_APP(userCenterPackageName);
				suffix_task_actions.put(clearAction_UserCenter);
				
				JSONObject uninstallAction_UserCenter = JsonActionService.createAction_UNINSTALL_APP(userCenterPackageName);
				suffix_task_actions.put(uninstallAction_UserCenter);
				
				
			}
			//end
			
			// 安装完是否打开APP
			appinfo.put("is_open_app_after_install", new Random().nextBoolean());
			
			// 是否是进详情页去下载 
			appinfo.put("is_enter_detail_page_to_download", new Random().nextInt(5) == 0 );
			
			// 处理App的下载方式
			if (isUsingWuJi) {
				appinfo.put("is_useing_my_own_wifi_to_download", false);
				
			} else {
				
				//use cache download
				boolean is_useing_cache_to_download = new Random().nextInt(2) == 0;
				is_useing_cache_to_download = true;
				
				appinfo.put("is_useing_cache_to_download", is_useing_cache_to_download);
				if (is_useing_cache_to_download == true) {
					if(downloadAppTag.equals("LiMingZhanGe")){
						String res_download_url = ServerConfig.getStorageBaseURL() + "/files/resources/oppoMarket/20768317.apk.nrdownload";
						JSONObject res_download_action = JsonActionService.createAction_DOWNLOAD_RESOURCE("20768317.apk.nrdownload", res_download_url);
						prefix_task_actions.put(res_download_action);
						return;
					}
					
				}
				
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
					
					appinfo.put("is_open_app_after_install", true);
					
				} else if (value < 10) {	
					appinfo.put("is_useing_my_own_wifi_to_download", true);
					appinfo.put("situation_before_download_done", 2);
					appinfo.put("situation_before_download_percent", 97);

					appinfo.put("is_open_app_after_install", true);
				}

			}
			//end
			
		}

	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {
		JSONObject appinfo = request.optJSONObject("appInfo");
		String downloadAppTag = appinfo.optString("downloadAppTag");
		
		int result = request.optInt("result");
		if (result != 0) {
			return;
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String sDate = dateFormat.format(new Date());
		String ZBZS = sDate + "_ChiBiZhanShen";
		String LMZG = sDate + "_LiMingZhanGe";
		
		synchronized (jsonPath) {
			JSONObject OPPOMarketJson = getJsonFromFile(jsonPath);
			//读出来 -1
			int v = 0;
			String downloadAppTag2 = "";
			int whichOne = 0;
			if (downloadAppTag.equals("ChiBiZhanShen")) {
				v = OPPOMarketJson.optInt(ZBZS);
				downloadAppTag2 = "ChiBiZhanShen";
				whichOne = 1;
			}
			if (downloadAppTag.equals("LiMingZhanGe")) {
				v = OPPOMarketJson.optInt(LMZG);
				downloadAppTag2 = "LiMingZhanGe";
				whichOne = 2;
			}
			v--;
			//写回去
			if (1 == whichOne) {
				OPPOMarketJson.put(ZBZS, v);
			}
			if (2 == whichOne) {
				OPPOMarketJson.put(LMZG, v);
			}
			FileWriter jsonWriter = new FileWriter(jsonPath);
			jsonWriter.write(OPPOMarketJson.toString());
			jsonWriter.flush();
			jsonWriter.close();
			//判断小于1，list remove	
			if (v < 1) {
				downloadAppTagList.remove(downloadAppTag2);
			}
			saveLog("report: {" + downloadAppTag + "-" + downloadAppTag2 + "-写回的值-" + v + "  }");
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
	
	private static JSONObject getJsonFromFile(String jsonPath) throws Exception {
		File jsonFile = new File(jsonPath);

		if (!jsonFile.exists()) {
			jsonFile.getParentFile().mkdirs();
			jsonFile.createNewFile();
		}

		FileInputStream fins = new FileInputStream(jsonFile);
		byte[] buf = new byte[(int) jsonFile.length()];
		fins.read(buf);
		fins.close();
		String jsonString = new String(buf, "utf-8");
		JSONObject OPPOMarketJson = new JSONObject(jsonString);
		return OPPOMarketJson;
	}
}
