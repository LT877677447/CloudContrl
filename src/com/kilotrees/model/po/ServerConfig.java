package com.kilotrees.model.po;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.kilotrees.util.FileUtil;
import com.kilotrees.util.InetAddressUtil;

public class ServerConfig {
	
	public static String contextRealPath = "";
	
	private static JSONObject configJson;
	
	// 短信验证码超时,以秒为单位
	public static int smscode_timeout = 60;

	// 广告预加时间，比如一个广告执行时间为３分钟，但要加上vpn切换，切换imei等机器信息或者下载apk之类
	public static final int adv_extend_time = 10;
	
	// 处理留存缓存表时，默认一次处理数量
	public static final int remain_cache_count = 20;
	
	// 最大的留存日数，最多2个月
	public static final int max_remain_days = 60;
	
	// 成功标记
	public static final int result_success_flag = 0;
	
	// 当活跃设备和需求设备不足时，重新取空闲设备分配的超时时间
	public static final int act_realloc_timeout = 60 * 10;
	
	// 计算设备分配数量时，活跃设备可能还没来得及上来，这里设置一个超时,超过这个时间才计算活跃设备是否足够
	public static final int act_realloc_wait = 60 * 3;
	
	// 广告分配标识，1：分配单一广告;2:分配给广告组；3:分配给单一的留存广告;4:广告和留存同时分到此设备上;5:分配留存组(多个留存同时做)
	// 6:临时分配.
	// 系统最好的方式是各台机器能自由灵活分配，2,4,5最好不用，系统最常用的是1,3,6.其它暂时不用因为6其实已经是实现了广告组功能
	public static final int adv_alloc_type_single = 1;
	
	// 这个功能预留着，我们可以在dispath中通过临时分配把可以同时执行的作务放在一起计算
	public static final int adv_alloc_type_group = 2;
	
	public static final int adv_alloc_type_remain = 3;
	
	// adv_alloc_type_lockremain这个情况只有某些广告象数明，我们无法修改机器设备信息才用到，
	// 之前做新增的广告，以后留存必须在同样的机器上执行，但这个限制非常大，而且分配和释放设备很复杂，尽量不用．
	public static final int adv_alloc_type_lockremain = 4;
	//
	public static final int adv_alloc_type_remain_group = 5;
	
	// 设备空闲或所分配的任务当前小时已经完成时会找其它任务临时做
	public static final int adv_alloc_type_temp = 6;

	// 2018-12-7 在其它目录增加一个文件来记录留存初始化时间,这个可能还是要放在数据表中，太容易出问题了
	private static JSONObject jsonExtent = new JSONObject();

	
	public static JSONObject getConfigJson() {
		return configJson;
	}
	
	/**
	 * 加载JSON
	 */
	public static void refresh() {
		try {
			String string =  FileUtil.readTextFile(ServerConfig.contextRealPath + "ServerConfig.json");
			JSONObject json = new JSONObject(string);

			String STATIC_FILE_IP = json.optString("STATIC_FILE_IP");
			String STATIC_FILE_PORT = json.optString("STATIC_FILE_PORT");

			String FILE_SERVER_IP = json.optString("FILE_SERVER_IP");
			String FILE_SERVER_PORT = json.optString("FILE_SERVER_PORT");

			String YUN_SERVER_IP = json.optString("YUN_SERVER_IP");
			String YUN_SERVER_PORT = json.optString("YUN_SERVER_PORT");

			String default_port = "9090";
			String default_ip = InetAddressUtil.getLANIPAddress();
			default_ip = "192.168.3.116";
			
			// STATIC_FILE
			if (STATIC_FILE_IP.isEmpty()) {
				string = string.replaceAll("\\[STATIC_FILE_IP\\]", default_ip);
				STATIC_FILE_IP = default_ip;
			} else {
				string = string.replaceAll("\\[STATIC_FILE_IP\\]", STATIC_FILE_IP);
			}
			if (STATIC_FILE_PORT.isEmpty()) {
				string = string.replaceAll("\\[STATIC_FILE_PORT\\]", default_port);
				STATIC_FILE_PORT = default_port;
			} else {
				string = string.replaceAll("\\[STATIC_FILE_PORT\\]", STATIC_FILE_PORT);
			}

			// FILE_SERVER
			if (FILE_SERVER_IP.isEmpty()) {
				string = string.replaceAll("\\[FILE_SERVER_IP\\]", default_ip);
				FILE_SERVER_IP = default_ip;
			} else {
				string = string.replaceAll("\\[FILE_SERVER_IP\\]", FILE_SERVER_IP);
			}
			if (FILE_SERVER_PORT.isEmpty()) {
				string = string.replaceAll("\\[FILE_SERVER_PORT\\]", default_port);
				FILE_SERVER_PORT = default_port;
			} else {
				string = string.replaceAll("\\[FILE_SERVER_PORT\\]", FILE_SERVER_PORT);
			}

			// YUN_SERVER
			if (YUN_SERVER_IP.isEmpty()) {
				string = string.replaceAll("\\[YUN_SERVER_IP\\]", default_ip);
				YUN_SERVER_IP = default_ip;
			} else {
				string = string.replaceAll("\\[YUN_SERVER_IP\\]", YUN_SERVER_IP);
			}
			if (YUN_SERVER_PORT.isEmpty()) {
				string = string.replaceAll("\\[YUN_SERVER_PORT\\]", default_port);
				YUN_SERVER_PORT = default_port;
			} else {
				string = string.replaceAll("\\[YUN_SERVER_PORT\\]", YUN_SERVER_PORT);
			}

			JSONObject config = new JSONObject(string);
			config.put("STATIC_FILE_IP", STATIC_FILE_IP);
			config.put("STATIC_FILE_PORT", STATIC_FILE_PORT);

			config.put("FILE_SERVER_IP", FILE_SERVER_IP);
			config.put("FILE_SERVER_PORT", FILE_SERVER_PORT);

			config.put("YUN_SERVER_IP", YUN_SERVER_IP);
			config.put("YUN_SERVER_PORT", YUN_SERVER_PORT);
			configJson = config;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	// 2018-12-9 增加留存表多次执行时的时间间隔，现在是半小时，感觉有点长，最好(10分钟）由这里动态配置，
	public static int getRemainReopenTimeout() {
		return configJson.optInt("remain_reopen_timeout", 600);
	}

	public static String getApkfileurl() {
		return configJson.optString("apkfileurl", "");
	}

	public static String getScriptfileurl() {
		return configJson.optString("scriptfileurl");
	}

	public static int getRefreshstep() {
		int ret = configJson.optInt("refreshstep", 10);
		if (ret < 10) {
			ret = 10;
		}
		return ret;
	}
	
	public static int getLoginDaemonInterval() {
		return configJson.optInt("loginDaemonInterval", 30);
	}

	public static int getRemainjobprocwork() {
		return configJson.optInt("remainjobprocwork", 0);
	}

	public static Date getRemainjobdoday() {
		// 因为每次刷新都把工程的文件刷新到部署目录下，造成每次都重新执行留存初始化，把这个配置放到其它目录中
		String contents = FileUtil.read("D:/WebServer/config/yunserv.json");
		try {
			jsonExtent = new JSONObject(contents);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String sDate = jsonExtent.optString("remainjobdoday", "2018-01-01 01:01:01");
		SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return fm.parse(sDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 把留存初始化时间写到本地
	 */
	public static void setRemainjobdoday(Date remainjobdoday) throws JSONException {
		SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sDate = fm.format(remainjobdoday);
		jsonExtent.put("remainjobdoday", sDate);
		
		FileUtil.write("D:/WebServer/config/yunserv.json", jsonExtent.toString());
	}

	public static String getZipDownloadURL() {
		return configJson.optString("zip_download_url");
	}

	public static String getZipUploadURL() {
		return configJson.optString("zip_upload_url");
	}

	public static String getRooturl() {
		return configJson.optString("rooturl");
	}

	public static String getFileServerBaseURL() {
		return "http://" + configJson.optString("FILE_SERVER_IP") + ":" + configJson.optString("FILE_SERVER_PORT") + "/";
	}
	
	public static String getFileServerServlet(String servletName) {
		return getFileServerBaseURL() + "/yunfilesctrl/" + servletName;
	}

	/**
	 * D:\WebServer
	 */
	public static String getStorageBaseURL() {
		String base = "http://" + configJson.optString("STATIC_FILE_IP") + ":" + configJson.optString("STATIC_FILE_PORT") + "/";
		return base + "/storage/";
	}

	public static String getStoragePrivateBaseURL() {
		String base = "http://" + configJson.optString("STATIC_FILE_IP") + ":" + configJson.optString("STATIC_FILE_PORT") + "/";
		return base + "/storage_private/";
	}

	public JSONObject getLogConfig() {
		return configJson.optJSONObject("log_config");
	}
	
}
