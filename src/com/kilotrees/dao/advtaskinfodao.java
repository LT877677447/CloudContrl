package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.services.ErrorLog_service;

/*
 * 系统启动或定时刷新时，返回有效的广告作务
 * */
public class advtaskinfodao {
	private static Logger log = Logger.getLogger(advtaskinfodao.class);
	/**select * from " +advtaskinfo.tablename + " where onlineflag = 1 "
				+ "and start_date < getdate() and end_date > getdate() "
				+ " and allcount > alldocount order by [prior] desc
	 * @return list.put(ati.getAdv_id(),ati);
	 */
	public static synchronized HashMap<Integer,advtaskinfo> getOnlineAdvtaskList()
	{
		HashMap<Integer,advtaskinfo> list = new HashMap<Integer,advtaskinfo>();
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select * from " +advtaskinfo.tablename + " where onlineflag = 1 "
				+ "and start_date < getdate() and end_date > getdate() "
				+ " and allcount > alldocount order by [prior] desc";
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				advtaskinfo ati = new advtaskinfo();
				ati.setAdv_id(rs.getInt("adv_id"));
				ati.setName(rs.getString("name"));
				ati.setPrior(rs.getInt("prior"));
				ati.setAdv_type(rs.getInt("adv_type"));
				ati.setApkid(rs.getInt("apkid"));
				ati.setCpid(rs.getInt("cpid"));
				ati.setChannelid(rs.getInt("channelid"));
				ati.setBdid(rs.getInt("bdid"));
				//ati.setGroupid(rs.getInt("groupid"));
				ati.setStart_date(new Date(rs.getTimestamp("start_date").getTime()));
				ati.setEnd_date(new Date(rs.getTimestamp("end_date").getTime()));
				ati.setAdv_content(rs.getString("adv_content"));
				ati.setServerbeanid(rs.getString("serverbean"));
				ati.setParams(rs.getString("params"));
				ati.setClientbean_info(rs.getString("clientbean_info"));
				ati.setRequesttime(rs.getInt("requesttime"));
				ati.setRemaintime(rs.getInt("remaintime"));
				ati.setTimeline(rs.getInt("timeline"));
				ati.setRem_timeline(rs.getInt("rem_timeline"));
				ati.setRemain_lock_dev(rs.getInt("remain_lock_dev"));
				ati.setRemain_rule(rs.getString("remain_rule"));
				ati.setAllcount(rs.getInt("allcount"));
				ati.setDayopencount(rs.getInt("dayopencount"));
				ati.setDayusercount(rs.getInt("dayusercount"));
				ati.setAlldocount(rs.getInt("alldocount"));
				ati.setExt(rs.getString("ext"));
				ati.setOnlineflag(rs.getInt("onlineflag"));
				ati.setAlias(rs.getString("alias"));
				if(rs.getTimestamp("online_time") != null)
					ati.setOnline_time(new Date(rs.getTimestamp("online_time").getTime()));
				list.put(ati.getAdv_id(),ati);
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
	/**
	 * 按id广告内容，给留存服务用，查询因为就算广告停了，留存还要做，所以查询整个表，不管广告是否已经做满或停止
	 * @return
	 */
	public static synchronized advtaskinfo getAdvtaskbyid(int advid)
	{	
		advtaskinfo ati = null;
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select top 1 * from " +advtaskinfo.tablename + " where adv_id=?";
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setInt(1, advid);
			ResultSet rs = ps.executeQuery();
		
			if(rs.next())
			{
				ati = new advtaskinfo();
				ati.setAdv_id(rs.getInt("adv_id"));
				ati.setName(rs.getString("name"));
				ati.setPrior(rs.getInt("prior"));
				ati.setAdv_type(rs.getInt("adv_type"));
				ati.setApkid(rs.getInt("apkid"));
				ati.setCpid(rs.getInt("cpid"));
				ati.setChannelid(rs.getInt("channelid"));
				ati.setBdid(rs.getInt("bdid"));
				//ati.setGroupid(rs.getInt("groupid"));
				ati.setStart_date(new Date(rs.getTimestamp("start_date").getTime()));
				ati.setEnd_date(new Date(rs.getTimestamp("end_date").getTime()));
				ati.setAdv_content(rs.getString("adv_content"));
				ati.setServerbeanid(rs.getString("serverbean"));
				ati.setParams(rs.getString("params"));
				ati.setClientbean_info(rs.getString("clientbean_info"));
				ati.setRequesttime(rs.getInt("requesttime"));
				ati.setRemaintime(rs.getInt("remaintime"));
				ati.setTimeline(rs.getInt("timeline"));
				ati.setRem_timeline(rs.getInt("rem_timeline"));
				ati.setRemain_lock_dev(rs.getInt("remain_lock_dev"));
				ati.setRemain_rule(rs.getString("remain_rule"));
				ati.setAllcount(rs.getInt("allcount"));
				ati.setDayopencount(rs.getInt("dayopencount"));
				ati.setDayusercount(rs.getInt("dayusercount"));
				ati.setAlldocount(rs.getInt("alldocount"));
				ati.setExt(rs.getString("ext"));
				ati.setOnlineflag(rs.getInt("onlineflag"));
				ati.setAlias(rs.getString("alias"));
				if(rs.getTimestamp("online_time") != null)
					ati.setOnline_time(new Date(rs.getTimestamp("online_time").getTime()));
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
		return ati;
	}
	/**select adv_id from " +advtaskinfo.tablename+ " where onlineflag = 2
	 * @return ArrayList<Integer> advtaskinfo表onlineflag=2的记录的adv_id集合
	 */
	public static synchronized ArrayList<Integer> getAdvtaskListOnlineflag2()
	{
		ArrayList<Integer> list = new ArrayList<Integer>();
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select adv_id from " +advtaskinfo.tablename 
				+ " where onlineflag = 2";
				
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				int adv_id = rs.getInt(1);
				list.add(adv_id);
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
	
	/**
	 * 取准备上线的广告列表
	 * @return 返回onlineflag=3 and dayusercount != 0的集合
	 */
	public static synchronized ArrayList<advtaskinfo> getAdvtaskListOnlineflag3()
	{
		ArrayList<advtaskinfo> list = new ArrayList<advtaskinfo>();
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select * from " +advtaskinfo.tablename + " where onlineflag = 3 "
				+ "and dayusercount <> 0 "
				+ "and start_date < getdate() and end_date > getdate() ";
				
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				advtaskinfo ati = new advtaskinfo();
				ati.setAdv_id(rs.getInt("adv_id"));
				ati.setName(rs.getString("name"));
				ati.setPrior(rs.getInt("prior"));
				ati.setAdv_type(rs.getInt("adv_type"));
				ati.setApkid(rs.getInt("apkid"));
				ati.setCpid(rs.getInt("cpid"));
				ati.setChannelid(rs.getInt("channelid"));
				ati.setBdid(rs.getInt("bdid"));
				//ati.setGroupid(rs.getInt("groupid"));
				ati.setStart_date(new Date(rs.getTimestamp("start_date").getTime()));
				ati.setEnd_date(new Date(rs.getTimestamp("end_date").getTime()));
				ati.setAdv_content(rs.getString("adv_content"));
				ati.setServerbeanid(rs.getString("serverbean"));
				ati.setParams(rs.getString("params"));
				ati.setClientbean_info(rs.getString("clientbean_info"));
				ati.setRequesttime(rs.getInt("requesttime"));
				ati.setRemaintime(rs.getInt("remaintime"));
				ati.setTimeline(rs.getInt("timeline"));
				ati.setRem_timeline(rs.getInt("rem_timeline"));
				ati.setRemain_lock_dev(rs.getInt("remain_lock_dev"));
				ati.setRemain_rule(rs.getString("remain_rule"));
				ati.setAllcount(rs.getInt("allcount"));
				ati.setDayopencount(rs.getInt("dayopencount"));
				ati.setDayusercount(rs.getInt("dayusercount"));
				ati.setAlldocount(rs.getInt("alldocount"));
				ati.setExt(rs.getString("ext"));
				ati.setOnlineflag(rs.getInt("onlineflag"));
				ati.setAlias(rs.getString("alias"));
				if(rs.getTimestamp("online_time") != null)
					ati.setOnline_time(new Date(rs.getTimestamp("online_time").getTime()));
				list.add(ati);
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
	/**
	 * 每成功执行一个广告后，更新些表的alldocount字段值
	 * @param ati 要拿来更新的advtaskinfo对象
	 */
	public static synchronized void updateAllDocount(advtaskinfo ati)
	{
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "update "+ advtaskinfo.tablename + " set alldocount=? "
				+ "where adv_id=?";
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);			
			ps.setInt(1, (int)ati.getAlldocount());
			ps.setInt(2, (int)ati.getAdv_id());
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
	 * 完成设备分配后，把3设为0 ,设置onlineflag、online_time 
	 * @param ati 要拿来更新的advtaskinfo对象
	 */
	public static synchronized void updateOnlineFlag(advtaskinfo ati)
	{
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "update "+ advtaskinfo.tablename + " set onlineflag=?,"
				+ "online_time=? where adv_id=?";
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);			
			ps.setInt(1, (int)ati.getOnlineflag());
			ps.setTimestamp(2, new Timestamp(ati.getOnline_time().getTime()));
			ps.setInt(3, (int)ati.getAdv_id());
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
//		System.out.println("test");
//		//ArrayList<advtaskinfo> list = advtaskinfodao.getOnlineAdvtaskList();
//		String s= "";
//		for(advtaskinfo e:list)
//		{
//			s += e.toString();
//			System.out.println(e.toString());
//			System.out.println("===========");
//			e.setAlldocount(e.getAlldocount() + 1);
//			updateAllDocount(e);
//		}
//		return s;		
	}
	public static synchronized void updateAdvDayUserCountByJson(int adv_id, int newcount) {
		// TODO Auto-generated method stub
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "update " +advtaskinfo.tablename + " set dayusercount=? where adv_id=?";
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);			
			ps.setInt(1, newcount);
			ps.setInt(2, adv_id);
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
