package com.kilotrees.services;

import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kilotrees.dao.requesttimedao;
import com.kilotrees.model.bo.error_result;

public class requesttime_service {
	private static Logger log = Logger.getLogger(requesttime_service.class);
	
	private static requesttime_service inst;
	private requesttime_service()
	{
		
	}
	
	public static requesttime_service getInstance()
	{
		synchronized(requesttime_service.class){
			if(inst == null)
			{
				inst = new requesttime_service();			
			}
		}
		return inst;
	}
	
	public JSONObject insertRequestTime(JSONObject request) {
		JSONObject response = new JSONObject();
		error_result er = new error_result();
		boolean isInActList = false;
		String orderid = request.optString("orderid");
		log.info("请求的orderid:" + orderid);
		requesttimedao.insert(orderid, new Date());
		return new JSONObject();
	}
	
	
}
