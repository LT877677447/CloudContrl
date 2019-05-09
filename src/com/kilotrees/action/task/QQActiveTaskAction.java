/**
 * @author Administrator
 * 2019年2月17日 上午12:32:15 
 */
package com.kilotrees.action.task;

import java.util.Random;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.dao.task.QQActiveTaskDao;
import com.kilotrees.model.bo.TaskBase;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.services.JsonActionService;

public class QQActiveTaskAction implements ITaskAction {
	private static Logger log = Logger.getLogger(QQActiveTaskAction.class);
	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		// TODO Auto-generated method stub

		JSONArray adtasks = response.optJSONArray("tasks");
		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");

		if (adtasks.length() > 0) {

			JSONObject adtask = (JSONObject) adtasks.get(0);

			// now use 'suffix_task_actions' to tell the device to zip file and upload
			int taskPhase = adtask.optInt("taskPhase");

			String packageName = adtask.optString("packageName");

			// 1. prefix_task_actions
			String zipTxlibFileName = "txlib.zip";
			String zipTxlibFilePath = "Resources/QQ/" + zipTxlibFileName;
			String zipTxlibDownloadURL = ServerConfig.getStorageBaseURL() + zipTxlibFilePath;

			String zipEventFileName = "getevent.zip";
			String zipEventFilePath = "Resources/QQ/" + zipEventFileName;
			String zipEventDownloadURL = ServerConfig.getStorageBaseURL() + zipEventFilePath;

			JSONObject action_unzip_txlib = JsonActionService.createAction_UNZIP_REMOTE_FILE(packageName, zipTxlibFileName, zipTxlibDownloadURL);
			prefix_task_actions.put(action_unzip_txlib);

			JSONObject action_unzip_event = JsonActionService.createAction_UNZIP_REMOTE_FILE(packageName, zipEventFileName, zipEventDownloadURL);
			prefix_task_actions.put(action_unzip_event);

			String qqNumber = adtask.optString("qqnum");
			String twoPrefixQQFolder = qqNumber.substring(0, 2);
			String zipQQFileName = qqNumber + ".zip";
			String zipQQFilePath = "Resources/QQ/" + twoPrefixQQFolder + "/" + zipQQFileName;

			if (taskPhase == TaskBase.TASK_PHASE_REMAIN) {
				// Download the remain zip file in phase remain
				String zipDownloadURL = ServerConfig.getStorageBaseURL() + zipQQFilePath;

				JSONObject action_UNZIP_FILE_QQ = JsonActionService.getFirstAction(prefix_task_actions, JsonActionService.ACTION_TYPE_UNZIP_FILE);
				if (action_UNZIP_FILE_QQ != null) {
					action_UNZIP_FILE_QQ.put("zip_file_name", zipQQFileName);
					action_UNZIP_FILE_QQ.put("zip_download_url", zipDownloadURL);
				} else {
					action_UNZIP_FILE_QQ = JsonActionService.createAction_UNZIP_REMOTE_FILE(packageName, zipQQFileName, zipDownloadURL);
					prefix_task_actions.put(action_UNZIP_FILE_QQ);
				}
			}
			
			// socket5 ------------------
			String socket5_fileName = "org.proxydroidApp.apk";
			String socket5_packageName = "org.proxydroid";
			String socket5_appfileurl = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk";

			JSONObject jsonInstall = JsonActionService.createAction_INSTALL_APP(socket5_packageName, socket5_fileName, socket5_appfileurl);
			prefix_task_actions.put(jsonInstall);
			
			JSONObject jsonStop = JsonActionService.createAction_STOP_APP(socket5_packageName);
			prefix_task_actions.put(jsonStop);
			
			JSONObject jsonOpen = JsonActionService.createAction_OPEN_APP(socket5_packageName, 0);
			prefix_task_actions.put(jsonOpen);
			
			JSONObject jsonClose = JsonActionService.createAction_CLOSE_APP(socket5_packageName, 0);
			suffix_task_actions.put(jsonClose);
			// socket5 ------------------
			
			// 2. suffix_task_actions
			String zipfiles = adtask.optString("zipfiles");
			String notZipRegex = adtask.optString("unzip_regex");

			String resourceUploadURL = ServerConfig.getFileServerServlet("ResourceUpload");
			String zipUploadURL = resourceUploadURL + "?fileName=" + zipQQFilePath;

			JSONObject action_ZIP_FILE = JsonActionService.getFirstAction(suffix_task_actions, JsonActionService.ACTION_TYPE_ZIP_FILE);
			if (action_ZIP_FILE != null) {
				action_ZIP_FILE.put("zip_file_name", zipQQFileName);
				action_ZIP_FILE.put("zip_upload_url", zipUploadURL);
			} else {
				action_ZIP_FILE = JsonActionService.createAction_ZIP_FILE(packageName, zipQQFileName, zipUploadURL, zipfiles, notZipRegex);
				suffix_task_actions.put(action_ZIP_FILE);
			}
			
			
			// 加群 与 加好友
			if (taskPhase == TaskBase.TASK_PHASE_NEWLY) {
				JSONObject extraInfo = new JSONObject();
				adtask.put("extraInfo", extraInfo);
				
				String group_1 = "821784648";//181--200
				String group_2 = "583154381";//125--200
				String group_3 = "744969210";//118--200
				String group_4 = "713681926";//1--200
				String group_5 = "941843758";//1--500
				
				
				String[] groups = new String[] {group_2, group_3,group_4,group_5};
				int index = new Random().nextInt(4);
				String qqGroupNumber = groups[index];
				
				String qqFriendNumber = "";
				extraInfo.put("QQGroup", qqGroupNumber);
				extraInfo.put("QQFriend", qqFriendNumber);
			}
			
		}

	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {
		int result_code = request.optInt("result");
		// String result_info = request.optString("result_info");
		String qqnum = request.optString("qqnum");

		if (result_code == 1000) {
			// 脚本错误， do nothing ...
		} else

		if (result_code >= 1001 && result_code < 2000) {
			// 网络错误， do nothing ..

		} else

		if (result_code == 2000) {
			// QQ号已经不能用, 更新数据库QQ号总表的 status = -999
			QQActiveTaskDao.updateResultError(qqnum);

		} else

		if (result_code == 2001) {
			// QQ号密码被修改了, 更新数据库QQ号总表的 status = -99
			// QQ 6.5 版本太低，需要更新到新版本才能登录成功
			QQActiveTaskDao.updateResultPassChanged(qqnum);

		} else

		if (result_code == 500) {
			// QQ号登录失败， 更新数据库QQ号总表的 status - 1; 如果之前status > 0, status = 0
			QQActiveTaskDao.updateResultFailure(qqnum);

		} else

		if (result_code == 0) {
			// QQ号登录成功， 更新数据库QQ号总表的 status + 1; 如果之前status < 0, status = 0
			String appInfo = request.optJSONObject("appInfo").toString();
			String phoneInfo = request.optJSONObject("phoneInfo").toString();
			QQActiveTaskDao.updateResultSuccess(qqnum, appInfo, phoneInfo);

		}

	}

}
