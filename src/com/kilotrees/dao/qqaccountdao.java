package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.qqaacount_info;
import com.kilotrees.services.ErrorLog_service;
import com.kilotrees.util.StringUtil;

/**
 * result : 0:nouse -1:login_failure -2:login_again 1:success 2:inuse
 * 
 * @author Administrator 2019年1月29日 下午1:17:58
 */
public class qqaccountdao {
	private static Logger log = Logger.getLogger(qqaccountdao.class);

	/**
	 * "select top 1 * from " + tableName + " where status=0"
	 */
	public synchronized static qqaacount_info getQQAccount(String tableName) {
		Connection con = connectionmgr.getInstance().getConnection();
		qqaacount_info qqinfo = new qqaacount_info();
		String sql = "select top 1 * from " + tableName + " where status=0";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				// qqinfo = new qqaacount_info();
				qqinfo.setQqnum(rs.getString("qqnum"));
				qqinfo.setPass(rs.getString("pass"));
				qqinfo.setAutoid(rs.getLong("autoid"));
				qqinfo.setGettime(new Date());
			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
			return null;
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return qqinfo;
	}

	/**
	 * "select top 1 * from " + tableName + " where status=0"
	 */
	public static qqaacount_info getQQAccountByQQNum(String tableName, String qqnum) {
		Connection con = connectionmgr.getInstance().getConnection();
		qqaacount_info qqinfo = null;
		String sql = "select top 1 * from " + tableName + " where qqnum = ?";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, qqnum);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				qqinfo = new qqaacount_info();
				qqinfo.setQqnum(rs.getString("qqnum"));
				qqinfo.setPass(rs.getString("pass"));
				qqinfo.setGettime(new Date(rs.getTimestamp("gettime").getTime()));
				qqinfo.setStauts(rs.getInt("status"));
				qqinfo.setAutoid(rs.getLong("autoid"));
				qqinfo.setResult(rs.getInt("result"));
				qqinfo.setInfo(rs.getString("info"));
			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return qqinfo;
	}

	public synchronized static qqaacount_info getQQAccountFirstTime(String tableName) {
		Connection con = connectionmgr.getInstance().getConnection();
		qqaacount_info qqinfo = null;
		String sql = "select top 1 * from " + tableName + " where status=0 and gettime is null";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				qqinfo = new qqaacount_info();
				String qqnum = rs.getString("qqnum");
				String pass = rs.getString("pass");
				if (StringUtil.isStringEmpty(qqnum) || StringUtil.isStringEmpty(pass)) {
					return null;
				}
				qqinfo.setQqnum(qqnum);
				qqinfo.setPass(pass);
				qqinfo.setAutoid(rs.getLong("autoid"));
				qqinfo.setGettime(new Date());
			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
			return null;
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return qqinfo;
	}

	/**
	 * "update " + tableName + " set gettime=?,status=1,result=2,info='inuse' " +
	 * "where qqnum=?"
	 * 
	 * @param tableName
	 * @param qqi
	 */
	public synchronized static void updateFetchStatus(String tableName, qqaacount_info qqi) {
		String sql = "update " + tableName + " set gettime=?,status=1,result=2,info='inuse' " + "where qqnum=?";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setTimestamp(1, new Timestamp(qqi.getGettime().getTime()));
			ps.setString(2, qqi.getQqnum());
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	/**
	 * "update " + tableName + " set autoid=?,result=?,info=?" + "where qqnum=?";
	 * 
	 * @param tableName
	 * @param qqi
	 */
	public synchronized static void updateResultStatus(String tableName, qqaacount_info qqi) {
		String sql = "update " + tableName + " set autoid=?,result=?,info=?" + "where qqnum=?";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setLong(1, qqi.getAutoid());
			ps.setInt(2, qqi.getResult());
			ps.setString(3, qqi.getInfo());
			ps.setString(4, qqi.getQqnum());
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	/**
	 * 登陆结果不成功时，回复QQ号状态，让它下次继续被拿到
	 * 
	 * @param qqi
	 */
	public synchronized static void resetStatus(qqaacount_info qqi) {
		String sql = "update tb_qqacount set status=0,result=0,info='nouse' where qqnum=?";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, qqi.getQqnum());
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	public synchronized static void updateResultFailure(String qqnum,String tableName) {
		String sql = "update "+ tableName +" set result = -1 , info = 'login_failure' where qqnum=?";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, qqnum);
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	public synchronized static void updateResultSuccess(String tableName,qqaacount_info QQAccount) {
		String sql = "update "+ tableName +" set result = 6 , info = 'login_success' where qqnum=?";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, QQAccount.getQqnum());
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}
	
	public synchronized static void updateRequestAgain(String qqnum,String tableName) {
		String sql = "update "+ tableName +" set result = -2 , info = 'login_again' where qqnum=?";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, qqnum);
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	public synchronized static void updateSuccess(String qqnum,String tableName) {
		String sql = "update "+ tableName +" set result = 1 , info = 'success' where qqnum=?";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, qqnum);
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

}
