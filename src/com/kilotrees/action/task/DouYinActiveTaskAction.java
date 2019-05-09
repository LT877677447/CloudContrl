/**
 * @author Administrator
 * 2019年4月27日 下午4:49:50 
 */
package com.kilotrees.action.task;

import java.util.Random;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.dao.deviceinfodao;
import com.kilotrees.dao.vpninfodao;
import com.kilotrees.dao.task.DouYinActiveTaskDao;
import com.kilotrees.dao.task.WXActiveTaskDao;
import com.kilotrees.model.bo.TaskBase;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.vpninfo;
import com.kilotrees.services.JsonActionService;
import com.kilotrees.util.InfoGenUtil;
import com.kilotrees.util.JSONObjectUtil;

public class DouYinActiveTaskAction implements ITaskAction {
	private static Logger log = Logger.getLogger(DouYinActiveTaskAction.class);

	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		JSONArray adtasks = response.optJSONArray("tasks");
		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");
		String dev_tag = response.optString("dev_tag");

		if (adtasks.length() == 0) {
			return;
		}

		JSONObject adtask = adtasks.getJSONObject(0);
		int taskPhase = adtask.optInt("taskPhase");
		String packageName = adtask.optString("packageName");
		String autoid = adtask.optString("autoid");
		String zipFileName = "data_" + autoid + ".zip";
		String zipFilePath = "files/zips/" + packageName + "/" + zipFileName;

		if (taskPhase == TaskBase.TASK_PHASE_REMAIN) {
			// Download the remain zip file in phase remain
			String zipDownloadURL = ServerConfig.getStorageBaseURL() + zipFilePath;

			JSONObject action_UNZIP_FILE = JsonActionService.getFirstAction(prefix_task_actions, JsonActionService.ACTION_TYPE_UNZIP_FILE);
			if (action_UNZIP_FILE != null) {
				action_UNZIP_FILE.put("zip_file_name", zipFileName);
				action_UNZIP_FILE.put("zip_download_url", zipDownloadURL);
			} else {
				action_UNZIP_FILE = JsonActionService.createAction_UNZIP_REMOTE_FILE(packageName, zipFileName, zipDownloadURL);
				prefix_task_actions.put(action_UNZIP_FILE);
			}
		}

		// 1.prefix action
		JSONObject enablePermissions = JsonActionService.createAction_ENABLE_PERMISSIONS_ANDROID_M(packageName, "0,1,2");
		prefix_task_actions.put(enablePermissions);
		
		JSONObject command1 = JsonActionService.createAction_EXEC_COMMANDS("input tap 99 180");
		prefix_task_actions.put(command1);
		
		JSONObject sleep = JsonActionService.createAction_SLEEP(2000);
		prefix_task_actions.put(sleep);
		
		
		JSONObject command2 = JsonActionService.createAction_EXEC_COMMANDS("input keyevent 4 ");//返回的actions
		prefix_task_actions.put(command2);
		
		// 2. suffix_task_actions
		String zipfiles = adtask.optString("zipfiles");
		String notZipRegex = adtask.optString("unzip_regex");

		String resourceUploadURL = ServerConfig.getFileServerServlet("ResourceUpload");
		String zipUploadURL = resourceUploadURL + "?fileName=" + zipFilePath;

		JSONObject action_ZIP_FILE = JsonActionService.getFirstAction(suffix_task_actions, JsonActionService.ACTION_TYPE_ZIP_FILE);
		if (action_ZIP_FILE != null) {
			action_ZIP_FILE.put("zip_file_name", zipFileName);
			action_ZIP_FILE.put("zip_upload_url", zipUploadURL);
		} else {
			action_ZIP_FILE = JsonActionService.createAction_ZIP_FILE(packageName, zipFileName, zipUploadURL, zipfiles, notZipRegex);
			suffix_task_actions.put(action_ZIP_FILE);
		}

		// socket5
		String devicesUsingWuJiVPN = "B6012 B6001 B6002 B6003 B6004";// 使用无极设备
		Boolean isUsingWuJiVPN = devicesUsingWuJiVPN.contains(dev_tag);
		if (!isUsingWuJiVPN) {
			// socket5
			String socket5PackageName = "org.proxydroid";
			String socket5FileName = "org.proxydroidApp.apk";
			String socket5APKurl = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/org.proxydroidApp.apk";

			JSONObject jsonInstall = JsonActionService.createAction_INSTALL_APP(socket5PackageName, socket5FileName, socket5APKurl);
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
//			String appDownloadURL = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/wuji_duli_60.apk";
//			JSONObject wujiVPN_Action = JsonActionService.createAction_INSTALL_APP("org.wuji", "wuji_duli_60.apk", appDownloadURL);
//			prefix_task_actions.put(wujiVPN_Action);

			// ----------- 无极VPN -----------
			String wuJiVPNPackageName = "org.wuji";
			String wuJiVPNAPKFileName = "wuji_duli_60.apk";
			String wuJiVPNAPKurl = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/wuji_duli_60.apk";

			JSONObject install = JsonActionService.createAction_INSTALL_APP(wuJiVPNPackageName, wuJiVPNAPKFileName, wuJiVPNAPKurl);
			JSONObject stop = JsonActionService.createAction_STOP_APP(wuJiVPNPackageName);
			JSONObject open = JsonActionService.createAction_OPEN_APP(wuJiVPNPackageName, 0);
			JSONObject close = JsonActionService.createAction_CLOSE_APP(wuJiVPNPackageName, 0);

			int vpnid = deviceinfodao.getDeviceInfo(dev_tag).getVpnid();
			vpninfo vpnInfo = vpninfodao.getVpnById(vpnid);
			open.put("vpnAccount", vpnInfo.getAccount());
			open.put("vpnPassword", vpnInfo.getPass());

			prefix_task_actions.put(install);
			prefix_task_actions.put(stop);
			prefix_task_actions.put(open);
			suffix_task_actions.put(close);
			suffix_task_actions.put(stop);
		}

		// phoneInfo
		JSONObject phoneInfo = adtask.optJSONObject("phoneInfo");
		phoneInfo.remove("Build.VERSION.SDK");
		phoneInfo.remove("Build.VERSION.SDK_INT");
		changePhoneInfoToWifi(phoneInfo);

//		JSONObject newPhoneInfo = changePhoneInfo(phoneInfo);
//		adtask.put("phoneInfo", newPhoneInfo);
		
		JSONObject connectionInfo = phoneInfo.optJSONObject("Wifi.ConnectionInfo");
		String macAddress = connectionInfo.optString("MacAddress");
		JSONObject fileContentsInfo  = phoneInfo.optJSONObject("Files.Contents");
		if (fileContentsInfo == null) {
			fileContentsInfo = new JSONObject();
			phoneInfo.put("Files.Contents", fileContentsInfo);
		}
		String pathWlan0Address = "/sys/class/net/wlan0/address";
		String pathWlan0AddressLink = "/sys/devices/fb000000.qcom,wcnss-wlan/net/wlan0/address";
		fileContentsInfo.put(pathWlan0Address, macAddress);
		fileContentsInfo.put(pathWlan0AddressLink, macAddress);
		 
		//appinfo
		JSONObject appinfo = adtask.optJSONObject("appInfo");
		appinfo.put("phase", "active");
	}

	@SuppressWarnings("unused")
	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {
		int result = request.optInt("result");

		String phoneNumber = request.optString("phoneNumber");
		if (result == 0) {
			//活跃成功
			JSONObject appinfoJson = request.optJSONObject("appInfo");
			JSONObject phoneInfoJson = request.optJSONObject("phoneInfo");

			String autoid = request.optString("autoid");
			DouYinActiveTaskDao.SuccActive(phoneNumber);
		} else {
			//活跃失败
			DouYinActiveTaskDao.failActive(phoneNumber);
		}
	}

	public static void changePhoneInfoToWifi(JSONObject phoneInfoTemplate) {
		try {

			JSONObject connectivityInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "Connectivity.");
			JSONObject activeNetworkInfo = connectivityInfo.optJSONObject("Connectivity.ActiveNetworkInfo");

			int mNetworkType = 1;
			boolean isUsingWIFI = mNetworkType == 1;

			activeNetworkInfo.put("mNetworkType", mNetworkType);
			String mExtraInfo = isUsingWIFI ? InfoGenUtil.genName() : "cmnet";
			String mTypeName = isUsingWIFI ? "WIFI" : "mobile";
			if (isUsingWIFI) {
				activeNetworkInfo.put("mExtraInfo", mExtraInfo);
				activeNetworkInfo.put("mTypeName", mTypeName);
				activeNetworkInfo.put("mReason", "");
				activeNetworkInfo.put("mSubtypeName", "");
				activeNetworkInfo.put("mSubtype", 0);
			} else {
				activeNetworkInfo.put("mExtraInfo", mExtraInfo);
				activeNetworkInfo.put("mTypeName", mTypeName);
				activeNetworkInfo.put("mReason", "2GVoiceCallEnded");
				activeNetworkInfo.put("mSubtypeName", "EDGE");
				activeNetworkInfo.put("mSubtype", 2);
			}

			if (isUsingWIFI) {
				// WIFI Is ["wlan0"], 4g Is []
				connectivityInfo.put("Connectivity.TetherableIfaces", new JSONArray(new String[] { "wlan0" }));

			} else {
				JSONObject activeNetworkQuotaInfo = new JSONObject();
				connectivityInfo.put("Connectivity.ActiveNetworkQuotaInfo", activeNetworkQuotaInfo);
				activeNetworkQuotaInfo.put("mSoftLimitBytes", 214748364);
				activeNetworkQuotaInfo.put("mHardLimitBytes", -1);
				activeNetworkQuotaInfo.put("NO_LIMIT", -1);
				activeNetworkQuotaInfo.put("mEstimatedBytes", 386779);

				// WIFI Is ["wlan0"], 4g Is []
				connectivityInfo.put("Connectivity.TetherableIfaces", new JSONArray());

			}

			JSONObject networkInfo = new JSONObject();
			connectivityInfo.put("Connectivity.NetworkInfo", networkInfo);
			networkInfo.put("mState", 4); // Connected
			networkInfo.put("mNetworkType", mNetworkType);
			networkInfo.put("mSubtype", 0);
			networkInfo.put("mSubtypeName", "");
			networkInfo.put("mExtraInfo", mExtraInfo);
			networkInfo.put("mTypeName", mTypeName);
			networkInfo.put("mIsFailover", false);
			networkInfo.put("mIsAvailable", true);

			// 3. Telephony -------------------------------------------------
			JSONObject telephonyInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "Telephony.");
			telephonyInfo.put("Telephony.DataActivity", isUsingWIFI ? 0 : 3);
			telephonyInfo.put("Telephony.DataNetworkType", isUsingWIFI ? 0 : 2);
			telephonyInfo.put("Telephony.NetworkType", isUsingWIFI ? 0 : 2);

			// ICCID(Integrate circuit card identity) 集成电路卡识别码(固化在手机SIM卡中) ICCID
			// 为IC 卡的唯一识别号码，共有20 位数字组成
			String simSerialNumber = "898600" + InfoGenUtil.gen2(14);
			telephonyInfo.put("Telephony.SimSerialNumber", simSerialNumber);
			telephonyInfo.put("Telephony.IccSerialNumber", simSerialNumber);

			telephonyInfo.put("Telephony.Line1Number", "150" + InfoGenUtil.gen2(8));
			telephonyInfo.put("Telephony.Msisdn", "150" + InfoGenUtil.gen2(8));

			telephonyInfo.put("Telephony.hasIccCard", true);
			telephonyInfo.put("Telephony.VoiceNetworkType", 16);
			telephonyInfo.put("Telephony.GroupIdLevel1", "ffffffff");
			telephonyInfo.put("Telephony.Line1AlphaTag", "@@@@@@@@@@@@@@");
			telephonyInfo.put("Telephony.isDataConnectivityPossible", true);

			// 4. WIFI -------------------------------------------------
			JSONObject wifiInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "Wifi.");
			JSONObject connectionInfo = wifiInfo.optJSONObject("Wifi.ConnectionInfo");

			String wifiName = mExtraInfo;
			Integer ipAddrConnected = InfoGenUtil.getOneRandomIntIP();
			if (isUsingWIFI) {
				// Wifi.ConnectionInfo
				connectionInfo.put("LinkSpeed", new Random().nextInt(100) + 433);
				connectionInfo.put("NetworkId", 0);
				connectionInfo.put("HiddenSSID", false);
				connectionInfo.put("Rssi", 0 - new Random().nextInt(100));
				connectionInfo.put("IpAddress", ipAddrConnected);
				connectionInfo.put("MeteredHint", false);
				connectionInfo.put("WifiSsid", wifiName);
				connectionInfo.put("SSID", wifiName);

				// Wifi.DhcpInfo
				int gatewayIpAddress = InfoGenUtil.getGatewayFromIntIP(ipAddrConnected);
				JSONObject dhcpInfo = new JSONObject();
				wifiInfo.put("Wifi.DhcpInfo", dhcpInfo);
				dhcpInfo.put("netmask", 16777215); // 255.255.255.0, 0xFFFFFF
				dhcpInfo.put("dns2", 0);
				dhcpInfo.put("dns1", gatewayIpAddress);
				dhcpInfo.put("serverAddress", gatewayIpAddress);
				dhcpInfo.put("ipAddress", ipAddrConnected);
				dhcpInfo.put("gateway", gatewayIpAddress);
				dhcpInfo.put("leaseDuration", (new Random().nextInt(50) + 50) * 1000);

			} else {
				// Wifi.ConnectionInfo
				connectionInfo.put("LinkSpeed", -1);
				connectionInfo.put("NetworkId", -1);
				connectionInfo.put("HiddenSSID", false);
				connectionInfo.put("Rssi", -200);
				connectionInfo.put("IpAddress", 0);
				connectionInfo.put("MeteredHint", false);
				connectionInfo.put("WifiSsid", "");
				connectionInfo.put("SSID", "");

				// Wifi.DhcpInfo
				JSONObject dhcpInfo = new JSONObject();
				wifiInfo.put("Wifi.DhcpInfo", dhcpInfo);
				dhcpInfo.put("netmask", -1);
				dhcpInfo.put("dns2", 0);
				dhcpInfo.put("dns1", 0);
				dhcpInfo.put("serverAddress", 0);
				dhcpInfo.put("ipAddress", 0);
				dhcpInfo.put("gateway", 0);
				dhcpInfo.put("leaseDuration", 0);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JSONObject changePhoneInfo(JSONObject phoneInfo) throws JSONException {
		JSONObject newPhoneInfo = new JSONObject();
		String key = "Build.SERIAL,Build.FINGERPRINT,Build.ID,Build.MANUFACTURER,Build.DEVICE,Build.TIME,Build.BRAND,Build.CPU_ABI,"
				+ "Build.VERSION.INCREMENTAL,Build.VERSION.RELEASE,Build.DISPLAY,Build.BRAND,Build.MODEL,Bluetooth.Address,Bluetooth.Name,"
				+ "Telephony.DeviceId,Telephony.SubscriberId,Telephony.Line1Number,Telephony.Msisdn,Telephony.NetworkOperator,Telephony.NetworkOperatorName,"
				+ "Telephony.IccSerialNumber,WebKit.UserAgent,Package.InstalledPackages";

		String keys[] = key.split(",");
		for (String str : keys) {
			Object value = phoneInfo.opt(str);
			newPhoneInfo.put(str, value);
		}

		JSONObject newSettings = new JSONObject();
		newPhoneInfo.put("Settings", newSettings);

		JSONObject SystemJson = new JSONObject();
		JSONObject SecureJson = new JSONObject();
		newSettings.put("System", SystemJson);
		newSettings.put("Secure", SecureJson);

		SystemJson.put("android_id", phoneInfo.optJSONObject("Settings").optJSONObject("System").opt("android_id"));
		SystemJson.put("android_id", phoneInfo.optJSONObject("Settings").optJSONObject("Secure").opt("android_id"));

		JSONObject ConnectionInfo = new JSONObject();
		newPhoneInfo.put("Wifi.ConnectionInfo", ConnectionInfo);
		ConnectionInfo.put("MacAddress", phoneInfo.optJSONObject("Wifi.ConnectionInfo").opt("MacAddress"));
		return newPhoneInfo;
	}

}
