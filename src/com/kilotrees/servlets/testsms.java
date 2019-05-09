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

import com.kilotrees.services.BeanService;
import com.kilotrees.util.StringUtil;

/**
 * Servlet implementation class testsms
 */
@WebServlet("/testsms")
public class testsms extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(testsms.class);

	public testsms() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String uri = request.getRequestURI();
		
		JSONObject jsoRequest = new JSONObject();
		JSONObject jsoSms = new JSONObject();
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		
		String sStep = request.getParameter("step");
		String adv_id = request.getParameter("adv_id");
		String dev_tag = request.getParameter("dev_tag");
		String itemid = request.getParameter("itemid");
		String serverbeanid = request.getParameter("serverbeanid");
		if(StringUtil.isStringEmpty(serverbeanid)) {
			serverbeanid = "yima";
		}
		int step = Integer.parseInt(sStep);
		try {
			jsoRequest.put("adv_id", adv_id);
			jsoRequest.put("dev_tag", dev_tag);
			jsoRequest.put("dosms", jsoSms);
			jsoRequest.put("serverbeanid", serverbeanid);
			jsoSms.put("itemid", itemid);
			
			jsoSms.put("excludeno", "170.171.172");
			if (step == 1) {
				// 取手机号
				jsoSms.put("smstype", "getmobile");
				
				String province = request.getParameter("province");
				if(!StringUtil.isStringEmpty(province)) {
					jsoSms.put("province", province);
				}else {
					jsoSms.put("province", "");
				}
				String city = request.getParameter("city");
				if(!StringUtil.isStringEmpty(city)) {
					jsoSms.put("city", city);
				}else {
					jsoSms.put("city", "");
				}
				
			} else if (step == 2) {
				String seqid = request.getParameter("seqid");
				String mobile = request.getParameter("mobile");
				//2019-1-12 有些广告需要取2次验证码，这时第一次不能释放手机号，第二次才释放，客户端带上release参数，第一次release=0，第二次release=1
				String release = request.getParameter("release");
				if(StringUtil.isStringEmpty(release) == false) {
					jsoSms.put("release", Integer.parseInt(release));
				}
//				if("0".equals(release))
//					jsoSms.put("release", 0);
				
				// 取验证码
				jsoSms.put("smstype", "getsms");
				jsoSms.put("mobile", mobile);
				jsoSms.put("seqid", seqid);
			}
			JSONObject result = BeanService.getInstance().handleBeanReqeust(jsoRequest,null);
			String str_result = result.toString();
			//解决跨域
			response.setHeader("Access-Control-Allow-Origin","*");
			response.getWriter().print(str_result);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
