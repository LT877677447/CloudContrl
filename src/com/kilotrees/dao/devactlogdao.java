package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.devactlog;
import com.kilotrees.services.ErrorLog_service;

/**
 * 把设备活跃日志记录在数据库表中,记录每天的设备活跃情况
 * 因为心跳时间为10秒，一台机一天就1000个记录，如果10000台机，就是1千万条记录，所以直接记录当天的活跃最后时间
 * 
 * 调用存储过程更新:
 * 
create proc devact_proc(
	@tag varchar(50),
	@act_time datetime,
	@status varchar(100)
)
as
begin
	select * from tb_devactlog where dev_tag = @tag and DATEDIFF(dd,@act_time,lastlogintime) = 0
	if(@@ROWCOUNT = 1)
	begin
		update tb_devactlog set lastlogintime = @act_time,devstatus = @status where dev_tag = @tag and DATEDIFF(dd,@act_time,lastlogintime) = 0
	End
	else
	begin
		insert tb_devactlog values(@tag,@act_time,@status)
	end
end

 */
public class devactlogdao {
	private static Logger log = Logger.getLogger(devactlogdao.class);
	/**
	 * 2018-11-3取消用存储过程,为了保证表中有记录，我们在注册机器时直接生成一条记录，这样不需要判断是否有记录而执行多次
	 * @param devLog
	 */
	public static synchronized void updateDevActTime(devactlog devLog)
	{
		Connection con = connectionmgr.getInstance().getConnection();
		//String sql = "select top 1 * from tb_devactlog where dev_tag=?"; 
		//String sql_insert = "insert tb_devactlog  values(?,?,?)";
		String sql_update = "update tb_devactlog set lastlogintime = ?,devstatus=? where dev_tag=?";
		PreparedStatement ps = null;
		//boolean bfound = false;
		try{
			ps = con.prepareStatement(sql_update);
			java.sql.Timestamp d = new java.sql.Timestamp(devLog.getLastlogintime().getTime());
			ps.setTimestamp(1, d);
			ps.setString(2, devLog.getDevstatus());
			ps.setString(3, devLog.getDev_tag());
			ps.execute();
			
//			if(rs.next())
//			{
//				rs.close();
//				ps.close();
//				ps = con.prepareStatement(sql_update);
//				java.sql.Timestamp d = new java.sql.Timestamp(devLog.getLastlogintime().getTime());
//				ps.setTimestamp(1, d);
//				ps.setString(2, devLog.getDevstatus());
//				ps.setString(3, devLog.getDev_tag());
//				ps.execute();
//			}
//			else
//			{
//				rs.close();
//				ps.close();
//				ps = con.prepareStatement(sql_insert);
//				ps.setString(1,devLog.getDev_tag());
//				java.sql.Timestamp d = new java.sql.Timestamp(devLog.getLastlogintime().getTime());
//				ps.setTimestamp(2, d);
//				ps.setString(3, devLog.getDevstatus());
//				ps.execute();
//			}
//			java.sql.Timestamp d = new java.sql.Timestamp(devLog.getLastlogintime().getTime());
//			ps.setTimestamp(2, d);
//			ps.setString(3, devLog.getDevstatus());			
//			ps.execute();
		}catch(Exception e)
		{
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(),e);
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
		/*
		Connection con = connectionmgr.getInstance().getConnection();
		String proStr = "{call devact_proc(?,?,?)}";  
		
		try{		
			CallableStatement cs = con.prepareCall(proStr);
			cs.setString(1, devLog.getDev_tag());
			cs.setTimestamp(2, new java.sql.Timestamp(devLog.getLastlogintime().getTime()));
			cs.setString(3,devLog.getDevstatus());
         	cs.execute();
         	cs.close();
		}catch(Exception e)
		{			
			log.error(e.getMessage(),e);
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,null);
		}*/
	}
	/**
	 * 只有注册时才用
	 * @param devLog
	 */
	public static synchronized void insertDevActTime(devactlog devLog)
	{
		Connection con = connectionmgr.getInstance().getConnection();
		
		String sql_insert = "insert tb_devactlog  values(?,?,?)";		
		PreparedStatement ps = null;
		
		try{
			ps = con.prepareStatement(sql_insert);
			ps.setString(1, devLog.getDev_tag());
			java.sql.Timestamp d = new java.sql.Timestamp(devLog.getLastlogintime().getTime());
			ps.setTimestamp(2, d);
			ps.setString(3, devLog.getDevstatus());			
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
	static int k = 0;
	public static void test()
	{
		devactlog test = new devactlog();
		test.setDev_tag("dev_2");
		test.setDevstatus("ok" + k++);
		test.setLastlogintime(new java.util.Date());
		updateDevActTime(test);
	}
}
