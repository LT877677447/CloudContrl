package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.advhttplink;
import com.kilotrees.model.po.tb_httpvisitrule;
import com.kilotrees.services.ErrorLog_service;

public class advhttplinkdao {
	private static Logger log = Logger.getLogger(advhttplinkdao.class);
	public static ArrayList<advhttplink> getLinkedList(int adv_id)
	{
		ArrayList<advhttplink> ls = new ArrayList<advhttplink>();
		String sql = "select * from " + advhttplink.tablename + " where adv_id = ?";
		Connection con = connectionmgr.getInstance().getConnection();
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setInt(1, adv_id);
			//String todayStr = DateUtil.getDateBeginString(new java.util.Date());
			//ps.setString(1, todayStr);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				advhttplink al = new advhttplink();
				al.setAdv_id(adv_id);
				al.setUrl_id(rs.getInt("url_id"));
				al.setUrl_value(rs.getString("url_value"));
				al.setChildens_id(rs.getString("childens_id"));
				al.setHttptimeout(rs.getInt("http_timeout"));
				al.setExt(rs.getString("ext"));
				ls.add(al);
			}
			rs.close();
		}catch(Exception e)
		{
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(),e);
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
		return ls;
	}
	
	public static String getHttpTreeRule(int maxdeep)
	{
		String rule = "30;35;20;10;5";//5层深度
		
		//String table = "tb_httpvisitrule";		
		String sql = "select * from " + tb_httpvisitrule.tablename + " where deep = ?";
		Connection con = connectionmgr.getInstance().getConnection();
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setInt(1, maxdeep);
			//String todayStr = DateUtil.getDateBeginString(new java.util.Date());
			//ps.setString(1, todayStr);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				rule = rs.getString("visitor_rule");
			}
			rs.close();
		}catch(Exception e)
		{
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(),e);
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
		return rule;
	}
}
