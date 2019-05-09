package com.kilotrees.dao.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;

import com.kilotrees.dao.connectionmgr;
import com.kilotrees.services.ErrorLog_service;

public class QQActiveTaskDao implements ITaskDao {

	private static Logger log = Logger.getLogger(QQActiveTaskDao.class);

	private static final String tableName = "tb_qqacount";

	@Override
	public int fetchActiveCount() {
		String activeTableName = tableName;

		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		String sql = "select count(*) from " + activeTableName + " where [status] > -3";
		try {
			ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			rs.next();
			int count = rs.getInt(1);
			rs.close();

			return count;
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}

		return 0;
	}

	@Override
	public Object fetchOneActiveAccount() {
		return fetchOneQQActiveAccount();
	}

	/*
	 * Private Methods
	 */
	private static QQActiveModel fetchOneQQActiveAccount() {
		QQActiveModel model = null;
		
//		model = fetchOneQQActiveAccountSuccess();
//		if (model != null) {
//			return model;
//		}
		
		
		String activeTableName = tableName;

		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;

		try {
			// 1.lastfetchtime = null and status > -3
			if (model == null) {
				String sql = "select top 1 * from " + activeTableName + " where [lastFetchTime] is null and status > -3";

				model = assembleQQActiveModelFromSQL(connection, sql);
			}

			// 2. lastlogintime = null and datediff(dd,lastfetchtime) > 2 and
			// status > -3
			if (model == null) {
				String fetchDayDiff = "lastLoginTime is null";
				String loginDayDiff = "DATEDIFF(DD, lastFetchTime, GETDATE()) > 0 ";
				String sql = "select top 1 * from " + activeTableName + " where " + fetchDayDiff + " and " + loginDayDiff + " and status > -3 ";

				model = assembleQQActiveModelFromSQL(connection, sql);
			}

			// 3.
			if (model == null) {
				String fetchDayDiff = "DATEDIFF(DD, lastFetchTime, GETDATE()) > 0";
				String loginDayDiff = "DATEDIFF(DD, lastLoginTime, GETDATE()) > 2";
				String sql = "select top 1 * from " + activeTableName + " where " + loginDayDiff + " and " + fetchDayDiff + " and status > -3";

				model = assembleQQActiveModelFromSQL(connection, sql);
			}

			// Got, update the [lastFetchTime] value
			if (model != null) {
				String sql = "update " + activeTableName + " set lastFetchTime = ? where qqnum = ?";
				try {
					ps = connection.prepareStatement(sql);
					ps.setTimestamp(1, new java.sql.Timestamp(new Date().getTime()));
					ps.setString(2, model.getQqnum());
					ps.execute();
				} catch (Exception e) {
					ErrorLog_service.system_errlog(e);
					log.error(e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}

		return model;
	}

	private static QQActiveModel assembleQQActiveModelFromSQL(Connection connection, String sql) {
		QQActiveModel model = null;
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			model = assembleQQActiveModelFromResultSet(rs);
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(null, ps);
		}
		return model;
	}

	private static QQActiveModel assembleQQActiveModelFromResultSet(ResultSet rs) {
		QQActiveModel model = null;
		try {
			if (rs.next()) {
				model = new QQActiveModel();
				model.setAutoid(rs.getLong("autoid"));
				model.setQqnum(rs.getString("qqnum"));
				model.setPass(rs.getString("pass"));
				model.setLastLoginTime(rs.getTimestamp("lastLoginTime") == null ? 0 : rs.getTimestamp("lastLoginTime").getTime());
				model.setLastFetchTime(rs.getTimestamp("lastFetchTime") == null ? 0 : rs.getTimestamp("lastFetchTime").getTime());
				model.setPhoneNum(rs.getString("phoneNum"));
				model.setEmail(rs.getString("email"));
				model.setAppinfo(rs.getString("appinfo"));
				model.setPhoneInfo(rs.getString("phoneinfo"));
				model.setStatus(rs.getInt("status"));
				model.setPriceLevel(rs.getInt("priceLevel"));
				model.setComment(rs.getString("comment"));
			}
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		}
		return model;
	}

	public static int updateResultError(String qqnum) {
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "update " + tableName + " set status = -999 where qqnum = ?";
		int num = 0;
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, qqnum);
			num = ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return num;
	}

	public static int updateResultFailure(String qqnum) {
		QQActiveModel model = getQQActiveModelByQQNum(qqnum);
		if (model == null) {
			throw new RuntimeException("QQActiveTaskDao.updateResultSuccess() : model is null !!");
		}
		int status = model.getStatus();
		String sql = "update " + tableName + " set status = 0 where qqnum = ?";
		if (status <= 0) {
			sql = "update " + tableName + " set status = status-1 where qqnum = ?";
		}
		Connection con = connectionmgr.getInstance().getConnection();
		int num = 0;
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, qqnum);
			num = ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return num;
	}

	public static int updateResultSuccess(String qqnum, String appInfo, String phoneInfo) {
		QQActiveModel model = getQQActiveModelByQQNum(qqnum);
		if (model == null) {
			throw new RuntimeException("QQActiveTaskDao.updateResultSuccess() : model is null !!");
		}
		int status = model.getStatus();
		String sql = "update " + tableName + " set status = " + (status < 0 ? "0" : "status+1") + ",[lastLoginTime]=?,appinfo=?,[phoneInfo]=? where qqnum = ?";
		Connection con = connectionmgr.getInstance().getConnection();
		int num = 0;
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setTimestamp(1, new Timestamp(new Date().getTime()));
			ps.setString(2, appInfo);
			ps.setString(3, phoneInfo);
			ps.setString(4, qqnum);
			num = ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return num;
	}

	public static QQActiveModel getQQActiveModelByQQNum(String qqnum) {
		QQActiveModel model = null;
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select top 1 * from " + tableName + " where qqnum = ?";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, qqnum);
			ResultSet rs = ps.executeQuery();
			model = assembleQQActiveModelFromResultSet(rs);
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return model;
	}

	/**
	 * @param qqnum
	 */
	public static Integer updateResultPassChanged(String qqnum) {
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "update " + tableName + " set status = -99 where qqnum = ?";
		int num = 0;
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, qqnum);
			num = ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return num;
	}
	
	public static QQActiveModel fetchOneQQActiveAccountSuccess() {
		QQActiveModel model = null;
		String activeTableName = tableName;
		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		String select = "select top 1 * from "+ tableName +" where lastLoginTime is not null and status >=1 and comment is null";
		String update = "update "+ tableName +" set comment = 'used' where autoid = ?";
		try {
			ps = connection.prepareStatement(select);
			ResultSet rs = ps.executeQuery();
			model = assembleQQActiveModelFromResultSet(rs);
			rs.close();
			ps.close();
			ps = connection.prepareStatement(update);
			ps.setLong(1, model.getAutoid());
			ps.executeUpdate();
			
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}
		return model;
	}
	
	
	
	
}
