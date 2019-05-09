package com.kilotrees.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.services.actdeviceinfo_service;

/**
 * Servlet implementation class devregist
 */
@WebServlet("/devregist")
public class devregist extends HttpServlet {
	private static Logger log = Logger.getLogger(devregist.class);
	private static final long serialVersionUID = 1L;
       
    public devregist() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request,response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//log.info();
		try {
			JSONObject json = servlet_proc.proc_request(request);
			JSONObject result;
			if(json == servlet_proc.system_busy)
				result = json;
			else
				result = actdeviceinfo_service.getInstance().devRegist(json);
			servlet_proc.proc_response(request,response, result);			
		} catch (JSONException e) {	
			e.printStackTrace();
			response.getWriter().println(e.getMessage());
		}		
	}

}
