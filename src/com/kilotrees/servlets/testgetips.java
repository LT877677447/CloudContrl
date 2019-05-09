package com.kilotrees.servlets;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet implementation class testgetips
 */
@WebServlet("/testgetips")
public class testgetips extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public testgetips() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// response.getWriter().append("Served at:
		// ").append(request.getContextPath());
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String type = request.getParameter("type");
		int count = Integer.parseInt(request.getParameter("count"));
		String s = "";
		try {
			if (type.equals("1")) {
				// json
				JSONObject jso = new JSONObject();
				jso.put("success", true);
				JSONArray jsoArray = new JSONArray();
				for(int i = 0; i < count; i++)
				{
					String ip = "192.168.3." + (i+1);
					JSONObject item = new JSONObject();
					item.put("ip", ip);
					item.put("port", 9090);
					jsoArray.put(item);
				}
				jso.put("data", jsoArray);
				s = jso.toString();
				
			} else if (type.equals("2")) {
				// 分行
				s += "\r\n\r\n";
				for(int i = 0; i < count; i++)
				{
					String ip = "192.168.1." + (i+1);
					s += ip;
					s += ":9090";
					s += "\r\n";
				}
				s += "\r\n";
			}
			response.getWriter().println(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static void parseLine(String s, ArrayList<String> list)
	{
		String[] ips;
		if(s.indexOf("\r\n") >= 0)
			ips = s.split("\r\n");
		else
			ips = s.split("\n");
		for(String ip : ips)
		{
			ip = ip.trim();
			if(ip.length() > 10 && ip.indexOf('.') > 0)
			{
				list.add(ip);
			}
		}
	}
	
	public static void main(String[] argv)
	{
		String s = "\r\n\r\n 192.168.3.1:9090\r\n192.168.3.2:8080\r\n\r\n";
		 ArrayList<String> list = new  ArrayList<String>();
		 parseLine(s,list);
		 for(String ss : list)
			 System.out.println(ss);
	}
}
