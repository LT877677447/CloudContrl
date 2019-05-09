package com.kilotrees.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.kilotrees.serverbean.httplinktree;

/**
 * Servlet implementation class testlinktree
 */
@WebServlet("/testlinktree")
public class testlinktree extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static httplinktree tree;
       
    public testlinktree() {
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
		String advid = request.getParameter("adv_id");
		int adv_id = Integer.parseInt(advid);
		if(tree == null)
			tree = new httplinktree(adv_id);
		
		try {
			response.getWriter().println(tree.getLinksRandom().toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
	}

}
