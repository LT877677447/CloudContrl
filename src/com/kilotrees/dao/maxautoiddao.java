package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.maxautoid;
import com.kilotrees.services.ErrorLog_service;

public class maxautoiddao {
	private static Logger log = Logger.getLogger(maxautoiddao.class);
	/**
	 * 如果表中没有记录，updateMaxid竟不出错，这样容易忘记给表加记录
	 * 若[yun].[dbo].[tb_maxautoid]无记录则插入一条，有记录则输出多少行
	 */
	public static synchronized void checkTable()
	{
		String sql = "select count(*) from " + maxautoid.tablename;
		String sql_insert = "insert " + maxautoid.tablename + " values(1,1)";
		Connection con = connectionmgr.getInstance().getConnection();
		int recorder_c = 0;
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				recorder_c = rs.getInt(1);
				log.info("check maxautoid table rs.count=" + recorder_c);
			}
			rs.close();
			if(recorder_c == 0)
			{
				log.info("insert recorder values(1,1)");
				ps.close();
				ps = con.prepareStatement(sql_insert);
				ps.execute();
			}
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
	
	public static synchronized maxautoid getMaxAutoid()
	{
		maxautoid mid = new  maxautoid();
		String sql = "select * from " + maxautoid.tablename;
		Connection con = connectionmgr.getInstance().getConnection();
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				mid.setMaxid(rs.getLong(1));
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
				
		return mid;
	}
	
	public static synchronized maxautoid getMaxSeqid()
	{
		maxautoid mid = new  maxautoid();
		String sql = "select * from " + maxautoid.tablename;
		Connection con = connectionmgr.getInstance().getConnection();
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				mid.setMaxid(rs.getLong(2));
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
				
		return mid;
	}
	
	public static synchronized boolean updateMaxid(maxautoid mid)
	{
		String sql = "update " + maxautoid.tablename + " set maxid=?";
		Connection con = connectionmgr.getInstance().getConnection();
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setLong(1, mid.getMaxid());
			ps.execute();
			return true;
		}catch(Exception e)
		{
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(),e);
			return false;
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
	}
	
	public static synchronized boolean updateSeqid(maxautoid mid)
	{
		String sql = "update " + maxautoid.tablename + " set maxseqid=?";
		Connection con = connectionmgr.getInstance().getConnection();
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setLong(1, mid.getMaxid());
			ps.execute();
			return true;
		}catch(Exception e)
		{
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(),e);
			return false;
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
	}
}
