package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kilotrees.model.bo.TaskBase;
import com.kilotrees.model.bo.TaskCPARemain;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.advremaintask;
import com.kilotrees.services.ErrorLog_service;

/**
 * 留存任务表操作
 * 
 * @author Administrator
 *
 */
public class advremaintaskdao {

	private static Logger log = Logger.getLogger(advremaintaskdao.class);

	/**获取今天要做的留存任务，如果全部获取整个列表，太占内存，分组取其数量
	 * @return 每个任务当天要做的所有留存
	 */
//	public static synchronized HashMap<Integer, cparemainruntime> getTodayRemainTaskRunTime() {
//		HashMap<Integer, cparemainruntime> list = new HashMap<Integer, cparemainruntime>();
//		String sql = "select adv_id,sum(dotoday) from " + advremaintask.tablename + " where ";
//		sql += "dotoday >= 1 group by adv_id";
//		Connection con = connectionmgr.getInstance().getConnection();
//
//		PreparedStatement ps = null;
//		try {
//			ps = con.prepareStatement(sql);
//			ResultSet rs = ps.executeQuery();
//			while (rs.next()) {
//				cparemainruntime ai = new cparemainruntime();
//				ai.setAdv_id(rs.getInt(1));
//				//2018-12-3,由于分拆了 todocount，这里不用了
//				//ai.setTodocount(rs.getInt(2));
//				list.put(ai.getAdv_id(), ai);
//			}
//			rs.close();
//		} catch (Exception e) {
//			// e.printStackTrace();
//			ErrorLog_service.system_errlog(e);
//			log.error(e.getMessage(), e);
//		} finally {
//			connectionmgr.getInstance().closeConnection(con, ps);
//		}
//		return list;
//	}
	/**sql:select distinct adv_id from advremaintask.tablename where dotoday >= 1 order by adv_id
	 * @return
	 */
	public static synchronized ArrayList<Integer> getTodayRemainTaskRunTimeIds() {
		ArrayList<Integer> list = new  ArrayList<Integer>();
		String sql = "select distinct adv_id from " + advremaintask.tablename + " where ";
		sql += "dotoday >= 1 order by adv_id";
		Connection con = connectionmgr.getInstance().getConnection();

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.add(rs.getInt(1));
			}
			rs.close();
		} catch (Exception e) {
			// e.printStackTrace();
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return list;
	}

	/**
	 * 取今天要做留存旧的留存用户数和剩下的dotoday总数量,不是活跃数
	 * 
	 * @param adv_id
	 * @return
	 */
	public static synchronized int[] getTodayRemainOldUserCount(int adv_id) {
		int[] count = new int[2];
		// String shortDateStr = DateUtil.getShortDateString(new
		// java.util.Date());
		String sql = "select count(*),sum(dotoday) from " + advremaintask.tablename + " where ";
		sql += "adv_id = ? " + "and datediff(dd,newregtime,getdate()) != 0 " 
		        + "and todayopencount > 0 and dotoday >=0";
		        //这里好象效率太低
//				+ "and dotoday >=0 "// 有些已经完成了就变成0
//				+ "and firstremaintime < getdate() "
//				+ "and substring(remaininfo,DATEDIFF(dd,firstremaintime,getdate())+1,1) = '1'";
		Connection con = connectionmgr.getInstance().getConnection();

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, adv_id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()){
				count[0] = rs.getInt(1);
				count[1] = rs.getInt(2);
			}
			rs.close();
		} catch (Exception e) {
			// e.printStackTrace();
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}

		return count;
	}

	/**
	 * 取当天新增并且要做多次活跃的用户数目和dotoday总数
	 * 
	 * @param adv_id
	 * @return
	 */
	public static synchronized int[] getTodayRemainNewUserCount(int adv_id) {
		int[] count = new int[2];
		//
		// java.util.Date());
		String sql = "select count(*),sum(dotoday) from " + advremaintask.tablename + " where ";
		sql += "adv_id = ? and todayopencount > 0 "// 如果广告停掉，不算
				+ "and datediff(dd,newregtime,getdate()) = 0 and dotoday >=0";
		Connection con = connectionmgr.getInstance().getConnection();

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, adv_id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()){
				count[0] = rs.getInt(1);
				count[1] = rs.getInt(2);
			}
			rs.close();
		} catch (Exception e) {
			// e.printStackTrace();
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}

		return count;
	}

	/**
	 * 取当前广告剩下的活跃总数
	 * 
	 * @param advid
	 * @return
	 */
	public static synchronized int getTodayRemainTaskCountByAdvId(int advid) {
		int count = 0;
		// String shortDateStr = DateUtil.getShortDateString(new
		// java.util.Date());
		String sql = "select sum(dotoday) from " + advremaintask.tablename + " where ";
		sql += "dotoday >= 1 and advid = ?";
		Connection con = connectionmgr.getInstance().getConnection();

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setLong(1, advid);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				count = rs.getInt(1);
			rs.close();
		} catch (Exception e) {
			// e.printStackTrace();
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}

		return count;
	}

	/**
	 * 取今天起７天内每天剩余留存数． 注意这个留存数没有乘以每天打开次数
	 * 
	 * @param adv_id
	 * @param daycount
	 * @return
	 */
	public static synchronized int[] getSpectDaysRemainCount(int adv_id, int daycount) {
		int[] dayscount = new int[daycount];
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			for (int i = 0; i < daycount; i++) {
				Calendar date = Calendar.getInstance();
				date.add(Calendar.DATE, i);
				date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE), 0, 0, 0);
				String sql = "select count(*) from " + advremaintask.tablename + " where dotoday > -1 and "
						+ "adv_id=? and " + "substring(remaininfo,DATEDIFF(dd,firstremaintime,?)+1,1) = '1'";
				// log.info(sql);
				ps = con.prepareStatement(sql);
				ps.setLong(1, adv_id);
				ps.setTimestamp(2, new java.sql.Timestamp(date.getTime().getTime()));
				ResultSet rs = ps.executeQuery();
				if (rs.next())
					dayscount[i] = rs.getInt(1);
				rs.close();
				ps.close();
			}
		} catch (Exception e) {
			// e.printStackTrace();
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return dayscount;
	}

	/**
	 * 取第一天做留存的时间
	 * 
	 * @param adv_id
	 * @return
	 */
	public static synchronized java.util.Date getFirstRemainDay(int adv_id) {
		Date d = null;
		String sql = "select top 1 firstremaintime from " + advremaintask.tablename
				+ " where adv_id=? order by firstremaintime";
		Connection con = connectionmgr.getInstance().getConnection();

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, adv_id);
			// log.info(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				d = new java.util.Date(rs.getTimestamp("firstremaintime").getTime());
			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return d;
	}

	/**
	 * 取当前最大的rid值，以后每次取留存时按倒序方式(为了更加随机)方式取最大的rid
	 */
	public static synchronized int getTodayRemainTaskMaxRid() {
		int rid = 0;
		String sql = "select max(rid) from " + advremaintask.tablename + " where ";
		sql += "dotoday >= 1";
		Connection con = connectionmgr.getInstance().getConnection();

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				rid = rs.getInt(1);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return rid + 1;
	}

	/**
	 * 2018-12-29
	 * 指定广告id随机取一个今天还没有做过的留存(dotoday = todayopencount)，对于留存来说，没有做过的优先处理
	 * 
	 */
	public static synchronized advremaintask getTodayRemainTaskNoFetch(int adv_id) {
		// ArrayList<advremaintask> list = new ArrayList<advremaintask>();
		advremaintask at = null;
		// String shortDateStr = DateUtil.getShortDateString(new
		// java.util.Date());
		String sql = "select top 1";
		sql += " * " + "from " + advremaintask.tablename + " where ";
		sql += "dotoday >= 1 and dotoday = todayopencount and adv_id = " + adv_id + " ";
		// 对于一天打开多次的广告，防止同一个留存很短时间内就做第二次激活,至少大于半小时(1800秒)
		//2018-12-9 这里改成由配置文件来设置，半小时感觉太长了
		int reopent_timeout = ServerConfig.getRemainReopenTimeout();
		sql += "and datediff(ss,lastfetchday,getdate()) > " + reopent_timeout + " ";;
		sql += "order by NEWID()";
		Connection con = connectionmgr.getInstance().getConnection();

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				at = new advremaintask();
				at.setRid(rs.getInt("rid"));
				at.setAutoid(rs.getLong("autoid"));
				at.setAdv_id(rs.getInt("adv_id"));
				at.setDev_tag(rs.getString("dev_tag"));
				at.setVpnid(rs.getInt("vpnid"));
				at.setLock_dev(rs.getInt("lock_dev"));
				at.setPhoneInfo(rs.getString("phoneinfo"));
				at.setTodayopencount(rs.getInt("todayopencount"));
				if (rs.getTimestamp("lastfetchday") != null)
					at.setLastfetchday(new Date(rs.getTimestamp("lastfetchday").getTime()));
				at.setNewregtime(new Date(rs.getTimestamp("newregtime").getTime()));
				at.setDotoday(rs.getInt("dotoday"));
				at.setAppinfo(rs.getString("appinfo"));
				// list.add(at);
			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return at;

	}
	/**
	 * 指定广告id随机取一个留存
	 */
	public static synchronized advremaintask getTodayRemainTask(int adv_id) {
		// ArrayList<advremaintask> list = new ArrayList<advremaintask>();
		advremaintask at = null;
		// String shortDateStr = DateUtil.getShortDateString(new
		// java.util.Date());
		String sql = "select top 1";
		sql += " * " + "from " + advremaintask.tablename + " where ";
		sql += "dotoday >= 1 and adv_id = " + adv_id + " ";
		// 对于一天打开多次的广告，防止同一个留存很短时间内就做第二次激活,至少大于半小时(1800秒)
		//2018-12-9 这里改成由配置文件来设置，半小时感觉太长了
		int reopent_timeout = ServerConfig.getRemainReopenTimeout();
		sql += "and datediff(ss,lastfetchday,getdate()) > " + reopent_timeout + " ";;
		sql += "order by NEWID()";
		Connection con = connectionmgr.getInstance().getConnection();

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				at = new advremaintask();
				at.setRid(rs.getInt("rid"));
				at.setAutoid(rs.getLong("autoid"));
				at.setAdv_id(rs.getInt("adv_id"));
				at.setDev_tag(rs.getString("dev_tag"));
				at.setVpnid(rs.getInt("vpnid"));
				at.setLock_dev(rs.getInt("lock_dev"));
				at.setPhoneInfo(rs.getString("phoneinfo"));
				at.setTodayopencount(rs.getInt("todayopencount"));
				if (rs.getTimestamp("lastfetchday") != null)
					at.setLastfetchday(new Date(rs.getTimestamp("lastfetchday").getTime()));
				at.setNewregtime(new Date(rs.getTimestamp("newregtime").getTime()));
				at.setDotoday(rs.getInt("dotoday"));
				at.setAppinfo(rs.getString("appinfo"));
				// list.add(at);
			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return at;

	}

	/**
	 * 2018-11-12 初始化留存打开次数时用到
	 * 返回该adv_id对应的advremaintask列表
	 * @param adv_id
	 * @param minRid
	 * @param count
	 * @return
	 */
	public static synchronized ArrayList<advremaintask> getTodayRemainTaskList(int adv_id, int minRid, int count) {
		// ArrayList<advremaintask> list = new ArrayList<advremaintask>();
		advremaintask at = null;
		ArrayList<advremaintask> ls = new ArrayList<advremaintask>();
		String sql = "select top " + count;
		sql += " * " + "from " + advremaintask.tablename + " where ";
		sql += "dotoday >= 1 and adv_id = " + adv_id + " ";
		sql += "and rid > " + minRid;
		sql += " order by rid";
		Connection con = connectionmgr.getInstance().getConnection();

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				at = new advremaintask();
				at.setRid(rs.getInt("rid"));
				at.setAutoid(rs.getLong("autoid"));
				at.setAdv_id(rs.getInt("adv_id"));
				at.setDev_tag(rs.getString("dev_tag"));
				// at.setVpnid(rs.getInt("vpnid"));
				// at.setLock_dev(rs.getInt("lock_dev"));
				// at.setPhoneInfo(rs.getString("phoneinfo"));
				at.setTodayopencount(rs.getInt("todayopencount"));
				if (rs.getTimestamp("lastfetchday") != null)
					at.setLastfetchday(new Date(rs.getTimestamp("lastfetchday").getTime()));
				at.setNewregtime(new Date(rs.getTimestamp("newregtime").getTime()));
				at.setDotoday(rs.getInt("dotoday"));
				ls.add(at);
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

	/**
	 * 2018-11-12 初始化留存打开次数时用到
	 * @param ls
	 */
	public static synchronized void initTodayRemainTaskOpenCount(ArrayList<advremaintask> ls) {
		String sql = "update " + advremaintask.tablename;
		// modify 2018-10-25，不在这里更新dotoday
		// sql += " set lastfetchday=getdate(),dotoday=?";
		// sql += " where rid=?";
		sql += " set todayopencount=?,dotoday=?";
		sql += " where rid=?";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			con.setAutoCommit(false);
			ps = con.prepareStatement(sql);
			for (advremaintask art : ls) {
				ps.setInt(1, art.getTodayopencount());
				ps.setInt(2, art.getDotoday());
				ps.setInt(3, art.getRid());
				ps.addBatch();
			}
			// ps = null;
			ps.executeBatch();
			con.commit();
			con.setAutoCommit(true);
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	/**
	 * lock_dev指定广告id随机取一个留存，不考虑locked 无用
	 */
//	public static synchronized advremaintask getTodayRemainLockedTask(int adv_id, String dev_tag) {
//		// ArrayList<advremaintask> list = new ArrayList<advremaintask>();
//		advremaintask at = null;
//		// String shortDateStr = DateUtil.getShortDateString(new
//		// java.util.Date());
//		String sql = "select top 1";
//		sql += " * " + "from " + advremaintask.tablename + " where ";
//		sql += "dotoday >= 1 and adv_id = " + adv_id + " ";
//		sql += "and dev_tag='" + dev_tag + "' ";
//		// 对于一天打开多次的广告，防止同一个留存很短时间内就做第二次激活,至少大于半小时(1800秒)
//		//2018-12-9 这里改成由配置文件来设置，半小时感觉太长了
//		int reopent_timeout = serviceconfig_service.getInstance().getConfig().getRemainReopenTimeout();
//		sql += "and datediff(ss,lastfetchday,getdate()) > " + reopent_timeout + " ";
//		sql += "and datediff(ss,lastfinishday,getdate()) > " + reopent_timeout + " ";
//		sql += "order by NEWID()";
//		Connection con = connectionmgr.getInstance().getConnection();
//
//		PreparedStatement ps = null;
//		try {
//			ps = con.prepareStatement(sql);
//			ResultSet rs = ps.executeQuery();
//			if (rs.next()) {
//				at = new advremaintask();
//				at.setRid(rs.getInt("rid"));
//				at.setAutoid(rs.getLong("autoid"));
//				at.setAdv_id(rs.getInt("adv_id"));
//				at.setDev_tag(rs.getString("dev_tag"));
//				at.setVpnid(rs.getInt("vpnid"));
//				at.setLock_dev(rs.getInt("lock_dev"));
//				at.setPhoneInfo(rs.getString("phoneinfo"));
//				at.setTodayopencount(rs.getInt("todayopencount"));
//				if (rs.getTimestamp("lastfetchday") != null)
//					at.setLastfetchday(new Date(rs.getTimestamp("lastfetchday").getTime()));
//				at.setNewregtime(new Date(rs.getTimestamp("newregtime").getTime()));
//				at.setDotoday(rs.getInt("dotoday"));
//				// list.add(at);
//			}
//			rs.close();
//		} catch (Exception e) {
//			log.error(e.getMessage(), e);
//		} finally {
//			connectionmgr.getInstance().closeConnection(con, ps);
//		}
//		return at;
//
//	}

	/**
	 * 服务器每做一定数目(100)的新增后，马上计算哪些需要做留存，把留存写入此表
	 * 如果Dayopencount大于1，则要把dotoday设为Dayopencount-1,马上做活跃
	 * 
	 * @param at
	 */
	public static synchronized void addNewRemainTask(advremaintask at) {
		String sql = "insert into " + advremaintask.tablename;
		sql += " values(?,?,?,?,?,?,?,?,?,?,   ?,?,?,?,'2118-10-26','',?)";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setLong(1, at.getAutoid());
			ps.setInt(2, at.getAdv_id());
			ps.setString(3, at.getDev_tag());
			ps.setInt(4, at.getVpnid());
			ps.setInt(5, at.getLock_dev());
			ps.setString(6, at.getPhoneInfo());
			ps.setTimestamp(7, new java.sql.Timestamp(at.getLastfinishday().getTime()));
			ps.setTimestamp(8, new java.sql.Timestamp(at.getLastfetchday().getTime()));
			// ps.setInt(9, at.getDayopencount());
			ps.setString(10, at.getRemaininfo());
			ps.setTimestamp(11, new java.sql.Timestamp(at.getFirstremaintime().getTime()));
			ps.setTimestamp(12, new java.sql.Timestamp(at.getLastremaintime().getTime()));
			// 设置dotoday
			//2018-11-30，这里的dayopencount在放入缓存表前已经减1，不需要再减了
			if (at.getTodayopencount() >= 1) {
//				ps.setInt(13, at.getTodayopencount() - 1);
//				ps.setInt(9, at.getTodayopencount() - 1);
				ps.setInt(13, at.getTodayopencount());
				ps.setInt(9, at.getTodayopencount());
			} else {
				ps.setInt(13, 0);
				ps.setInt(9, 0);
			}
			ps.setTimestamp(14, new java.sql.Timestamp(at.getNewregtime().getTime()));
			ps.setString(15, at.getAppinfo());
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	/**
	 * 完成留存任务后，更新today字段,减去1，并设置lastfinishday为当前时间
	 * 这里要注意dotoday可能同步问题会变成负数，如果变成-1，这个留存以后就不会执行了． 重新复盖appinfo和phoneinfo
	 * 
	 * @param at
	 */
	public static synchronized void finisthTodayRemainTask(TaskBase task) {
		String sql = "update " + advremaintask.tablename;
		sql += " set lastfinishday=getdate(),dotoday=dotoday-1";
		sql += ",phoneinfo=?,appinfo=?";
		sql += " where rid=? and dotoday > 0";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			
			ps = con.prepareStatement(sql);
			
			JSONObject phoneInfo = task.getPhoneInfo();
			JSONObject appInfo = task.getAppInfo();
			ps.setString(1, phoneInfo != null ? phoneInfo.toString() : "{}");
			ps.setString(2, appInfo != null ? appInfo.toString() : "{}");
			
			ps.setInt(3, ((TaskCPARemain)task).getRid());
			ps.execute();
			
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	/**
	 * 设备取留存任务后，更新lastfetchday字段为当前时间，
	 * 如果今天第一次取，重新留存任务每天打开数量随机，如果每个都打开同样次数就太死板(2018-10-25后不用这种方式了)
	 * 
	 * @param at
	 */
	public static synchronized void fetchTodayRemainTask(advremaintask at) {
		String sql = "update " + advremaintask.tablename;
		// modify 2018-10-25，不在这里更新dotoday
		// sql += " set lastfetchday=getdate(),dotoday=?";
		// sql += " where rid=?";
		sql += " set lastfetchday=getdate()";
		sql += " where rid=?";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			// 2018-10-25
			// ps.setInt(1, at.getDotoday());
			ps.setInt(1, at.getRid());
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	/**
	 * 停止tb_advtaskinfo表中isoffline=2的广告留存
	 * 
	 * @param advid
	 */
	public static synchronized void updateRemainTaskoffline2(int advid) {
		String sql = "update " + advremaintask.tablename;
		sql += " set dotoday = -2,stoptime=getdate() where adv_id=?";
		// sql += " where rid=?";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, advid);
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	/**
	 * 初始化时执行存储过程：把过时的和已经停止任务留存autoid放到tb_delautoid表中，并且把dotoday设为-1
	 * 计算每条记录今天是否要做留存，如果需要把dayopencount,dotoday暂时设为1，如果不需要就设为0
	 * @return
	 */
	public static synchronized boolean proc_init() {
		// String sql = "";
		Connection con = null;
		Statement ps = null;

		try {
			con = connectionmgr.getInstance().getConnection();
			ps = con.createStatement();
			// sql = "update tb_serverconfig set remainjobprocwork=1";

			// sql = "insert into tb_delautoid select
			// autoid,0,'proc_init:1',GETDATE() from tb_advremaintask where
			// lastremaintime < GETDATE() and dotoday > -1";//and autoid not
			// in(select del_autoid from tb_delautoid)";
			// ps.addBatch("update tb_serverconfig set remainjobprocwork=1");
			con.setAutoCommit(false);
			ps.addBatch(
					"insert into tb_delautoid select autoid,0,'proc_init:1',GETDATE() from tb_advremaintask  where lastremaintime < GETDATE() and  dotoday > -1");
			ps.addBatch(
					"insert into tb_delautoid select autoid,0,'proc_init:2',GETDATE() from tb_advremaintask  where adv_id in(select adv_id from tb_advtaskinfo where onlineflag = 2) and  dotoday > -1");
			ps.addBatch(
					"update tb_advremaintask set todayopencount=-1,dotoday = -1,stoptime = getdate(),ext='proc_init:3' where lastremaintime < GETDATE() and dotoday > -1");
			ps.addBatch(
					"update tb_advremaintask set todayopencount=-2,dotoday = -2 ,stoptime = getdate(),ext='proc_init:4' where adv_id in(select adv_id from tb_advtaskinfo where onlineflag = 2)  and dotoday > -1");
			ps.addBatch(
					"update tb_advremaintask set dotoday = 1,todayopencount = 1  where dotoday > -1 and substring(remaininfo,DATEDIFF(dd,firstremaintime,GETDATE())+1,1) = '1'");
			ps.addBatch(
					"update tb_advremaintask set dotoday = 0,todayopencount = 0 where  dotoday > -1 and substring(remaininfo,DATEDIFF(dd,firstremaintime,GETDATE())+1,1) <> '1'");
			// ps.addBatch("update tb_serverconfig set
			// remainjobdoday=GETDATE()");
			// ps.addBatch("update tb_serverconfig set remainjobprocwork=0");
			ps.executeBatch();
			con.commit();
			con.setAutoCommit(true);
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
			return false;
		} finally {
			try {
				if (!(con==null)) {
					con.close();
				}
				if (!(ps==null)) {
					ps.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public static void test() {
		class Entity {
			int rid;
			int todayopencount;

			public int getRid() {
				return rid;
			}

			public void setRid(int rid) {
				this.rid = rid;
			}

			public int getTodayopencount() {
				return todayopencount;
			}

			public void setTodayopencount(int todayopencount) {
				this.todayopencount = todayopencount;
			}

		}
		List<Entity> ls = new ArrayList<>();
		Entity entity = new Entity();
		for (int k = 1; k < 1001; k++) {
			entity.setRid(k);
			entity.setTodayopencount(k + 1);
			ls.add(entity);
		}
		String sql = "update " + " Table_1 ";
		// modify 2018-10-25，不在这里更新dotoday
		// sql += " set lastfetchday=getdate(),dotoday=?";
		// sql += " where rid=?";
		sql += " set todayopencount=? ";
		sql += " where rid=?";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		Date d1 = new Date();
		log.info("执行前：" + d1.getTime());
		try {
			con.setAutoCommit(false);
			ps = con.prepareStatement(sql);
			for (Entity art : ls) {
				ps.setInt(1, art.getTodayopencount());
				ps.setInt(2, art.getRid());
				ps.addBatch();
			}
			// ps = null;
			ps.executeBatch();
			con.commit();
			Date d2 = new Date();
			log.info("执行后：" + d2.getTime());
			long diff = d2.getTime() - d1.getTime();
			log.info(diff);
			con.setAutoCommit(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

}
