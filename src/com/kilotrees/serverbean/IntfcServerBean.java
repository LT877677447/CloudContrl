package com.kilotrees.serverbean;

import org.json.JSONObject;

public interface IntfcServerBean {
	//content表示上传的http body的二进制流，比如图片
	public JSONObject handleBeanReqeust(JSONObject _jsoRequest,byte[] content);
	
}
