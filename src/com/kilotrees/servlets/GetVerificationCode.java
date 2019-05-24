package com.kilotrees.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.util.FileUtil;

@WebServlet("/GetVerificationCode")
public class GetVerificationCode extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public GetVerificationCode() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			JSONObject json = servlet_proc.proc_request(request);
			JSONObject result = null;
			if (json == servlet_proc.system_busy) {
				result = json;
			} else {
				result = responseResult(request, response);
			}
			servlet_proc.proc_response(request, response, result);
		} catch (JSONException e) {
			e.printStackTrace();
			response.getWriter().println(e.getMessage());
		}
	}

	private JSONObject responseResult(HttpServletRequest request, HttpServletResponse response) throws JSONException {
		JSONObject result = new JSONObject();

		String account = request.getHeader("account");
		if (account == null) {
			account = request.getParameter("account");
		}

		String contents = FileUtil.readTextFile("D:\\WebServer\\temp\\verifaction_code.json");
		JSONObject jsonObject = new JSONObject(contents);
		if (jsonObject.has(account)) {
			String code = jsonObject.optString(account);
			result.put("验证码", code);
		}

		return result;
	}

}
