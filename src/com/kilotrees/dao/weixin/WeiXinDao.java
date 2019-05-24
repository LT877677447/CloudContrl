/**
 * @author Administrator
 * 2019年4月30日 下午3:33:15 
 */
package com.kilotrees.dao.weixin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;

import com.kilotrees.dao.connectionmgr;
import com.kilotrees.services.ErrorLog_service;

public class WeiXinDao {
	private static Logger log = Logger.getLogger(WeiXinDao.class);
	private static final String tableName = "tb_WXAccount";
	private static final String LogName = "tb_DouYinAccount_log";

	/**
	 * 注册抖音成功，status=1
	 * 
	 * @param phoneNumber
	 * @param pass
	 * @param registTime
	 * @param appinfo
	 * @param phoneInfo
	 * @param comment
	 */
	public static void newAccount(String autoid, String weixinAccount,String phoneNumber, String password, Date registTime, String appinfo, String phoneInfo, int status, String comment,String isOverSeas) {
		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		String sql = "insert into " + tableName + "(autoid,weixinAccount,phoneNumber,password,registTime,appinfo,phoneInfo,status,comment,isOverSeas) values(?,?,?,?,?,?,?,?,?,?)";
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, autoid);
			ps.setString(2, weixinAccount);
			ps.setString(3, phoneNumber);
			ps.setString(4, password);
			ps.setTimestamp(5, new Timestamp(registTime.getTime()));
			ps.setString(6, appinfo);
			ps.setString(7, phoneInfo);
			ps.setInt(8, status);
			ps.setString(9, comment);
			ps.setString(10, isOverSeas);
			ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}
	}

	public static void haiwaiRegisted(String phoneNumber) {
		Connection connection = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		String sql = "update [tb_WX_haiwai_Account] set registed=1,fetched=0 where phoneNumber=?";
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, phoneNumber);

			ps.executeUpdate();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(connection, ps);
		}

	}

}
