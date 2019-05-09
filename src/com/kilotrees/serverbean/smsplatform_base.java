package com.kilotrees.serverbean;

import org.json.JSONObject;

/**
 * 短信验证码或发送对接平台,客户端发送时生成一个dosms的json请求包,并指明使用的平台id，服务器接收到请求后把任务交给对应平台处理
 * dosms的json格式:dotype,platformid,step,itemid,mobile,seqid
 * @author Administrator
 *
 */
public abstract class smsplatform_base extends ServerBeanBase {
	//平台id,计划是用动态加载，考虑到这类平台只有2-3个，直接硬编码就行了
	protected String platfromId = "";
	
	public abstract JSONObject doSms(JSONObject smsJso);
	
	/**
	 * 硬编码
	 * @param platforId
	 * @return
	 */
//	public static smsplatform_base getPlatform(String platforId)
//	{
////		if("yima".equals(platforId))
////			return smsplatform_yima.getInstance();
////		else
//			return null;
//	}
	
}
