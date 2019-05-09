package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.advremainlog;
import com.kilotrees.services.ErrorLog_service;

public class advremainlogdao {
	private static Logger log = Logger.getLogger(advremainlogdao.class);
	public synchronized static void addNewRemainLog(advremainlog alog)
	{		
		String sql = "insert into " + advremainlog.getCurTableName();
		sql += " values(?,?,?,?,?,?,?,?,?,?)";		
		Connection con = connectionmgr.getInstance().getConnection();		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setLong(1, alog.getAutoid());
			ps.setInt(2,alog.getAdv_id());			
			ps.setString(3, alog.getDev_tag());
			ps.setInt(4, alog.getVpnid());
			ps.setInt(5,alog.getStep());
			ps.setInt(6, alog.getResult());
			ps.setString(7, alog.getLoginfo());
			ps.setTimestamp(8, new java.sql.Timestamp(alog.getLogtime().getTime()));	
			ps.setString(9, alog.getIp());
			ps.setString(10, alog.getArea());
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
	public static void test()
	{
		advremainlog log = new advremainlog();
		log.setAdv_id(1);
		log.setDev_tag("dev_tag1");
		log.setLoginfo("loginfo1");
		log.setLogtime(new Date());
		log.setResult(0);
		log.setStep(1);
		log.setVpnid(3);
		addNewRemainLog(log);
	}
}
