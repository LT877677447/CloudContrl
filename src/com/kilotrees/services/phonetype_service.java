package com.kilotrees.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.dao.phonetypedao;
import com.kilotrees.model.bo.error_result;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.phonetype;
import com.kilotrees.util.FileUtil;
import com.kilotrees.util.InfoGenUtil;
import com.kilotrees.util.JSONObjectUtil;

/**
 * 管理机型信息,模似客户端的机器信息
 *
 */
public class phonetype_service {
	private static Logger log = Logger.getLogger(phonetype_service.class);

	private static phonetype_service inst;

	/**
	 * 机型信息表list
	 */
	List<phonetype> list = new ArrayList<phonetype>();
	ArrayList<Integer> indexList = new ArrayList<Integer>();

	private phonetype_service() {
		
	}

	public static phonetype_service getInstance() {
		synchronized (phonetype_service.class) {
			if (inst == null) {
				inst = new phonetype_service();
			}
		}
		return inst;
	}

	/** 
	 * 拿100条机器信息更新list
	 */
	public void refresh() {
		List<phonetype> ls = phonetypedao.getPhoneInfoList();
		synchronized (list) {
			if (ls != null) {
				this.list = ls;
				indexList.clear();
				for (int i = 0; i < list.size(); i++) {
					indexList.add(i);
					// 按权重大小重复多加
					for (int j = 1; j < list.get(i).getUse_radio(); j++) {
						indexList.add(i);
					}
				}
			} else {
				ErrorLog_service.system_errlog(getClass().getName()+"phonetypedao.getPhoneTypeList list = null,出错!!!");
				log.error("phonetypedao.getPhoneTypeList list = null,出错!!!");
			}
		}
	}

	/**
	 * 注册机型，客户端从外面抓取到原始机型信息，在这里入库
	 */
	public synchronized JSONObject registPhoneType(JSONObject request) {
		return registPhoneType(request, null);
	}
	
	public synchronized JSONObject registPhoneType(JSONObject request, String tableName) {
		// 结果，不只是包含失败消息
		error_result er = new error_result();
		// 从json中获取机型信息
		String phone_type = request.optString("Build.MODEL");
		try {
			// 将机型信息添加到tb_phonetype表中
			phonetypedao.addPhoneInfo(phone_type, request.toString(), tableName);
			if (er.getErr_code() == 0)
				er.setErr_info("成功");
			
			return er.toJSONObject();
			
		} catch (JSONException e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
			er.setErr_code(error_result.System_exception_error);
			er.setErr_info(e.getMessage());
		}
		
		return null;
	}
	

	/**
	 * 按权重比例随机选择一种机型．
	 */
	public phonetype getPhoneRandom() {
		int index = 0;
		synchronized (list) {
			int size = indexList.size();
			if (size == 0) {
				return null;
			}
			// System.out.println("indexList.size() = " + indexList.size());
			java.util.Random rand = new java.util.Random();
			int i_rand = rand.nextInt(size);

			index = indexList.get(i_rand);
			return list.get(index);
		}
	}
	
	/**
	 * 根据手机型号限定模拟手机信息参数，这里从数据库中获取网上搜出来的手机信息，然后替换imei,imsi,mac这些可变参数，组成json返回
	 * 象sdk.version，cpu，屏幕参数必须是原始的，不能变。
	 * 
	 * @param typesLimits,以后处理
	 * @return
	 * @throws JSONException
	 */
	public JSONObject randPhoneInfo() {
		JSONObject phoneInfo = new JSONObject();
		
		phonetype phoneRandom = getPhoneRandom();
		if (phoneRandom != null) {
			try {
				phoneInfo = new JSONObject(phoneRandom.getPhone_info());
				
				JSONObject staticInfo = getStaticMatchingInfo(phoneInfo);
				JSONObjectUtil.mergeJSONObject(phoneInfo, staticInfo);
				
				JSONObject stableInfo = reandomTheStablePhoneInfo(phoneInfo);
				JSONObjectUtil.mergeJSONObject(phoneInfo, stableInfo);
				
				JSONObject unstableInfo = reandomTheUnstablePhoneInfo(phoneInfo);
				JSONObjectUtil.mergeJSONObject(phoneInfo, unstableInfo);
				
				synchronizeSameValues(phoneInfo);
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return phoneInfo;
	}
	
	// 强匹配信息，机型对应的CPU、内存大小(也可变，但一般是固定的几个值)、
	public static JSONObject getStaticMatchingInfo(JSONObject phoneInfoTemplate){
		JSONObject result = new JSONObject();
		
		// 如果更改 Build.VERSION.SDK 与 Build.VERSION.RELEASE ，要注意对应
		// 但阳光的是不对应的，只改Build.VERSION.RELEASE， 因为改 Build.VERSION.SDK 会致使某些APP运行时使用高版本API时Crash
		// 但我们的 这里Build的信息，直接用收集回来的信息，拿出一个机型信息后不作改变
		JSONObject buildInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "Build.");
		JSONObject screenInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "Screen.");
		JSONObject cpuInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "/proc/cpuinfo");
		JSONObject memInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "/proc/meminfo");
		JSONObject cmdlineInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "/proc/cmdline");
		JSONObject mountsInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "/proc/mounts");
		JSONObject versionInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "/proc/version");
		
		
		JSONObjectUtil.mergeJSONObject(result, buildInfo);
		JSONObjectUtil.mergeJSONObject(result, screenInfo);
		JSONObjectUtil.mergeJSONObject(result, cpuInfo);
		JSONObjectUtil.mergeJSONObject(result, memInfo);
		JSONObjectUtil.mergeJSONObject(result, cmdlineInfo);
		JSONObjectUtil.mergeJSONObject(result, mountsInfo);
		JSONObjectUtil.mergeJSONObject(result, versionInfo);
		
		return result;
	}
	
	// 仅在新增时要做改变的Hook信息
	public static JSONObject reandomTheStablePhoneInfo(JSONObject phoneInfoTemplate) throws JSONException {
		JSONObject result = new JSONObject();
		
		// 1. Telephony -------------------------------------------------
		// IMEI、IMSI... (Telephony.DeviceId及Telephony.SubscriberId)等
		JSONObject telephonyInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "Telephony.");
		phoneInfoTemplate.remove("Telephony.asBinder");	// redundant
		phoneInfoTemplate.remove("Telephony.InterfaceDescriptor");	// redundant, no need to hook
		telephonyInfo.remove("Telephony.AllCellInfo");
		telephonyInfo.remove("Telephony.CellLocation");
		
		// 2. SystemProperties -------------------------------------------------
		JSONObject systemPropertiesInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "SystemProperties.");
		
		// IMEI & IMSI
		String imei = InfoGenUtil.genImei();
		String imsi = InfoGenUtil.genImsi();
		telephonyInfo.put("Telephony.DeviceId", imei);
		telephonyInfo.put("Telephony.SubscriberId", imsi);
		
		// IMSI 改变后，需要改变一些运营商信息
		String imsiPrefix = imsi.substring(0, 5);
		String imsiOperatorName = "CMCC";
		
		String networkOperator = imsiPrefix;
		String simOperator = imsiPrefix;
		if (imsiPrefix.equals("46000") || imsiPrefix.equals("46002") || imsiPrefix.equals("46004") || imsiPrefix.equals("46007")) {
			
			imsiOperatorName = new Random().nextInt(10) == 0 ? "CMCC" : "中国移动";
			networkOperator = "46000";
			
		} else if (imsiPrefix.equals("46001") || imsiPrefix.equals("46006") || imsiPrefix.equals("46009")) {
			
			imsiOperatorName = new Random().nextInt(10) == 0 ? "CUCC" : "中国联通";
			
		} else if (imsiPrefix.equals("46003") || imsiPrefix.equals("46005") || imsiPrefix.equals("46011 ")) {
			
			imsiOperatorName = new Random().nextInt(10) == 0 ? "CTCC" : "中国电信";
			
		}
		
		telephonyInfo.put("Telephony.NetworkOperator", networkOperator);
		telephonyInfo.put("Telephony.SimOperator", simOperator);
		telephonyInfo.put("Telephony.NetworkOperatorName", imsiOperatorName);
		telephonyInfo.put("Telephony.SimOperatorName", imsiOperatorName);
		
		systemPropertiesInfo.put("SystemProperties.gsm.operator.numeric", imsiPrefix);
		systemPropertiesInfo.put("SystemProperties.gsm.sim.operator.numeric", imsiPrefix);
		systemPropertiesInfo.put("SystemProperties.gsm.apn.sim.operator.numeric", imsiPrefix);

		systemPropertiesInfo.put("SystemProperties.gsm.operator.alpha", imsiOperatorName);
		systemPropertiesInfo.put("SystemProperties.gsm.sim.operator.alpha", imsiOperatorName);
		systemPropertiesInfo.put("SystemProperties.gsm.apn.sim.operator.alpha", imsiOperatorName);
		
		systemPropertiesInfo.put("SystemProperties.gsm.sim.operator.iso-country", "cn");
		
		// 3. Settings -------------------------------------------------
		JSONObject settingsInfo = phoneInfoTemplate.optJSONObject("Settings");
		JSONObject settingsSystemInfo = settingsInfo.optJSONObject("System");
		JSONObject settingsSecureInfo = settingsInfo.optJSONObject("Secure");
//		JSONObject settingsGlobalInfo = settingsInfo.optJSONObject("Global");
				
		// Android id 
		String android_id = InfoGenUtil.genAndroidID();
		settingsSystemInfo.put("android_id", android_id);
		settingsSecureInfo.put("android_id", android_id);
		
		// 一般收集上来的没打开发者模式手机信息都是0的,这里还是设一下让所有手机都是0吧
		settingsSystemInfo.put("adb_enabled", 0);
		settingsSecureInfo.put("adb_enabled", 0);
		
		
		// 4. Bluetooth -------------------------------------------------
		JSONObject bluetoothInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "Bluetooth.");
		bluetoothInfo.put("Bluetooth.Address", InfoGenUtil.genMac());
		bluetoothInfo.put("Bluetooth.Name", InfoGenUtil.genName());
		
		
		// 5. WIFI -------------------------------------------------
		JSONObject wifiInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "Wifi.");
		// MAC (always have)
		String macAddress = InfoGenUtil.genMac();
		String bbsid = InfoGenUtil.genMac();
		if (!wifiInfo.has("Wifi.ConnectionInfo")) {
			wifiInfo.put("Wifi.ConnectionInfo", new JSONObject());
		}
		JSONObject connectionInfo = wifiInfo.optJSONObject("Wifi.ConnectionInfo");
		connectionInfo.put("MacAddress", macAddress);
		connectionInfo.put("BSSID", bbsid);
		
		
		// 6. /sys/class/net/ files -------------------------------------------------
		JSONObject fileContentsInfo  = phoneInfoTemplate.optJSONObject("Files.Contents");
		if (fileContentsInfo == null) {
			fileContentsInfo = new JSONObject();
			phoneInfoTemplate.put("Files.Contents", fileContentsInfo);
		}
		
		String pathWlan0Address = "/sys/class/net/wlan0/address";
		String pathWlan0AddressLink = "/sys/devices/fb000000.qcom,wcnss-wlan/net/wlan0/address";
		fileContentsInfo.put(pathWlan0Address, macAddress);
		fileContentsInfo.put(pathWlan0AddressLink, macAddress);
		
		String pathIFINET6 = "/proc/net/if_inet6";	// /system/xbin/ifconfig -a 命令取这个文件的内容
		String inet6Contents = fileContentsInfo.optString(pathIFINET6);
		
		String rmnet0String = "fe800000000000008225ccd524c1441f 03 40 20 80   rmnet0";
		String dummy0String = "fe800000000000006c9e69fffefdb351 02 40 20 80   dummy0";
		String wlan0String =  "fe80000000000000020af5fffe545f14 38 40 20 80    wlan0";
		String loString = "00000000000000000000000000000001 01 80 10 80       lo";
		
		String rmnet0Address = InfoGenUtil.genMac().replace(":", "");
		String dummy0Address = InfoGenUtil.genMac().replace(":", "");
		String wlan0Address = macAddress.replace(":", "");
		
		int endIndex = rmnet0String.indexOf(" ");
		rmnet0String = rmnet0String.replace(rmnet0String.substring(endIndex - rmnet0Address.length(), endIndex), rmnet0Address);
		
		endIndex = dummy0String.indexOf(" ");
		dummy0String = dummy0String.replace(dummy0String.substring(endIndex - dummy0Address.length(), endIndex), dummy0Address);
		
		endIndex = wlan0String.indexOf(" ");
		wlan0String = wlan0String.replace(wlan0String.substring(endIndex - wlan0Address.length(), endIndex), wlan0Address);
		
		if (inet6Contents.isEmpty()) {
			inet6Contents = rmnet0String + "\r\n" + dummy0String + "\r\n" + wlan0String + "\r\n" + loString;
		}
		if (!inet6Contents.contains("rmnet0")) {
			inet6Contents += "\r\n" + rmnet0String;
		}
		if (!inet6Contents.contains("dummy0")) {
			inet6Contents += "\r\n" + dummy0String;
		}
		if (!inet6Contents.contains("wlan0")) {
			inet6Contents += "\r\n" + wlan0String;
		}
		if (!inet6Contents.contains("lo")) {
			inet6Contents += "\r\n" + loString;
		}
		
		fileContentsInfo.put(pathIFINET6, inet6Contents);
		
		
		// 7. /system/build.prop file -------------------------------------------------
		String buildPropContents = fileContentsInfo.optString("/system/build.prop");
		// static match now, should be synchronize with SystemProperties.xxxx values 
		// but now we didn't change too much.
		
		
		// 8. Package.InstalledPackages -------------------------------------------------
		String fileContents = FileUtil.read(ServerConfig.contextRealPath + "/phone/PreInstalledApps.json");
		JSONObject phonePreInstalledJson = new JSONObject(fileContents);
		
		String brand =  phoneInfoTemplate.optString("Build.BRAND").toUpperCase();
		JSONObject installedPackages = phoneInfoTemplate.optJSONObject("Package.InstalledPackages");
		if(installedPackages == null) {
			installedPackages = new JSONObject();
		}
		
		JSONObject generalAppJson = phonePreInstalledJson.optJSONObject("General_Installed_APPS");
		JSONObjectUtil.mergeJSONObject(installedPackages, generalAppJson);
		
		Iterator<?> keys = phonePreInstalledJson.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (brand.contains(key)) {
				JSONObject brandAppJson = phonePreInstalledJson.optJSONObject(key);
				JSONObjectUtil.mergeJSONObject(installedPackages, brandAppJson);
			}
		}
		
		// Finally, merge all results -------------------------------------------------
		JSONObjectUtil.mergeJSONObject(result, telephonyInfo);
		JSONObjectUtil.mergeJSONObject(result, systemPropertiesInfo);
		JSONObjectUtil.mergeJSONObject(result, settingsInfo);
		JSONObjectUtil.mergeJSONObject(result, bluetoothInfo);
		JSONObjectUtil.mergeJSONObject(result, wifiInfo);
		result.put("Files.Contents", fileContentsInfo);
		result.put("Package.InstalledPackages", installedPackages);
		
		return result;
	}
	
	// 新增与留存时都要做改变的Hook信息
	public static JSONObject reandomTheUnstablePhoneInfo(JSONObject phoneInfoTemplate) throws JSONException {
		JSONObject result = new JSONObject();
		
		// 1. Connectivity -------------------------------------------------
		JSONObject connectivityInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "Connectivity.");
		connectivityInfo.remove("Connectivity.asBinder");	// redundant
		
		// WIFI or 4G
		JSONObject activeNetworkInfo = null;
		if (connectivityInfo.has("Connectivity.ActiveNetworkInfo")) {
			activeNetworkInfo = connectivityInfo.optJSONObject("Connectivity.ActiveNetworkInfo");
		}
		if (activeNetworkInfo == null) {
			activeNetworkInfo = new JSONObject();
		}
		connectivityInfo.put("Connectivity.ActiveNetworkInfo", activeNetworkInfo);
		connectivityInfo.put("Connectivity.ProvisioningOrActiveNetworkInfo", activeNetworkInfo);
		
		// 0:MOBILE; 1:WIFI
		int mNetworkType = InfoGenUtil.genNetworkType();
		boolean isUsingWIFI = mNetworkType == 1;
		boolean isSimCardInserted = true;
		
		activeNetworkInfo.put("mNetworkType", mNetworkType); 
		// [wifiname] or cmnet
		String mExtraInfo = isUsingWIFI ? InfoGenUtil.genName() : "cmnet";
		String mTypeName  = isUsingWIFI ? "WIFI" : "mobile";
		if (isUsingWIFI) {
			activeNetworkInfo.put("mExtraInfo", mExtraInfo); 
			activeNetworkInfo.put("mTypeName", mTypeName);
			activeNetworkInfo.put("mReason", "");
			activeNetworkInfo.put("mSubtypeName", "");
			activeNetworkInfo.put("mSubtype", 0);
		} else {
			activeNetworkInfo.put("mExtraInfo",  mExtraInfo); 
			activeNetworkInfo.put("mTypeName", mTypeName);
			activeNetworkInfo.put("mReason", "2GVoiceCallEnded");
			activeNetworkInfo.put("mSubtypeName", "EDGE");
			activeNetworkInfo.put("mSubtype", 2);
		}
		
		
		// 2. SystemProperties -------------------------------------------------
		JSONObject systemPropertiesInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "SystemProperties.");
		// assume SIM card is inserted, using WIFI but with mobile network enable.
		if (isSimCardInserted) {
			systemPropertiesInfo.put("SystemProperties.gsm.sim.state", "READY");
			systemPropertiesInfo.put("SystemProperties.gsm.network.type", "EDGE");
			
			// operator was set in stable info
			
		} else {
			systemPropertiesInfo.put("SystemProperties.gsm.sim.state", "ABSENT");
			systemPropertiesInfo.put("SystemProperties.gsm.network.type", "Unknown");
			
			// operator
			systemPropertiesInfo.put("SystemProperties.gsm.operator.isroaming", "false");
			systemPropertiesInfo.put("SystemProperties.gsm.sim.operator.iso-country", "");
			systemPropertiesInfo.put("SystemProperties.gsm.apn.sim.operator.numeric", "");
			systemPropertiesInfo.put("SystemProperties.gsm.operator.alpha", "");
			systemPropertiesInfo.put("SystemProperties.gsm.operator.iso-country", "");
			systemPropertiesInfo.put("SystemProperties.gsm.operator.numeric", "");
			systemPropertiesInfo.put("SystemProperties.gsm.sim.operator.alpha", "");
			systemPropertiesInfo.put("SystemProperties.gsm.sim.operator.numeric", "");
		}
		
		
		if (isUsingWIFI) {
			// WIFI Is ["wlan0"], 4g Is []
			connectivityInfo.put("Connectivity.TetherableIfaces", new JSONArray(new String[]{"wlan0"})); 
			
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
		networkInfo.put("mState", 4);	// Connected
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

		// ICCID(Integrate circuit card identity) 集成电路卡识别码(固化在手机SIM卡中) ICCID 为IC 卡的唯一识别号码，共有20 位数字组成
		String simSerialNumber = "898600" + InfoGenUtil.gen2(14);
		telephonyInfo.put("Telephony.SimSerialNumber", simSerialNumber);
		telephonyInfo.put("Telephony.IccSerialNumber", simSerialNumber);
		
		String lineNumber = "150" + InfoGenUtil.gen2(8);
		telephonyInfo.put("Telephony.Line1Number", lineNumber);
		telephonyInfo.put("Telephony.Line1NumberForDisplay", lineNumber);
		telephonyInfo.put("Telephony.Msisdn", lineNumber);

		telephonyInfo.put("Telephony.hasIccCard", true);
		telephonyInfo.put("Telephony.VoiceNetworkType", 16);
		telephonyInfo.put("Telephony.GroupIdLevel1", "ffffffff");
		telephonyInfo.put("Telephony.Line1AlphaTag", "@@@@@@@@@@@@@@");
		telephonyInfo.put("Telephony.isDataConnectivityPossible", true);
		
		
		// 4. WIFI -------------------------------------------------
		JSONObject wifiInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "Wifi.");
		JSONObject connectionInfo = wifiInfo.optJSONObject("Wifi.ConnectionInfo");
		String macAddress = connectionInfo.optString("MacAddress");
		String bssid = connectionInfo.optString("BSSID");
		String wifiName = activeNetworkInfo.optString("mExtraInfo");
		
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
		
		// wifi scan results
		JSONArray wifiScanResults = createWifiScanResults(bssid, wifiName, macAddress);
		wifiInfo.put("Wifi.ScanResults", wifiScanResults);
		
		// 5. Battery -------------------------------------------------
		JSONObject batteryInfo = JSONObjectUtil.optJSONWithKeyPrefix(phoneInfoTemplate, "Battery.");
		// battery info now is handled by client end 
		
		JSONObject batterChargeInfo = batteryInfo.optJSONObject("Battery.ACTION_BATTERY_CHANGED");
		if (batterChargeInfo == null) {
			batterChargeInfo = new JSONObject();
			batterChargeInfo.put("Battery.ACTION_BATTERY_CHANGED", batterChargeInfo);
		}
		
		// 1 未知; 2 正在充; 3 未充电，放电状态; 4 连上充电线了，但不在充电; 5 充满了
	    int status = 3; 
	    
	    // https://stackoverflow.com/a/35083685
		// A. 默认全部不在充电
		batterChargeInfo.put("plugged", 0);
		batterChargeInfo.put("status", status);
		// B. 这表示在用着USB(1:AC;2:USB)充电, 给一定比例吗
//		batterChargeInfo.put("plugged", 2);	// 
//		batterChargeInfo.put("status", 2);
		
		if (status != 5) {
			batterChargeInfo.put("level", new Random().nextInt(80) + 20);
		} else {
			batterChargeInfo.put("level", 100);
		}
		
		JSONObjectUtil.mergeJSONObject(result, connectivityInfo);
		JSONObjectUtil.mergeJSONObject(result, systemPropertiesInfo);
		JSONObjectUtil.mergeJSONObject(result, telephonyInfo);
		JSONObjectUtil.mergeJSONObject(result, wifiInfo);
		JSONObjectUtil.mergeJSONObject(result, batteryInfo);
		
		return result;
	}
	
	// 设置是否使用WIFI
	public static void setPhoneInfoIsUsingWifi(JSONObject phoneInfo, boolean isUsingWIFI) {
		try {

			// 0:MOBILE; 1:WIFI
			JSONObject activeNetworkInfo = phoneInfo.optJSONObject("Connectivity.ActiveNetworkInfo");

			// [wifiname] or cmnet
			int mNetworkType = isUsingWIFI ? 1 : 0;
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
				phoneInfo.put("Connectivity.TetherableIfaces", new JSONArray(new String[] { "wlan0" }));

			} else {
				
				JSONObject activeNetworkQuotaInfo = new JSONObject();
				phoneInfo.put("Connectivity.ActiveNetworkQuotaInfo", activeNetworkQuotaInfo);
				activeNetworkQuotaInfo.put("mSoftLimitBytes", 214748364);
				activeNetworkQuotaInfo.put("mHardLimitBytes", -1);
				activeNetworkQuotaInfo.put("NO_LIMIT", -1);
				activeNetworkQuotaInfo.put("mEstimatedBytes", 386779);

				// WIFI Is ["wlan0"], 4g Is []
				phoneInfo.put("Connectivity.TetherableIfaces", new JSONArray());

			}
			
			// important!!! 客户端多数通过 Connectivity.NetworkInfo 的 mNetworkType 来判断是 WIFI 和 4G
			JSONObject networkInfo = new JSONObject();
			phoneInfo.put("Connectivity.NetworkInfo", networkInfo);
			networkInfo.put("mState", 4);	// Connected
			networkInfo.put("mNetworkType", mNetworkType);
			networkInfo.put("mSubtype", 0);
			networkInfo.put("mSubtypeName", "");
			networkInfo.put("mExtraInfo", mExtraInfo);
			networkInfo.put("mTypeName", mTypeName);
			networkInfo.put("mIsFailover", false);
			networkInfo.put("mIsAvailable", true);

			// 3. Telephony -------------------------------------------------
			phoneInfo.put("Telephony.DataActivity", isUsingWIFI ? 0 : 3);
			phoneInfo.put("Telephony.DataNetworkType", isUsingWIFI ? 0 : 2);
			phoneInfo.put("Telephony.NetworkType", isUsingWIFI ? 0 : 2);

			// 4. WIFI -------------------------------------------------
			JSONObject connectionInfo = phoneInfo.optJSONObject("Wifi.ConnectionInfo");
			String wifiName = activeNetworkInfo.optString("mExtraInfo");

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
				phoneInfo.put("Wifi.DhcpInfo", dhcpInfo);
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
				phoneInfo.put("Wifi.DhcpInfo", dhcpInfo);
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
	
	// 随机生成WIFI扫描列表 
	public static JSONArray createWifiScanResults(String connectedBSSID, String connectedWifiName, String MacAddress) {
		JSONArray wifiScanResults = new JSONArray();
		
		try {
			int count = new Random().nextInt(10) + 8;
			long timestamp = new Random().nextInt(10000) + 50000; 
			for (int i = 0; i < count; i++) {
				JSONObject json = new JSONObject();
				json.put("capabilities", InfoGenUtil.genCapabilities());
				json.put("distance", -1);
				json.put("BSSID", InfoGenUtil.genMac());
				json.put("level", -30 - new Random().nextInt(20));
				json.put("distanceSd", -1);
				json.put("SSID", InfoGenUtil.genName());
				json.put("timestamp", timestamp *  1000);
				json.put("frequency", ((new Random().nextInt(2) == 1) ? 2000 : 5000 ) + new Random().nextInt(1000));
				
				// contain connected info itself
				if (i == 0) {
					json.put("capabilities", InfoGenUtil.genCapabilities());
					json.put("distance", -1);
					json.put("BSSID", connectedBSSID);
					json.put("level", -30 - new Random().nextInt(20));
					json.put("distanceSd", -1);
					json.put("SSID", connectedWifiName);
					json.put("timestamp", timestamp *  1000);
					json.put("frequency", ((new Random().nextInt(2) == 1) ? 2000 : 5000 ) + new Random().nextInt(1000));
					json.put("MacAddress", MacAddress);
				}
				
				wifiScanResults.put(json);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wifiScanResults;
	}

	public static void  synchronizeSameValues(JSONObject phoneInfo) throws JSONException  {
		String model = phoneInfo.optString("Build.MODEL");
		phoneInfo.put("SystemProperties.ro.product.model", model);
	}
}
