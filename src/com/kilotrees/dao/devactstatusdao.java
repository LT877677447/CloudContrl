package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.devactstatus;
import com.kilotrees.services.ErrorLog_service;

public class devactstatusdao {
	private static Logger log = Logger.getLogger(devactstatusdao.class);

	public static synchronized devactstatus getDevActStatus(String dev_tag) {
		devactstatus das = null;
		String sql = "select * from " + devactstatus.tablename + " where dev_tag=?";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, dev_tag);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				das = new devactstatus();
				das.setActsid(rs.getInt("actsid"));
				das.setDev_tag(rs.getString("dev_tag"));
				if (rs.getTimestamp("lastlogintime") != null) {
					java.util.Date d = new java.util.Date(rs.getTimestamp("lastlogintime").getTime());
					das.setLastlogintime(d);
				}
				if (rs.getTimestamp("lastfetchtasktime") != null) {
					java.util.Date d = new java.util.Date(rs.getTimestamp("lastfetchtasktime").getTime());
					das.setLastfetchtasktime(d);
				}
				das.setOnline(rs.getInt("online"));
				das.setStatus(rs.getString("status"));
			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return das;
	}
	
	public static synchronized void addNewDevActStatus(devactstatus devstatus){
		String sql = "insert " + devactstatus.tablename + " values(?,?,?,?,?)";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, devstatus.getDev_tag());
			if(devstatus.getLastlogintime() == null)
				ps.setTimestamp(2, null);
			else{
				java.sql.Timestamp t = new java.sql.Timestamp(devstatus.getLastlogintime().getTime());
				ps.setTimestamp(2, t);
			}
			if(devstatus.getLastfetchtasktime() == null)
				ps.setTimestamp(3, null);
			else{
				java.sql.Timestamp t = new java.sql.Timestamp(devstatus.getLastfetchtasktime().getTime());
				ps.setTimestamp(3, t);
			}
			ps.setInt(4, devstatus.getOnline());
			ps.setString(5, devstatus.getStatus());
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			if(rs.next())
				devstatus.setActsid(rs.getInt(1));
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	public static synchronized void updateLastLoginTime(devactstatus devstatus) {
		Connection con = connectionmgr.getInstance().getConnection();
		String sql_update = "update " + devactstatus.tablename + 
				" set lastlogintime = ?,online=?,status = ? where dev_tag=?";
		PreparedStatement ps = null;
		// boolean bfound = false;
		try {
			ps = con.prepareStatement(sql_update);
			java.sql.Timestamp t = new java.sql.Timestamp(devstatus.getLastlogintime().getTime());
			ps.setTimestamp(1, t);
			ps.setInt(2, 1);
			ps.setString(3, devstatus.getStatus());
			ps.setString(4,devstatus.getDev_tag());
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}
	
	public static synchronized void updateLastFetchTaskTime(devactstatus devstatus) {
		Connection con = connectionmgr.getInstance().getConnection();
		String sql_update = "update " + devactstatus.tablename +" set lastfetchtasktime = ? where dev_tag=?";
		PreparedStatement ps = null;
		// boolean bfound = false;
		try {
			ps = con.prepareStatement(sql_update);
			java.sql.Timestamp t = new java.sql.Timestamp(devstatus.getLastfetchtasktime().getTime());
			ps.setTimestamp(1, t);
			ps.setString(2,devstatus.getDev_tag());
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}
	
	public static synchronized void updateOnLine(devactstatus devstatus) {
		Connection con = connectionmgr.getInstance().getConnection();
		String sql_update = "update " + devactstatus.tablename +" set online = ?,status=? where dev_tag=?";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql_update);
			ps.setInt(1, devstatus.getOnline());
			ps.setString(2, devstatus.getStatus());
			ps.setString(3,devstatus.getDev_tag());
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}
	
	public static void test(int type)
	{
		devactstatus dev = new devactstatus();
		dev.setDev_tag("test1");
		if(type == 1)
		{
			dev.setLastlogintime(new Date());
			dev.setOnline(1);
			dev.setStatus("type1");
			addNewDevActStatus(dev);
		}
		else if(type == 2)
		{
			dev.setLastlogintime(new Date());
			dev.setOnline(1);
			dev.setStatus("2");
			updateLastLoginTime(dev);
			
		}
		else if(type == 3)
		{
			dev.setLastfetchtasktime(new Date());
			dev.setOnline(1);
			dev.setStatus("3");
			updateLastFetchTaskTime(dev);
			
		}
		else if(type == 4)
		{
			dev.setOnline(0);
			dev.setStatus("断线");
			updateOnLine(dev);
		}
		else if(type == 5)
		{
			dev = getDevActStatus("test1");
		}
	}
}
