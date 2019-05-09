package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.timelineinfo;
import com.kilotrees.services.ErrorLog_service;
/**
 * 取时间曲线列表信息
 * @author Administrator
 *
 */
public class timelinedao {
	private static Logger log = Logger.getLogger(timelinedao.class);
	
	/**select * from tb_timeline
	 * @return
	 */
	public static HashMap<Integer,timelineinfo> getTimeLineList()
	{
		HashMap<Integer,timelineinfo> list = new HashMap<Integer,timelineinfo>();
		String sql = "select * from " + timelineinfo.tablename;
		Connection con = connectionmgr.getInstance().getConnection();
		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				timelineinfo ti = new timelineinfo();
				ti.setTimelineid(rs.getInt("timelineid"));
				ti.setDistribute_hours(rs.getString("distribute_hours"));
				ti.setMark(rs.getString("mark"));
				list.put(ti.getTimelineid(),ti);
			}
			rs.close();
		}catch(Exception e)
		{
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(),e);
		}
		finally
		{
			connectionmgr.getInstance().closeConnection(con,ps);
		}				
		return list;
	}
}
