/**
 * @author Administrator
 * 2019年4月29日 下午5:59:13 
 */
package com.kilotrees.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.kilotrees.dao.connectionmgr;
import com.sun.org.apache.regexp.internal.recompile;

public class DouYinIssueLog {
	private static String tableName = "[tb_DouYinIssueLog]";
	
	public static int log(String dev_tag,String phoneInfo,Date issueTime) {
		int num = 0;
		Connection connection = connectionmgr.getInstance().getConnection();
		String sql = " insert into "+tableName+"(dev_tag,phoneInfo,issueTime) values(?,?,?)";
		PreparedStatement pStatement = null;
		try {
			pStatement = connection.prepareStatement(sql);
			pStatement.setString(1, dev_tag);
			pStatement.setString(2, phoneInfo);
			pStatement.setTimestamp(3, new Timestamp(issueTime.getTime()));
			num = pStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			connectionmgr.getInstance().closeConnection(connection, pStatement);
		}
		return num;
	}
	
	
	
	
	
}
