/**
 * @author Administrator
 * 2019年4月18日 下午9:55:48 
 */
package com.kilotrees.action.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.action.ITaskAction;
import com.kilotrees.dao.deviceinfodao;
import com.kilotrees.dao.phonetypedao;
import com.kilotrees.dao.vpninfodao;
import com.kilotrees.dao.douYin.DouYinDao;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.phonetype;
import com.kilotrees.model.po.vpninfo;
import com.kilotrees.services.JsonActionService;
import com.kilotrees.util.DouYinIssueLog;
import com.kilotrees.util.InfoGenUtil;
import com.kilotrees.util.JSONObjectUtil;

public class DouYinZhuCeTaskAction implements ITaskAction {
	private static List<JSONObject> phoneInfoList = new ArrayList<>();
	private static Integer phoneInfoIndex2Take = 0;
	
	static {
		
		try {
			List<phonetype> p2 = phonetypedao.getOPPOPhoneInfo("tb_phonetype", "oppo");
			for (int i = 0; i < p2.size(); i++) {
				JSONObject phoneInfo = new JSONObject(p2.get(i).getPhone_info());
				phoneInfoList.add(phoneInfo);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public void handleTaskRequest(JSONObject request, JSONObject response) throws Exception {
		JSONArray adtasks = response.optJSONArray("tasks");
		JSONObject adtask = adtasks.optJSONObject(0);
		JSONArray prefix_task_actions = response.optJSONArray("prefix_actions");
		JSONArray suffix_task_actions = response.optJSONArray("suffix_actions");
		String dev_tag = response.optString("dev_tag");
		String packageName = adtask.optString("packageName");
		
		if (adtasks.length() == 0) {
			return;
		}
		
		//VPN
		String devicesUsingWuJiVPN = "A6001 A6002 A6003 A6004 A6005 A6006 A6007 A6008 A6009 A6010 A6011 A6012 A6013 A6014 A6015"
								   +" B6001 B6002 B6003 B6004 B6005 B6006 B6007 B6008 B6009 B6010 B6011 B6012 B6013 B6014 B6015";// 使用无极设备
		Boolean isUsingWuJiVPN = devicesUsingWuJiVPN.contains(dev_tag);
		if (!isUsingWuJiVPN) {
			// socket5
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
//			String appDownloadURL = ServerConfig.getStoragePrivateBaseURL() + "/phone_files/update/wuji_duli_60.apk";
//			JSONObject wujiVPN_Action = JsonActionService.createAction_INSTALL_APP("org.wuji", "wuji_duli_60.apk",
//					appDownloadURL);
//			prefix_task_actions.put(wujiVPN_Action);
			
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
		
		//prefix action
		JSONObject enablePermissions = JsonActionService.createAction_ENABLE_PERMISSIONS_ANDROID_M(packageName, "0,1,2");
		prefix_task_actions.put(enablePermissions);
		
		JSONObject command = JsonActionService.createAction_EXEC_COMMANDS("input keyevent 4 ");//返回的actions
		prefix_task_actions.put(command);
		
		JSONObject sleepAction = JsonActionService.createAction_SLEEP(4000);
		prefix_task_actions.put(sleepAction);
		
		
		//phoneInfo
//		JSONObject phoneInfo = new JSONObject();
//		phoneInfo = phoneInfoList.get(phoneInfoIndex2Take);
//		phoneInfoIndex2Take++;
//		phoneInfoIndex2Take = phoneInfoIndex2Take % phoneInfoList.size();
//		adtask.put("phoneInfo", phoneInfo);
		
		JSONObject phoneInfo = adtask.optJSONObject("phoneInfo"); 
		phoneInfo.remove("Build.VERSION.SDK");
		phoneInfo.remove("Build.VERSION.SDK_INT");
		changePhoneInfoToWifi(phoneInfo);
		phoneInfo.remove("Screen.widthPixels");
		phoneInfo.remove("Screen.heightPixels");
		
		//appinfo
		JSONObject appinfo = adtask.optJSONObject("appInfo");
		appinfo.put("phase", "zhuce");
		
		DouYinIssueLog.log(dev_tag, adtask.optJSONObject("phoneInfo").toString(), new Date());
	}

	@Override
	public void handleTaskReport(JSONObject request, JSONObject response) throws Exception {
		int result_code = request.optInt("result");

		JSONObject appinfoJson = request.optJSONObject("appInfo");
		JSONObject phoneInfoJson = request.optJSONObject("phoneInfo");
		
		String phoneNumber = appinfoJson.optString("phoneNumber");
		String pass = appinfoJson.optString("password");
		String comment = "注册成功|result:"+result_code;
		String appinfo = appinfoJson.toString();
		String phoneInfo = phoneInfoJson.toString();
		String autoid = request.optLong("autoid")+"";
		int status = 1;
		if (result_code == 0) {
			DouYinDao.newAccountSuccess(autoid,phoneNumber, pass, new Date(), appinfo, phoneInfo,status,comment);
		}else {
			//抖音注册失败,logSubType = 1 
			String info = "注册失败|result:"+result_code;
			DouYinDao.RegistLog_1(phoneNumber, pass, info, appinfo, phoneInfo,  new Date());
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
		for(String str:keys) {
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
