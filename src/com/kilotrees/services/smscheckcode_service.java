package com.kilotrees.services;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kilotrees.dao.sms_checkcodedao;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.util.StringUtil;

/**
 * 处理短信验证码，对接第三方短信平台，首先从平台中取得后码，然后客户端写入号码并发送短信，平台收到短信内容后把内容返回给我们
 * 
 * @author Administrator
 *
 */
public class smscheckcode_service {
	private static Logger log = Logger.getLogger(smscheckcode_service.class);
	private static smscheckcode_service inst;
	
	private smscheckcode_service() {

	}

	public static smscheckcode_service getInstance() {
		synchronized(smscheckcode_service.class){
			if (inst == null) {
				inst = new smscheckcode_service();
			}
		}
		return inst;
	}
	
	public void refresh()
	{
		processTimeoutSmscode();
	}
	/**
	 * 处理短信验证码超时
	 */
	void processTimeoutSmscode()
	{
		//int timeoutofsec = serverconfig.smscode_timeout;
		JSONObject jso = ServerConfig.getConfigJson();
		if (jso != null) {
			int t = jso.optInt("smscode_timeout",0);
			if(t > 10)
				ServerConfig.smscode_timeout = t;
		}
		sms_checkcodedao.processTimeoutSmscode(ServerConfig.smscode_timeout);
	}
	
	
	/**
	 * 处理验证码平台的回传  解释出验证码 各个平台的验证码参数各式都不大一样，处理手法也可能不一样
	 * 
	 * @param request
	 * @throws ServletException
	 * @throws IOException
	 */
	public void smscodeReport(HttpServletRequest request) throws ServletException, IOException {
		//int seqid = -1;
		//String smscode = "";
		String smstext = "";
		String mobile = "";
		int adv_id = 0;
		//platid,adv_id是我们给出的回传参数，
		//这里可能会有变化，看对接方如何定义接口，一般用手机号就行了，但如果多个业务大跑，平台也难保障短时间给出同一个手机号
		adv_id = Integer.parseInt(request.getParameter("adv_id"));
		String platform_id = request.getParameter("platformid");
		if ("1".equals(platform_id)) {

		} else if ("1".equals(platform_id)) {

		}

		if (adv_id > 0 && StringUtil.isStringEmpty(smstext) == false) {
//			sms_checkcodedao.passBackSmsCheckCode(adv_id, smstext, mobile);
		}
	}
}
