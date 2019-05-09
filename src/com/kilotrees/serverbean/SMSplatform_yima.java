package com.kilotrees.serverbean;

import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.dao.sms_checkcodedao;
import com.kilotrees.model.bo.error_result;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.sms_checkcode;
import com.kilotrees.services.autoid_service;
import com.kilotrees.util.StringUtil;
import com.kilotrees.util.myHttp;

/**
 * 易码短信平台接口
 * 
 * @author Administrator
 *
 */
public class SMSplatform_yima extends ServerBeanBase {
	private static Logger log = Logger.getLogger(SMSplatform_yima.class);
	// 易接短信验证码平台登录token
	protected String sms_login_token = "";
	// 接码平台配置信息
	private JSONObject thSmsJsoPF;
	private static String loginUrl = "http://api.fxhyd.cn/UserInterface.aspx?action=login";
	private static String getmobileurl = "http://api.fxhyd.cn/UserInterface.aspx?action=getmobile";
	private static String getsmsurl = "http://api.fxhyd.cn/UserInterface.aspx?action=getsms";
	private static String sendsmsurl = "http://api.fxhyd.cn/UserInterface.aspx?action=sendsms";
	private static String userName = "qswl168";
	private static String pass = "dasheng01";
	private static SMSplatform_yima inst;

	private SMSplatform_yima() {
		serverbeanid = "yima";
	}

	public static SMSplatform_yima getInstance() {
		synchronized (SMSplatform_yima.class) {
			if (inst == null) {
				inst = new SMSplatform_yima();
			}
		}
		return inst;
	}

	boolean login() {
		// 每次刷新
		JSONObject jso = ServerConfig.getConfigJson();
		thSmsJsoPF = jso.optJSONObject("smspf_" + this.serverbeanid);
		
		if (sms_login_token.length() > 0)
			return true;
		try {
			// thSmsJsoPF = jso.getJSONObject("smspf_" + this.platfromId);
			if(thSmsJsoPF != null){
				if(!StringUtil.isStringEmpty(thSmsJsoPF.optString("loginurl")))
					loginUrl = thSmsJsoPF.optString("loginurl");
				if(!StringUtil.isStringEmpty(thSmsJsoPF.optString("getmobileurl")))
					getmobileurl = thSmsJsoPF.optString("getmobileurl");
				if(!StringUtil.isStringEmpty(thSmsJsoPF.optString("getsmsurl")))
					getsmsurl = thSmsJsoPF.optString("getsmsurl");
				if(!StringUtil.isStringEmpty(thSmsJsoPF.optString("sendsmsurl")))
					sendsmsurl = thSmsJsoPF.optString("sendsmsurl");
				if(!StringUtil.isStringEmpty(thSmsJsoPF.optString("username")))
						userName = thSmsJsoPF.optString("username");
				if(!StringUtil.isStringEmpty(thSmsJsoPF.optString("pass")))
					pass = thSmsJsoPF.optString("pass");
			}
			if (loginUrl.indexOf("?") < 0)
				loginUrl += "?";
			if (loginUrl.indexOf("action=login") < 0)
				loginUrl += "action=login";
			loginUrl += "&username=" + userName + "&password=" + pass;

			myHttp dt = new myHttp();
			if (dt.getUrlSynTry(loginUrl, 1)) {
				byte[] content = dt.getContent();
				String contStr = new String(content, "utf-8");
				log.info(contStr);
				int pos = contStr.indexOf("success|");
				if (pos >= 0){
					this.sms_login_token = contStr.substring(pos + "success|".length());
					this.sms_login_token = trim(this.sms_login_token);
					log.info(this.sms_login_token);
				}
				else {
					log.error("易码平台登录失败:" + contStr);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		sms_login_token = sms_login_token.trim();
		if (sms_login_token.length() > 0)
			return true;
		return false;
	}
	/**
	 * 按易接平台的返回格式取正确的内容,这些平台一般以一个0为一行结尾
	 * @param retContent
	 * @return
	 */
	static String trim(String retContent)
	{
		int pos = retContent.indexOf("\r\n0");
		if(pos > 0)
			retContent = retContent.substring(0,pos);
		return retContent;
	}

	public JSONObject doSms(JSONObject jsoRequest) {
		// TODO Auto-generated method stub
		JSONObject smsJso = jsoRequest.optJSONObject("dosms");//进一步封装的json对象
		JSONObject jsoRet = new JSONObject();//要返回的json对象
		error_result err = new error_result();//放错误信息
		String smsType = smsJso.optString("smstype");//手机号 or 验证码
		int itemid = smsJso.optInt("itemid");
		String dev_tag = jsoRequest.optString("dev_tag");
		int adv_id = jsoRequest.optInt("adv_id");
		try {
			if (login() == false) {//没有登录
				err.setErr_code(error_result.dev_sms_login_error);
				err.setErr_info("smsplatform_yima login error");
				jsoRet.put("err_result", err.toJSONObject());
				return jsoRet;
			}
			if (smsType.equals("getmobile")) {
				String excludeno = smsJso.optString("excludeno","");// 排除号段
				String url = getmobileurl;//
				
				if (url.indexOf("?") < 0)
					url += "?";
				if (url.indexOf("action=getmobile") < 0)
					url += "action=getmobile";
				url += "&token=" + sms_login_token;
				url += "&itemid=" + itemid;
				url += "&excludeno=" + excludeno;
				myHttp dt = new myHttp();
				sms_checkcode sms = new sms_checkcode();
				sms.setAdv_id(adv_id);
				sms.setReq_dev(dev_tag);
				String mobile = "";				
				if (dt.getUrlSynTry(url, 1)) {
					byte[] content = dt.getContent();
					String contStr = new String(content, "utf-8");
					int pos = contStr.indexOf("success|");
					if (pos >= 0){
						mobile = contStr.substring(pos + "success|".length());
						mobile = trim(mobile);
						log.info(mobile);
					}
					else {
						log.error("易码平台取手机号失败:" + contStr);
						err.setErr_code(error_result.dev_getsms_mobile_error);
						err.setErr_info("易码平台取手机号失败:" + contStr);
					}
				} else {
					err.setErr_code(error_result.server_http_error);
					err.setErr_info("dt.getUrlSynTry,url=" + url + ":dt.respcode:" + dt.getResponCode());
				}
				if (mobile.length() > 0) {
					sms.setMobile(mobile);
					sms.setGetmo_time(new Date());
					sms.setSeqid(autoid_service.getMaxSeqid());
					sms_checkcodedao.addSmsCheckCode(sms);
				}
				jsoRet.put("err_result", err.toJSONObject());
				jsoRet.put("seqid", sms.getSeqid());
				jsoRet.put("mobile", sms.getMobile());
			} else if (smsType.equals("sendsms")) {
				String smstext = smsJso.optString("smstext");//
				String url = sendsmsurl;//
				String mobile = smsJso.optString("mobile");//
				int seqid = smsJso.optInt("seqid");
				if (url.indexOf("?") < 0)
					url += "?";
				if (url.indexOf("action=sendsms") < 0)
					url += "action=sendsms";
				url += "&token=" + sms_login_token;
				url += "&itemid=" + itemid;
				url += "&mobile=" + mobile;
				url += "&sms=" + smstext;
				myHttp dt = new myHttp();
				if (dt.getUrlSynTry(url, 1)) {
					byte[] content = dt.getContent();
					String contStr = new String(content, "utf-8");
					int pos = contStr.indexOf("success");
					if (pos < 0) {
						err.setErr_code(error_result.dev_sendsms_error);
						err.setErr_info("易码平台发短信失败:" + contStr);
					}
					sms_checkcodedao.sendSms(seqid, smstext, contStr);
				} else {
					err.setErr_code(error_result.server_http_error);
					err.setErr_info("dt.getUrlSynTry,url=" + url + ":dt.respcode:" + dt.getResponCode());
				}
				jsoRet.put("err_result", err.toJSONObject());
			} else if (smsType.equals("getsms")) {
				//客户端10秒钟取一次，最多取3-6次，
				String mobile = smsJso.optString("mobile");//
				Long seqid = smsJso.optLong("seqid");
				int release = smsJso.optInt("release", 1);
				sms_checkcode sms = new sms_checkcode();
				sms.setAdv_id(adv_id);
				sms.setReq_dev(dev_tag);
				sms.setMobile(mobile);
				sms.setSeqid(seqid);
				String url = getsmsurl;//
				if (url.indexOf("?") < 0)
					url += "?";
				if (url.indexOf("action=getsms") < 0)
					url += "action=getsms";
				url += "&token=" + sms_login_token;
				url += "&itemid=" + itemid;
				url += "&mobile=" + mobile;
				if (release == 1)
					url += "&release=1";
				myHttp dt = new myHttp();
				if (dt.getUrlSynTry(url, 1)) {
					byte[] content = dt.getContent();
					String contStr = new String(content, "utf-8");
					int pos = contStr.indexOf("success|");
					if (pos > 0) {
						String smstext = contStr.substring(pos + "success|".length());
						smstext = trim(smstext);
						sms.setSmstext(smstext);
						//这里是我们主动取短信而不是平台回调,所以回传短信和提取短信都同时做了,后面getSmsCheckCode是为了更新fech_time字段
						sms_checkcodedao.passBackSmsCheckCode(seqid, smstext, "");
						sms_checkcodedao.devGetSmsCheckCode(seqid);
						jsoRet.put("mobile", mobile);
						jsoRet.put("smstext", sms.getSmstext());
					} else if (contStr.indexOf("3001") >= 0) {
						err.setErr_code(3001);
						err.setErr_info("短信尚未到达");
					} else {
						err.setErr_code(error_result.dev_getsms_text_error);
						err.setErr_info(contStr);
						sms_checkcodedao.passBackSmsCheckCode(seqid, "", contStr);
					}
				} else {
					err.setErr_code(error_result.server_http_error);
					err.setErr_info("dt.getUrlSynTry,url=" + url + ":dt.respcode:" + dt.getResponCode());
				}
				jsoRet.put("err_result", err.toJSONObject());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return jsoRet;
	}

	public static void main(String[] argv) {
		String s = "success|abc";
		int pos = s.indexOf("success|");
		System.out.println(s.substring(pos + "success|".length()));
	}

	@Override
	public JSONObject handleBeanReqeust(JSONObject _jsoRequest, byte[] content) {
		// TODO Auto-generated method stub
		return this.doSms(_jsoRequest);
	}

	@Override
	public void handleTaskParasm(JSONObject jsoTask) throws JSONException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleTaskResport(JSONObject _jsoResponse) throws JSONException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(int adv_id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refresh(int adv_id) {
		// TODO Auto-generated method stub
		
	}
	
}
