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

import com.kilotrees.services.phonetype_service;

/**
 * Servlet implementation class addphoneinfo
 */
@WebServlet("/addphoneinfo")
public class addphoneinfo extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Logger log = Logger.getLogger(HttpServlet.class);
       
    public addphoneinfo() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request,response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			JSONObject json = servlet_proc.proc_request(request);
			JSONObject result;
			if(json == servlet_proc.system_busy) {
				result = json;
			} else {
				/*
				String timeString = new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
				String phone_type = json.optString("Build.MODEL");
				String fileName = "D:\\WebServer\\" + "PhoneInfos\\" + phone_type + "\\" + phone_type + "-" + timeString + ".json";
				File file = new File(fileName);
				File fileParent = file.getParentFile();
				
				if (!fileParent.exists()) {
					fileParent.mkdirs();
				}
				
				FileWriter fileWriter = new FileWriter(file, false);
				fileWriter.write(json.toString());
				fileWriter.flush();
				fileWriter.close();
				*/
				String tableName = request.getParameter("table");
				if (tableName == null) {
					result = phonetype_service.getInstance().registPhoneType(json);
				} else {
					result = phonetype_service.getInstance().registPhoneType(json, tableName);
				}
			}
			servlet_proc.proc_response(request,response, result);			
		} catch (JSONException e) {	
			e.printStackTrace();
			response.getWriter().println(e.getMessage());
		}		
	}

}
