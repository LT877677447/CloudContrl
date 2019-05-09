package com.kilotrees.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.devicepiex;
import com.kilotrees.model.po.useragent;
import com.kilotrees.services.ErrorLog_service;

public class useragentdao {
	private static Logger log = Logger.getLogger(useragentdao.class);

	public static void loadUserAgentFromFiles() {
		String sql = "insert " + useragent.tablename + " values(" + "?,'',?)";

		File dir = new File(ServerConfig.contextRealPath + "/files/task_links");

		File[] files = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File arg0) {
				// TODO Auto-generated method stub
				String fileName = arg0.getName();
				if (fileName.endsWith("user_agent.list"))
					return true;
				return false;
			}
		});
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {

			for (File f : files) {
				String dev_type = f.getName();
				int pos = dev_type.indexOf("_");
				dev_type = dev_type.substring(0, pos);
				InputStreamReader bins = new InputStreamReader(new FileInputStream(f));
				BufferedReader read = new BufferedReader(bins);
				String line;
				while (true) {
					line = read.readLine();
					if (line == null)
						break;
					if (line.length() == 0)
						continue;
					ps = con.prepareStatement(sql);
					ps.setString(1, dev_type);
					ps.setString(2, line);
					ps.execute();
					ps.close();
					ps = null;
				}
			}
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		log.info("load file end");
	}

	public static HashMap<String, ArrayList<String>> getAllUa() {
		HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		String sql = "select * from " + useragent.tablename + " order by type";
		try {
			ps = con.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String type = rs.getString("type");
				String ua = rs.getString("user_agent");

				ArrayList<String> ls = hm.get(type);
				if (ls == null) {
					ls = new ArrayList<String>();
					hm.put(type, ls);
				}
				ls.add(ua);
			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return hm;
	}

	public static ArrayList<String> getDevTypes() {
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		ArrayList<String> ls = new ArrayList<String>();

		String sql = "select distinct [type] from " + useragent.tablename;
		try {
			ps = con.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String type = rs.getString("type");
				ls.add(type);
			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return ls;
	}

	public static HashMap<String, ArrayList<String>> getDevPiex() {
		HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		String sql = "select * from " + devicepiex.tablename + " order by type";
		try {
			ps = con.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String type = rs.getString("type");
				String ua = rs.getString("pixel");

				ArrayList<String> ls = hm.get(type);
				if (ls == null) {
					ls = new ArrayList<String>();
					hm.put(type, ls);
				}
				ls.add(ua);
			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return hm;
	}

	public static void addUmuuid(int adv_id, String comname,String[] uuidinfo) {
		String sql = "insert tb_linkumid values(?,?,?,?,?,?,?,getdate())";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, adv_id);
			ps.setString(2, comname);
			ps.setString(3, uuidinfo[0]);
			ps.setString(4, uuidinfo[1]);
			ps.setString(5, uuidinfo[2]);
			ps.setString(6, uuidinfo[3]);
			ps.setString(7, uuidinfo[4]);
			ps.execute();

		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}
	
	public static String[] getOldUmuuid(int adv_id) {
		String sql = "select top 1 * from  tb_linkumid where adv_id=? and"
				+ " datediff(ss,create_time,getdate()) > 900 "
				+ "order by NEWID()";
		
		String[] uids = new String[5];
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, adv_id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				uids[0] = rs.getString("cnzzid");
				uids[1] = rs.getString("umuuid");
				uids[2] = rs.getString("useragent");
				uids[3] = rs.getString("devpiex");
				uids[4] = rs.getString("cookie");
			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return uids;
	}
	/*
	 * 
CREATE TABLE [dbo].[tb_linkbaiduid](
	[seq_id] [int] IDENTITY(1,1) NOT NULL,
	[autoid] [int] NOT NULL,
	[adv_id] [int] NOT NULL,
	[com] [varchar](50) NOT NULL,
	[useragent] [varchar](1000) NOT NULL,
	[devpiex] [varchar](50) NOT NULL,
	[cookie] [varchar](500) NOT NULL,
	[other_param] [varchar](500) NOT NULL,
	[create_time] [datetime] NOT NULL
) ON [PRIMARY]
	 */
	public static void addBaiduid(int adv_id, String comname,int autoid,String[] uuidinfo) {
		String sql = "insert tb_linkbaiduid values(?,?,?,?,?,?,?,getdate())";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, autoid);
			ps.setInt(2, adv_id);
			ps.setString(3, comname);
			ps.setString(4, uuidinfo[0]);
			ps.setString(5, uuidinfo[1]);
			ps.setString(6, uuidinfo[2]);
			ps.setString(7, uuidinfo[3]);
			//ps.setString(7, uuidinfo[4]);
			ps.execute();

		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	public static String[] getOldBaiduid(int adv_id) {
		// TODO Auto-generated method stub
		String sql = "select top 1 * from  tb_linkbaiduid where adv_id=? and"
				+ " datediff(dd,create_time,getdate()) > 0 "
				+ "order by NEWID()";
		
		String[] uids = new String[5];
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, adv_id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				
				uids[0] = rs.getString("useragent");
				uids[1] = rs.getString("devpiex");
				uids[2] = rs.getString("cookie");
				//其它小参数，用分号分开
				uids[3] = rs.getString("other_param");				
				java.sql.Timestamp ts = rs.getTimestamp("create_time");
				uids[4] = String.valueOf(ts.getTime());
			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return uids;
	}
}
