/**
 * @author Administrator
 * 2019年2月15日 下午6:40:00 
 */
package com.kilotrees.action.task;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.dao.qqaccountdao;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.services.advnewtask_service;
import com.kilotrees.services.qqaccount_service;
import com.kilotrees.util.StringUtil;

public class LiMaoYueDuTaskAction implements ITaskAction{
	private static Logger log = Logger.getLogger(LiMaoYueDuTaskAction.class);
	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		String dev_tag = response.optString("dev_tag");
		//需要加socket5的设备
		boolean isInSocks5Devices = dev_tag.equals("AOS600011234564897641") ;

		try {
			JSONArray adtasks = response.optJSONArray("tasks");
			JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
			JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");

			if (adtasks.length() > 0) {

				// 1. prefix_task_actions
				JSONObject jsonSta = new JSONObject();
				jsonSta.put("action", "INSTALL_APP");
				jsonSta.put("filename", "qq-6.5.apk");
				jsonSta.put("packageName", "com.tencent.mobileqq");
				jsonSta.put("file_url", ServerConfig.getStorageBaseURL() + "/files/apks/qq-6.5.apk");
				prefix_task_actions.put(jsonSta);

				boolean noSock5 = false;
				if (dev_tag.equals("AOS60003") || dev_tag.equals("AOS60002")|| dev_tag.equals("AOS60001")|| dev_tag.equals("AOS60004")|| dev_tag.equals("AOS60005")) {
					JSONObject jsonVPN = new JSONObject();
					jsonVPN.put("action", "INSTALL_APP");
					jsonVPN.put("filename", "wuji_duli_60.apk");
					jsonVPN.put("packageName", "org.wuji");
					jsonVPN.put("file_url",
							ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/wuji_duli_60.apk");
					prefix_task_actions.put(jsonVPN);
					noSock5 = true;
				}

				JSONObject jsonStopQQ = new JSONObject();
				jsonStopQQ.put("action", "STOP_APP");
				jsonStopQQ.put("filename", "qq-6.5.apk");
				jsonStopQQ.put("packageName", "com.tencent.mobileqq");
				jsonStopQQ.put("file_url", ServerConfig.getStoragePrivateBaseURL() + "/files/apks/qq-6.5.apk");
				prefix_task_actions.put(jsonStopQQ);

				JSONObject jsonClear = new JSONObject();
				jsonClear.put("action", "CLEAR_APP");
				jsonClear.put("filename", "qq-6.5.apk");
				jsonClear.put("packageName", "com.tencent.mobileqq");
				jsonClear.put("file_url", ServerConfig.getStorageBaseURL() + "/files/apks/qq-6.5.apk");
				prefix_task_actions.put(jsonClear);

				JSONObject jsonUpdate = new JSONObject();
				jsonUpdate.put("action", "UPDATE_SCRIPT");
				jsonUpdate.put("packageName", "com.tencent.mobileqq");
				jsonUpdate.put("scripturl_version", 11);
				jsonUpdate.put("scripturl",
						ServerConfig.getStorageBaseURL() + "/files/scripts/com.tencent.mobileqq.apk");
				prefix_task_actions.put(jsonUpdate);

				// socket5
				if (isInSocks5Devices) {
					if (!noSock5) {
						JSONObject jsonInstall = new JSONObject();
						jsonInstall.put("action", "INSTALL_APP");
						jsonInstall.put("filename", "org.proxydroidApp.apk");
						jsonInstall.put("packageName", "org.proxydroid");
						jsonInstall.put("file_url",
								ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk");
						prefix_task_actions.put(jsonInstall);

						JSONObject jsonStop = new JSONObject();
						jsonStop.put("action", "STOP_APP");
						jsonStop.put("filename", "org.proxydroidApp.apk");
						jsonStop.put("packageName", "org.proxydroid");
						jsonStop.put("file_url",
								ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk");
						prefix_task_actions.put(jsonStop);

						JSONObject jsonOpen = new JSONObject();
						jsonOpen.put("action", "OPEN_APP");
						jsonOpen.put("filename", "org.proxydroidApp.apk");
						jsonOpen.put("packageName", "org.proxydroid");
						jsonOpen.put("file_url",
								ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk");
						prefix_task_actions.put(jsonOpen);

						JSONObject jsonClose = new JSONObject();
						jsonClose.put("action", "CLOSE_APP");
						jsonClose.put("filename", "org.proxydroidApp.apk");
						jsonClose.put("packageName", "org.proxydroid");
						jsonClose.put("file_url",
								ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk");
						suffix_task_actions.put(jsonClose);

						JSONObject jsonStop2 = new JSONObject();
						jsonStop2.put("action", "STOP_APP");
						jsonStop2.put("filename", "org.proxydroidApp.apk");
						jsonStop2.put("packageName", "org.proxydroid");
						jsonStop2.put("file_url",
								ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk");
						suffix_task_actions.put(jsonStop2);
					}
				}

				JSONObject jsonUN = new JSONObject();
				jsonUN.put("action", "UNINSTALL_APP");
				jsonUN.put("packageName", "com.tencent.mobileqq");
				suffix_task_actions.put(jsonUN);

				// 2. QQ
				JSONObject adtask = (JSONObject) adtasks.get(0);
				adtask.put("shared_package_name", "com.tencent.mobileqq");
				JSONObject appInfo = adtask.optJSONObject("appInfo");
				// JSONObject phoneInfo = adtask.optJSONObject("phoneInfo"); //

				JSONObject paraJson = new JSONObject();
				String adv_id = adtask.optString("adv_id");
				long autoid = 0;
				String sAutoid = request.optString("autoid");
				if (!StringUtil.isStringEmpty(sAutoid)) {
					autoid = Long.parseLong(sAutoid);
				}
				paraJson.put("action", 1);
				paraJson.put("adv_id", adv_id);
				paraJson.put("autoid", autoid);
				paraJson.put("dev_tag", dev_tag);
				JSONObject qqJson = qqaccount_service.getInstance().handleRequest(paraJson);
				String qqnum = qqJson.optString("qqnum");
				String pass = qqJson.optString("pass");

				appInfo.put("qqnum", qqnum);
				appInfo.put("pass", pass);
				if(isInSocks5Devices) {
					if (!noSock5) {
						appInfo.put("isSocket5", true);
					}
				}
				
				// 2. suffix_task_actions
				// JSONObject zipJson = new JSONObject();
				// zipJson.put("action", "ZIP_FILE");
				// zipJson.put("zip_file_name", qqnum + ".zip");
				// zipJson.put("packageName", "com.tencent.mobileqq");
				// zipJson.put("unzip_regex", "");
				// zipJson.put("zip_upload_url", serverconfig.getFileServerBaseURL()+
				// "/zipupload?");
				// suffix_task_actions.put(jsonClear);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {

		JSONObject appInfo = request.optJSONObject("appInfo");
		JSONObject phoneInfo = request.optJSONObject("phoneInfo");
		
		int adv_id = request.optInt("adv_id");
		String tableName = "tb_qqaccount_";
		advtaskinfo ai = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo();
		String alias = ai.getAlias().trim();
		if(!StringUtil.isStringEmpty(alias)) {
			tableName += alias;
		}else {
			log.error(this.getClass().getName()+" : 缺少alias，拿不到QQ号");
		}
		
		String qqnum = appInfo.optString("qqnum");
		String qqLoginResult = appInfo.optString("qq_login_result");
		if (qqLoginResult.equals("success")) {
			qqaccountdao.updateSuccess(qqnum,tableName);
		} else if (qqLoginResult.equals("failed")) {
			// 客户端反馈失败，不能再使用该QQ
			qqaccountdao.updateResultFailure(qqnum,tableName);
		} else {
			// 客户端反馈失败，可用继续下发该QQ
			qqaccountdao.updateRequestAgain(qqnum,tableName);
		}

		// String dashengLoginResult = appInfo.optString("dashen_login_result");

	}

	
}
