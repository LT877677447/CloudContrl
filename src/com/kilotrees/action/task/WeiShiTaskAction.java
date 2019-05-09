/**
 * @author Administrator
 * 2019年2月25日 下午4:15:47 
 */
package com.kilotrees.action.task;

import java.util.Random;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.dao.qqaccountdao;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.model.po.qqaacount_info;
import com.kilotrees.services.advnewtask_service;
import com.kilotrees.services.qqaccount_service;
import com.kilotrees.util.JSONObjectUtil;
import com.kilotrees.util.StringUtil;

public class WeiShiTaskAction implements ITaskAction {
	private static Logger log = Logger.getLogger(WeiShiTaskAction.class);

	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		JSONArray adtasks = response.optJSONArray("tasks");
		JSONObject adtask = adtasks.optJSONObject(0);
		String dev_tag = response.optString("dev_tag");
		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");

		if (adtasks.length() > 0) {
			// random 80% 8 20% 2
			Random random = new Random();
			int b = (int) (random.nextFloat() * 100) + 1;// 1-100
			if (b <= 50) {
				adtask.put("random", 8);
			} else {
				adtask.put("random", 2);
			}

			// 拿QQ号，放到appInfo中下发
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
			JSONObject appInfo = adtask.optJSONObject("appInfo");
			appInfo.put("qqnum", qqnum);
			appInfo.put("pass", pass);

			// 安装QQ
			JSONObject jsonSta = new JSONObject();
			jsonSta.put("action", "INSTALL_APP");
			jsonSta.put("filename", "qq-6.5.apk");
			jsonSta.put("packageName", "com.tencent.mobileqq");
			jsonSta.put("file_url", ServerConfig.getStorageBaseURL() + "/files/apks/qq-6.5.apk");
			prefix_task_actions.put(jsonSta);

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
			jsonUpdate.put("scripturl", ServerConfig.getStorageBaseURL() + "/files/scripts/com.tencent.mobileqq.apk");
			prefix_task_actions.put(jsonUpdate);

			JSONObject jsonUN = new JSONObject();
			jsonUN.put("action", "UNINSTALL_APP");
			jsonUN.put("packageName", "com.tencent.mobileqq");
			suffix_task_actions.put(jsonUN);

			adtask.put("shared_package_name", "com.tencent.mobileqq");

			// 去掉phoneInfo中的Build.开头的
			JSONObject phoneInfo = adtask.optJSONObject("phoneInfo");
			JSONObjectUtil.removeWithKeyPrefix(phoneInfo, "Build.");

			// socket5
			 JSONObject jsonInstall = new JSONObject();
			 jsonInstall.put("action", "INSTALL_APP");
			 jsonInstall.put("filename", "org.proxydroidApp.apk");
			 jsonInstall.put("packageName", "org.proxydroid");
			 jsonInstall.put("file_url", ServerConfig.getStoragePrivateBaseURL() +
			 "/phone_files/update/org.proxydroidApp.apk");
			 prefix_task_actions.put(jsonInstall);
			
			 JSONObject jsonStop = new JSONObject();
			 jsonStop.put("action", "STOP_APP");
			 jsonStop.put("filename", "org.proxydroidApp.apk");
			 jsonStop.put("packageName", "org.proxydroid");
			 jsonStop.put("file_url", ServerConfig.getStoragePrivateBaseURL() +
			 "/phone_files/update/org.proxydroidApp.apk");
			 prefix_task_actions.put(jsonStop);
			
			 JSONObject jsonOpen = new JSONObject();
			 jsonOpen.put("action", "OPEN_APP");
			 jsonOpen.put("filename", "org.proxydroidApp.apk");
			 jsonOpen.put("packageName", "org.proxydroid");
			 jsonOpen.put("file_url", ServerConfig.getStoragePrivateBaseURL() +
			 "/phone_files/update/org.proxydroidApp.apk");
			 prefix_task_actions.put(jsonOpen);
			
			 JSONObject jsonClose = new JSONObject();
			 jsonClose.put("action", "CLOSE_APP");
			 jsonClose.put("filename", "org.proxydroidApp.apk");
			 jsonClose.put("packageName", "org.proxydroid");
			 jsonClose.put("file_url", ServerConfig.getStoragePrivateBaseURL() +
			 "/phone_files/update/org.proxydroidApp.apk");
			 suffix_task_actions.put(jsonClose);
			
			 JSONObject jsonStop2 = new JSONObject();
			 jsonStop2.put("action", "STOP_APP");
			 jsonStop2.put("filename", "org.proxydroidApp.apk");
			 jsonStop2.put("packageName", "org.proxydroid");
			 jsonStop2.put("file_url", ServerConfig.getStoragePrivateBaseURL() +
			 "/phone_files/update/org.proxydroidApp.apk");
			 suffix_task_actions.put(jsonStop2);
		}

	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {
		JSONObject appinfo = request.optJSONObject("appinfo");
		if(appinfo != null) {
			String WeiShi_login_result = appinfo.optString("WeiShi_login_result");
			if (WeiShi_login_result.equals("success")) {
				String qqnum = appinfo.optString("qqnum");
				int adv_id = request.optInt("adv_id");
				String tableName = "tb_qqaccount_";
				advtaskinfo ai = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo();
				String alias = ai.getAlias().trim();
				if (!StringUtil.isStringEmpty(alias)) {
					tableName += alias;
				} else {
					log.error(this.getClass().getName() + " : 缺少alias，拿不到QQ号");
				}
				qqaacount_info QQAccount = qqaccountdao.getQQAccountByQQNum(tableName, qqnum);
				qqaccountdao.updateResultSuccess(tableName, QQAccount);
			}
		}
	}

}
