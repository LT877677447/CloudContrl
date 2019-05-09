package com.kilotrees.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.services.qqaccount_service;

/**
 * 取QQ帐号密码或者返回登录状况 如果参数action=1，表示向服务器取QQ帐号密码，这时客户端返回这个广告未有使用的帐号密码。
 * 如果参数action=2，表示返回登录情况，如果登录失败，服务器要设置此帐号无效
 */
@WebServlet("/qqaccount")
public class qqaccount extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public qqaccount() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// response.getWriter().append("Served at:
		// ").append(request.getContextPath());
		String sAction = request.getParameter("action");
		int actiontype = Integer.parseInt(sAction);
		JSONObject jsoRequest = new JSONObject();
		JSONObject jsoResp = null;
		String adv_id = request.getParameter("adv_id");
		String sAutoid = request.getParameter("autoid");
		long autoid = 0;
		if(sAutoid != null)
			autoid = Long.parseLong(sAutoid);
		try {
			jsoRequest.put("action", actiontype);
			jsoRequest.put("adv_id", adv_id);
			jsoRequest.put("autoid", autoid);
			if (actiontype == 1) {
				String dev_tag = request.getParameter("dev_tag");
				jsoRequest.put("dev_tag", dev_tag);
			} else {
				String qqnum = request.getParameter("qqnum");
				String sResult = request.getParameter("result");
				int result = Integer.parseInt(sResult);
				jsoRequest.put("qqnum", qqnum);
				jsoRequest.put("result", result);
				jsoRequest.put("info", request.getParameter("info"));
			}
			jsoResp = qqaccount_service.getInstance().handleRequest(jsoRequest);
			String str_result = jsoResp.toString();
			response.getWriter().print(str_result);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new ServletException("jsonException:" + e.getMessage());
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
