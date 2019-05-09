package com.kilotrees.serverbean;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.dao.sourceurlsdao;
import com.kilotrees.dao.useragentdao;
import com.kilotrees.log.LogFile;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.services.advnewtask_service;
import com.kilotrees.util.StringUtil;
import com.kilotrees.util.httplinks_um;

/**
 * http大点击的serverbean
 * 
 * @author Administrator
 *
 */
public class httplinks_1 extends ServerBeanBase {
	protected static Logger log = Logger.getLogger(httplinks_1.class);
	// public static String proxyip_url = "";
	// public static HashMap<Integer, ArrayList<String>> ipList = null;// new
	// HashMap<Integer,
	// ArrayList<String>>();
	static HashMap<Integer, httplinktree> httptreeList = new HashMap<Integer, httplinktree>();
	// public static String vendor_id = "";
	// public static String vendor_title = "";
	public static HashMap<Integer, ArrayList<String>> soureurlMap = new HashMap<Integer, ArrayList<String>>();
	public static HashMap<String, ArrayList<String>> uaList = new HashMap<String, ArrayList<String>>();
	public static HashMap<String, ArrayList<String>> pixelList = new HashMap<String, ArrayList<String>>();
	public static ArrayList<String> devTypeList = new ArrayList<String>();
	// public static ArrayList<advReUsed> advuseList = new
	// ArrayList<advReUsed>();
	public static boolean inited;
	// public static boolean togetips = true;
	public static int jsoExt_version = 0;
	// public static getIpsThread ipThread;
	private static httplinks_1 inst;
	static HashMap<Integer, uminfo> uminfoList = new HashMap<Integer, uminfo>();
	// 旧用户占百分比，友盟是15分钟之后的用户算新用户
	// static int olduserPercent = 10;
	// pv和uv比例，在1分钟内，一个用户执行重复的访问，这里表示有20%的用户，在一分钟内重复一次
	// static int pvuvPercent = 120;

	// static Object GETIP_LOCKED = new Object();

	private httplinks_1() {

		// TODO Auto-generated constructor stub
	}

	public static httplinks_1 getInstance() {
		synchronized (httplinks_1.class) {
			if (inst == null) {
				inst = new httplinks_1();
			}
		}
		return inst;
	}

	static ArrayList<String> readFile(String fileName) throws IOException {
		ArrayList<String> list = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		while (true) {
			String s = br.readLine();
			if (s == null)
				break;
			s = s.trim();
			if (s.length() > 0)
				list.add(s);
		}
		br.close();
		return list;
	}

	@Override
	public void init(int adv_id) {
		// TODO Auto-generated method stub
		if (inited)
			return;
		try {
			log.info("init");
			// advtaskinfo task =
			// advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo();
			// String fileName = serverconfig.contextRealPath +
			// "files/httplink_1.json";
			// File file = new File(fileName);
			// FileInputStream fins = new FileInputStream(file);
			// byte[] buf = new byte[(int) file.length()];
			// fins.read(buf);
			// fins.close();
			// String s = new String(buf, "utf-8");
			// JSONObject jsoExt = new JSONObject(s);
			// olduserPercent = jsoExt.optInt("olduserPercent", 10);
			// pvuvPercent = jsoExt.optInt("pvuvPercent", 120);

			// 用临时列表变量,可以减少同步消耗时长.
			// ArrayList<String> devTypeListTemp;
			// ArrayList<String> soureListTemp = new ArrayList<String>();
			// HashMap<String, ArrayList<String>> pixelListTemp = new
			// HashMap<String, ArrayList<String>>();
			// HashMap<String, ArrayList<String>> uaListTemp = new
			// HashMap<String, ArrayList<String>>();
			// ArrayList<advReUsed> advuseListTemp = new ArrayList<advReUsed>();

			// jsoExt_version = jsoExt.optInt("version", 0);
			// proxyip_url = jsoExt.optString("proxyip_url");
			// vendor_id = jsoExt.optString("vendor_id");
			// vendor_title = jsoExt.optString("vendor_title");

			// JSONArray jsoArray;

			// JSONArray jsoArray = jsoExt.optJSONArray("source_urls");
			// for (int i = 0; i < jsoArray.length(); i++) {
			// JSONObject jso = jsoArray.getJSONObject(i);
			// String value = jso.optString("value");
			// soureListTemp.add(value);
			// }

			// jsoArray = jsoExt.optJSONArray("advuses");
			// for (int i = 0; i < jsoArray.length(); i++) {
			// JSONObject jso = jsoArray.getJSONObject(i);
			// advReUsed ar = new advReUsed();
			// ar.adv_id = jso.getInt("adv_id");
			// ar.reuse = jso.getInt("reuse");
			// advuseListTemp.add(ar);
			// }

			// jsoArray = jsoExt.optJSONArray("devtypes");
			// for (int i = 0; i < jsoArray.length(); i++) {
			// JSONObject jso = jsoArray.getJSONObject(i);
			// String value = jso.optString("value");
			// devTypeList.add(value);
			// }
			// String fileDir = jsoExt.optString("filesdir");
			// if (fileDir.endsWith("/") == false)
			// fileDir += "/";
			// if (fileDir.startsWith("/"))
			// fileDir = fileDir.substring(1);

			// String devtypeFile = fileDir + "devtypes.list";
			// //devTypeListTemp = readFile(devtypeFile);
			//
			// for (String devtype : devTypeListTemp) {
			// String pixFile = fileDir + devtype + "_device.list";
			// String uaFile = fileDir + devtype + "_user_agent.list";
			// ArrayList<String> list1 = readFile(pixFile);
			// ArrayList<String> list2 = readFile(uaFile);
			// if (list1.size() > 0 && list2.size() > 0) {
			// pixelListTemp.put(devtype, list1);
			// uaListTemp.put(devtype, list2);
			// }
			// }
			// jsoArray = jsoExt.optJSONArray("showpixels");
			// for (int i = 0; i < jsoArray.length(); i++) {
			// JSONObject jso = jsoArray.getJSONObject(i);
			// String value = jso.optString("value");
			// pixelList.add(value);
			// }

			synchronized (devTypeList) {
				devTypeList = useragentdao.getDevTypes();
			}

			synchronized (pixelList) {
				pixelList = useragentdao.getDevPiex();
			}

			synchronized (uaList) {
				uaList = useragentdao.getAllUa();
			}

			// synchronized (advuseList) {
			// advuseList = advuseListTemp;
			// }

			// synchronized (soureList) {
			// soureList = soureListTemp;
			// }

			inited = true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void refresh(int adv_id) {
		// try {
		// String fileName = serverconfig.contextRealPath +
		// "files/httplink_1.json";
		// File file = new File(fileName);
		// FileInputStream fins = new FileInputStream(file);
		// byte[] buf = new byte[(int) file.length()];
		// fins.read(buf);
		// fins.close();
		// String s = new String(buf, "utf-8");
		// JSONObject jsoExt = new JSONObject(s);
		//
		// int newVersion = jsoExt.optInt("version", 0);
		// if (newVersion == jsoExt_version)
		// return;
		// } catch (Exception e) {
		// log.error(e.getMessage(), e);
		// }
		synchronized (httplinks_1.class) {
			inited = false;
			init(adv_id);
		}
	}

	static String getDevTypeRandom(int adv_id) {
		// 这里要看有没有机型限制
		String sAlloced = "";
		advtaskinfo task = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo();
		//
		JSONObject jsoExt = task.getExtJso();
		// devtypes,指定机型，细分到牌子，比如Xiaomi
		sAlloced = jsoExt.optString("devtypes", "");
		ArrayList<String> typesls = new ArrayList<String>();
		synchronized (devTypeList) {
			typesls.addAll(devTypeList);
		}
		if (sAlloced.length() > 0) {
			String[] ss = sAlloced.split(";");
			for (int i = 0; i < typesls.size(); i++) {
				String t = typesls.get(i);
				for (String t1 : ss) {
					if (t1.equalsIgnoreCase(t)) {
						typesls.remove(i);
						i--;
						break;
					}
				}
			}
		}
		String s;
		int size = typesls.size();
		java.util.Random rand = new java.util.Random();
		int r = rand.nextInt(size);
		s = typesls.get(r);
		return s;
	}

	static String getShowPixelRandom(String devtype) {
		String s = "";
		synchronized (pixelList) {
			ArrayList<String> list = pixelList.get(devtype);
			int size = list.size();
			java.util.Random rand = new java.util.Random();
			int r = rand.nextInt(size);
			s = list.get(r);
		}
		return s;
	}

	static String getUserAgentRandom(String devtype) {
		String s = "";
		synchronized (uaList) {
			ArrayList<String> list = uaList.get(devtype);
			int size = list.size();
			java.util.Random rand = new java.util.Random();
			int r = rand.nextInt(size);
			s = list.get(r);
		}
		return s;
	}

	static String getSourceUrlRandom(int adv_id) {
		String s = "";
		synchronized (soureurlMap) {
			ArrayList<String> ls = soureurlMap.get(adv_id);
			if (ls == null || ls.size() == 0) {
				ls = new ArrayList<String>();
				advtaskinfo task = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo();
				JSONObject jsoExt = task.getExtJso();
				int type = -1;
				// 按比重找出100条左右的url
				String sourceurl_types = jsoExt.optString("sourceurl_types", "");
				if (sourceurl_types.length() > 0) {
					// 格式：1:80表示类别为1(比如游戏）占比80%，其余占20，暂不支持多个类别
					String[] sr = sourceurl_types.split(":");
					type = Integer.parseInt(sr[0]);
					int c = Integer.parseInt(sr[1]);
					ArrayList<String> l1 = sourceurlsdao.getUrlsRandom(type, c);
					ArrayList<String> l2 = sourceurlsdao.getUrlsOtherType(type, 100 - c);
					ls.addAll(l1);
					ls.addAll(l2);
				} else
					ls = sourceurlsdao.getUrlsRandom(-1, 100);
				soureurlMap.put(adv_id, ls);
				// ls = soureurlMap.get(adv_id);
			}
			s = ls.remove(0);
		}
		return s;
	}

	// 友盟id,默认取10%的老用户,15分钟前的是老用户，还有的uv和pv比例
	static boolean checkIfNewUmuu(int adv_id, int type) {
		boolean usenew = true;
		uminfo uf = uminfoList.get(adv_id);
		if (uf == null) {
			uf = new uminfo();
			// uf.newid_count = 1;
			// 防止整除时为0
			// uf.oldid_count = 1;
			uminfoList.put(adv_id, uf);
		} else {
			advtaskinfo task = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo();
			//
			JSONObject jsoExt = task.getExtJso();
			if (type == 0) {
				int olduserPercent = jsoExt.optInt("umolduserPercent", 10);
				if (olduserPercent > 0) {
					// pvuvPercent = jsoExt.optInt("pvuvPercent", 120);
					if (olduserPercent >= 100)
						usenew = false;
					if (uf.um_oldid_count > 0
							&& ((uf.um_newid_count + uf.um_oldid_count) / uf.um_oldid_count) >= (100 / olduserPercent))
						usenew = false;
					else if (uf.um_oldid_count == 0 && uf.um_newid_count >= 100 / olduserPercent)
						usenew = false;
				}
			} else {
				int olduserPercent = jsoExt.optInt("baiduolduserPercent", 10);
				if (olduserPercent > 0) {
					// pvuvPercent = jsoExt.optInt("pvuvPercent", 120);
					if (olduserPercent >= 100)
						usenew = false;
					if (uf.baidu_oldid_count > 0 && ((uf.baidu_newid_count + uf.baidu_oldid_count)
							/ uf.baidu_oldid_count) >= (100 / olduserPercent))
						usenew = false;
					else if (uf.baidu_oldid_count == 0 && uf.baidu_newid_count >= 100 / olduserPercent)
						usenew = false;
				}
			}
		}
		return usenew;
	}

	static String[] getOldUmuuid(int adv_id) {
		String[] uids = new String[5];
		if (checkIfNewUmuu(adv_id, 0))
			return uids;
		uids = useragentdao.getOldUmuuid(adv_id);
		if (uids[0] != null) {
			uminfo uf = uminfoList.get(adv_id);
			uf.um_oldid_count += 1;
		}
		return uids;
	}

	static String[] getOldBaiduid(int adv_id) {
		String[] uids = new String[5];
		if (checkIfNewUmuu(adv_id, 1)) {
			uminfo uf = uminfoList.get(adv_id);
			uf.baidu_newid_count += 1;
			return uids;
		}
		uids = useragentdao.getOldBaiduid(adv_id);
		if (uids[0] != null) {
			uminfo uf = uminfoList.get(adv_id);
			uf.baidu_oldid_count += 1;
		}
		else
		{
			uminfo uf = uminfoList.get(adv_id);
			uf.baidu_newid_count += 1;
		}
		return uids;
	}

	static String[] createUmuuid(int adv_id, String useragent, String showpixel, String comname) {
		String[] uids = new String[5];
		uminfo uf = uminfoList.get(adv_id);

		// if(!usenew)
		// {
		// uids = useragentdao.getOldUmuuid(adv_id);
		// if(uids[0] == null)
		// usenew = true;
		// else
		// uf.oldid_count = uf.oldid_count + 1;
		// }
		// if(usenew)
		{
			uids[0] = httplinks_um.create_cnzz_eid();
			int w, h;
			showpixel = showpixel.toLowerCase();
			String[] s2 = showpixel.split("x");
			w = Integer.parseInt(s2[0]);
			h = Integer.parseInt(s2[1]);
			uids[1] = httplinks_um.create_umuuid(useragent, w, h);
			uids[2] = useragent;
			uids[3] = showpixel;
			uids[4] = "";
			// 这里是否只保存10%左右的数据？不然表太大？
			java.util.Random rand = new java.util.Random();
			int r = rand.nextInt(100);
			advtaskinfo task = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo();
			//
			JSONObject jsoExt = task.getExtJso();
			int olduserPercent = jsoExt.optInt("olduserPercent", 10);
			if (r < olduserPercent)
				useragentdao.addUmuuid(adv_id, comname, uids);
			uf.um_newid_count += 1;
		}
		// uf.print();
		return uids;
	}

	static void logMyFile(String log) {
		String logfile = ServerConfig.contextRealPath + "files/log/httplink.log";
		LogFile.writeLogFile(logfile, log + "\r\n");
	}

	@Override
	public void handleTaskParasm(JSONObject jsoTask) throws JSONException {
		// TODO Auto-generated method stub
		int adv_id = jsoTask.optInt("adv_id");

		synchronized (httplinks_1.class) {
			if (inited == false) {
				log.error("handleTaskParasm not inited");
				// init();
				return;
			}
		}
		httplinktree tree = httptreeList.get(adv_id);
		if (tree == null) {
			tree = new httplinktree(adv_id);
			httptreeList.put(adv_id, tree);
		}
		advtaskinfo task = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo();
		JSONObject jsoExt = task.getExtJso();
		// 类别，暂时是友盟和百度，如果2者都要，用分号分开
		JSONObject jsoumlink = jsoExt.optJSONObject("umlink");
		JSONObject jsobaidulink = jsoExt.optJSONObject("baidulink");

		JSONObject jsoUrls = tree.getLinksRandom();
		jsoTask.put("vendor_url", jsoUrls /* jsoExt.optString("vendor_url") */);
		// jsoTask.put("vendor_id", jsoExt.optString("vendor_id"));
		jsoTask.put("vendor_title", jsoExt.optString("vendor_title"));
		// jsoTask.put("linktype", linktype);

		String value = getSourceUrlRandom(adv_id);
		jsoTask.put("source_url", value);
		int visit_sourceurl_percent = jsoExt.optInt("visit_sourceurl_percent",0);
		java.util.Random rand =  new java.util.Random();
		if(visit_sourceurl_percent > 0)
		{
			//访问原始url
			int r1 = rand.nextInt(100);
			if(r1 < visit_sourceurl_percent)
				jsoTask.put("visit_linksurl", true);
			else
				jsoTask.put("visit_linksurl", false);
		}
		if (jsoumlink != null) {
			String[] uids = getOldUmuuid(adv_id);
			boolean addpv = false;
			if (uids[0] == null) {
				logMyFile("create new ummid");
				String dev_type = getDevTypeRandom(adv_id);
				String user_agent = getUserAgentRandom(dev_type);
				String pixel = getShowPixelRandom(dev_type);
				uids = createUmuuid(adv_id, user_agent, pixel, tree.com);
				rand = new java.util.Random();
				int r = rand.nextInt(100);
				int pvuvPercent = jsoExt.optInt("pvuvPercent", 120);
				if (r < pvuvPercent - 100)
					addpv = true;
			} else {
				logMyFile("get old ummid=" + uids[1]);
			}
			JSONObject jsoLink = new JSONObject();
			jsoLink.put("cnzz_eid", uids[0]);
			jsoLink.put("umuuid", uids[1]);

			jsoLink.put("user_agent", uids[2]);
			jsoLink.put("showpixel", uids[3]);
			jsoLink.put("addpv", addpv);
			jsoLink.put("vendor_id", jsoumlink.optString("vendor_id"));
			// jsoLink.put("vendor_title", jsoumlink.optString("vendor_title"));
			jsoTask.put("umlink", jsoLink);
		}
		if (jsobaidulink != null) {
			String[] uids = getOldBaiduid(adv_id);
			uids[3] = "24-bit;zh-cn;0;1;1.2.33";
			boolean addpv = false;
			boolean newcookie = true;
			if (uids[0] == null) {
				logMyFile("create new baiduid");
				String dev_type = getDevTypeRandom(adv_id);
				String user_agent = getUserAgentRandom(dev_type);
				String pixel = getShowPixelRandom(dev_type);
				uids[0] = user_agent;
				uids[1] = pixel;
				uids[2] = "";
				// uids[3] = "24-bit;zh-cn;0;1;1.2.33";
				// uids = createUmuuid(adv_id, user_agent, pixel, tree.com);
				rand = new java.util.Random();
				int r = rand.nextInt(100);
				int pvuvPercent = jsoExt.optInt("pvuvPercent", 120);
				if (r < pvuvPercent - 100)
					addpv = true;
			} else {
				logMyFile("get old baiduid:" + uids[2]);
				newcookie = false;
			}
			JSONObject jsoLink = new JSONObject();
			jsoLink.put("newcookie", newcookie);
			jsoLink.put("user_agent", uids[0]);
			jsoLink.put("showpixel", uids[1]);
			jsoLink.put("cookie", uids[2]);
			String[] smparams = uids[3].split(";");
			if (smparams.length >= 5) {
				jsoLink.put("cl", smparams[0]);
				jsoLink.put("ln", smparams[1]);
				jsoLink.put("ja", smparams[2]);
				jsoLink.put("ck", smparams[3]);
				jsoLink.put("v", smparams[4]);
			}
			if(newcookie == false)
				jsoLink.put("lt", Long.parseLong(uids[4]));
			jsoLink.put("addpv", addpv);
			jsoLink.put("vendor_id", jsobaidulink.optString("vendor_id"));
			// jsoLink.put("vendor_title",
			// jsobaidulink.optString("vendor_title"));
			jsoTask.put("baidulink", jsoLink);
		}
		int getIpMethod = jsoExt.optInt("getipmethod", 1);
		jsoTask.put("getipmethod", getIpMethod);
		if (getIpMethod == 1) {
			value = IPGetter.getInstance().getIpsRandom(adv_id);
			if (StringUtil.isStringEmpty(value)) {
				log.error("IPGetter.getInstance().getIpsRandom null adv_id=" + adv_id);
			}
			jsoTask.put("proxyip", value);
		}
	}

	static class uminfo {
		// 使用友盟统计的新旧用户数
		int um_newid_count;
		int um_oldid_count;
		// 百度统计的新旧用户
		int baidu_newid_count;
		int baidu_oldid_count;

		public void print() {
			// String info = "newid_count:" + newid_count + ";oldid_count=" +
			// oldid_count;
			// System.out.println(info);
		}
	}

	public static void test() {
		String showpixel = "360x640";
		// String[] s2 = showpixel.split("x");
		// int w = Integer.parseInt(s2[0]);
		// int h = Integer.parseInt(s2[1]);
		// System.out.println("w=" + w + ";h=" + h);
		String ua = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36";

		for (int i = 0; i < 20; i++) {
			String[] uids = createUmuuid(1, ua, showpixel, "www.baidu.com");
			System.out.println("uid[0]=" + uids[0] + "uid[1]=" + uids[1]);
		}
	}

	public static void main(String[] argv) {
		// String url =
		// "http://d.jghttp.golangapi.com/getip?num=3&type=2&pro=&city=0&yys=0&port=11&pack=914&ts=0&ys=0&cs=0&lb=1&sb=0&pb=45&mr=0&regions=";
		String showpixel = "360x640";
		String[] s2 = showpixel.split("x");
		int w = Integer.parseInt(s2[0]);
		int h = Integer.parseInt(s2[1]);
		// System.out.println("w=" + w + ";h=" + h);
		String ua = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36";

		for (int i = 0; i < 20; i++) {
			String[] uids = createUmuuid(1, ua, showpixel, "");
			System.out.println("uid[0]=" + uids[0] + "uid[1]=" + uids[1]);
		}

	}

	@Override
	public void handleTaskResport(JSONObject _jsoResponse) throws JSONException {
		// TODO Auto-generated method stub
		int adv_id = _jsoResponse.getInt("adv_id");
		int autoid = _jsoResponse.getInt("autoid");
		if (adv_id == 0)
			return;
		JSONObject jsoLink = _jsoResponse.optJSONObject("umlink");
		if (jsoLink != null) {

		}
		jsoLink = _jsoResponse.optJSONObject("baidulink");
		if (jsoLink != null) {
			boolean newcookie = jsoLink.optBoolean("newcookie");
			if (newcookie == false)
				return;
			if (jsoLink.optString("cookie", "").length() == 0)
				return;
			String[] uids = new String[5];
			uids[0] = jsoLink.optString("user_agent");
			uids[1] = jsoLink.optString("showpixel");
			uids[2] = jsoLink.optString("cookie", "");
			uids[3] = "24-bit;zh-cn;0;1;1.2.33";

			// java.util.Random rand = new java.util.Random();
			// int r = rand.nextInt(100);
			// advtaskinfo task =
			// advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo();
			// JSONObject jsoExt = task.getExtJso();
			// int olduserPercent = jsoExt.optInt("olduserPercent", 10);
			// if (r < olduserPercent)
			useragentdao.addBaiduid(adv_id, "", autoid, uids);
		}
	}

	@Override
	public JSONObject handleBeanReqeust(JSONObject _jsoRequest, byte[] content) {
		// TODO Auto-generated method stub
		return null;
	}
}
