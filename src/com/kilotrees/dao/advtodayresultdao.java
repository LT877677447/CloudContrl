package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.advtodayresult;
import com.kilotrees.services.ErrorLog_service;

public class advtodayresultdao {
	private static Logger log = Logger.getLogger(advtodayresultdao.class);
	/**select * from tb_advtodayresult where datediff(dd,result_time,getdate()) = 0
	 * 服务启动时需要把今天所有执行结果信息收集起来,这个表作为中间表，方便后台查询，在服务器重启时计算新增还有多少没有完成，
	 * 这里没有统计广告每个设备打开次数，对于每个广告第天打开次数，实际上可以通过新增和留存日志表查到每天任务执行情况
	 * ．
	 * @return
	 */
	public static synchronized HashMap<String,advtodayresult> getAdvTodayResultSet()
	{
		HashMap<String,advtodayresult> list = new HashMap<String,advtodayresult>();
		String sql = "select * from tb_advtodayresult where datediff(dd,result_time,getdate()) = 0";
		Connection con = connectionmgr.getInstance().getConnection();
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			//String todayStr = DateUtil.getDateBeginString(new java.util.Date());
			//ps.setString(1, todayStr);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				advtodayresult ar = new advtodayresult();
				ar.setTid(rs.getInt("tid"));
				ar.setAdvid(rs.getInt("advid"));
				ar.setIsremain(rs.getInt("isremain"));
				ar.setNewuser_success_count(rs.getInt("newuser_success_count"));
				ar.setNewuser_err_count(rs.getInt("newuser_err_count"));
				ar.setNewuser_success_opentcount(rs.getInt("newuser_success_opentcount"));
				ar.setNewuser_err_opentcount(rs.getInt("newuser_err_opentcount"));
				//
				ar.setRemain_olduser_success_count(rs.getInt("remain_olduser_success_count"));
				ar.setRemain_newuser_success_count(rs.getInt("remain_newuser_success_count"));
				ar.setRemain_olduser_success_opentcount(rs.getInt("remain_olduser_success_opentcount"));
				ar.setRemain_newuser_success_opentcount(rs.getInt("remain_newuser_success_opentcount"));
				ar.setRemain_err_opentcount(rs.getInt("remain_err_opentcount"));
				
				java.util.Date d = new java.util.Date(rs.getTimestamp("result_time").getTime());
				ar.setResult_time(d);
				list.put(ar.getKey(),ar);
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
	 * 这里可能有每天跨度问题，这里由服务器控制好时间逻辑
	 */
	public static synchronized void updateAdvTodayResult(advtodayresult rs)
	{
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "update tb_advtodayresult set "
				+ "newuser_success_count=?,newuser_err_count=?,newuser_success_opentcount=?,"
				+ "newuser_err_opentcount=?,remain_olduser_success_count=?,remain_newuser_success_count=?,"
				+ "remain_olduser_success_opentcount=?,remain_newuser_success_opentcount=?,remain_err_opentcount=?,"
				+ "result_time=? "				
				 + "where tid=?";
		
		//log.info(sql);
	
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);			
			ps.setInt(1, rs.getNewuser_success_count());
			ps.setInt(2, rs.getNewuser_err_count());
			ps.setInt(3, rs.getNewuser_success_opentcount());
			ps.setInt(4, rs.getNewuser_err_opentcount());
			//
			ps.setInt(5, rs.getRemain_olduser_success_count());
			ps.setInt(6, rs.getRemain_newuser_success_count());
			ps.setInt(7, rs.getRemain_olduser_success_opentcount());
			ps.setInt(8, rs.getRemain_newuser_success_opentcount());
			ps.setInt(9, rs.getRemain_err_opentcount());
			
			java.sql.Timestamp t = new java.sql.Timestamp(rs.getResult_time().getTime());
			ps.setTimestamp(10, t);			
			ps.setInt(11, rs.getTid());
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
	
	public static synchronized void newAdvTodayResult(advtodayresult ar)
	{
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "insert into tb_advtodayresult values(?,?,?,?,?,?,?,?,?,?,?,?)";
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);			
			ps.setInt(1, (int)ar.getAdvid());
			ps.setInt(2, (int)ar.getIsremain());
			ps.setInt(3, ar.getNewuser_success_count());
			ps.setInt(4, ar.getNewuser_err_count());
			ps.setInt(5, ar.getNewuser_success_opentcount());
			ps.setInt(6, ar.getNewuser_err_opentcount());
			//
			ps.setInt(7, ar.getRemain_olduser_success_count());
			ps.setInt(8, ar.getRemain_newuser_success_count());
			ps.setInt(9, ar.getRemain_olduser_success_opentcount());
			ps.setInt(10, ar.getRemain_newuser_success_opentcount());
			ps.setInt(11, ar.getRemain_err_opentcount());
			java.sql.Timestamp t = new java.sql.Timestamp(ar.getResult_time().getTime());
			ps.setTimestamp(12, t);
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			if(rs.next())
				ar.setTid(rs.getInt(1));
			rs.close();
		}catch(Exception e)
		{
			ErrorLog_service.system_errlog(e);
			e.printStackTrace();
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
				
	}
	
		
	public static void main(String[] argvs)
	{
		HashMap<Integer,String> h = new HashMap<Integer,String>();
		h.put(1, "s1");
		h.put(3, "s3");
		h.put(4, "s4");
		h.put(2, "s2");
		
		List<String> sl = new ArrayList<String>();
		sl.addAll(h.values());
		for(int i = 0; i < sl.size(); i++)
			System.out.println(sl.get(i));
		System.out.println("----");
		for(String s : h.values())
			System.out.println(s);
		
		
		//System.out.println(udate.toString());
	}
}
