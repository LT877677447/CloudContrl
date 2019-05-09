package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.sourceurls;
import com.kilotrees.services.ErrorLog_service;

public class sourceurlsdao {
	private static Logger log = Logger.getLogger(sourceurlsdao.class);
	/**
	 * 按广告类别，指定获取条数
	 */
	public static ArrayList<String> getUrlsRandom(int type,int count)
	{
		ArrayList<String> ls = new ArrayList<String>();
		if(count <= 0)
			count = 100;
		
		String sql = "select top " + count + " url from " + sourceurls.tablename;
		if(type >= 0)
			sql += " where type=" + type;
		sql += " order by newid()";
		Connection con = connectionmgr.getInstance().getConnection();
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				ls.add(rs.getString("url"));
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
	//获取其它类别的广告
	public static ArrayList<String> getUrlsOtherType(int type,int count)
	{
		ArrayList<String> ls = new ArrayList<String>();
		if(count <= 0)
			count = 100;
		
		String sql = "select top " + count + " url from " + sourceurls.tablename;
		if(type >= 0)
			sql += " where type<>" + type;
		sql += " order by newid()";
		Connection con = connectionmgr.getInstance().getConnection();
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				ls.add(rs.getString("url"));
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
}
