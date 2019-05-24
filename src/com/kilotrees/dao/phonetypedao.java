package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.kilotrees.model.bo.error_result;
import com.kilotrees.model.po.phonetype;
import com.kilotrees.services.ErrorLog_service;

/**
 * 获取机型列表信息，因为有权重，这里不能随机分配，由phonetypeservice类处理
 */
public class phonetypedao {
	private static Logger log = Logger.getLogger(phonetypedao.class);

	/**
	 * 从phonetype表拿100条机器信息
	 * 
	 * @return
	 */
	public static synchronized ArrayList<phonetype> getPhoneInfoList() {
		return getPhoneInfoList(phonetype.tablename);
	}

	/**
	 * SELECT top 100 * from " + tableName + " order by use_radio desc
	 * 
	 * @param tableName
	 * @return
	 */
	public static synchronized ArrayList<phonetype> getPhoneInfoList(String tableName) {
		ArrayList<phonetype> list = new ArrayList<phonetype>();
		Connection con = connectionmgr.getInstance().getConnection();
		phonetype pt;
		String sql = "SELECT top 100 * from " + tableName + " order by use_radio desc";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				pt = new phonetype();
				pt.setP_id(rs.getInt("p_id"));
				pt.setPhone_type(rs.getString("phone_type"));
				pt.setPhone_info(rs.getString("phone_info"));
				pt.setUse_radio(rs.getInt("use_radio"));
				list.add(pt);
			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}

		return list;
	}

	public static synchronized boolean checkPhoneInfo(String phone_type) {
		boolean exist = false;
		Connection con = connectionmgr.getInstance().getConnection();

		String sql = "SELECT top 1 * from " + phonetype.tablename + " where phone_type=?";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, phone_type);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				exist = true;
			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return exist;
	}

	public static synchronized void addPhoneInfo(String phone_type, String pInfo, String tableName) {
		Connection con = connectionmgr.getInstance().getConnection();
		if (tableName == null) {
			tableName = "tb_phonetype_tmp";
		}
		String sql = "insert " + tableName + " values(?,?,0)";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, phone_type);
			ps.setString(2, pInfo);
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}

	}

	public static synchronized void updatePhoneInfo(String phone_type, String pInfo, error_result err) {
		Connection con = connectionmgr.getInstance().getConnection();

		String sql = "update " + phonetype.tablename + " set phone_info=? where phone_type=?";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, pInfo);
			ps.setString(2, phone_type);
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
			err.setErr_code(error_result.System_sql);
			err.setErr_info(e.getMessage());
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}

	}

	/**
	 * 从阳江的系统中收集机型信息并加到临时表tb_phonetype_tmp中，此临时表和正式表相比，去掉了机型重复限制
	 * 
	 * @param phone_type
	 * @param pInfo
	 */
	public static synchronized void addPhoneInfo_tmp(String phone_type, String pInfo) {
		Connection con = connectionmgr.getInstance().getConnection();

		String sql = "insert into " + "tb_phonetype" + " values(?,?,0)";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, phone_type);
			ps.setString(2, pInfo);
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	public static synchronized ArrayList<phonetype> getOPPOPhoneInfo(String tableName,String key) {
		ArrayList<phonetype> list = new ArrayList<phonetype>();
		PreparedStatement ps = null;
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "SELECT  [p_id] ,[phone_type]  ,[phone_info]  ,[use_radio]   FROM " + tableName + "  where phone_type like '%" + key + "%'";
		try {
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				phonetype pt = new phonetype();
				pt.setP_id(rs.getInt("p_id"));
				pt.setPhone_type(rs.getString("phone_type"));
				pt.setPhone_info(rs.getString("phone_info"));
				pt.setUse_radio(rs.getInt("use_radio"));
				list.add(pt);
			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return list;
	}
	
	public static synchronized ArrayList<String> getPhoneInfo(String tableName,String key) {
		ArrayList<String> list = new ArrayList<String>();
		PreparedStatement ps = null;
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "SELECT  "+key+" FROM " + tableName ;
		try {
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				 String s = rs.getString(key);
				 list.add(s);
			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return list;
	}

}
