package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class testremaindao {

	public synchronized static void addTestRemain(String imei, String mac, String imsi, String mode, int sdkversion, int opencount, int autoid )
	{		
		String sql = "insert into " + "tb_testremain";
		sql += " values(?,?,?,?,?,?,?,getdate())";		
		System.out.println(sql);
		Connection con = connectionmgr.getInstance().getConnection();		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setString(1, imei);
			ps.setString(2, mac);
			ps.setString(3, imsi);
			ps.setString(4, mode);
			ps.setInt(5, sdkversion);
			ps.setInt(6, opencount);
			ps.setInt(7, autoid);
			ps.execute();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
	}
	
}
