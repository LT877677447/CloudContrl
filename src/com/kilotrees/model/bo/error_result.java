package com.kilotrees.model.bo;

import org.json.JSONException;
import org.json.JSONObject;

public class error_result {
	public static final int success = 0;
	public static final int System_exception_error = -1;
	public static final int System_sql = -2;
	public static final int orderid_same_as_last = -3;
	public static final int reportid_same_as_last = -4;
	public static final int dev_request_params_error = -5;
	public static final int server_http_error = -6;
	public static final int server_init_busy = -7;
	public static final int device_not_exist = -10;
	public static final int device_regist_alreadyexist = -11;
	public static final int dev_getsms_mobile_error = -12;
	public static final int dev_getsms_text_error = -13;
	public static final int dev_sms_login_error = -14;
	public static final int dev_sendsms_error = -15;
	public static final int get_piccode_error = -16;

	private int err_code;
	private String err_info = "";

	public int getErr_code() {
		return err_code;
	}

	public void setErr_code(int err_code) {
		this.err_code = err_code;
	}

	public String getErr_info() {
		return err_info;
	}

	public void setErr_info(String err_info) {
		this.err_info = err_info;
	}

	public JSONObject toJSONObject() throws JSONException {
		JSONObject jso = new JSONObject();
		jso.put("err_code", err_code);
		jso.put("err_info", err_info);
		return jso;
	}
}
