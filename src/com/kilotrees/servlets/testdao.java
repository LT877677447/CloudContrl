package com.kilotrees.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kilotrees.dao.devactstatusdao;
import com.kilotrees.services.main_service;

/**
 * Servlet implementation class testdao
 */
@WebServlet("/testdao")
public class testdao extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public testdao() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//String s = advtaskinfodao.test();
		//response.getWriter().println(s);
		if(main_service.getInstance().isSystem_ready())
		{
			//useragentdao.loadUserAgentFromFiles();
			//httplinks_1.test();
			//response.getWriter().println("load files useragend end");
			int type = Integer.parseInt(request.getParameter("type"));
			devactstatusdao.test(type);
			try {
				//long maxid = autoid_service.getMaxAutoid();
				//long maxseq = autoid_service.getMaxSeqid();
//				JSONObject jso = new JSONObject();
//				jso.put("adv_id", 7);
//				server_bean sb = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(7).getAdvinfo().getServerBean();
//				if(sb != null)
//					sb.handleTaskParasm(jso);
//				response.getWriter().println(jso.toString());
				//response.getWriter().println("maxseq=" + maxseq);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
