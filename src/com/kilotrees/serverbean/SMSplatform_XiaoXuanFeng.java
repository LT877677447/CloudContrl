package com.kilotrees.serverbean;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.dao.sms_checkcodedao;
import com.kilotrees.dao.douYin.DouYinDao;
import com.kilotrees.model.bo.error_result;
import com.kilotrees.model.po.sms_checkcode;
import com.kilotrees.services.autoid_service;
import com.kilotrees.util.FileUtil;
import com.kilotrees.util.myHttp;

/**
 * 易码短信平台接口
 * 
 * @author Administrator
 *
 */
public class SMSplatform_XiaoXuanFeng extends ServerBeanBase {
	private static Logger log = Logger.getLogger(SMSplatform_XiaoXuanFeng.class);
	//查余额
	private static String BalanceUrl = "http://xin.szxiaoxuanfeng.com/api/customer/get-balance?";
	//拿手机号
	private static String mobileurl = "http://xin.szxiaoxuanfeng.com/api/customer/get-mobile?";
//	private static String mobileurl = "http://192.168.3.116:8180/My/mobileNo?";
	//拿验证码
	private static String smsurl = "http://xin.szxiaoxuanfeng.com/api/customer/get-sms?";
//	private static String smsurl = "http://192.168.3.116:8180/My/Code?";
	//报告重复手机号
	private static String RepeatNum = "http://xin.szxiaoxuanfeng.com/api/customer/report?";
	//拿手机号的秘钥
	private static String miyao = "fYN8nvAEx4E2wOxg";
	//用户id
	private static String user_id = "10187";
	
	private static SMSplatform_XiaoXuanFeng inst;

	private SMSplatform_XiaoXuanFeng() {
		serverbeanid = "XiaoXuanFeng";
	}

	public static SMSplatform_XiaoXuanFeng getInstance() {
		synchronized (SMSplatform_XiaoXuanFeng.class) {
			if (inst == null) {
				inst = new SMSplatform_XiaoXuanFeng();
			}
		}
		return inst;
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
		JSONObject smsJso = jsoRequest.optJSONObject("dosms");
		JSONObject jsoRet = new JSONObject();
		error_result err = new error_result();
		String smsType = smsJso.optString("smstype");
		//把yima的itemid当作business_id使用,详见小旋风文档 
		String business_id = smsJso.optString("itemid"); 
		String province = smsJso.optString("province");
		String city = smsJso.optString("city");
		
		String dev_tag = jsoRequest.optString("dev_tag");
		int adv_id = jsoRequest.optInt("adv_id");
		try {
			//临时测试加的(1/2)
//			byte[] bs = FileUtil.readBytes("F:\\共享文件夹\\手机号.txt");
//			byte[] bs2 = FileUtil.readBytes("F:\\共享文件夹\\验证码.txt");
//			String phoneNum = new String(bs);
//			String code = new String(bs2);
			
			if (smsType.equals("getmobile")) {
				//test(2/2)
//				jsoRet.put("mobile", phoneNum);
//				return jsoRet;
				
				long seqid = autoid_service.getMaxSeqid();//URL中的参数（16位随机字符）
				String url = mobileurl;//
				String param = "business_id=" + business_id;
				param += "&city=" + city;
				param += "&isp=0";
				param += "&nonce_str=" + seqid;
				param += "&province=" + province;
				param += "&user_id=" + user_id;
				
				String sign = encryption(param+miyao);
				param += "&sign=" + sign;
				url += param;
				myHttp dt = new myHttp();
				sms_checkcode sms = new sms_checkcode();
				sms.setAdv_id(adv_id);
				sms.setReq_dev(dev_tag);
				sms.setSeqid(seqid);
				String mobile = "";
				String info = "";
				if (dt.getUrlSynTry(url, 1)) {
					byte[] content = dt.getContent();
					String contStr = new String(content, "utf-8");
					info = contStr;
					//成功：200 失败：1008
					String code = contStr.substring(contStr.indexOf("code")+6, contStr.indexOf(",", contStr.indexOf("code")+6)).trim();
					//成功："ok" 失败:"账号已锁定"
					String message = contStr.substring(contStr.indexOf("message")+9, contStr.indexOf(",", contStr.indexOf("message")+9)).trim();
					//成功："18258790806" 失败：null
					String data = contStr.substring(contStr.indexOf("data")+6, contStr.indexOf(",", contStr.indexOf("data")+6)).trim();
					//1554120877
					String timestamp = contStr.substring(contStr.indexOf("timestamp")+11, contStr.indexOf("}", contStr.indexOf("timestamp")+11)).trim();
					
					if (message.contains("ok") && !data.equals("null")){
						//成功拿到手机号
						mobile = trim(data);
						mobile = mobile.replace("\"", "");
					}
					else {
						//http 通信成功，但没拿到号码
//						err.setErr_code(error_result.dev_getsms_mobile_error);
//						err.setErr_info("小旋风取手机号失败:" + contStr);
						DouYinDao.SmsLog_2(mobile,"" ,"adv_id:"+adv_id+"|dev_tag:"+dev_tag+"|取手机号通讯成功，但没拿到手机号:"+info, "", "",  new Date());
					}
				} else {
					//http通信失败
//					err.setErr_code(error_result.server_http_error);
//					err.setErr_info("dt.getUrlSynTry,url=" + url + ":dt.respcode:" + dt.getResponCode());
					DouYinDao.SmsLog_3(mobile,"" ,"adv_id:"+adv_id+"|dev_tag:"+dev_tag+"|取手机号通讯失败:"+info, "", "",  new Date());
				}
				if (mobile.length() > 0) {
					sms.setMobile(mobile);
					sms.setGetmo_time(new Date());
					sms_checkcodedao.addSmsCheckCode(sms);
					DouYinDao.SmsLog_1(mobile,"" ,"adv_id:"+adv_id+"|dev_tag:"+dev_tag+"|取手机号成功:"+info, "", "",  new Date());
				}
				jsoRet.put("err_result", err.toJSONObject());
				jsoRet.put("seqid", sms.getSeqid());
				jsoRet.put("mobile", sms.getMobile());
			} else if (smsType.equals("getsms")) {
				//test(1/1)
//				jsoRet.put("mobile", phoneNum);
//				jsoRet.put("smstext", code);
//				return jsoRet;
				
				//客户端10秒钟取一次，最多取3-6次，  
				String mobile = smsJso.optString("mobile");//
				jsoRet.put("mobile", mobile);
				Long seqid = smsJso.optLong("seqid");
				sms_checkcode sms = new sms_checkcode();
				sms.setAdv_id(adv_id);
				sms.setReq_dev(dev_tag);
				sms.setMobile(mobile);
				sms.setSeqid(seqid);
				String url = smsurl;
				String info = "";
				String param = "user_id="+user_id;
				param += "&business_id=" + business_id;
				param += "&mobile=" + mobile;
				param += "&nonce_str=" + seqid;
				
				String jiami_param = "business_id="+business_id+"&mobile="+mobile+"&nonce_str="+seqid+"&user_id="+user_id;
				String sign = encryption(jiami_param+miyao);
				
				param += "&sign=" + sign;
				
				url += param;
				myHttp dt = new myHttp();
				if (dt.getUrlSynTry(url, 1)) {
					byte[] content = dt.getContent();
					String contStr = new String(content, "utf-8");
					info = contStr;
					//成功：200 失败：1008
					String code = contStr.substring(contStr.indexOf("code")+6, contStr.indexOf(",", contStr.indexOf("code")+6)).trim();
					//成功："ok" 失败:"账号已锁定"
					String message = contStr.substring(contStr.indexOf("message")+9, contStr.indexOf(",", contStr.indexOf("message")+9)).trim();
					//成功："123456" 失败：null
					String data = contStr.substring(contStr.indexOf("data")+6, contStr.indexOf(",", contStr.indexOf("data")+6)).trim();
					//1554120877
					String timestamp = contStr.substring(contStr.indexOf("timestamp")+11, contStr.indexOf("}", contStr.indexOf("timestamp")+11)).trim();

					if (message.contains("ok") && !data.equals("null")) {
						//成拿到验证码
						String smstext = contStr.trim();
						sms.setSmstext(smstext);
						//这里是我们主动取短信而不是平台回调,所以回传短信和提取短信都同时做了,后面getSmsCheckCode是为了更新fech_time字段
						sms_checkcodedao.XXFCode1(smstext, seqid);
						sms_checkcodedao.devGetSmsCheckCode(seqid);
						jsoRet.put("mobile", mobile);
						jsoRet.put("smstext", data.replace("\"", ""));
						DouYinDao.SmsLog_4(mobile,data ,"seqid:"+seqid+"|adv_id:"+adv_id+"|dev_tag:"+dev_tag+"|取验证码成功:"+info, "", "",  new Date());
					} else {
						//socket通讯成功，没有验证码
//						err.setErr_code(error_result.dev_getsms_text_error);
//						err.setErr_info(contStr);
						sms_checkcodedao.XXFCode2(contStr, seqid);
						DouYinDao.SmsLog_5(mobile,"" ,"seqid:"+seqid+"|adv_id:"+adv_id+"|dev_tag:"+dev_tag+"|取手机号通讯成功，但没拿到手机号:"+info, "", "",  new Date());
					}
				} else {
					//socket通讯失败
//					err.setErr_code(error_result.server_http_error);
//					err.setErr_info("dt.getUrlSynTry,url=" + url + ":dt.respcode:" + dt.getResponCode());
					DouYinDao.SmsLog_6(mobile,"" ,"seqid:"+seqid+"|adv_id:"+adv_id+"|dev_tag:"+dev_tag+"|取验证码通讯失败:"+info, "", "",  new Date());
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
		
		String aString = new SMSplatform_XiaoXuanFeng().encryption("business_id=12&city=&isp=0&nonce_str=1558066386640686&province=&user_id=10187");
		System.out.println(aString);
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
	
	public String encryption(String plainText) {
		String re_md5 = new String();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();

			int i;

			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}

			re_md5 = buf.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return re_md5;
	}
	
	
}
