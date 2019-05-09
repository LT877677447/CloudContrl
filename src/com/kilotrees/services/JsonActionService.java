package com.kilotrees.services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.dao.ActionModelDao;
import com.kilotrees.dao.apkinfodao;
import com.kilotrees.model.bo.TaskBase;
import com.kilotrees.model.po.ActionModel;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.model.po.apkinfo;
import com.kilotrees.util.JSONArrayUtil;
import com.kilotrees.util.StringUtil;

public class JsonActionService {

	/**
	 * 添加Action:下载/上传 压缩/解压的留存文件 ，安装APP，更新脚本
	 */
	public static void createCommonActionJSON(JSONArray prefix_task_actions, JSONArray suffix_task_actions,
			TaskBase[] adtasks) throws JSONException {
		if (adtasks == null) {
			return;
		}

		for (int i = 0; i < adtasks.length; i++) {
			TaskBase adtask = adtasks[i];

			advtaskinfo taskInfo = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adtask.getAdv_id()).getAdvinfo();
			if (taskInfo == null) {
				continue;
			}
			apkinfo apkInfo = apkinfodao.getApkInfo(taskInfo.getApkid());
			if (apkInfo == null) {
				continue;
			}

			boolean isInRemainPhase = adtask.getTaskPhase() == TaskBase.TASK_PHASE_REMAIN;
			
			String autoid = adtask.getAutoid() + "";
			String packageName = apkInfo.getPackagename();

			ActionModel action = ActionModelDao.queryActions(adtask.getAdv_id(), adtask.getTaskPhase());
			
			//isCustomPrefixAction：管理后台没有让运营填写action，所以写死了为false
			boolean isCustomPrefixAction = action != null && !StringUtil.isStringEmpty(action.getPrefix_actions());
			boolean isCustomSuffixAction = action != null && !StringUtil.isStringEmpty(action.getSuffix_actions());
			
			if (isCustomPrefixAction) {
				
				String prefixString = action.getPrefix_actions();
				if (prefixString.contains("[AUTO_ID]")) {
					prefixString = prefixString.replace("[AUTO_ID]", autoid);
				}
				
				JSONArray prefixs = new JSONArray(prefixString);
				JSONArrayUtil.copy(prefix_task_actions, prefixs);
				
			}
			
			if (isCustomSuffixAction) {
				
				String suffixString = action.getSuffix_actions();
				if (suffixString.contains("[AUTO_ID]")) {
					suffixString = suffixString.replace("[AUTO_ID]", autoid);
				}
				
				JSONArray suffixs = new JSONArray(suffixString);
				JSONArrayUtil.copy(suffix_task_actions, suffixs);
			}

			// 配置默认的 Prefxi Actions ---------------------
			if (isCustomPrefixAction == false) {
				
				JSONObject UNINSTALL_APP_ACTION = JsonActionService.createAction_UNINSTALL_APP(packageName);
				prefix_task_actions.put(UNINSTALL_APP_ACTION);
				
				String apkFileName = apkInfo.getApkfile();
				String apkDownloadURL = ServerConfig.getApkfileurl() + apkFileName;
				
				JSONObject INSTALL_APP_ACTION = JsonActionService.createAction_INSTALL_APP(packageName, apkFileName, apkDownloadURL );
				prefix_task_actions.put(INSTALL_APP_ACTION);
				
				JSONObject CLEAR_APP_ACTION = JsonActionService.createAction_CLEAR_APP(packageName);
				prefix_task_actions.put(CLEAR_APP_ACTION);
				
				String scriptDownloadURL = ServerConfig.getScriptfileurl() + apkInfo.getReg_scriptfiles();
				int scriptVersion = apkInfo.getScriptfile_version();
				
				JSONObject UPDATE_SCRIPT_ACTION = JsonActionService.createAction_UPDATE_SCRIPT(packageName, scriptDownloadURL, scriptVersion);
				prefix_task_actions.put(UPDATE_SCRIPT_ACTION);
				
				if (isInRemainPhase) {
					
					String zipFileName = "data_" + autoid + ".zip";
					
					String UNZIP_BASE_URL = ServerConfig.getZipDownloadURL();
					String zipDownloadURL = UNZIP_BASE_URL + "/" + packageName + "/" + zipFileName;
					
					JSONObject UNZIP_FILE_ACTION = JsonActionService.createAction_UNZIP_REMOTE_FILE(packageName, zipFileName, zipDownloadURL);
					prefix_task_actions.put(UNZIP_FILE_ACTION);
					
				}
				
			}

			// 配置默认的 Suffix Actions ---------------------
			if (isCustomSuffixAction == false) {
				
				String zipfilesString = apkInfo.getZipfiles();
				boolean isNeedZipLocalFiles = zipfilesString.length() != 0;
				
				if (isNeedZipLocalFiles) {
					
					String zipFileName = "data_" + autoid + ".zip";
					
					String ZIP_BASE_URL = ServerConfig.getFileServerServlet("ResourceUpload");
					String zipUploadURL = ZIP_BASE_URL + "?fileName=files/zips/" + packageName + "/" + zipFileName;
					
					String unzipFileRegex = apkInfo.getUnzip_regex();
					
					JSONObject ZIP_FILE_ACTION = JsonActionService.createAction_ZIP_FILE(packageName, zipFileName, zipUploadURL, zipfilesString, unzipFileRegex);
					suffix_task_actions.put(ZIP_FILE_ACTION);
					
				}
				
				JSONObject UNINSTALL_APP_ACTION = JsonActionService.createAction_UNINSTALL_APP(packageName);
				suffix_task_actions.put(UNINSTALL_APP_ACTION);
				
			}
		}

	}
	
	/**
	 * 前置/后置 动作JSON的 TYPE
	 */
	
	public static final String ACTION_TYPE_DOWNLOAD_RESOURCE = "DOWNLOAD_RESOURCE";
	
	public static final String ACTION_TYPE_UNZIP_FILE = "UNZIP_FILE";
	
	public static final String ACTION_TYPE_ZIP_FILE = "ZIP_FILE";

	public static final String ACTION_TYPE_INSTALL_APP = "INSTALL_APP";
	
	public static final String ACTION_TYPE_UNINSTALL_APP = "UNINSTALL_APP";
	
	public static final String ACTION_TYPE_UPDATE_SCRIPT = "UPDATE_SCRIPT";
	
	public static final String ACTION_TYPE_DELETE_SCRIPT = "DELETE_SCRIPT";
	
	// need script to tell done
	public static final String ACTION_TYPE_OPEN_APP = "OPEN_APP";
	
	// need script to tell done
	public static final String ACTION_TYPE_CLOSE_APP = "CLOSE_APP";
	
	public static final String ACTION_TYPE_START_APP = "START_APP";
	
	public static final String ACTION_TYPE_STOP_APP = "STOP_APP";
	
	public static final String ACTION_TYPE_CLEAR_APP = "CLEAR_APP";
	
	public static final String ACTION_TYPE_ENABLE_APP = "ENABLE_APP";
	
	public static final String ACTION_TYPE_DISABLE_APP = "DISABLE_APP";
	
	public static final String ACTION_TYPE_DELETE_FILES = "DELETE_FILES";
	
	public static final String ACTION_TYPE_CLEAR_DIRS = "CLEAR_DIRS";
	
	public static final String ACTION_TYPE_EXEC_COMMANDS = "EXEC_COMMANDS";
	
	public static final String ACTION_TYPE_EXEC_METHOD = "EXEC_METHOD";
	
	public static final String ACTION_TYPE_SET_SETTINGS = "SET_SETTINGS";
	
	public static final String ACTION_TYPE_ENABLE_PERMISSIONS_ANDROID_M = "ENABLE_PERMISSIONS_ANDROID_M";

	public static final String ACTION_TYPE_SLEEP = "SLEEP";
	
	public static final String ACTION_TYPE_BACK_TO_HOME = "BACK_TO_HOME";
	
	public static final String ACTION_TYPE_START_ACTIVITY = "START_ACTIVITY";
	
	public static final String ACTION_TYPE_SWITCH_WIFI = "SWITCH_WIFI";
	
	public static final String ACTION_TYPE_ACCESSIBILITY = "ACCESSIBILITY";
	
	/**
	 * 前置/后置 动作JSON的 工具方法
	 */

	public static JSONArray getActions(JSONArray prefix_suffix_actions, String actionKey) {
		JSONArray actions = new JSONArray();
		for (int i = 0; i < prefix_suffix_actions.length(); i++) {
			JSONObject actionJson = prefix_suffix_actions.optJSONObject(i);
			if (actionJson.optString("action").equals(actionKey)) {
				actions.put(actionJson);
			}
		}
		return actions;
	}

	public static JSONObject getFirstAction(JSONArray prefix_suffix_actions, String actionKey) {
		JSONArray actions = getActions(prefix_suffix_actions, actionKey);
		if (actions.length() > 0) {
			return actions.optJSONObject(0);
		}
		return null;
	}

	public static void insertAction(JSONArray prefix_suffix_actions, JSONObject action, int atIndex) {
		JSONArrayUtil.insert(prefix_suffix_actions, action, atIndex);
	}

	/**
	 * 前置/后置 动作JSON的 生成方法
	 */

	public static JSONObject createAction_DOWNLOAD_RESOURCE(String res_file_name, String res_download_url)
			throws JSONException {
		JSONObject action_DOWNLOAD_RESOURCE = new JSONObject();
		action_DOWNLOAD_RESOURCE.put("action", ACTION_TYPE_DOWNLOAD_RESOURCE);

		action_DOWNLOAD_RESOURCE.put("res_file_name", res_file_name);
		action_DOWNLOAD_RESOURCE.put("res_download_url", res_download_url);
		return action_DOWNLOAD_RESOURCE;
	}

	public static JSONObject createAction_UNZIP_LOCAL_FILE(String packageName, String zipFileName, String zipLocalPath) throws JSONException {
		return createAction_UNZIP_FILE(packageName, zipFileName, null, zipLocalPath, 0 , 0);
	}
	
	public static JSONObject createAction_UNZIP_REMOTE_FILE(String packageName, String zipFileName, String zipDownloadURL) throws JSONException {
		return createAction_UNZIP_FILE(packageName, zipFileName, zipDownloadURL, null, 0, 0);
	}
	
	public static JSONObject createAction_UNZIP_REMOTE_FILE(String packageName, String zipFileName, String zipDownloadURL, int zipVersion) throws JSONException {
		return createAction_UNZIP_FILE(packageName, zipFileName, zipDownloadURL, null, zipVersion, 0);
	}

	public static JSONObject createAction_UNZIP_FILE(String packageName, String zipFileName, String zipDownloadURL, String zipLocalPath,
			int zipVersion, long unzipTimeout) throws JSONException {
		JSONObject action_UNZIP_FILE = new JSONObject();
		action_UNZIP_FILE.put("action", ACTION_TYPE_UNZIP_FILE);
		action_UNZIP_FILE.put("packageName", packageName);

		// > 0 means tell the device need preserve it or not, for saving network data traffic
		action_UNZIP_FILE.put("zip_version", zipVersion); 

		action_UNZIP_FILE.put("zip_file_name", zipFileName);
		
		if (zipDownloadURL != null) {
			action_UNZIP_FILE.put("zip_download_url", zipDownloadURL);
		}
		
		// 如果不为空, 则解压本地ZIP文件, 不处理下载操作
		if (zipLocalPath != null) {
			action_UNZIP_FILE.put("zip_downloaded_path", zipLocalPath);
		}
		
		// milliseconds, 0 is auto, judge by device, about 1 MB 1 Minute
		action_UNZIP_FILE.put("unzip_timeout", unzipTimeout); 
		
		return action_UNZIP_FILE;
	}

	public static JSONObject createAction_ZIP_FILE(String packageName, String zipFileName, String zipUploadURL,
			String zipfiles, String notZipRegex) throws JSONException {
		return createAction_ZIP_FILE(packageName, zipFileName, zipUploadURL, zipfiles, notZipRegex, 0);
	}
	
	public static JSONObject createAction_ZIP_FILE(String packageName, String zipFileName, String zipUploadURL,
			String zipfiles, String notZipRegex, int zipTimeout) throws JSONException {
		JSONObject action_ZIP_FILE = new JSONObject();
		action_ZIP_FILE.put("action", ACTION_TYPE_ZIP_FILE);
		action_ZIP_FILE.put("packageName", packageName);

		action_ZIP_FILE.put("zip_file_name", zipFileName);
		action_ZIP_FILE.put("zip_upload_url", zipUploadURL);
		action_ZIP_FILE.put("zip_files", zipfiles);
		action_ZIP_FILE.put("not_zip_regex", notZipRegex);
		
		// milliseconds, 0 is auto, judge by device, about 1 MB 1 Minute
		action_ZIP_FILE.put("zip_timeout", zipTimeout);

		return action_ZIP_FILE;
	}

	public static JSONObject createAction_INSTALL_APP(String packageName, String fileName, String appDownloadURL)
			throws JSONException {
		JSONObject action_INSTALL_APP = new JSONObject();
		action_INSTALL_APP.put("action", ACTION_TYPE_INSTALL_APP);
		action_INSTALL_APP.put("packageName", packageName);

		action_INSTALL_APP.put("file_name", fileName);
		action_INSTALL_APP.put("file_url", appDownloadURL);
		return action_INSTALL_APP;

	}

	public static JSONObject createAction_UNINSTALL_APP(String packageName) throws JSONException {
		JSONObject action_UNINSTALL_APP = new JSONObject();
		action_UNINSTALL_APP.put("action", ACTION_TYPE_UNINSTALL_APP);
		action_UNINSTALL_APP.put("packageName", packageName);
		return action_UNINSTALL_APP;
	}

	public static JSONObject createAction_UPDATE_SCRIPT(String packageName, String scriptDownloadURL, int scriptVersion)
			throws JSONException {
		JSONObject json_UPDATE_SCRIPT = new JSONObject();
		json_UPDATE_SCRIPT.put("action", ACTION_TYPE_UPDATE_SCRIPT);
		json_UPDATE_SCRIPT.put("packageName", packageName);

		json_UPDATE_SCRIPT.put("script_url", scriptDownloadURL);
		json_UPDATE_SCRIPT.put("script_version", scriptVersion);

		return json_UPDATE_SCRIPT;
	}

	public static JSONObject createAction_DELETE_SCRIPT(String packageName) throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_DELETE_SCRIPT);
		action.put("packageName", packageName);
		return action;
	}

	public static JSONObject createAction_OPEN_APP(String packageName, long timeoutMillis) throws JSONException {
		JSONObject action_OPEN_APP = new JSONObject();
		action_OPEN_APP.put("action", ACTION_TYPE_OPEN_APP);
		action_OPEN_APP.put("packageName", packageName);
		if (timeoutMillis != 0) {
			action_OPEN_APP.put("timeout", timeoutMillis);
		}
		return action_OPEN_APP;
	}

	public static JSONObject createAction_CLOSE_APP(String packageName, long timeoutMillis) throws JSONException {
		JSONObject action_CLOSE_APP = new JSONObject();
		action_CLOSE_APP.put("action", ACTION_TYPE_CLOSE_APP);
		action_CLOSE_APP.put("packageName", packageName);
		if (timeoutMillis != 0) {
			action_CLOSE_APP.put("timeout", timeoutMillis);
		}
		return action_CLOSE_APP;
	}

	public static JSONObject createAction_START_APP(String packageName) throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_START_APP);
		action.put("packageName", packageName);
		return action;
	}

	public static JSONObject createAction_STOP_APP(String packageName) throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_STOP_APP);
		action.put("packageName", packageName);
		return action;
	}

	public static JSONObject createAction_CLEAR_APP(String packageName) throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_CLEAR_APP);
		action.put("packageName", packageName);
		return action;
	}

	public static JSONObject createAction_ENABLE_APP(String packageName) throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_ENABLE_APP);
		action.put("packageName", packageName);
		return action;
	}

	public static JSONObject createAction_DISABLE_APP(String packageName) throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_DISABLE_APP);
		action.put("packageName", packageName);
		return action;
	}

	public static JSONObject createAction_DELETE_FILES(JSONArray deletePaths) throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_DELETE_FILES);

		action.put("delete_paths", deletePaths);
		return action;
	}

	public static JSONObject createAction_CLEAR_DIRS(JSONArray clearDirs) throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_CLEAR_DIRS);

		action.put("clear_dirs", clearDirs);
		return action;
	}

	public static JSONObject createAction_EXEC_COMMANDS(String commands) throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_EXEC_COMMANDS);

		action.put("execute_commands", commands);
		return action;
	}

	public static JSONObject createAction_EXEC_ROMSERVER_METHOD(String className, String methodName) throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_EXEC_METHOD);
		action.put("className", className);
		action.put("methodName", methodName);
		
		action.put("isRomServerMethod", true);

		return action;
	}
	
	public static JSONObject createAction_EXEC_SCRIPT_METHOD(String packageName, long timeout, String className, String methodName) throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_EXEC_METHOD);
		action.put("className", className);
		action.put("methodName", methodName);
		
		action.put("isRomServerMethod", false);
		
		// script method
		action.put("packageName", packageName);
		action.put("timeout", timeout);

		return action;
	}

	public static JSONObject createAction_SET_SETTINGS(String settingsType, String settingsKey, String settingsValue)
			throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_SET_SETTINGS);

		 // value is one of: Secure, System, Global
		action.put("settings_type", settingsType);
		
		action.put("settings_key", settingsKey);
		action.put("settings_value", settingsValue);
		
		return action;
	}
	
	public static JSONObject createAction_ENABLE_PERMISSIONS_ANDROID_M(String packageName, String permissionsIndexes)
			throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_ENABLE_PERMISSIONS_ANDROID_M);

		action.put("packageName", packageName);
		
		if (permissionsIndexes != null) {
			action.put("permissionsIndexes", permissionsIndexes);
		}
		
		// 另外还有:
		// "permissionsCount" 		// 权限总个数，默认9个
		// "timeMillisWaitForPage"	// 等待权限页打开时间
		// "timeMillisBeforeTap"	// 每个点击前等待时间
		// "timeMillisAfterTap"		// 每个点击后等待时间
		
		return action;
	}
	
	public static JSONObject createAction_SLEEP(long sleepMillis)
			throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_SLEEP);

		action.put("sleepMillis", sleepMillis);
		
		return action;
	}
	
	public static JSONObject createAction_BACK_TO_HOME() throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_BACK_TO_HOME);

		return action;
	}
	
	public static JSONObject createAction_START_ACTIVITY(String intent_action) throws JSONException {
		return createAction_START_ACTIVITY(intent_action, null, null, null);
	}
	
	public static JSONObject createAction_START_ACTIVITY(String intent_action, JSONObject bundle_json, JSONArray intent_categories, JSONArray intent_flags) throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_START_ACTIVITY);

		action.put("intent_action", intent_action);
		if (bundle_json != null) {
			action.put("bundle_json", bundle_json);
		}
		if (intent_categories != null) {
			action.put("intent_categories", intent_categories);
		}
		if (intent_flags != null) {
			action.put("intent_flags", intent_flags);
		}
		
		return action;
	}
	
	public static JSONObject createAction_SWITCH_WIFI(String wifi_name, String wifi_password) throws JSONException {
		return createAction_SWITCH_WIFI(wifi_name, wifi_password, -1);
	}
	
	public static JSONObject createAction_SWITCH_WIFI(String wifi_name, String wifi_password, long switch_timeout ) throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_SWITCH_WIFI);

		action.put("wifi_name", wifi_name);
		action.put("wifi_password", wifi_password);
		if (switch_timeout != -1) {
			action.put("intent_action", switch_timeout);
		}
		
		return action;
	}
	
	public static JSONObject createAction_ACCESSIBILITY_GLOBAL(Boolean isGlobal, int actionCode) throws JSONException {
		return createAction_ACCESSIBILITY(true, actionCode, null, null);
	}
	
	public static JSONObject createAction_ACCESSIBILITY_NODE(Boolean isGlobal, int actionCode, String actionText, JSONObject actionBundle) throws JSONException {
		return createAction_ACCESSIBILITY(false, actionCode, actionText, actionBundle);
	}
	
	public static JSONObject createAction_ACCESSIBILITY(Boolean isGlobal, int actionCode, String actionText, JSONObject actionBundle) throws JSONException {
		JSONObject action = new JSONObject();
		action.put("action", ACTION_TYPE_ACCESSIBILITY);

		action.put("isGlobal", isGlobal);
		action.put("actionCode", actionCode);
		
		if (actionText != null) {
			action.put("actionText", actionText);
		}
		if (actionBundle != null) {
			action.put("actionBundle", actionBundle);
		}
		
		return action;
	}

}
