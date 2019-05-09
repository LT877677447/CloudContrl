/**
 * @author Administrator
 * 2019年1月19日 下午6:02:21 
 */
package com.kilotrees.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.dao.WeixinDao;
import com.kilotrees.model.po.tb_weixinaccount;
import com.kilotrees.model.po.tb_weixinlink;
import com.kilotrees.model.po.tb_weixinlinkFail;
import com.kilotrees.model.po.tb_weixinlog;
import com.kilotrees.util.DateUtil;

public class WeixinService {
	private Logger log = Logger.getLogger(WeixinService.class);
	private static WeixinService inst = null;
	private WeixinDao dao = WeixinDao.getInstance();

	public static WeixinService getInstance() {
		synchronized (WeixinService.class) {
			if (inst == null) {
				inst = new WeixinService();
			}
		}
		return inst;
	}

	/**
	 * 查询所有tb_weixinaccount order by id
	 * 
	 * @return
	 */
	public List<tb_weixinaccount> getAllAccount() {
		return WeixinDao.getInstance().getAllAccount();
	}

	/**
	 * 查询所有tb_weixinlink order by id
	 * 
	 * @return
	 */
	public JSONArray getAllLink() {
		JSONArray json = new JSONArray();
		List<tb_weixinlink> list = WeixinDao.getInstance().getAllLink();
		List<String> str = new ArrayList<>();
		for (tb_weixinlink val : list) {
			String link = val.getLinkUrl();
			str.add(link);
		}
		json.put(str);
		return json;
	}

	public JSONObject fetchLink(tb_weixinaccount account) throws JSONException {
		JSONObject json = new JSONObject();
		int fetchNumber = 3;
		//先拿之前失败的
		List<tb_weixinlinkFail> failLinks = dao.getFailLink(account, fetchNumber);
		fetchNumber -= failLinks.size();
		
		//再拿新的
		List<tb_weixinlink> newLinks = new ArrayList<>();	
		int complete = account.getComplete();
		if(fetchNumber > 0) {	
			newLinks = dao.getRangeLink(complete, fetchNumber);
		}
		//封装要返回的link
		List<String> str = new ArrayList<>();
		for (tb_weixinlinkFail val : failLinks) {
			str.add(val.getLinkUrl());
		}
		for (tb_weixinlink val : newLinks) {
			str.add(val.getLinkUrl());
		}
		
		//更新[tb_weixinaccount]表
		if(account.getComment().equals("新增") && account.getComplete() > 0) {
			account.setComment("");
		}
		if(newLinks.size() > 0) {
			account.setComplete(newLinks.get(newLinks.size()-1).getId());
		}
		account.setLastFetchTime(new Date());
		dao.updateAccount(account);
		
		json.put("links", str);
		return json;
	}
	
	
	
	
	public tb_weixinaccount createAccount(String strAccount)  {
		tb_weixinaccount account = new tb_weixinaccount();
		account.setAccount(strAccount);
		account.setComplete(0);
		account.setLastFetchTime(DateUtil.getDate("2019-1-1 00:00:00"));
		account.setJoinTime(new Date());
		account.setComment("新增");
		account = dao.insertAccount(account);
		account = dao.getSingleAccount(account.getAccount());
		return account;
	}

	/**
	 * 查询所有tb_weixinlog order by id
	 * 
	 * @return
	 */
	public List<tb_weixinlog> getAllLog() {
		return WeixinDao.getInstance().getAllLog();
	}

	public void writeLog(String type, String message, String comment, Date logtime) {
		tb_weixinlog log = new tb_weixinlog();
		log.setType(type);
		log.setMessage(message);
		log.setComment(comment);
		log.setLogTime(logtime);
		dao.writeLog(log);
	}

	public tb_weixinaccount getSingleAccount(String account) {
		return dao.getSingleAccount(account);
	}

	/**
	 * 新增tb_weixinlink
	 * 
	 * @param newVal
	 */
	public void addWeixinLink(tb_weixinlink newVal) {
		WeixinDao.getInstance().insertWeixinLink(newVal);
	}
	
	/**成功时，更新[tb_weixinlinkFail]表记录，检查该link是否已经做完
	 * @param account
	 * @param strLink
	 */
	public void handleSuccess(tb_weixinaccount account,tb_weixinlink link) {
		//去tb_weixinlinkFail表删除记录
		dao.deleteWeixinLinkFail(account, link);

		//更新link表
		int currentCount = link.getCurrentCount();
		link.setCurrentCount(currentCount + 1);
		dao.updateLink(link);
		
		//如果link已经做完，把该link在tb_weixinlinkFail的记录删掉
		if(link.getCurrentCount() >= link.getNeedCount()) {
			dao.deleteWeixinLinkFail(link);
		}
	}
		
	public void handleFail(tb_weixinaccount account,tb_weixinlink link) {
		tb_weixinlinkFail linkFail = new tb_weixinlinkFail();
		linkFail.setIdAccount(account.getId());
		linkFail.setAccount(account.getAccount());
		linkFail.setIdLink(link.getId());
		linkFail.setLinkUrl(link.getLinkUrl());
		int failureNumber;
		tb_weixinlinkFail failLink = dao.getSingleFailLink(account, link);
		if(failLink == null) {
			failureNumber = 1;
		}else {
			failureNumber = failLink.getFailureNumber() + 1;
		}
		linkFail.setFailureNumber(failureNumber);
		linkFail.setLastFailTime(new Date());
		if(failureNumber == 1) {
			dao.insertFail(linkFail);
		}else {
			dao.updateFail(linkFail);
		}
		
	}
	
	public tb_weixinlinkFail getSingleFailLink(tb_weixinaccount account,tb_weixinlink link) {
		return dao.getSingleFailLink(account, link);
	}
	
	
	
	public tb_weixinlink getSingleLink(String strLink) {
		return dao.getSingleLink(strLink);
	}
	
}
