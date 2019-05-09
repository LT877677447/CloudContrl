package com.kilotrees.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kilotrees.services.ErrorLog_service;


@WebServlet("/testErrorLog")
public class testErrorLog extends HttpServlet{
	private static final long serialVersionUID = 1L; 

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	
	

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String error = "12a";
			Integer a = Integer.parseInt(error);
		} catch (NumberFormatException e) {
			ErrorLog_service.system_errlog(e);
		}
		
		
	}

	
	
	
	
	
	
}
