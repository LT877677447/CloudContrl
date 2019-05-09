package com.kilotrees.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.dao.phonetypedao;
import com.kilotrees.model.po.phonetype;
import com.kilotrees.services.phonetype_service;
import com.kilotrees.util.StringUtil;

/**
 * Servlet implementation class GetRandomPhoneInfo
 */
@WebServlet("/GetRandomPhoneInfo")
public class GetRandomPhoneInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public GetRandomPhoneInfo() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			JSONObject json = servlet_proc.proc_request(request);
			JSONObject result = null;
			if(json == servlet_proc.system_busy) {
				result = json;
			} else {
				result = responseResult(request, response);
			}
			servlet_proc.proc_response(request,response, result);			
		} catch (JSONException e) {	
			e.printStackTrace();
			response.getWriter().println(e.getMessage());
		}
	}
	
	
	private JSONObject responseResult(HttpServletRequest request, HttpServletResponse response) throws JSONException {
		JSONObject result = new JSONObject();
		
		JSONObject phoneInfo = null;
		
		String tableName = request.getParameter("table");
		
		if (!StringUtil.isStringEmpty(tableName)) {
			List<phonetype> ls = phonetypedao.getPhoneInfoList(tableName);
			
			java.util.Random rand = new java.util.Random();
			int index = rand.nextInt(ls.size());
			phonetype phonetype = ls.get(index);
			
			phoneInfo = new JSONObject(phonetype.getPhone_info());
		}
		
		if (phoneInfo == null) {
			phoneInfo = phonetype_service.getInstance().randPhoneInfo();
			// remove all 'Build.' prefix keys
			
//			String[] names = JSONObject.getNames(result);
//			for(int i = 0; i < names.length; i++) {
//				String key = names[i];
//				if (key.startsWith("Build.")) {
//					result.remove(key);
//				}
//			}
		}
		
		result.put("phoneInfo", phoneInfo);
		
		return result;
		
	}

}
