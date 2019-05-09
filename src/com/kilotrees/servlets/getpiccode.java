package com.kilotrees.servlets;

import java.io.DataInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.services.BeanService;
import com.kilotrees.util.StringUtil;

/**
 * Servlet implementation class getpiccode
 */
@WebServlet("/getpiccode")
public class getpiccode extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public getpiccode() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// response.getWriter().append("Served at: ").append(request.getContextPath());
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// doGet(request, response);
		JSONObject jsoRequest = new JSONObject();
		// JSONObject jsoSms = new JSONObject();
		String content_len = request.getHeader("Content-Length");
		int picDataLen = Integer.parseInt(content_len);
		byte[] picData = new byte[picDataLen];
		DataInputStream ins = new DataInputStream(request.getInputStream());
		ins.readFully(picData);
		// byte[] buf = new byte[100];
		// int len = ins.read(buf);

		// System.out.println(buf);
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		String autoid = request.getParameter("autoid");
		String adv_id = request.getParameter("adv_id");
		String dev_tag = request.getParameter("dev_tag");
		int itype = 0;
		String type = request.getParameter("type");
		if (StringUtil.isStringEmpty(type) == false)
			itype = Integer.parseInt(type);

		String serverbeanid = request.getParameter("serverbeanid");
		if (serverbeanid == null)
			serverbeanid = "lianzhong";
		try {
			jsoRequest.put("adv_id", adv_id);
			jsoRequest.put("dev_tag", dev_tag);
			jsoRequest.put("type", itype);
			jsoRequest.put("autoid", Long.parseLong(autoid));
			jsoRequest.put("serverbeanid", serverbeanid);

			// ins.readFully(picData);
			JSONObject result = BeanService.getInstance().handleBeanReqeust(jsoRequest, picData);
			String str_result = result.toString();
			response.getWriter().print(str_result);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
