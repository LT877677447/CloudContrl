package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.advremaincachetmp;
import com.kilotrees.services.ErrorLog_service;

public class advremaincachetmpdao {
	private static Logger log = Logger.getLogger(advremaincachetmpdao.class);
	
	public synchronized static void addCache(advremaincachetmp cache)
	{		
		String sql = "insert into " + advremaincachetmp.tablename;
		sql += " values(?,?,?,?,?,?,?,?,?,?,?)";		
		
		Connection con = connectionmgr.getInstance().getConnection();		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setLong(1, cache.getAutoid());
			ps.setInt(2,cache.getAdv_id());
			ps.setString(3, cache.getDev_tag());
			ps.setInt(4, cache.getVpnid());	
			ps.setInt(5, cache.getLock_dev());
			ps.setString(6, cache.getPhoneInfo());
			ps.setInt(7, cache.getUsedtime());
			ps.setInt(8, cache.getProcok());
			ps.setTimestamp(9, new java.sql.Timestamp(cache.getAdv_finish_time().getTime()));
			//2018-11-14增加appInfo
			ps.setString(10, cache.getAppinfo());
			//2018-11-30增加retopencount
			ps.setInt(11, cache.getRetopencount());
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
	/**
	 * 2019-1-6为方便跟踪数据， 增加参数delcachdays，当delcachdays > 0时，按指定超时日期删除表数据。
	 * @param delcachdays
	 */
	public synchronized static void resetCacheTable(int delcachdays)
	{
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "truncate table " + advremaincachetmp.tablename;
		
		if(delcachdays > 0)
		{			
			sql = "delete " + advremaincachetmp.tablename + " where datediff(dd,adv_finish_time,getdate()) > " + delcachdays;
		}
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.execute();			
		}catch(Exception e)
		{
			ErrorLog_service.system_errlog(e);
			connectionmgr.getInstance().closeConnection(con,ps);
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
	}
	
	/**select distinct adv_id from advremaincachetmp.tablename where procok = 0 order by adv_id
	 * @return 返回未处理(procok=0)的留存任务ID集合
	 */
	public synchronized static ArrayList<Integer> getAdvIdList()
	{		
		ArrayList<Integer> list = new ArrayList<Integer>();
		String sql = "select distinct adv_id from " + advremaincachetmp.tablename + " where ";
		sql += "procok = 0 order by adv_id";
		Connection con = connectionmgr.getInstance().getConnection();
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				Integer advid = rs.getInt(1);
				list.add(advid);
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
	
	/**select * from advremaincachetmp.tablename where procok = 0 and adv_id = ? order by cacheid
	 * @param adv_id 任务id
	 * @return 对应任务未处理（prock=0）的advremaincachetmp集合
	 */
	public synchronized static ArrayList<advremaincachetmp> getCacheList(int adv_id)
	{		
		ArrayList<advremaincachetmp> list = new ArrayList<advremaincachetmp>();
		String sql = "select * from " + advremaincachetmp.tablename + " where ";
		sql += "procok = 0 and adv_id = ? order by cacheid";
		Connection con = connectionmgr.getInstance().getConnection();
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setInt(1, adv_id);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				advremaincachetmp cache = new advremaincachetmp();
				cache.setAutoid(rs.getLong("autoid"));
				cache.setCacheid(rs.getInt("cacheid"));
				cache.setAdv_id(rs.getInt("adv_id"));
				cache.setDev_tag(rs.getString("dev_tag"));
				cache.setVpnid(rs.getInt("vpnid"));
				cache.setLock_dev(rs.getInt("lock_dev"));
				cache.setPhoneInfo(rs.getString("phoneinfo"));
				cache.setUsedtime(rs.getInt("usedtime"));
				//cache.setProcok(rs.getInt("procok"));
				if(rs.getTimestamp("adv_finish_time") != null)
					cache.setAdv_finish_time(new Date(rs.getTimestamp("adv_finish_time").getTime()));
				cache.setAppinfo(rs.getString("appinfo"));
				//2018-11-30
				cache.setRetopencount(rs.getInt("retopencount"));
				list.add(cache);
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
	
	/**update " + advremaincachetmp.tablename + " set procok=1 "+ "where cacheid <=? and adv_id = ?
	 * @param max_cacheid
	 * @param adv_id
	 */
	public synchronized static void finishCacheProc(int max_cacheid,int adv_id)
	{
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "update " + advremaincachetmp.tablename + " set procok=1 "
				+ "where cacheid <=? and adv_id = ?";
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);			
			ps.setInt(1, max_cacheid);
			ps.setInt(2, adv_id);
			ps.execute();			
		}catch(Exception e)
		{	
			ErrorLog_service.system_errlog(e);
			connectionmgr.getInstance().closeConnection(con,ps);
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
	}
}
