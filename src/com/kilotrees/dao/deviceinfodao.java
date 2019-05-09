package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.kilotrees.model.bo.error_result;
import com.kilotrees.model.po.deviceinfo;
import com.kilotrees.services.ErrorLog_service;

/**
 * 取设备信息
 */
public class deviceinfodao {
	private static Logger log = Logger.getLogger(deviceinfodao.class);
	
	/**根据设备标签dev_tag查找deviceinfo对象
	 * @param dev_tag 设备标签
	 * @return deviceinfo对象
	 */
	public synchronized static deviceinfo getDeviceInfo(String dev_tag)
	{
		deviceinfo di = null;
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select top 1 * from " + deviceinfo.tablename + " where device_tag=?";
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setString(1, dev_tag);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				di = new deviceinfo();
				di.setDevice_tag(dev_tag);
				di.setClientid(rs.getInt("clientid"));								
				di.setPhone_type(rs.getString("phone_type"));
				di.setAlloc_adv(rs.getInt("alloc_adv"));
				di.setAlloc_adv_temp(rs.getString("alloc_adv_temp"));
				di.setExt(rs.getString("ext"));
				di.setExt(rs.getString("ext_old"));				
				di.setRegisttime(new Date(rs.getTimestamp("registtime").getTime()));
				if(rs.getTimestamp("alloctime") != null)
					di.setAlloctime(new Date(rs.getTimestamp("alloctime").getTime()));
				di.setAlloc_type_old(rs.getInt("alloc_type_old"));
				di.setAlloc_type(rs.getInt("alloc_type"));
				di.setVpnid(rs.getInt("vpnid"));
				di.setAdvstatus_reset(rs.getInt("advstatus_reset"));
				di.setNouse(rs.getInt("nouse"));
				di.setHand_locked(rs.getInt("hand_locked"));
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
		return di;
	}
	
	/**sql :"select * from " + deviceinfo.tablename
	 */
	public synchronized static HashMap<String,deviceinfo> getAllDeviceInfo()
	{
		deviceinfo di = null;
		HashMap<String,deviceinfo> lst = new HashMap<String,deviceinfo>();
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select * from " + deviceinfo.tablename;
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				di = new deviceinfo();
				di.setDevice_tag(rs.getString("device_tag"));
				di.setClientid(rs.getInt("clientid"));								
				di.setPhone_type(rs.getString("phone_type"));
				di.setAlloc_adv(rs.getInt("alloc_adv"));
				di.setAlloc_adv_temp(rs.getString("alloc_adv_temp"));
				di.setExt(rs.getString("ext"));
				di.setExt_old(rs.getString("ext_old"));				
				di.setRegisttime(new Date(rs.getTimestamp("registtime").getTime()));
				if(rs.getTimestamp("alloctime") != null) {
					di.setAlloctime(new Date(rs.getTimestamp("alloctime").getTime()));
				}
				di.setAlloc_type_old(rs.getInt("alloc_type_old"));
				di.setAlloc_type(rs.getInt("alloc_type"));
				di.setVpnid(rs.getInt("vpnid"));
				di.setAdvstatus_reset(rs.getInt("advstatus_reset"));
				di.setNouse(rs.getInt("nouse"));
				di.setHand_locked(rs.getInt("hand_locked"));
				lst.put(di.getDevice_tag(),di);				
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
		return lst;
	}
	
	/**返回所有正在使用（nouse=0）中的deviceinfo
	 * @return HashMap(device_tag字符串,deviceinfo对象)
	 */
	public synchronized static HashMap<String,deviceinfo> getAllUsedDeviceInfo()
	{
		deviceinfo di = null;
		HashMap<String,deviceinfo> lst = new HashMap<String,deviceinfo>();
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select * from " + deviceinfo.tablename + " where nouse=0";
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				di = new deviceinfo();
				di.setDevice_tag(rs.getString("device_tag"));
				di.setClientid(rs.getInt("clientid"));								
				di.setPhone_type(rs.getString("phone_type"));
				di.setAlloc_adv(rs.getInt("alloc_adv"));
				di.setAlloc_adv_temp(rs.getString("alloc_adv_temp"));
				di.setExt(rs.getString("ext"));
				di.setExt_old(rs.getString("ext_old"));				
				di.setRegisttime(new Date(rs.getTimestamp("registtime").getTime()));
				if(rs.getTimestamp("alloctime") != null)
					di.setAlloctime(new Date(rs.getTimestamp("alloctime").getTime()));
				di.setAlloc_type_old(rs.getInt("alloc_type_old"));
				di.setAlloc_type(rs.getInt("alloc_type"));
				di.setVpnid(rs.getInt("vpnid"));
				di.setAdvstatus_reset(rs.getInt("advstatus_reset"));
				di.setHand_locked(rs.getInt("hand_locked"));
				lst.put(di.getDevice_tag(),di);
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
		return lst;
	}
	
	/**查正在使用中的设备数量
	 * @return int结果
	 */
	public synchronized static int getInUsedDeviceCount()
	{
		int count  = 0;
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select count(*) from " + deviceinfo.tablename + " where nouse=0";
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);			
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				count = rs.getInt(1);			
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
		return count;
	}
	/**
	 * 广告分配好设备后，更新设备信息
	 * @param di 要拿来更新的deviceinfo对象
	 */
	public synchronized static void updateDeviceInfo(deviceinfo di)
	{
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "update "+ deviceinfo.tablename + " set "
				+ "alloc_adv=?"
				+ ",alloc_adv_temp=?"
				+ ",ext=?"
				+ ",ext_old=?"
				+ ",alloc_type_old=?"
				+ ",alloc_type=?"
				+ ",alloctime=?"
				+ ",advstatus_reset=?"
				+ " where device_tag=?";
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);	
			ps.setInt(1, di.getAlloc_adv());
			ps.setString(2,di.getAlloc_adv_temp());
			ps.setString(3, di.getExt());
			ps.setString(4, di.getExt_old());
			ps.setInt(5, di.getAlloc_type_old());
			ps.setInt(6, di.getAlloc_type());
			ps.setTimestamp(7, new Timestamp(di.getAlloctime().getTime()));
			ps.setInt(8, di.getAdvstatus_reset());
			ps.setString(9, di.getDevice_tag());
			ps.execute();			
		}catch(Exception e)
		{
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(),e);
			log.error(di.getAlloc_adv_temp() + "-len=" + di.getAlloc_adv_temp().length());
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
	}
	
	/**注册新设备
	 * @param di 要拿来注册的新设备deviceinfo对象
	 * @param er 如果出错，用来放出错信息
	 */
	public synchronized static void addDeviceInfo(deviceinfo di,error_result er)
	{
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "insert "+ deviceinfo.tablename + " values(?,?,?,?,'',?,'',getdate(),null,null,'',null,null,0,0)";
				
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);	
			ps.setString(1, di.getDevice_tag());
			ps.setInt(2, di.getClientid());
			ps.setString(3, di.getPhone_type());
			ps.setInt(4, di.getAlloc_adv());
			ps.setString(5, di.getExt());
			ps.execute();
			
		}catch(Exception e)
		{
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(),e);
			er.setErr_code(error_result.System_sql);
			er.setErr_info(e.getMessage());
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
	}
	
	/** 查使用同一vpn的设备数量
	 * @param vpnId VPNID值
	 * @return int型数量
	 */
	public synchronized static int getDeviceCountByVpnid(int vpnId) {
		Connection con = connectionmgr.getInstance().getConnection();
		String deviceinfoSql="select count(*) FROM [yun].[dbo].[tb_deviceinfo] as ydtdvei where ydtdvei.vpnid= ? ";
		PreparedStatement ps = null;
		int count=0;
		try{
			ps = con.prepareStatement(deviceinfoSql);
			ps.setInt(1, vpnId);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				count=rs.getInt(1);
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
		return count;
	}
}
