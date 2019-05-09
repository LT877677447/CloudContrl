package com.kilotrees.serverbean;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.dao.advhttplinkdao;
import com.kilotrees.log.LogFile;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.advhttplink;
import com.kilotrees.util.StringUtil;

/**
 * http链接搜索树，从advhttplink取出所有链接数据，模拟人工深度点击
 * 
 * @author Administrator
 *
 */
public class httplinktree {
	private static Logger log = Logger.getLogger(advhttplinkdao.class);
	ArrayList<advhttplink> linksls = new ArrayList<advhttplink>();
	//后面商量按正常的页面模式，子节点可以包含父节点，这个不用了
	ArrayList<linknode> nodels = new ArrayList<linknode>();
	// 按访问深度比例放入各个深度数据，列表总长度为100，用于随机取深度值,之前想法是子节点不能包含父节点，否则会无限循环，但后面否定了。所以暂时不用了
	ArrayList<Integer> deepsLs = new ArrayList<Integer>();
	int adv_id;
	// 访问规则，每个层次访问百分比，每个访问之间的停留时间范围,现在最大5层，我们可以在数据库中预先设定各层数的规则
	String rule = "30;35;20;10;5";
	int max_deep;
	String com = "";

	public httplinktree(int advid) {
		adv_id = advid;
		// try{
		init();
		// }catch(Exception e)
		
	}

	void test() {
		advhttplink root = new advhttplink();
		root.setAdv_id(1);
		root.setUrl_id(1);
		root.setUrl_value("http://url_1");
		root.setChildens_id("2;3;4");
		linksls.add(root);

		advhttplink cap = new advhttplink();
		cap.setAdv_id(1);
		cap.setUrl_id(16);
		cap.setUrl_value("http://url_" + cap.getUrl_id());
		cap.setChildens_id("");
		linksls.add(cap);

		cap = new advhttplink();
		cap.setAdv_id(1);
		cap.setUrl_id(17);
		cap.setUrl_value("http://url_" + cap.getUrl_id());
		cap.setChildens_id("");
		linksls.add(cap);

		cap = new advhttplink();
		cap.setAdv_id(1);
		cap.setUrl_id(4);
		cap.setUrl_value("http://url_" + cap.getUrl_id());
		cap.setChildens_id("7;8;9");
		linksls.add(cap);

		cap = new advhttplink();
		cap.setAdv_id(1);
		cap.setUrl_id(2);
		cap.setUrl_value("http://url_" + cap.getUrl_id());
		cap.setChildens_id("5;6");
		linksls.add(cap);

		cap = new advhttplink();
		cap.setAdv_id(1);
		cap.setUrl_id(3);
		cap.setUrl_value("http://url_" + cap.getUrl_id());
		cap.setChildens_id("");
		linksls.add(cap);

		cap = new advhttplink();
		cap.setAdv_id(1);
		cap.setUrl_id(8);
		cap.setUrl_value("http://url_" + cap.getUrl_id());
		cap.setChildens_id("13;14");
		linksls.add(cap);

		cap = new advhttplink();
		cap.setAdv_id(1);
		cap.setUrl_id(5);
		cap.setUrl_value("http://url_" + cap.getUrl_id());
		cap.setChildens_id("");
		linksls.add(cap);

		cap = new advhttplink();
		cap.setAdv_id(1);
		cap.setUrl_id(6);
		cap.setUrl_value("http://url_" + cap.getUrl_id());
		cap.setChildens_id("10;11");
		linksls.add(cap);

		cap = new advhttplink();
		cap.setAdv_id(1);
		cap.setUrl_id(7);
		cap.setUrl_value("http://url_" + cap.getUrl_id());
		cap.setChildens_id("12");
		linksls.add(cap);

		cap = new advhttplink();
		cap.setAdv_id(1);
		cap.setUrl_id(9);
		cap.setUrl_value("http://url_" + cap.getUrl_id());
		cap.setChildens_id("15");
		linksls.add(cap);

		cap = new advhttplink();
		cap.setAdv_id(1);
		cap.setUrl_id(10);
		cap.setUrl_value("http://url_" + cap.getUrl_id());
		cap.setChildens_id("");
		linksls.add(cap);

		cap = new advhttplink();
		cap.setAdv_id(1);
		cap.setUrl_id(11);
		cap.setUrl_value("http://url_" + cap.getUrl_id());
		cap.setChildens_id("");
		linksls.add(cap);

		cap = new advhttplink();
		cap.setAdv_id(1);
		cap.setUrl_id(12);
		cap.setUrl_value("http://url_" + cap.getUrl_id());
		cap.setChildens_id("");
		linksls.add(cap);

		cap = new advhttplink();
		cap.setAdv_id(1);
		cap.setUrl_id(13);
		cap.setUrl_value("http://url_" + cap.getUrl_id());
		cap.setChildens_id("");
		linksls.add(cap);

		cap = new advhttplink();
		cap.setAdv_id(1);
		cap.setUrl_id(14);
		cap.setUrl_value("http://url_" + cap.getUrl_id());
		cap.setChildens_id("");
		linksls.add(cap);

		cap = new advhttplink();
		cap.setAdv_id(1);
		cap.setUrl_id(15);
		cap.setUrl_value("http://url_" + cap.getUrl_id());
		cap.setChildens_id("16;17");
		linksls.add(cap);

	}

	public String print() {
		// Collections.sort(nodels);
		int deep = nodels.get(0).deep;
		String s = "";
		for (linknode n : nodels) {
			// if(n.deep != )
			s += n.print();
		}
		return s;
	}

	void init(){
		linksls = advhttplinkdao.getLinkedList(adv_id);
		// 测试

		// test();

		if (linksls.size() == 0) {
			return;
		}
//		if (checkLinkedTree() == false)
//			throw new httplinkexception("checkLinkedTree");
		// searchTreeNode_1(null);
//		searchTreeNode_2();
//		if (max_deep == 0) {
//			log.error("httplinktree max_deep = 0");
//		} else
//			log.info("httplinktree max_deep=" + max_deep);

		// 安深度排序nodels
//		this.rule = advhttplinkdao.getHttpTreeRule(this.max_deep);
//		String[] srule = rule.split(";");
//		for (int i = 0; i < srule.length; i++) {
//			int count = Integer.parseInt(srule[i]);
//			int deep = i + 1;
//			for (int j = 0; j < count; j++) {
//				deepsLs.add(deep);
//			}
//		}
		
		String rootUrl = getHttpLink(1).getUrl_value();
		com = getComFromUrl(rootUrl);
	}

	advhttplink getHttpLink(int url_id) {
		for (advhttplink e : linksls) {
			if (e.getUrl_id() == url_id) {
				return e;
			}
		}
		return null;
	}

	/**
	 * 检查当前子页面的所有子页是否又包含父页面，如果是则肯定有问题 searchTreeNode递归会死循环
	 * 不能用了
	 * @param parentUrlId
	 * @param childId
	 * @return
	 */
	boolean checkLinkedTree() {
		// 首先检查所有child是否在表中有记录
		for (advhttplink cur : linksls) {
			String ss = cur.getChildens_id();
			if (StringUtil.isStringEmpty(ss))
				continue;
			String[] sids = ss.split(";");
			for (String s : sids) {
				int id = Integer.parseInt(s);
				if (getHttpLink(id) == null) {
					log.error("checkLinkedTree id=" + id + "getHttpLink is null");
					return false;
				}
			}
		}
		// 检查当前curlid是否又包含在它的子子孙孙中
		for (advhttplink cur : linksls) {
			ArrayList<Integer> ls = new ArrayList<Integer>();
			int urlid = cur.getUrl_id();
			getAllChilders(urlid, ls);
			for (int i = 0; i < ls.size(); i++) {
				if (urlid == ls.get(i)) {
					log.error("checkLinkedTree urlid is in childs,urlid=" + urlid);
					for (int id : ls)
						log.error("all childs id:" + id);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 递归获取当前urlid的所有子孙id
	 * 
	 * @param urlId
	 * @param ls
	 */
	void getAllChilders(int urlId, ArrayList<Integer> ls) {
		advhttplink cur = getHttpLink(urlId);
		String ss = cur.getChildens_id();
		if (StringUtil.isStringEmpty(ss))
			return;
		String[] sids = ss.split(";");
		for (String s : sids) {
			int id = Integer.parseInt(s);
			ls.add(id);
			getAllChilders(id, ls);
		}
	}

	/**
	 * 递归搜索各个节点的链接(终向搜索)
	 * 
	 * @param url_id
	 */
	void searchTreeNode_1_nouse(linknode parentNode) {
		if (parentNode == null) {
			// 最原始的链接url_id必须为1
			advhttplink e = getHttpLink(1);
			linknode node = new linknode();
			node.deep = 1;
			max_deep = 1;
			node.lasturlid = 1;
			node.nodeLinks.add(e.getUrl_value());
			nodels.add(node);
			searchTreeNode_1_nouse(node);

		} else {
			// int url_id = parentNode.lasturlid;
			// int deep = parentNode.deep + 1;
			advhttplink parentlink = getHttpLink(parentNode.lasturlid);
			String childsStr = parentlink.getChildens_id();
			if (StringUtil.isStringEmpty(childsStr) == true)
				return;
			String[] childs = childsStr.split(";");
			for (String s : childs) {
				int id = Integer.parseInt(s);
				advhttplink cur = getHttpLink(id);
				linknode node = new linknode();
				node.deep = parentNode.deep + 1;
				node.lasturlid = cur.getUrl_id();
				node.nodeLinks = (ArrayList<String>) parentNode.nodeLinks.clone();
				node.nodeLinks.add(cur.getUrl_value());
				nodels.add(node);
				if (node.deep > max_deep)
					max_deep = node.deep;
				searchTreeNode_1_nouse(node);
			}
		}
	}

	public static String getComFromUrl(String url)
	{
		String com = "";
		URL r;
		try {
			r = new URL(url);
			com = r.getHost();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			
		}
		
		return com;
	}
	/**
	 * 横向搜索,不用递归,这种方式本身nodels就按深度排序，不需要重新排序。纯属测试一下算法
	 * 不再使用
	 * @param parentNode
	 */
	void searchTreeNode_2_nouse() {
		ArrayList<linknode> brotherls = new ArrayList<linknode>();
		ArrayList<linknode> childsls;

		// 最原始的链接url_id必须为1
		advhttplink pRoot = getHttpLink(1);
		linknode node = new linknode();
		node.deep = 1;
		node.lasturlid = pRoot.getUrl_id();
		brotherls.add(node);
		// int deep = 1;
		while (brotherls.size() > 0) {
			childsls = new ArrayList<linknode>();
			for (linknode e : brotherls) {
				if (e.deep > max_deep)
					max_deep = e.deep;
				advhttplink ap = this.getHttpLink(e.lasturlid);
				// 有问题,不能取得父接点的nodeLinks,在linknode加一个父接点
				if (e.parent != null) {
					e.nodeLinks = (ArrayList<String>) e.parent.nodeLinks.clone();
					e.nodeExt = (ArrayList<String>) e.parent.nodeExt.clone();
					e.httptimeouts = (ArrayList<Integer>)e.httptimeouts.clone();
				}
				e.nodeLinks.add(ap.getUrl_value());
				e.nodeExt.add(ap.getExt());
				e.httptimeouts.add(ap.getHttptimeout());
				nodels.add(e);

				String childsStr = ap.getChildens_id();
				if (StringUtil.isStringEmpty(childsStr) == true)
					continue;
				String[] childs = childsStr.split(";");
				for (String s : childs) {
					int id = Integer.parseInt(s);
					advhttplink ce = getHttpLink(id);
					// childsls.add(ce);
					node = new linknode();
					node.parent = e;
					node.deep = e.deep + 1;
					node.lasturlid = ce.getUrl_id();
					childsls.add(node);
				}
			}
			brotherls = childsls;
			// deep += 1;
		}
	}

	/**
	 * 随机取出一组访问连接
	 * 不用这个方法
	 * @return
	 * @throws JSONException
	 */
	public JSONObject getLinksRandom_nouse() throws JSONException {
		JSONObject jso = new JSONObject();
		JSONArray jarray = new JSONArray();

		int deep = 1;
		int r = 0;
		if (max_deep > 1) {
			java.util.Random rand = new java.util.Random();
			deep = rand.nextInt(this.deepsLs.size());
			deep = deepsLs.get(deep);

			// System.out.println("getLinksRandom deep=" + deep);

			int begin = 0;
			int end = 0;
			for (int i = 0; i < nodels.size(); i++) {
				linknode n = nodels.get(i);
				if (n.deep == deep && begin == 0) {
					begin = i;
					end = i;
				} else if (end > 0 && n.deep > deep) {
					end = i - 1;
					break;
				}
			}
			// System.out.println("getLinksRandom begin=" + begin + ",end=" +
			// end);
			r = begin;
			if (end > begin) {
				rand = new java.util.Random();
				r = rand.nextInt(end + 1 - begin);
				r += begin;
			}
		}

		// System.out.println("getLinksRandom r=" + r);
		linknode node = nodels.get(r);
		for (int i = 0; i < node.nodeLinks.size(); i++) {
			String s = node.nodeLinks.get(i);
			JSONObject jb = new JSONObject();
			jb.put("url", s);
			jb.put("httptimeout", node.httptimeouts.get(i));
			s = node.nodeExt.get(i);
			if (s.indexOf(";") > 0) {
				String[] ss = s.split(";");
				int begin = Integer.parseInt(ss[0]);
				int end = Integer.parseInt(ss[1]);
				java.util.Random rand = new java.util.Random();
				r = rand.nextInt(end + 1 - begin);
				r += begin;
				jb.put("sleeptime", r);
			}
			
			jarray.put(jb);
		}

		jso.put("links", jarray);
		return jso;
	}
	void logMyFile(String log) {
		String logfile = ServerConfig.contextRealPath + "files/log/" + httplinktree.class.getName() + ".log";
		File f = new File(logfile);
		if(f.exists() == false)
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		LogFile.writeLogFile(logfile, log + "\r\n");
	}
	/**
	 * 先随机计算出深度（最大10），然后实时从root中的child中随机出路径
	 * 以后考虑深度比例
	 * @return
	 * @throws JSONException
	 */
	public JSONObject getLinksRandom() throws JSONException {
		JSONObject jso = new JSONObject();
		JSONArray jarray = new JSONArray();
		//int deep = 1;
		java.util.Random rand = new java.util.Random();
		int m_deep = rand.nextInt(10) + 1;
		advhttplink link = getHttpLink(1);
		logMyFile("getLinksRandom deep=" + m_deep);
		int deep = 0;
		while(m_deep-- > 0)
		{
			deep++;
			JSONObject jb = new JSONObject();
			jb.put("url", link.getUrl_value());
			jb.put("httptimeout", link.getHttptimeout());
			String s = link.getExt();
			if (s.indexOf(";") > 0) {
				String[] ss = s.split(";");
				int begin = Integer.parseInt(ss[0]);
				int end = Integer.parseInt(ss[1]);
				rand = new java.util.Random();
				int r = rand.nextInt(end + 1 - begin);
				r += begin;
				jb.put("sleeptime", r);
			}
			jarray.put(jb);
			
			String childs = link.getChildens_id();
			if(StringUtil.isStringEmpty(childs))
				break;
			//随机选取子节点
			String[] sids = childs.split(";");
			if(sids.length < 1)
				break;
			rand = new java.util.Random();
			int r = rand.nextInt(sids.length);
			link = getHttpLink(Integer.parseInt(sids[r]));
		}
		logMyFile("getLinksRandom really deep=" + deep);
		jso.put("links", jarray);
		logMyFile(jso.toString());
		return jso;
	}


	class linknode implements Comparable<linknode> {
		linknode parent;
		int deep = 1;
		int lasturlid;
		ArrayList<String> nodeLinks = new ArrayList<String>();
		// 页面浏览时间
		ArrayList<String> nodeExt = new ArrayList<String>();
		ArrayList<Integer> httptimeouts = new ArrayList<Integer>();

		String print() {
			String info = "url_id=" + lasturlid + ";deep=" + deep;
			info += "{";
			for (String s : nodeLinks)
				info += s;
			info += "}";
			System.out.println(info);
			return info;
		}

		public int compareTo(linknode o) {
			// TODO Auto-generated method stub
			if (this.deep == o.deep)
				return 0;
			if (this.deep < o.deep)
				return -1;
			else
				return 1;
		}
	}

	public static void main(String[] argv) throws Exception {
		int i = 1;
		while(i-- > 0){
			System.out.println("test " + i);
		}
	}
}
