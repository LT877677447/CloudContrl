package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.sms_checkcode;
import com.kilotrees.services.ErrorLog_service;
import com.kilotrees.util.DateUtil;

/**
 * @author Administrator
 * 2019年4月2日 下午12:50:35 
 */
public class sms_checkcodedao {
	private static Logger log = Logger.getLogger(sms_checkcodedao.class);
    /**
     * 获取手机号后,插入sms_checkcode新记录
     * @param scc
     */
	public synchronized static void addSmsCheckCode(sms_checkcode scc) {
		String sql = "insert into " + sms_checkcode.tablename;
		sql += " values(?,?,?,?,?,'','',?,?,null,0,'',null,null,0)";
		// System.out.println(sql);
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setLong(1, scc.getSeqid());
			ps.setString(2, scc.getReq_dev());
			ps.setInt(3, scc.getAdv_id());
			ps.setString(4, scc.getMobile());
			ps.setInt(5, scc.getIssendsms());
			ps.setString(6, scc.getCheck_code_url());
			ps.setTimestamp(7, new java.sql.Timestamp(scc.getGetmo_time().getTime()));
			// ps.get
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}
	
	public synchronized static void sendSms(int seqid, String smstext, String result) {

		String sql = "update " + sms_checkcode.tablename + " set proc_result=3,issendsms=1, smstext=?," + "send_time=getdate() ";
		sql += "errinfo=? ";
		sql += "where seqid=? " + "and datediff(ss,getmo_time,getdate()) < ?";
		
		int timeout = ServerConfig.smscode_timeout;
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, smstext);
			ps.setString(2, result);
			ps.setInt(2, seqid);
			ps.setInt(3, timeout);
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	/**
	 * 平台回传sms验证码 (seqid, smstext, "")
	 * errinfo不能有换行,之前一直更新失败,但又不报错
	 * @param seqid
	 * @param smscode
	 */
	public synchronized static void passBackSmsCheckCode(Long seqid, String smstext, String errinfo) {

		String sql = "update " + sms_checkcode.tablename;
		sql += " set smstext=?,getcode_time=getdate(),proc_result=1 ";
		sql += "where seqid=? and errinfo=? " + "and datediff(ss,getmo_time,getdate()) < ?";
		int timeout = ServerConfig.smscode_timeout;
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, smstext);
			// ps.setString(2, smscode);
			ps.setLong(2, seqid);
			ps.setString(3, errinfo);
			ps.setInt(4, timeout);
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}

	/**
	 * 客户端提取短信,并更新fetch_time 
	 * @param seqid
	 * @return
	 */
	public synchronized static sms_checkcode devGetSmsCheckCode(Long seqid) {
		String sql = "select * from " + sms_checkcode.tablename + " where seqid=?";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		sms_checkcode scc = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setLong(1, seqid);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				scc = new sms_checkcode();
				scc.setSeqid(seqid);
				scc.setReq_dev(rs.getString("req_dev"));
				scc.setAdv_id(rs.getInt("adv_id"));
				scc.setSmstext(rs.getString("smstext"));
				scc.setMobile(rs.getString("mobile"));
				scc.setSmscode(rs.getString("smscode"));
				java.sql.Timestamp ts = rs.getTimestamp("getcode_time");
				if (ts != null)
					scc.setGetcode_time(new java.util.Date(ts.getTime()));
				scc.setErrcode(rs.getInt("errcode"));
				scc.setErrinfo(rs.getString("errinfo"));
				scc.setFetch_time(new Date());
			}
			rs.close();
			ps.close();
			sql = "update " + sms_checkcode.tablename + " set proc_result=2,fetch_time=getdate()" + " where seqid=?";
			ps = con.prepareStatement(sql);
			ps.setLong(1, seqid);
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		return scc;
	}

	/**5分钟刷一次
	 * @param timeoutofsec
	 */
	public static void processTimeoutSmscode(int timeoutofsec) {
		String sDate = DateUtil.getDateString(new Date());
		sDate = "超时:" + sDate;
		String sql = "update " + sms_checkcode.tablename + " set errcode=1,errinfo=?"
				+ " where proc_result = 0 "
				+ " and errcode = 0 "
				//+ "and proc_result = 0 "
				//+ "and getcode_time is null "
				//+ "and send_time is null "
				+ "and datediff(ss,getmo_time,getdate()) > ?";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, sDate);
			ps.setInt(2, timeoutofsec);
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}
	
	
	/**小旋风拿验证码成功
	 * @param sms_checkcode
	 */
	public static void XXFCode1(String smstext,Long seqid) {
		String sql = "update " + sms_checkcode.tablename;
		sql += " set smstext=?,getcode_time=getdate(),proc_result=1,errinfo='' ";
		sql += " where seqid=? ";
//		int timeout = serverconfig.smscode_timeout;
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, smstext);
			ps.setLong(2, seqid);
//			ps.setInt(3, timeout);
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}
	/**小旋风拿验证码失败
	 * @param sms_checkcode
	 * 时间限制SQL：and datediff(ss,getmo_time,getdate()) < ?
	 */
	public static void XXFCode2(String errinfo,Long seqid) {
		
		String sql = "update " + sms_checkcode.tablename;
		sql += " set getcode_time=getdate(),proc_result=1,errinfo=?,smstext='' ";
		sql += " where seqid=? ";
//		int timeout = serverconfig.smscode_timeout;
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, errinfo);
			ps.setLong(2, seqid);
//			ps.setInt(3, timeout);
			ps.execute();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
	}
	
	
	
}
