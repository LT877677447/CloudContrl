package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.devpostmsg;
import com.kilotrees.util.LogUtil;

public class devpostmsgdao {
private static Logger log = Logger.getLogger(devpostmsgdao.class);
	
	public synchronized static void addPostMsgLog(devpostmsg msg)
	{		
		String sql = "insert into " + devpostmsg.tablename;
		sql += " values(?,?,?)";		
		//System.out.println(sql);
		Connection con = connectionmgr.getInstance().getConnection();		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setString(1,msg.getDev_tag());
			ps.setString(2, msg.getMessage());					
			ps.setTimestamp(3, new Timestamp(msg.getPosttime().getTime()));
			//ps.get
			ps.execute();
		}catch(Exception e)
		{
			log.error(e.getMessage(),e);
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
	}
	
	public synchronized static void system_errlog(String slog)
	{		
		String sql = "insert into " + devpostmsg.tablename;
		sql += " values(?,?,?)";		
		Connection con = connectionmgr.getInstance().getConnection();		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setString(1,"Log_String");
			ps.setString(2, slog);					
			ps.setTimestamp(3, new Timestamp(new Date().getTime()));
			ps.execute();
		}catch(Exception e)
		{
			log.error(e.getMessage(),e);
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
	}
	
	public synchronized static void system_errlog(Exception exception)
	{		
		String sql = "insert into " + devpostmsg.tablename;
		sql += " values(?,?,?)";		
		Connection con = connectionmgr.getInstance().getConnection();		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setString(1,"Log_StackTrace");
			ps.setString(2, LogUtil.log(exception));					
			ps.setTimestamp(3, new Timestamp(new Date().getTime()));
			ps.execute();
		}catch(Exception e)
		{
			//ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(),e);
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
	}
	
	
}
