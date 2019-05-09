package com.kilotrees.serverbean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 动态生成任务的业务参数信息
 * 处理脚本通信基础类,具体业务bean类继续此类
 * 这里的短信暂时用易接的接口
 * @author Administrator
 *
 */
public abstract class ServerBeanBase implements IntfcServerBean{
	//int adv_id;
	//protected static Logger log = Logger.getLogger(serverbean.class);
	protected JSONObject jsoRequest;
	protected String serverbeanid;
	
	protected ServerBeanBase()
	{
		//adv_id = advid;
	}
	public abstract void handleTaskParasm(JSONObject jsoTask) throws JSONException;
	public abstract void handleTaskResport(JSONObject _jsoResponse) throws JSONException;
	
	public abstract void init(int adv_id);
	public abstract void refresh(int adv_id);
	
	public static IntfcServerBean getServerBean(String id)
	{
		if("yima".equals(id) || SMSplatform_yima.class.getName().equals(id)) {
			return SMSplatform_yima.getInstance();
		}else if("lianzhong".equals(id) || piccode_lianzhong.class.getName().equals(id)) {
			return piccode_lianzhong.getInstance();
		}else if("httplinks_1".equals(id) || httplinks_1.class.getName().equals(id)) {
			return httplinks_1.getInstance();
		}else if("XiaoXuanFeng".equals(id) || SMSplatform_XiaoXuanFeng.class.getName().equals(id)) {
			return SMSplatform_XiaoXuanFeng.getInstance();
		}
		return null;
	}
	
	public static void main(String[] argv)
	{
		System.out.println(ServerBeanBase.class.getName());
	}


}
