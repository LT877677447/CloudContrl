/**
 * @author Administrator
 * 2019年5月7日 下午3:27:15 
 */
package com.kilotrees.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.kilotrees.dao.connectionmgr;

@WebServlet("/WeixinHaiWai")
public class WeixinHaiWai extends HttpServlet {
	private static final long serialVersionUID = 1982992235639120340L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String[] account = getAccount();
		JSONObject json = new JSONObject();
		try {
			json.put("phoneNumber", account[0]);
			json.put("password", account[1]);
			json.put("url", account[2]);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		byte[] bs = json.toString().getBytes("utf-8");
		
		resp.setContentLength(bs.length);
		resp.getOutputStream().write(bs);
		resp.getOutputStream().flush();
	}

	public static synchronized String[] getAccount() {
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		String phoneNumber = "";
		String password = "";
		String url = "";
		try {
			con.setAutoCommit(false);

			String sql = "select top 1 * from [yun].[dbo].[tb_WX_haiwai_Account] where 1=1\r\n" + "  and registed = 0" + "  and fetched = 0" + "  and effective = 0"
					+ "order by fetchTime asc";
			String update = "update [yun].[dbo].[tb_WX_haiwai_Account] set fetched=1,fetchTime=getDate() where phoneNumber = ?";
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
//			ps.close();
			while (rs.next()) {
				phoneNumber = rs.getString("phoneNumber");
				password = rs.getString("password");
				url = rs.getString("url");
			}
			ps.close();
			ps = con.prepareStatement(update);
			ps.setString(1, phoneNumber);
			ps.executeUpdate();

			con.commit();
			con.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return new String[] {phoneNumber,password,url};
	}

}
