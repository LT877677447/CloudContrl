package com.kilotrees.services;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.model.bo.error_result;
import com.kilotrees.serverbean.ServerBeanBase;
/**
 * 某些广告业务比较复杂时，客户端脚本在工作过程中还要和服务器交互才能完成，
 * 当前adtaskdispath_center,advnewtask_service等等都是管理和调度的工作，没有真实涉及业务内容
 * 这种情况下服务器增加一个动态加载业务组件客户端的广告脚本交互，处理客户端脚本请求．
 * @author Administrator
 *
 */
public class BeanService {
	private static Logger log = Logger.getLogger(BeanService.class);
	private static BeanService inst;
	
	private BeanService()
	{
		
	}
	public static BeanService getInstance()
	{
		synchronized(BeanService.class){
			if (inst == null) {
				inst = new BeanService();
			}
		}
		return inst;
	}
	
	public JSONObject handleBeanReqeust(JSONObject request,byte[] content)
	{
		JSONObject response = new JSONObject();
		error_result err = new error_result();
		String dev_tag = request.optString("dev_tag");
		int adv_id = request.optInt("adv_id");
		ServerBeanBase sbean = null;
		if(adv_id <= 0)
		{
			try {
			err.setErr_code(error_result.dev_request_params_error);
			err.setErr_info("广告id错误:" + dev_tag + ";adv_id=" + adv_id);
			response.put("err_result", err.toJSONObject());
			}
			catch (JSONException e) {
				ErrorLog_service.system_errlog(e);
				log.error(e.getMessage(), e);
			}
			return response;			
		}
//		sbean = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo().getServerBean();
//		if(sbean != null)
//			response = sbean.handleBeanReqeust(request);
//		else
//			log.error("get serverbean error");
		response = ServerBeanBase.getServerBean(request.optString("serverbeanid")).handleBeanReqeust(request, content);
		return response;
	}
}
