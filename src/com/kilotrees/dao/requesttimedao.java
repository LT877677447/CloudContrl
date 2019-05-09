package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;

import com.kilotrees.services.ErrorLog_service;

public class requesttimedao {
	public static Logger log = Logger.getLogger(requesttimedao.class);
	
	/**插入新记录到tb_requesttime，保存请求的orderid和请求时间
	 * @param orderid 客户端此次请求的orderid
	 * @param datetime 请求的时间
	 */
	public static synchronized void insert(String orderid,Date datetime) {
//		insert into tb_requesttime(orderid,generatetime) values(1,CURRENT_TIMESTAMP);
		String sql = "insert into tb_requesttime(orderid,generatetime) values(?,?)";
		Connection con = connectionmgr.getInstance().getConnection();
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setString(1, orderid);
			ps.setTimestamp(2, new Timestamp(datetime.getTime()));
			ps.execute();
		}catch(Exception e)
		{
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(),e);
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}
		
		
		
	}

}
