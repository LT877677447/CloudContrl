package com.kilotrees.servlets;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.model.bo.error_result;
import com.kilotrees.services.main_service;
import com.kilotrees.util.GZIPUtil;
import com.kilotrees.util.StringUtil;

public class servlet_proc {

	public static JSONObject system_busy = null;

	static {
		try {
			system_busy = new JSONObject();
			error_result err = new error_result();
			err.setErr_code(error_result.server_init_busy);
			err.setErr_info("system busy");
			system_busy.put("err_result", err.toJSONObject());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static JSONObject proc_request(HttpServletRequest request) throws ServletException, IOException, JSONException {
		if (!main_service.getInstance().isSystem_ready()) {
			return system_busy;
		}
		int content_len = request.getContentLength();
		if (content_len > 0) {
			byte[] buffer = new byte[content_len];
			DataInputStream dataInputStream = new DataInputStream(request.getInputStream());
			dataInputStream.readFully(buffer);

			// check if request already gzip, for compatible
			String encoding = request.getHeader("Content-Encoding");
			if (!StringUtil.isStringEmpty(encoding) && encoding.contains("gzip")) {
				buffer = GZIPUtil.uncompress(buffer);
			}

			JSONObject json = new JSONObject(new String(buffer, "utf-8"));
			return json;
		}
		return null;
	}

	public static void proc_response(HttpServletRequest request, HttpServletResponse response, JSONObject result) throws ServletException, IOException, JSONException {
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");

		byte[] buffer = result.toString().getBytes("utf-8");

		// check if request already gzip, for compatible
		String encoding = request.getHeader("Content-Encoding");
		if (!StringUtil.isStringEmpty(encoding) && encoding.contains("gzip")) {
			buffer = GZIPUtil.compress(buffer);
			response.setHeader("Content-Encoding", "gzip");
		}

		response.setContentLength(buffer.length);

		OutputStream outputStream = response.getOutputStream();
		outputStream.write(buffer);
		outputStream.flush();

	}
}
