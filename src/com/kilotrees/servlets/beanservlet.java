package com.kilotrees.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.services.BeanService;

/**
 *在客户脚本执行广告过程中处理客户端的请求，比如验证码，图片之类
 *如果有二进制数据，比如图片数据，用base64加密并放在picdata字段中
 */
@WebServlet("/beanservlet")
public class beanservlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * 
     */
    public beanservlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			JSONObject json = servlet_proc.proc_request(request);
			JSONObject result = BeanService.getInstance().handleBeanReqeust(json,null);
			servlet_proc.proc_response(request,response, result);			
		} catch (JSONException e) {
			e.printStackTrace();
			//response.getWriter().println(e.getMessage());
		}
	}
}
