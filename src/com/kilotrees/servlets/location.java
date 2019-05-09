package com.kilotrees.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.kilotrees.services.gpsload_service;

/**
 * Servlet implementation class location
 */
@WebServlet("/location")
public class location extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public location() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		doPost(request,response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//doGet(request, response);
		int adv_id = Integer.parseInt(request.getParameter("adv_id"));
		String ip = request.getParameter("ip");
		String lon = request.getParameter("lon");
		String lat = request.getParameter("lat");
		double dlon = Double.parseDouble(lon);
		double dlat = Double.parseDouble(lat);
		JSONObject ret = gpsload_service.getInstance().randPoints_1(adv_id, ip, dlon, dlat);
		response.getWriter().println(ret.toString());
	}

}
