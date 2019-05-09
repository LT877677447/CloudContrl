package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.vpninfo;
import com.kilotrees.services.ErrorLog_service;


public class vpninfodao {
	private static Logger log = Logger.getLogger(vpninfodao.class);
	//随机获取有效的单条vpn
	public static synchronized vpninfo getUsedVpnRandom()
	{
		Connection con = connectionmgr.getInstance().getConnection();
		vpninfo vi = null;
		//SQL 中 【不等于】使用‘<>’，此时要注意此条件会将字段为null的数据也当做满足不等于的条件而将数据筛选掉。
		String sql = "SELECT top 1 *  FROM [yun].[dbo].[tb_vpninfo] where nouse <> 1 order by NEWID() ";
		/**
		 * 随机一条vpn，device_count>deviceinfo表中已分配该VPN的条数
		 */
//		String sql="SELECT top 1 *  FROM [yun].[dbo].[tb_vpninfo] as ydtvpn where ydtvpn.device_count is null or ydtvpn.device_count > (select count(*) FROM [yun].[dbo].[tb_deviceinfo] as ydtdvei where ydtdvei.vpnid=ydtvpn.vpnid ) and nouse <> 1   order by NEWID();";
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			vi = new vpninfo();
			if(rs.next())
			{
				vi.setVpnid(rs.getInt("vpnid"));
				vi.setVpnurl(rs.getString("vpnurl"));
				vi.setAccount(rs.getString("account"));
				vi.setPass(rs.getString("pass"));
				vi.setStatic_flag(rs.getInt("static_flag"));
				vi.setAreas(rs.getString("areas"));
				vi.setVpntype(rs.getInt("vpntype"));
				//新增每条VPN最大可以有几台设备，需要关注有没有异常
//				vi.setDeviceCount(rs.getInt("device_count"));
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
		
		return vi;
	}
	/**
	 * 按指定vpnid获承vpn信息
	 * @param vpnid
	 * @return
	 */
	public static synchronized vpninfo getVpnById(int vpnid)
	{
		Connection con = connectionmgr.getInstance().getConnection();
		vpninfo vi = null;
		String sql = "SELECT  * FROM tb_vpninfo where vpnid = ? order by NEWID() ";
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setInt(1, vpnid);
			ResultSet rs = ps.executeQuery();
			vi = new vpninfo();
			if(rs.next())
			{
				vi.setVpnid(rs.getInt("vpnid"));
				vi.setVpnurl(rs.getString("vpnurl"));
				vi.setAccount(rs.getString("account"));
				vi.setPass(rs.getString("pass"));
				vi.setStatic_flag(rs.getInt("static_flag"));
				vi.setAreas(rs.getString("areas"));
				vi.setVpntype(rs.getInt("vpntype"));
				vi.setNouse(rs.getInt("nouse"));
				vi.setDeviceCount(rs.getInt("device_count"));
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
		
		return vi;
	}
}
