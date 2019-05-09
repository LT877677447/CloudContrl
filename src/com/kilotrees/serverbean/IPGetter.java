package com.kilotrees.serverbean;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.log.LogFile;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.services.advnewtask_service;
import com.kilotrees.services.main_service;
import com.kilotrees.util.httplinks_um;
import com.kilotrees.util.myHttp;

/**
 * 从ip代理商获取ip
 * 
 * @author Administrator
 *
 */
public class IPGetter {
	protected static Logger log = Logger.getLogger(IPGetter.class);
	private static IPGetter inst;
	private JSONObject jsonConfig;
	ArrayList<_ProxyIPURL> ipurlList = new ArrayList<_ProxyIPURL>();
	ArrayList<advReUsed> advuseList = new ArrayList<advReUsed>();
	public static HashMap<Integer, ArrayList<String>> ipList = null;// new
	// HashMap<Integer,
	// ArrayList<String>>();
	static HashMap<Integer, ArrayList<String>> ipListBackup = new HashMap<Integer, ArrayList<String>>();
	public static boolean togetips = true;
	static Object GETIP_LOCKED = new Object();
	// 缓存ip数
	int max_ipcache_cont = 50;
	int cur_ipcache_cout = 0;
	int lastJsonVersion = 0;

	static boolean btest = false;

	private IPGetter() {
		init();
	}

	public static IPGetter getInstance() {
		synchronized (IPGetter.class) {
			if (inst == null)
				inst = new IPGetter();
		}
		return inst;
	}
	
	/**
	 * 这个刷新放在最后
	 */
	public static void service_refresh() {
		//
		if (!main_service.getInstance().isSystem_ready()) {
			httplinks_um.init();
			IPGetter.getInstance();
		} else {
			IPGetter.getInstance().refresh();
		}

	}

	void createConfigJson() {
		try {
			String fileName = ServerConfig.contextRealPath + "files/extjson/ipgetter.json";
			File file = new File(fileName);
			if (file.exists() == false)
				return;
			FileInputStream fins = new FileInputStream(file);
			byte[] buf = new byte[(int) file.length()];
			fins.read(buf);
			fins.close();
			String s = new String(buf, "utf-8");
			jsonConfig = new JSONObject(s);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void init() {
		refresh();
		if(advuseList.size() > 0){
			for (_ProxyIPURL p : ipurlList) {
				getIpsThread t = new getIpsThread(p);
				t.start();
			}
		}
		else
		{
			log.error("advuseList size = 0,IPGetter没有关联的advid");
		}
		if (btest) {
			// int advid;
			for (advReUsed e : advuseList){
				new fetchip_test(e.adv_id).start();
				//只启动一个线程
				break;
			}
		}
	}

	public void refresh() {
		createConfigJson();
		int newVersion = jsonConfig.optInt("version", 1);
		if (newVersion == lastJsonVersion)
			return;
		lastJsonVersion = newVersion;
		try {
			max_ipcache_cont = jsonConfig.optInt("max_ipcache_cont", 50);
			JSONArray jsoArray;
			ArrayList<_ProxyIPURL> ipurlListTemp = new ArrayList<_ProxyIPURL>();
			ArrayList<advReUsed> advuseListTemp = new ArrayList<advReUsed>();
			/**
			 * 使用此服务的advid
			 */

			jsoArray = jsonConfig.optJSONArray("advids");
			for (int i = 0; i < jsoArray.length(); i++) {
				JSONObject jso = jsoArray.getJSONObject(i);
				advReUsed ar = new advReUsed();

				ar.adv_id = jso.getInt("adv_id");
				ar.reuse = jso.getInt("reuse");
				ar.proxyid = jso.optString("proxyid", "");
				if (btest == false) {
					if (advnewtask_service.getInstance().getAdvTaskRunTimeInfo(ar.adv_id) == null)
						continue;
					if (advnewtask_service.getInstance().getAdvTaskRunTimeInfo(ar.adv_id).isOffline())
						continue;
				}
				advuseListTemp.add(ar);
			}
			jsoArray = jsonConfig.optJSONArray("proxyipurls");
			for (int i = 0; i < jsoArray.length(); i++) {
				JSONObject jso = jsoArray.getJSONObject(i);
				if (jso.optBoolean("stop"))
					continue;
				_ProxyIPURL p = new _ProxyIPURL();
				p.id = jso.getString("id");
				p.url = jso.getString("url");
				p.count = jso.optInt("count");

				ipurlListTemp.add(p);
			}
			synchronized (advuseList) {
				advuseList = advuseListTemp;
			}
			
			if (ipurlList.size() == 0) {
				ipurlList = ipurlListTemp;
			} else {
				// for(_ProxyIPURL p : ipurlList)
				// 看哪些是已经停止的代理
				for (Iterator<_ProxyIPURL> it = ipurlList.iterator(); it.hasNext();) {
					_ProxyIPURL p = it.next();
					boolean bfound = false;
					for (_ProxyIPURL pnew : ipurlListTemp) {
						if (p.id.equals(pnew.id)) {
							bfound = true;
							break;
						}
					}
					if (bfound == false) {
						p.stop = true;
						it.remove();
					}
				}
				//新增的代理，一般新增代理，增加解释返回内容函数，要改代码，所以要重启服务器，这里不作处理
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public String getIpsRandom(int advid) {
		String s = "";
		if (ipList == null) {
			logMyFile("" + advid + "getIpsRandom ipList is null");
			return "";
		}
		int size;
		synchronized (GETIP_LOCKED) {
			ArrayList<String> list = ipList.get(advid);
			if (list == null) {
				logMyFile("" + advid + "getIpsRandom list null");
				return s;
			}
			size = list.size();
			if (size > 0) {
				// java.util.Random rand = new java.util.Random();
				int r = 0;// rand.nextInt(size);
				s = list.get(r);
				list.remove(r);
			}
			size = list.size();
		}
		if (size == 0) {
			exchangeIpList();
		}
		return s;
	}

	static void logMyFile(String log) {
		String logfile = ServerConfig.contextRealPath + "files/log/ipgetter.log";
		LogFile.writeLogFile(logfile, log + "\r\n");
	}

	void updateIps(String proxyid, ArrayList<String> newList) {

		if (newList.size() == 0) {
			return;
		}
		// boolean test = true;
		if (btest) {
			String log = proxyid + "生成ip:\r\n";
			for (String s : newList)
				log += s + "\r\n";
			logMyFile(log);

		}
		ArrayList<advReUsed> advuseListTemp = new ArrayList<advReUsed>();
		synchronized (advuseList) {
			advuseListTemp = (ArrayList<advReUsed>) advuseList.clone();
		}
		synchronized (GETIP_LOCKED) {
			cur_ipcache_cout += newList.size();

			for (advReUsed e : advuseListTemp) {
				ArrayList<String> list = (ArrayList<String>) newList.clone();
				if (e.reuse > 0) {
					// 增加相对应重复率的条目
					int c = newList.size() * e.reuse / 100;
					for (int i = 0; i < c; i++) {
						// 如果重复大于100,就会越界
						int k = i % newList.size();
						String s = newList.get(k);
						list.add(s);
					}
				}
				// 如果某个广告指定代理商，则看是不是在这个指定范围内，否则不处理
				if (e.proxyid.length() > 0 && e.proxyid.indexOf(proxyid) == -1)
					continue;
				if (ipListBackup.get(e.adv_id) == null)
					ipListBackup.put(e.adv_id, list);
				else
					ipListBackup.get(e.adv_id).addAll(list);
			}

			if (ipList == null && cur_ipcache_cout >= max_ipcache_cont)//
			{
				// 第一次时,先取2次的数据到ipList和ipListBackup,之前没有考虑多线程，每取一次就停止，现在
				//增加cur_ipcache_cout,max_ipcache_cont对应多线程
				logMyFile("第一次取满数据，先把数据放到ipList，cur_ipcache_cout=" + cur_ipcache_cout);
				ipList =  new HashMap<Integer, ArrayList<String>>();
				ipList.putAll(ipListBackup);
				ipListBackup = new HashMap<Integer, ArrayList<String>>();
				cur_ipcache_cout = 0;
			} else {
				if (cur_ipcache_cout >= max_ipcache_cont)
					togetips = false;
			}
		}
	}

	void exchangeIpList() {
		logMyFile("###exchangeIpList cur_ipcache_cout=" + cur_ipcache_cout);
//		synchronized (ipList) {
//			//ipList = ipListBackup;
//		}
		if(ipListBackup.size() == 0)
		{
			logMyFile("###exchangeIpList ipListBackup size = 0");
			return;
		}
		synchronized (GETIP_LOCKED) {
			ipList.clear();
			ipList.putAll(ipListBackup);
			ipListBackup = new HashMap<Integer, ArrayList<String>>();
			togetips = true;
			cur_ipcache_cout = 0;
			GETIP_LOCKED.notifyAll();
		}
	}

	boolean parse(String id, String s, ArrayList<String> list) throws JSONException {

		if (id.equals("niumowang") == false) {
			//json格式
			return parseLine(s, list);

		}
		// 牛魔王代理
		int pos = s.indexOf("{");
		int pos2 = s.lastIndexOf('}');
		s = s.substring(pos, pos2 + 1);
		JSONObject jso = new JSONObject(s);
		boolean success = jso.optBoolean("success");
		if (success) {
			JSONArray jsoArray = jso.getJSONArray("data");
			for (int i = 0; i < jsoArray.length(); i++) {
				JSONObject jsItem = jsoArray.getJSONObject(i);
				s = jsItem.optString("ip");
				s += ":";
				s += jsItem.optString("port");
				list.add(s);
			}
			return true;
		}
		return false;
	}

	boolean parseLine(String s, ArrayList<String> list) {
		String[] ips;
		boolean ret = false;
		if (s.indexOf("\r\n") >= 0)
			ips = s.split("\r\n");
		else
			ips = s.split("\n");
		for (String ip : ips) {
			ip = ip.trim();
			if (ip.length() > 10 && ip.indexOf('.') > 0 && ip.indexOf(':') > 0) {
				list.add(ip);
				ret = true;
			}
		}
		return ret;
	}

	// 哪些广告使用
	class advReUsed {
		int adv_id;
		// 重复率
		int reuse;
		// 使用的代理，有时要指定某个广告使用哪个代理商的ip,如果多个，用分号分开
		String proxyid = "";
	}

	class _ProxyIPURL {
		// 代理商id
		String id;
		// 获取ip的url
		String url;
		// 一次获取个数，一般放在url中，其实没有多大意义
		int count;
		boolean stop = false;
	}

	class getIpsThread extends Thread {
		Logger log = Logger.getLogger(getIpsThread.class);
		// String id;
		// String proxyip_url;
		_ProxyIPURL proxyInfo;

		getIpsThread(_ProxyIPURL p) {
			proxyInfo = p;
			logMyFile("getIpsThread id:" + proxyInfo.id + ";url=" + proxyInfo.url);
		}

		/**
		 * 这里按各种平台和参数配置解释出ip内容,最好用json格式
		 * 
		 * @param s
		 * @param list
		 * @throws JSONException
		 */
		int test = 0;

		public void run() {
			main_service.getInstance().addNewThread(this);
			int err_count = 0;
			while (main_service.getInstance().isThreadWork()) {
				try {
					String url = proxyInfo.url;
					if (togetips == false) {
						synchronized (GETIP_LOCKED) {
							GETIP_LOCKED.wait();
						}
					}
					if (proxyInfo.stop) {
						log.warn("proxyInfo stop true");
						logMyFile("getIpsThread=" + proxyInfo.id + " stop");
						return;
					}

					myHttp dt = new myHttp();
					if (dt.getUrlSynTry(url, 3)) {
						byte[] buf = dt.getContent();
						String s = new String(buf, "utf-8");
						ArrayList<String> list = new ArrayList<String>();
						if (parse(proxyInfo.id, s, list)) {
							updateIps(proxyInfo.id, list);
							err_count = 0;
						} else {
							log.error("getIpsThread getip empty");
							err_count++;
							sleep(3000 * err_count);
							continue;
						}
					} else {
						log.error("getIpsThread  getUrlSynTry error:" + dt.getResponCode());
						sleep(3000);
						continue;
					}
					sleep(10);
				} catch (Exception e) {
					if(main_service.getInstance().isThreadWork() == false)
						break;
					log.error(e.getMessage(), e);
					try {
						sleep(3000);
					} catch (Exception e1) {

					}
				}
			}
		}
	}

	class fetchip_test extends Thread {
		int adv_id;

		fetchip_test(int advid) {
			this.adv_id = advid;
		}

		public void run() {
			int count = 0;
			while (true) {
				try {
					sleep(1000);
					count++;
					String ipport = IPGetter.getInstance().getIpsRandom(adv_id);
					logMyFile("fetchip_test[" + adv_id + "] getipport:" + count);
				} catch (Exception e) {
					logMyFile("exception=" + e.getMessage());
				}
			}
		}
	}
}
