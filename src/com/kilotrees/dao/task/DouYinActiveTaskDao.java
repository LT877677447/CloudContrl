/**
 * @author Administrator
 * 2019年4月27日 下午5:35:13 
 */
package com.kilotrees.dao.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kilotrees.dao.connectionmgr;
import com.kilotrees.model.bo.TaskDouYinActive;
import com.kilotrees.model.bo.TaskWinXinActive;
import com.kilotrees.model.po.advgroup;
import com.kilotrees.services.ErrorLog_service;

public class DouYinActiveTaskDao implements ITaskDao {
	private static Logger log = Logger.getLogger(DouYinActiveTaskDao.class);

	private static boolean refreshed = true;
	private static Timer timer = null;
	private static final String tableName = "tb_DouYinAccount";

	@Override
	public int fetchActiveCount() {
		ArrayList<advgroup> list = new ArrayList<advgroup>();
		String sql = "select count(*) from " + tableName + " where lastFetchTime is null";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		int count = 0;
		try {
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				count = rs.getInt(1);

			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return count;
	}

	@Override
	public Object fetchOneActiveAccount() {
		if(!refreshed) {
			refresh();
			Calendar date = Calendar.getInstance();
			// 设置时间为 xx-xx-xx 00:00:00
			date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE), 0, 0, 0);
			// 第二天才执行
			date.add(Calendar.DAY_OF_MONTH, 1);
			// 一天的毫秒数
			long daySpan = 24 * 60 * 60 * 1000;
			// 得到定时器实例
			timer = new Timer();
			// 使用匿名内方式进行方法覆盖
			timer.schedule(new TimerTask() {
				public void run() {
					refresh();
				}
			}, date.getTime(), daySpan); // daySpan是一天的毫秒数，也是执行间隔
		}
		
		
		TaskDouYinActive model = null;

		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		int type = 0;
		try {
			// firstFetchTime is null
			if (model == null) {
				String sql = "select top 1 * from " + tableName + " where [firstFetchTime] is null and status != 2";
				model = assembleQQActiveModelFromSQL(connection, sql);
				type = 1;
			}

			// lastFetchTime is null
			if (model == null) {
				String sql = "select top 1 * from " + tableName + " where [lastFetchTime] is null and status != 2";
				model = assembleQQActiveModelFromSQL(connection, sql);
				type = 2;
			}

			// lastLoginTime is null
			if (model == null) {
				String sql = "select top 1 * from " + tableName + " where [lastLoginTime] is null and status != 2";
				model = assembleQQActiveModelFromSQL(connection, sql);
				type = 3;
			}

			// status = 3
			if (model == null) {
				String sql = "select top 1 * from " + tableName + " where status = 3";
				model = assembleQQActiveModelFromSQL(connection, sql);
				type = 4;
			}

			// none
			if (model == null) {
				type = 5;
			}

			// Got, update the [lastFetchTime] value
			if (model != null) {
				String sql = "update " + tableName;
				if (type == 1) {
					sql += " set firstFetchTime = ?,status = 2,comment = '提取原因：号码第一次提取(firstFetchTime is null)'";
				}
				if (type == 2) {
					sql += " set lastFetchTime = ?,status = 2,comment = '提取原因：号码第二次提取(lastFetchTime is null)'";
				}
				if (type == 3) {
					sql += " set lastFetchTime = ?,status = 2,comment = '提取原因：号码未被成功登陆过(lastLoginTime is null)'";
				}
				if (type == 4) {
					sql += " set lastFetchTime = ?,status = 2,comment = '提取原因：号码一般提取'";
				}
				sql += " where phoneNumber = ?";
				ps = connection.prepareStatement(sql);
				ps.setTimestamp(1, new java.sql.Timestamp(new Date().getTime()));
				ps.setString(2, model.getPhoneNumber());
				ps.execute();
			}
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}

		return model;
	}

	private void refresh()  {
		Connection connection = connectionmgr.getInstance().getConnection();
		String sql = "update "+tableName+" set status = 3,comment = '超时释放' where status = 2 and DATEDIFF(MI,lastFetchTime,GETDATE()) > 12";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.executeUpdate();
			ps.close();
			sql = "update "+tableName+" set status = 3,comment = '超时释放' where status = 2 and lastFetchTime is null and DATEDIFF(MI,firstFetchTime,GETDATE()) > 12";
			ps = connection.prepareStatement(sql);
			ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(),e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}
		refreshed = true;
	}

	private static TaskDouYinActive assembleQQActiveModelFromSQL(Connection connection, String sql) {
		TaskDouYinActive model = null;
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

	private static TaskDouYinActive assembleQQActiveModelFromResultSet(ResultSet rs) {
		TaskDouYinActive model = null;
		try {
			if (rs.next()) {
				model = new TaskDouYinActive();
				model.setAutoid(rs.getLong("autoid"));
				model.setPhoneNumber(rs.getString("phoneNumber"));
				model.setPass(rs.getString("pass"));
				
				Timestamp registTime = rs.getTimestamp("registTime");
				if (registTime != null) {
					model.setRegistTime(registTime.getTime());
				}
				
				Timestamp firstFetchTime = rs.getTimestamp("firstFetchTime");
				if (firstFetchTime != null) {
					model.setFirstFetchTime(firstFetchTime.getTime());
				}
				
				Timestamp lastFetchTime = rs.getTimestamp("lastFetchTime");
				if (lastFetchTime != null) {
					model.setLastFetchTime(lastFetchTime.getTime());
				}
				
				Timestamp lastLoginTime = rs.getTimestamp("lastLoginTime");
				if (lastLoginTime != null) {
					model.setLastLoginTime(lastLoginTime.getTime());
				}
				
				model.setAppInfo(new JSONObject(rs.getString("appinfo")));
				model.setPhoneInfo(new JSONObject(rs.getString("phoneInfo")));
				
				model.setStatus(rs.getInt("status"));
				model.setComment(rs.getString("comment"));
			}
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		}
		return model;
	}

	public static Integer SuccActive(String phoneNumber) {
		int num = 0;
		String sql = "update " + tableName + " set lastLoginTime = ?,status=4,comment='活跃成功(不再被释放)' where phoneNumber=?";
		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setTimestamp(1, new Timestamp(new Date().getTime()));
			ps.setString(2, phoneNumber);
			num = ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}
		return num;
	}
	
	public static Integer failActive(String phoneNumber) {
		int num = 0;
		String sql = "update " + tableName + " set status=3,comment='活跃失败' where phoneNumber=?";
		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, phoneNumber);
			num = ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}
		return num;
	}

}
