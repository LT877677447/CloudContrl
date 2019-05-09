package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.advnewreglog;
import com.kilotrees.model.po.deviceinfo;
import com.kilotrees.services.ErrorLog_service;

public class advnewreglogdao {
	private static Logger log = Logger.getLogger(advnewreglogdao.class);
	/**向[tb_advnewreglog_xx]插入一条记录
	 * @param advlog 要插入的advnewreglog对象
	 */
	public synchronized static void addNewRegLog(advnewreglog advlog)
	{		
		String sql = "insert into " + advnewreglog.getCurTableName();
		sql += " values(?,?,?,?,?,?,?,?,?,?,?)";		
		//System.out.println(sql);
		Connection con = connectionmgr.getInstance().getConnection();		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setLong(1, advlog.getAutoid());
			ps.setInt(2,advlog.getAdv_id());
			ps.setString(3, advlog.getDev_tag());
			ps.setInt(4, advlog.getVpnid());
			ps.setInt(5,advlog.getStep());
			ps.setInt(6, advlog.getResult());
			ps.setString(7, advlog.getLoginfo());
			ps.setString(8, advlog.getAppInfo());
			ps.setTimestamp(9, new java.sql.Timestamp(advlog.getLogtime().getTime()));
			ps.setString(10, advlog.getIp());
			ps.setString(11, advlog.getArea());
			//ps.get
			ps.executeUpdate();
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
	
	/**检查设备是否已经正常上传了日志
	 * @param di   要检查的deviceinfo对象
	 * @param todayFlag 值为1则查找今日成功上传的日志
	 * @return true：已经上传日志  false：没有上传日志
	 */
	public synchronized static boolean checkDevLog(deviceinfo di,int todayFlag)
	{		
		boolean hasDo = false;
		String sql = "select top 1 * from " + advnewreglog.getCurTableName();
		sql += " where dev_tag=?";
		if(todayFlag == 1)
			sql += " and datediff(dd,logtime,getdate()) = 0 and result=0";
		//System.out.println(sql);
		Connection con = connectionmgr.getInstance().getConnection();		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setString(1, di.getDevice_tag());
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				hasDo = true;
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
		return hasDo;
	}

	public static void test()
	{
		advnewreglog log = new advnewreglog();
		log.setAutoid(1);
		log.setAdv_id(1);
		log.setDev_tag("dev_tag1");
		log.setLoginfo("loginfo1");
		log.setLogtime(new Date());
		//log.setPhoneInfo("{huwei p20}");
		log.setResult(0);
		log.setStep(1);
		log.setVpnid(3);
		addNewRegLog(log);
	}
}
