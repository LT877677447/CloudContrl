package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.apkinfo;
import com.kilotrees.services.ErrorLog_service;

public class apkinfodao {
	private static Logger log = Logger.getLogger(apkinfodao.class);
	
	public static synchronized apkinfo getApkInfo(int apkid)
	{
		apkinfo ai = null;
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select top 1 * from " + apkinfo.tablename +  " where apkid=?";
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setInt(1, apkid);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				ai = new apkinfo();
				ai.setApkid(apkid);
				ai.setPackagename(rs.getString("packagename"));
				ai.setApkname(rs.getString("apkname"));
				ai.setApkfile(rs.getString("apkfile"));
				//ai.setRemaintype(rs.getInt("remaintype"));
				//ai.setTimeline(rs.getInt("timeline"));
				//ai.setRemain_rule(rs.getString("remain_rule"));
				ai.setReg_scriptfiles(rs.getString("reg_scriptfiles"));
				ai.setRem_scriptfiles(rs.getString("rem_scriptfiles"));
				ai.setZipfiles(rs.getString("zipfiles"));
				ai.setUnzip_regex(rs.getString("unzip_regex"));
				ai.setRegscriptparams(rs.getString("regscriptparams"));
				ai.setRemscriptparams(rs.getString("remscriptparams"));
				ai.setSdcard_dir(rs.getString("sdcard_dir"));
				ai.setScriptfile_version(rs.getInt("scriptfile_version"));
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
		
		return ai;
	}
	
	public static synchronized void updateZifiles(int apkid,String zipfiles)
	{
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "update " + apkinfo.tablename +  " set zipfiles = ?"
				+ " where apkid=?";
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setString(1, zipfiles);
			ps.setInt(2, apkid);
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
		System.out.println("apkinfodao.test()");
		apkinfo aid = getApkInfo(10);
		System.out.println(aid.toString());
		
		aid = getApkInfo(11);
		System.out.println(aid.toString());
		
		aid = getApkInfo(12);
		if(aid != null)
		System.out.println(aid.toString());
	}
}
