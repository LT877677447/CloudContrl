package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.advgroup;
import com.kilotrees.services.ErrorLog_service;

public class advgroupdao {
	private static Logger log = Logger.getLogger(advgroupdao.class);
	
	public static synchronized ArrayList<advgroup> getAdvGroupList()
	{
		ArrayList<advgroup> list = new ArrayList<advgroup>();
		String sql = "select * from " + advgroup.tablename + " where onlineflag > 0 and LEN(advids) > 0 order by groupid";
		Connection con = connectionmgr.getInstance().getConnection();
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			//String todayStr = DateUtil.getDateBeginString(new java.util.Date());
			//ps.setString(1, todayStr);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				advgroup g = new advgroup();
				g.setGroupid(rs.getInt("groupid"));
				g.setName(rs.getString("name"));
				g.setAdvids(rs.getString("advids"));
				g.setType(rs.getInt("type"));
				g.setMaxdotime(rs.getInt("maxdotime"));
				java.util.Date d = new java.util.Date(rs.getTimestamp("modify_time").getTime());
				g.setModify_time(d);
				g.setOnlineflag(rs.getInt("onlineflag"));
				list.add(g);
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
				
		return list;
	}
	
	public static void updateOnlineFlag(advgroup g)
	{
		String sql = "update " + advgroup.tablename + " set onlineflag=?"
				+ " where groupid=?";
		Connection con = connectionmgr.getInstance().getConnection();
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setInt(1, g.getOnlineflag());
			ps.setInt(2, g.getGroupid());
			ps.execute();
		}catch(Exception e)
		{
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(),e);
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
	}
	
	public static void updateAdvs(advgroup g)
	{
		String sql = "update " + advgroup.tablename + " set advids=?"
				+ " where groupid=?";
		Connection con = connectionmgr.getInstance().getConnection();
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setString(1, g.getAdvids());
			ps.setInt(2, g.getGroupid());
			ps.execute();
		}catch(Exception e)
		{
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(),e);
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
	}
	
}
