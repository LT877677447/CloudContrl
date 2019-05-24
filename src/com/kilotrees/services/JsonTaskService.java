/**
 * @author Administrator
 * 2019年2月15日 下午4:01:54 
 */
package com.kilotrees.services;

import java.io.IOException;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.dao.apkinfodao;
import com.kilotrees.model.bo.TaskBase;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.model.po.apkinfo;
import com.kilotrees.serverbean.ServerBeanBase;
import com.kilotrees.service.adv.runtime.api.ITaskRuntime;
import com.kilotrees.util.JSONObjectUtil;
import com.kilotrees.util.StringUtil;

public class JsonTaskService {

	public static advtaskinfo getAdvTaskInfo(TaskBase task) {
		int adv_id = task.getAdv_id();
		ITaskRuntime advRunTime = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id);
		advtaskinfo taskInfo = advRunTime.getAdvinfo();
		return taskInfo;
	}
	
	/**处理adtask的 appinfo 和 phoneInfo，再把adtask转json
	 * @param adtasks
	 * @param dev_tag
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 */
	public static JSONArray createCommonTaskJSON(TaskBase[] adtasks, String dev_tag)
			throws JSONException, IOException {
		
		JSONArray jsonTasks = new JSONArray();
		
		if (adtasks == null) {
			return jsonTasks;
		}

		for (int i = 0; i < adtasks.length; i++) {
			TaskBase adtask = adtasks[i];

			ITaskRuntime advRunTime = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adtask.getAdv_id());
			advtaskinfo taskInfo = advRunTime.getAdvinfo();
			apkinfo apkInfo = apkinfodao.getApkInfo(taskInfo.getApkid());

			// 任务id，如果是新增任务，其值是0，如果是留存任务，其值为-9
			if (apkInfo != null) {
				if (adtask.getTaskPhase() == TaskBase.TASK_PHASE_REMAIN) {
					adtask.setScriptRunParam(apkInfo.getRemscriptparams());
				} else {
					adtask.setScriptRunParam(apkInfo.getRegscriptparams());
				}
			}

			adtask.setDev_tag(dev_tag);
			
			
			JSONObject json = null;
			// 新增
			if (adtask.getTaskPhase() == TaskBase.TASK_PHASE_NEWLY) {
				json = JsonTaskService.createCommonJsonInNewlyPhase(adtask);
			} else
			// 留存
			if (adtask.getTaskPhase() == TaskBase.TASK_PHASE_REMAIN) {
				json = JsonTaskService.createCommonJsonInRemainPhase(adtask);
			} else {
				json = JsonTaskService.createJsonObjectFromTask(adtask);
			}
			

			// Handle JSON by bean
			ServerBeanBase sbean = (ServerBeanBase) ServerBeanBase.getServerBean(taskInfo.getServerbeanid());
			if (sbean != null) {
				sbean.handleTaskParasm(json);
			}
			jsonTasks.put(json);
		}
		
		return jsonTasks;
	}
	
	/**为task添加appinfo和phoneinfo，再把task转成json
	 * @param task
	 * @return
	 * @throws JSONException
	 */
	public static JSONObject createCommonJsonInNewlyPhase(TaskBase task) throws JSONException {
		// 1. Create information of Device
		JSONObject phoneInfo = phonetype_service.getInstance().randPhoneInfo();
		task.setPhoneInfo(phoneInfo);

		// 2. Create information of Application
		ITaskRuntime advRunTime = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(task.getAdv_id());
		JSONObject appInfo = new JSONObject();
		// 当某广告要求工作时长按百份比在一定范围变化时，由服务器指定随机时长
		int workingtime = advRunTime.getWorkingTimeRand();
		appInfo.put("workingtime", workingtime);
		task.setAppInfo(appInfo);

		// 3. Change Uid of Application
		advtaskinfo taskInfo = getAdvTaskInfo(task);
		apkinfo apkInfo = apkinfodao.getApkInfo(taskInfo.getApkid());
		String packageName = apkInfo.getPackagename();

//		JsonTaskService.createFakedUid(packageName, appInfo, phoneInfo);
		
		return createJsonObjectFromTask(task);
	}

	/**处理一下task里面的phoneInfo和appInfo，再把task转json
	 * @param task
	 * @return
	 * @throws JSONException
	 */
	public static JSONObject createCommonJsonInRemainPhase(TaskBase task) throws JSONException {
		JSONObject appInfo = task.getAppInfo();
		JSONObject phoneInfo = task.getPhoneInfo();

		JSONObject unstableInfo = phonetype_service.reandomTheUnstablePhoneInfo(phoneInfo);
		
		// 改变wifi扫描信息
		boolean isChangeTheWifiScanResult = new Random().nextInt(5) == 1;
		if (isChangeTheWifiScanResult) {
			if (appInfo != null) {
				JSONObject connectionInfo = phoneInfo.optJSONObject("Wifi.ConnectionInfo");
				String bssid = connectionInfo.optString("BSSID");
				String wifiName = connectionInfo.optString("SSID");
				String macAddress = connectionInfo.optString("MacAddress");
				JSONArray wifiScanResults = phonetype_service.createWifiScanResults(bssid, wifiName, macAddress);
				appInfo.put("Wifi.ScanResults", wifiScanResults);
			}
		}
		
		JSONObjectUtil.mergeJSONObject(phoneInfo, unstableInfo);
		
		return createJsonObjectFromTask(task);
	}
	
	/**把task转成json
	 * @param task
	 * @return
	 * @throws JSONException
	 */
	public static JSONObject createJsonObjectFromTask(TaskBase task) throws JSONException {
		JSONObject json = task.toJSONObject();
		
		advtaskinfo taskInfo = getAdvTaskInfo(task);
		if (taskInfo == null) {
			return json;
		}
		
		apkinfo apkInfo = apkinfodao.getApkInfo(taskInfo.getApkid());
		task.setScriptTimeout(taskInfo.getRequesttime());
		task.setZipfiles(apkInfo.getZipfiles());
		task.setPackageName(apkInfo.getPackagename());
		
		// cause task updated
		json = task.toJSONObject();
	
		json.put("unzip_regex", apkInfo.getUnzip_regex());

		// 广告客户端动态加载bean
		if (StringUtil.isStringEmpty(taskInfo.getClientbean_info()) == false) {
			String[] info = taskInfo.getClientbean_info().replaceAll(",", ";").split(";");
			if (info.length >= 5) {
				json.put("jobtype", "thad_bean");
				json.put("bean_name", info[0]);
				json.put("bean_class", info[1]);
				json.put("bean_version", Integer.parseInt(info[2]));
				json.put("bean_params", info[3]);
				String scrUrl = ServerConfig.getScriptfileurl();
				if (scrUrl.endsWith("/") == false) {
					scrUrl += "/";
				}
				scrUrl += "beans/";
				// scrUrl += info[0] + "_" + info[2] + ".apk";有可能是pyon脚本;
				// info[4]文件名包括后缀
				scrUrl += info[4];
				json.put("bean_url", scrUrl);
			}
		}
		return json;
	}

	private static void createFakedUid(String packageName, JSONObject appInfo, JSONObject phoneInfo)
			throws JSONException {
		int fakedUid = new Random().nextInt(10000) + 20000;
		appInfo.put("UID", fakedUid);

		// 1. handle 'Package.ApplicationInfo'
		JSONObject applicationInfoJson = phoneInfo.optJSONObject("Package.ApplicationInfo");
		if (applicationInfoJson == null) {
			applicationInfoJson = new JSONObject();
			phoneInfo.put("Package.ApplicationInfo", applicationInfoJson);
		}
		JSONObject packageJson = applicationInfoJson.optJSONObject(packageName);
		if (packageJson == null) {
			packageJson = new JSONObject();
			applicationInfoJson.put(packageName, packageJson);
		}
		packageJson.put("uid", fakedUid);

		// 2. handle 'Package.InstalledPackages'
		JSONObject installedPackagesJson = phoneInfo.optJSONObject("Package.InstalledPackages");
		if (installedPackagesJson == null) {
			installedPackagesJson = new JSONObject();
			phoneInfo.put("Package.InstalledPackages", installedPackagesJson);
		}
		JSONObject installedInnerJson = installedPackagesJson.optJSONObject(packageName);
		if (installedInnerJson == null) {
			installedInnerJson = new JSONObject();
			installedPackagesJson.put(packageName, installedInnerJson);
		}
		JSONObject innerApplicationInfo = installedInnerJson.optJSONObject("applicationInfo");
		if (innerApplicationInfo == null) {
			innerApplicationInfo = new JSONObject();
			installedInnerJson.put("applicationInfo", innerApplicationInfo);
		}
		innerApplicationInfo.put("uid", fakedUid);

		// 3. handle 'Package.PackageInfo'
		JSONObject packageInfoJson = phoneInfo.optJSONObject("Package.PackageInfo");
		if (packageInfoJson == null) {
			packageInfoJson = new JSONObject();
			phoneInfo.put("Package.PackageInfo", packageInfoJson);
		}
		JSONObject innerPackgeInfoJson = packageInfoJson.optJSONObject(packageName);
		if (innerPackgeInfoJson == null) {
			innerPackgeInfoJson = new JSONObject();
			packageInfoJson.put(packageName, innerPackgeInfoJson);
		}
		innerApplicationInfo = innerPackgeInfoJson.optJSONObject("applicationInfo");
		if (innerApplicationInfo == null) {
			innerApplicationInfo = new JSONObject();
			innerPackgeInfoJson.put("applicationInfo", innerApplicationInfo);
		}
		innerApplicationInfo.put("uid", fakedUid);
	}

}
