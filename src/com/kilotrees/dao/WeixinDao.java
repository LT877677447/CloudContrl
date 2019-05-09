/**
 * @author Administrator
 * 2019年1月19日 下午3:20:48 
 */
package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.tb_weixinaccount;
import com.kilotrees.model.po.tb_weixinlink;
import com.kilotrees.model.po.tb_weixinlinkFail;
import com.kilotrees.model.po.tb_weixinlog;
import com.kilotrees.services.ErrorLog_service;

public class WeixinDao {
	private static Logger log = Logger.getLogger(WeixinDao.class);
	private static WeixinDao inst = null;

	private WeixinDao() {

	}

	public static WeixinDao getInstance() {
		synchronized (WeixinDao.class) {
			if (inst == null) {
				inst = new WeixinDao();
			}
		}
		return inst;
	}

	// ---------------------------------------------------------------------------------------------------------------------
	/**
	 * 查询所有tb_weixinlink order by id
	 * 
	 * @return
	 */
	public List<tb_weixinlink> getAllLink() {
		List<tb_weixinlink> list = new ArrayList<>();
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select * from " + tb_weixinlink.tablename + " order by id";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				tb_weixinlink link = new tb_weixinlink();
				link.setId(rs.getInt("id"));
				link.setLinkUrl(rs.getString("linkUrl"));
				link.setNeedCount(rs.getInt("needCount"));
				link.setCurrentCount(rs.getInt("currentCount"));
				link.setAddTime(new Date(rs.getTimestamp("addTime").getTime()));
				link.setComment(rs.getString("comment"));
				list.add(link);
			}
			rs.close();
			return list;
		} catch (SQLException e) {
			ErrorLog_service.system_errlog(e);
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return list;
	}

	/**
	 * 从comliete开始，查询fetchNumber条link
	 * 
	 * @param complete
	 * @param fetchNumber
	 * @return
	 */
	public List<tb_weixinlink> getRangeLink(int complete, int fetchNumber) {
		List<tb_weixinlink> list = new ArrayList<>();
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select top " + fetchNumber + " * from " + tb_weixinlink.tablename + " where id > ? and currentCount < needCount order by id asc";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, complete);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				tb_weixinlink link = new tb_weixinlink();
				link.setId(rs.getInt("id"));
				link.setLinkUrl(rs.getString("linkUrl"));
				link.setNeedCount(rs.getInt("needCount"));
				link.setCurrentCount(rs.getInt("currentCount"));
				link.setAddTime(new Date(rs.getTimestamp("addTime").getTime()));
				link.setComment(rs.getString("comment"));
				list.add(link);
			}
			rs.close();
		} catch (SQLException e) {
			ErrorLog_service.system_errlog(e);
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return list;
	}

	/**
	 * 从comliete开始，查询fetchNumber条link
	 * 
	 * @param complete
	 * @param fetchNumber
	 * @return
	 */
	public tb_weixinlink getSingleLink(String strLink) {
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select * from " + tb_weixinlink.tablename + " where linkUrl = ?";
		PreparedStatement ps = null;
		tb_weixinlink link = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, strLink);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				link = new tb_weixinlink();
				link.setId(rs.getInt("id"));
				link.setLinkUrl(rs.getString("linkUrl"));
				link.setNeedCount(rs.getInt("needCount"));
				link.setCurrentCount(rs.getInt("currentCount"));
				link.setAddTime(new Date(rs.getTimestamp("addTime").getTime()));
				link.setComment(rs.getString("comment"));
			}
			rs.close();
		} catch (SQLException e) {
			ErrorLog_service.system_errlog(e);
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return link;
	}

	/**
	 * 先拿以前失败的link来刷
	 * 
	 * @return
	 */
	public List<tb_weixinlinkFail> getFailLink(tb_weixinaccount account, int fetchNumber) {
		List<tb_weixinlinkFail> list = new ArrayList<>();
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select top " + fetchNumber + " * from " + tb_weixinlinkFail.tablename + "  where account = ? and failureNumber < 4";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, account.getAccount());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				tb_weixinlinkFail link = new tb_weixinlinkFail();
				link.setIdAccount(rs.getInt("idAccount"));
				link.setAccount(rs.getString("account"));
				link.setIdLink(rs.getInt("idLink"));
				link.setLinkUrl(rs.getString("linkUrl"));
				link.setFailureNumber(rs.getInt("failureNumber"));
				link.setLastFailTime(new Date(rs.getTimestamp("lastFailTime").getTime()));
				list.add(link);
			}
			rs.close();
		} catch (SQLException e) {
			ErrorLog_service.system_errlog(e);
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return list;
	}
	
	/**
	 * 先拿以前失败的link来刷
	 * 
	 * @return
	 */
	public tb_weixinlinkFail getSingleFailLink(tb_weixinaccount account,tb_weixinlink link) {
		List<tb_weixinlinkFail> list = new ArrayList<>();
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select * from "+tb_weixinlinkFail.tablename+" where account=? and idLink=?";
		PreparedStatement ps = null;
		tb_weixinlinkFail failFink = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, account.getAccount());
			ps.setInt(2, link.getId());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				failFink = new tb_weixinlinkFail();
				failFink.setIdAccount(rs.getInt("idAccount"));
				failFink.setAccount(rs.getString("account"));
				failFink.setIdLink(rs.getInt("idLink"));
				failFink.setLinkUrl(rs.getString("linkUrl"));
				failFink.setFailureNumber(rs.getInt("failureNumber"));
				failFink.setLastFailTime(new Date(rs.getTimestamp("lastFailTime").getTime()));
			}
			rs.close();
		} catch (SQLException e) {
			ErrorLog_service.system_errlog(e);
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return failFink;
	}
	
	
	
	
	/**
	 * 新增tb_weixinlink
	 * 
	 * @param link
	 */
	public void insertWeixinLink(tb_weixinlink link) {
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "insert into " + tb_weixinlink.tablename + "values(?,?,?,?,?)";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, link.getLinkUrl());
			ps.setInt(2, link.getNeedCount());
			ps.setInt(3, link.getCurrentCount());
			ps.setTimestamp(4, new Timestamp(link.getAddTime().getTime()));
			ps.setString(5, link.getComment());
			ps.execute();
		} catch (SQLException e) {
			ErrorLog_service.system_errlog(e);
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	/**增加Fail表记录
	 * @param account
	 * @param link
	 */
	public Integer insertFail(tb_weixinlinkFail linkFail) {
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "insert into "+ tb_weixinlinkFail.tablename +" values (?,?,?,?,?,?)";
		PreparedStatement ps = null;
		int num = 0;
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, linkFail.getIdAccount());
			ps.setString(2, linkFail.getAccount());
			ps.setInt(3, linkFail.getIdLink());
			ps.setString(4, linkFail.getLinkUrl());
			ps.setInt(5, linkFail.getFailureNumber());
			ps.setTimestamp(6, new Timestamp(linkFail.getLastFailTime().getTime()));
			num = ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return num;
	}

	/**更新Fail表
	 * @param linkFail
	 */
	public Integer updateFail(tb_weixinlinkFail linkFail) {
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "update "+ tb_weixinlinkFail.tablename +" set idAccount=?,account=?,idLink=?,linkUrl=?,failureNumber=?,lastFailTime=?"
				+ " where account=? and idLink=?";
		PreparedStatement ps = null;
		int num = 0;
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, linkFail.getIdAccount());
			ps.setString(2, linkFail.getAccount());
			ps.setInt(3, linkFail.getIdLink());
			ps.setString(4, linkFail.getLinkUrl());
			ps.setInt(5, linkFail.getFailureNumber());
			ps.setTimestamp(6, new Timestamp(linkFail.getLastFailTime().getTime()));
			ps.setString(7, linkFail.getAccount());
			ps.setInt(8, linkFail.getIdLink());
			num = ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return num;
	}
	
	
	/**
	 * 查询id最大的link记录
	 * 
	 * @param newVal
	 */
	public tb_weixinlink getLastLink() {
		String sql = "select * from " + tb_weixinlink.tablename + " where id = (select MAX(id) from " + tb_weixinlink.tablename + ")";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		tb_weixinlink link = new tb_weixinlink();
		try {
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				link.setId(rs.getInt("id"));
				link.setLinkUrl(rs.getString("linkUrl"));
				link.setNeedCount(rs.getInt("needCount"));
				link.setCurrentCount(rs.getInt("currentCount"));
				link.setAddTime(new Date(rs.getTimestamp("addTime").getTime()));
				link.setComment(rs.getString("comment"));
			}
			rs.close();
		} catch (SQLException e) {
			ErrorLog_service.system_errlog(e);
			e.printStackTrace();
			return null;
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return link;
	}

	// -------------------------------------------------------------------------------------------------------------------
	/**
	 * 查询所有tb_weixinaccount order by id
	 * 
	 * @return
	 */
	public List<tb_weixinaccount> getAllAccount() {
		List<tb_weixinaccount> list = new ArrayList<>();
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select * from " + tb_weixinaccount.tablename + " order by id"; // order by adv_id
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				tb_weixinaccount account = new tb_weixinaccount();
				account.setId(rs.getInt("id"));
				account.setAccount(rs.getString("account"));
				account.setComplete(rs.getInt("complete"));
				account.setLastFetchTime(new Date(rs.getTimestamp("lastFetchTime").getTime()));
				account.setJoinTime(new Date(rs.getTimestamp("joinTime").getTime()));
				account.setComment(rs.getString("comment"));
				list.add(account);
			}
			rs.close();
		} catch (SQLException e) {
			ErrorLog_service.system_errlog(e);
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return list;
	}

	/**
	 * 通过account查询[tb_weixinaccount]
	 * 
	 * @return
	 */
	public tb_weixinaccount getSingleAccount(String strAccount) {
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select * from " + tb_weixinaccount.tablename + " where account = ?"; // order by adv_id
		PreparedStatement ps = null;
		try {
			tb_weixinaccount obj = null;
			ps = con.prepareStatement(sql);
			ps.setString(1, strAccount);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				obj = new tb_weixinaccount();
				obj.setId(rs.getInt("id"));
				obj.setAccount(rs.getString("account"));
				obj.setComplete(rs.getInt("complete"));
				obj.setLastFetchTime(new Date(rs.getTimestamp("lastFetchTime").getTime()));
				obj.setJoinTime(new Date(rs.getTimestamp("joinTime").getTime()));
				obj.setComment(rs.getString("comment"));
			}
			rs.close();
			return obj;
		} catch (SQLException e) {
			ErrorLog_service.system_errlog(e);
			e.printStackTrace();
			return null;
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	/**
	 * 新增Account
	 * 
	 * @return
	 */
	public tb_weixinaccount insertAccount(tb_weixinaccount account) {
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "insert into " + tb_weixinaccount.tablename + " values(?,?,?,?,?)";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, account.getAccount());
			ps.setInt(2, account.getComplete());
			ps.setTimestamp(3, new Timestamp(account.getLastFetchTime().getTime()));
			ps.setTimestamp(4, new Timestamp(account.getJoinTime().getTime()));
			ps.setString(5, account.getComment());
			ps.execute();
			return account;
		} catch (SQLException e) {
			ErrorLog_service.system_errlog(e);
			e.printStackTrace();
			return null;
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	/**
	 * 更新Account
	 * 
	 * @return
	 */
	public synchronized Integer updateAccount(tb_weixinaccount account) {
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "update " + tb_weixinaccount.tablename + " set account=?,complete=?,lastFetchTime=?,joinTime=?,comment=? " + " where id = ?";
		PreparedStatement ps = null;
		int num = 0;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, account.getAccount());
			ps.setInt(2, account.getComplete());
			ps.setTimestamp(3, new Timestamp(account.getLastFetchTime().getTime()));
			ps.setTimestamp(4, new Timestamp(account.getJoinTime().getTime()));
			ps.setString(5, account.getComment());
			ps.setInt(6, account.getId());
			num = ps.executeUpdate();
		} catch (SQLException e) {
			ErrorLog_service.system_errlog(e);
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return num;
	}

	/**
	 * 更新link
	 * 
	 * @param link
	 * @return
	 */
	public synchronized void updateLink(tb_weixinlink link) {
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "update " + tb_weixinlink.tablename + " set linkUrl=?,needCount=?,currentCount=?,addTime=?,comment=? " + " where id = ?";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, link.getLinkUrl());
			ps.setInt(2, link.getNeedCount());
			ps.setInt(3, link.getCurrentCount());
			ps.setTimestamp(4, new Timestamp(link.getAddTime().getTime()));
			ps.setString(5, link.getComment());
			ps.setInt(6, link.getId());
			ps.execute();
		} catch (SQLException e) {
			ErrorLog_service.system_errlog(e);
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	// --------------------------------------------------------------------------------------------------------------------------------
	/**
	 * link全部做完了，删除所有account的该link
	 * 
	 * @param link
	 */
	public synchronized Integer deleteWeixinLinkFail(tb_weixinlink link) {
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "delete from " + tb_weixinlinkFail.tablename + " where idLink = ?";
		PreparedStatement ps = null;
		int num = 0;
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, link.getId());
			num = ps.executeUpdate();
		} catch (SQLException e) {
			ErrorLog_service.system_errlog(e);
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return num;
	}

	/**
	 * 查询所有tb_weixinlog order by id
	 * 
	 * @return
	 */
	public List<tb_weixinlog> getAllLog() {
		List<tb_weixinlog> list = new ArrayList<>();
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select * from " + tb_weixinlog.tablename + " order by id";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				tb_weixinlog log = new tb_weixinlog();
				log.setId(rs.getInt("id"));
				log.setType(rs.getString("type"));
				log.setMessage(rs.getString("message"));
				log.setComment(rs.getString("comment"));
				log.setLogTime(new Date(rs.getTimestamp("logTime").getTime()));
				list.add(log);
			}
			rs.close();
			return list;
		} catch (SQLException e) {
			ErrorLog_service.system_errlog(e);
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return list;
	}

	/**
	 * 新增tb_weixinlog
	 * 
	 * @param log
	 */
	public void writeLog(tb_weixinlog log) {
		List<tb_weixinlog> list = new ArrayList<>();
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "insert into " + tb_weixinlog.tablename + " values(?,?,?,?)";
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, log.getType());
			ps.setString(2, log.getMessage());
			ps.setString(3, log.getComment());
			ps.setTimestamp(4, new Timestamp(log.getLogTime().getTime()));
			ps.execute();
		} catch (SQLException e) {
			ErrorLog_service.system_errlog(e);
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}

	}

	// --------------------------------------------------------------------------------------------------------------------------------
	/**
	 * 拿了link后,同时更新account和links
	 * 
	 * @param links
	 * @param account
	 */
	public void fetchUpdate(List<tb_weixinlink> links, tb_weixinaccount account) {
		Connection con = null;
		Statement preSta = null;
		try {
			con = connectionmgr.getInstance().getConnection();
			con.setAutoCommit(false);
			String updateAccount = "update " + tb_weixinaccount.tablename + " set complete=" + account.getComplete() + ",lastFetchTime='"
					+ new Timestamp(account.getLastFetchTime().getTime()) + "' where id=" + account.getId();
			preSta = con.createStatement();

			preSta.addBatch(updateAccount);

			String updateLinks = "";
			for (tb_weixinlink link : links) {
				updateLinks = "update " + tb_weixinlink.tablename + " set currentCount=" + link.getCurrentCount() + " where id=" + link.getId();
				preSta.addBatch(updateLinks);
			}
			preSta.executeBatch();
			con.commit();
			con.setAutoCommit(true);
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
			try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				if (!(con == null)) {
					con.close();
				}
				if (!(preSta == null)) {
					preSta.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 删除该account已经成功的link
	 * 
	 * @param account
	 * @param link
	 */
	public Integer deleteWeixinLinkFail(tb_weixinaccount account, tb_weixinlink link) {
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "delete from " + tb_weixinlinkFail.tablename + " where account = ? and idLink = ?";
		PreparedStatement ps = null;
		int num = 0;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, account.getAccount());
			ps.setInt(2, link.getId());
			num = ps.executeUpdate();
		} catch (SQLException e) {
			ErrorLog_service.system_errlog(e);
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return num;
	}

}