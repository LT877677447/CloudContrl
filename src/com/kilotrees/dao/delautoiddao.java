package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.kilotrees.services.ErrorLog_service;

/*

CREATE TABLE [dbo].[tb_delautoid](
	[del_autoid] [bigint] NOT NULL,
	[delflag] [int] NOT NULL,
	[delinfo] [varchar](100) NULL,
	[deltime] [datetime] NULL,
 CONSTRAINT [PK_tb_delautoid] PRIMARY KEY CLUSTERED 
(
	[del_autoid] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]


当留存不需要或已经完成时，把autoid放入表中，服务器线程不断读取这些autoid并发到局域网的服务器中删除备份的留存文件
*/
public class delautoiddao {
	static String tablename = "tb_delautoid";
	private static Logger log = Logger.getLogger(delautoiddao.class);
	
	public synchronized static ArrayList<Long> getDelAutoids(int count)
	{
		ArrayList<Long> ls = new ArrayList<Long>();
		String sql = "select top " + count + " del_autoid from  " + tablename;
		sql += " where delflag=0 order by del_autoid";		
		//System.out.println(sql);
		Connection con = connectionmgr.getInstance().getConnection();		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				ls.add(rs.getLong(1));
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
	
	public synchronized static void addDelAutoid(long autoid,String delInfo)
	{
		String sql = "insert into " + tablename;
		sql += " values(?,0,?,getdate())";		
		//System.out.println(sql);
		Connection con = connectionmgr.getInstance().getConnection();		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setLong(1,autoid);
			ps.setString(2, delInfo);
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
	
	public synchronized static void updatesDelAutoid(String autoidsStr)
	{
		String sql = "update " + tablename;
		sql += " set delflag=1 where del_autoid in (" + autoidsStr + ")" ;		
		//log.info(sql);
		Connection con = connectionmgr.getInstance().getConnection();		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);			
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
		//addDelAutoid(21);
		//addDelAutoid(22);
	}
	
	public static void main(String[] argv)
	{
		String autoidsStr = "123,100,300";
		String sql = "update " + tablename;
		sql += " set delflag=1 where del_autoid in ('" + autoidsStr + "')" ;
		
		System.out.println(sql);
	}
}
