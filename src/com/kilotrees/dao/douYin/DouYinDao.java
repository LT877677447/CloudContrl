/**
 * @author Administrator
 * 2019年4月18日 下午10:29:46 
 */
package com.kilotrees.dao.douYin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;

import com.kilotrees.dao.connectionmgr;
import com.kilotrees.services.ErrorLog_service;

public class DouYinDao {
	private static Logger log = Logger.getLogger(DouYinDao.class);
	private static final String tableName = "tb_DouYinAccount";
	private static final String LogName = "tb_DouYinAccount_log";

	/**注册抖音成功，status=1
	 * @param phoneNumber
	 * @param pass
	 * @param registTime
	 * @param appinfo
	 * @param phoneInfo
	 * @param comment
	 */
	public static void newAccountSuccess(String autoid,String phoneNumber,String pass,Date registTime,String appinfo,String phoneInfo,int status,String comment) {
		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		String sql = "insert into " + tableName + "(autoid,phoneNumber,pass,registTime,appinfo,phoneInfo,status,comment) values(?,?,?,?,?,?,?,?)";
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, autoid);
			ps.setString(2, phoneNumber);
			ps.setString(3, pass);
			ps.setTimestamp(4, new Timestamp(registTime.getTime()));
			ps.setString(5, appinfo);
			ps.setString(6, phoneInfo);
			ps.setInt(7, status);
			ps.setString(8, comment);
			ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}
	}
	
	/**注册抖音修改密码失败，status=10
	 * @param phoneNumber
	 * @param pass
	 * @param registTime
	 * @param appinfo
	 * @param phoneInfo
	 * @param comment
	 */
	public static void newAccountFail(String zip_autoid,String phoneNumber,String pass,Date registTime,String appinfo,String phoneInfo,String comment) {
		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		String sql = "insert into " + tableName + "(zip_autoid,phoneNumber,pass,registTime,appinfo,phoneInfo,status,comment) values(?,?,?,?,?,?,?,?)";
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, zip_autoid);
			ps.setString(2, phoneNumber);
			ps.setString(3, pass);
			ps.setTimestamp(4, new Timestamp(registTime.getTime()));
			ps.setString(5, appinfo);
			ps.setString(6, phoneInfo);
			ps.setInt(7, 10);
			ps.setString(8, comment);
			ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}
	}
	
	/**抖音注册失败,logType=2,logSubType=1
	 * @param phoneNumber
	 * @param pass
	 * @param info
	 * @param appinfo
	 * @param phoneInfo
	 * @param logSubType
	 * @param logTime
	 */
	public static void RegistLog_1(String phoneNumber, String pass,String info,String appinfo,String phoneInfo,Date logTime) {
		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		String sql = "insert into " + LogName + "(phoneNumber,pass,info,appinfo,phoneInfo,logSubType,logType,logTime) values(?,?,?,?,?,?,?,?)";
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, phoneNumber);
			ps.setString(2, pass);
			ps.setString(3, info);
			ps.setString(4, appinfo);
			ps.setString(5, phoneInfo);
			ps.setInt(6, 1);
			ps.setInt(7, 2);
			ps.setTimestamp(8, new Timestamp(logTime.getTime()));
			ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}
	}
	
	/**XiaoXuanFeng拿手机号成功,logType=1,logSubType=1
	 * @param phoneNumber
	 * @param pass
	 * @param info
	 * @param appinfo
	 * @param phoneInfo
	 * @param logSubType
	 * @param logTime
	 */
	public static void SmsLog_1(String phoneNumber, String pass,String info,String appinfo,String phoneInfo,Date logTime) {
		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		String sql = "insert into " + LogName + "(phoneNumber,pass,info,appinfo,phoneInfo,logSubType,logType,logTime) values(?,?,?,?,?,?,?,?)";
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, phoneNumber);
			ps.setString(2, pass);
			ps.setString(3, info);
			ps.setString(4, appinfo);
			ps.setString(5, phoneInfo);
			ps.setInt(6, 1);
			ps.setInt(7, 1);
			ps.setTimestamp(8, new Timestamp(logTime.getTime()));
			ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}
		
	} 
	
	/**XiaoXuanFeng拿手机号通讯成功，但没拿到手机号,logType=1,logSubType=2
	 * @param phoneNumber
	 * @param pass
	 * @param info
	 * @param appinfo
	 * @param phoneInfo
	 * @param logSubType
	 * @param logTime
	 */
	public static void SmsLog_2(String phoneNumber, String pass,String info,String appinfo,String phoneInfo,Date logTime) {
		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		String sql = "insert into " + LogName + "(phoneNumber,pass,info,appinfo,phoneInfo,logSubType,logType,logTime) values(?,?,?,?,?,?,?,?)";
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, phoneNumber);
			ps.setString(2, pass);
			ps.setString(3, info);
			ps.setString(4, appinfo);
			ps.setString(5, phoneInfo);
			ps.setInt(6, 2);
			ps.setInt(7, 1);
			ps.setTimestamp(8, new Timestamp(logTime.getTime()));
			ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}
	} 
	
	/**XiaoXuanFeng拿手机号通讯失败,logType=1,logSubType=3
	 * @param phoneNumber
	 * @param pass
	 * @param info
	 * @param appinfo
	 * @param phoneInfo
	 * @param logSubType
	 * @param logTime
	 */
	public static void SmsLog_3(String phoneNumber, String pass,String info,String appinfo,String phoneInfo,Date logTime) {
		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		String sql = "insert into " + LogName + "(phoneNumber,pass,info,appinfo,phoneInfo,logSubType,logType,logTime) values(?,?,?,?,?,?,?,?)";
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, phoneNumber);
			ps.setString(2, pass);
			ps.setString(3, info);
			ps.setString(4, appinfo);
			ps.setString(5, phoneInfo);
			ps.setInt(6, 3);
			ps.setInt(7, 1);
			ps.setTimestamp(8, new Timestamp(logTime.getTime()));
			ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}
	} 
	
	/**XiaoXuanFeng拿验证码成功,logType=1,logSubType=4
	 * @param phoneNumber
	 * @param pass
	 * @param info
	 * @param appinfo
	 * @param phoneInfo
	 * @param logSubType
	 * @param logTime
	 */
	public static void SmsLog_4(String phoneNumber, String pass,String info,String appinfo,String phoneInfo,Date logTime) {
		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		String sql = "insert into " + LogName + "(phoneNumber,pass,info,appinfo,phoneInfo,logSubType,logType,logTime) values(?,?,?,?,?,?,?,?)";
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, phoneNumber);
			ps.setString(2, pass);
			ps.setString(3, info);
			ps.setString(4, appinfo);
			ps.setString(5, phoneInfo);
			ps.setInt(6, 4);
			ps.setInt(7, 1);
			ps.setTimestamp(8, new Timestamp(logTime.getTime()));
			ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}
	} 
	
	/**XiaoXuanFeng拿验证码通讯成功，但没拿到验证码,logType=1,logSubType=5
	 * @param phoneNumber
	 * @param pass
	 * @param info
	 * @param appinfo
	 * @param phoneInfo
	 * @param logSubType
	 * @param logTime
	 */
	public static void SmsLog_5(String phoneNumber, String pass,String info,String appinfo,String phoneInfo,Date logTime) {
		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		String sql = "insert into " + LogName + "(phoneNumber,pass,info,appinfo,phoneInfo,logSubType,logType,logTime) values(?,?,?,?,?,?,?,?)";
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, phoneNumber);
			ps.setString(2, pass);
			ps.setString(3, info);
			ps.setString(4, appinfo);
			ps.setString(5, phoneInfo);
			ps.setInt(6, 5);
			ps.setInt(7, 1);
			ps.setTimestamp(8, new Timestamp(logTime.getTime()));
			ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}
	} 
	
	/**XiaoXuanFeng拿验证码通讯失败,logType=1,logSubType=6
	 * @param phoneNumber
	 * @param pass
	 * @param info
	 * @param appinfo
	 * @param phoneInfo
	 * @param logSubType
	 * @param logTime
	 */
	public static void SmsLog_6(String phoneNumber, String pass,String info,String appinfo,String phoneInfo,Date logTime) {
		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		String sql = "insert into " + LogName + "(phoneNumber,pass,info,appinfo,phoneInfo,logSubType,logType,logTime) values(?,?,?,?,?,?,?,?)";
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, phoneNumber);
			ps.setString(2, pass);
			ps.setString(3, info);
			ps.setString(4, appinfo);
			ps.setString(5, phoneInfo);
			ps.setInt(6, 6);
			ps.setInt(7, 1);
			ps.setTimestamp(8, new Timestamp(logTime.getTime()));
			ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}
	} 
}
