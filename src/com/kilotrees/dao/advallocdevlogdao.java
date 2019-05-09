package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.advallocdevlog;
import com.kilotrees.services.ErrorLog_service;

public class advallocdevlogdao {
	private static Logger log = Logger.getLogger(advallocdevlogdao.class);
	
	public synchronized static void addAllocLog(advallocdevlog alloclog)
	{	
		String sql = "insert into " + advallocdevlog.tablename;
		sql += " values(?,?,?,?,?)";		
		//System.out.println(sql);
		Connection con = connectionmgr.getInstance().getConnection();		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setInt(1,alloclog.getAdvid());
			ps.setString(2, alloclog.getDev_tag());
			ps.setInt(3, alloclog.getAlloc_type());
			ps.setInt(4, alloclog.getFree());
			ps.setTimestamp(5, new java.sql.Timestamp(alloclog.getAlloc_time().getTime()));
			//ps.get
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
