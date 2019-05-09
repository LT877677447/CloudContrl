package com.kilotrees.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.services.adtaskdispath_center;

/**
 * Servlet implementation class taskcenter
 */
@WebServlet("/taskcenter")
public class taskcenter extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public taskcenter() {
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
			JSONObject result;
			if(json == servlet_proc.system_busy)
				result = json;
			else
				result =  adtaskdispath_center.getInstance().handleTaskRequest(json);
			servlet_proc.proc_response(request,response, result);
		} catch (JSONException e) {
			e.printStackTrace();
			response.getWriter().println(e.getMessage());
		}

	}

}
