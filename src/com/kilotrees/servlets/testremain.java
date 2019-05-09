package com.kilotrees.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kilotrees.dao.testremaindao;

/**
 * Servlet implementation class testremain
 */
@WebServlet("/testremain")
public class testremain extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public testremain() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//String url = "http://192.168.3.68:9090/testremain?" + "imei=" + imei + "&" + "mac=" + mac + "&" + "imsi=" + imsi
		//+ "&" + "mode=" + phoneType + "&" + "sdkversion=" + Build.VERSION.SDK_INT + "&"
		//+ "opencount=" + open_count + "&" + "autoid=" + autoid;
		String imei = request.getParameter("imei");
		String mac = request.getParameter("mac");
		String imsi = request.getParameter("imsi");
		String mode = request.getParameter("mode");
		int sdkversion = Integer.valueOf(request.getParameter("sdkversion"));
		int opencount = Integer.valueOf(request.getParameter("opencount"));
		int autoid = Integer.valueOf(request.getParameter("autoid"));
		
		testremaindao.addTestRemain(imei, mac, imsi, mode, sdkversion, opencount, autoid);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
