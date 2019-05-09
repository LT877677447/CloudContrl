package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.ActionModel;
import com.kilotrees.services.ErrorLog_service;

public class ActionModelDao {

	private static Logger log = Logger.getLogger(ActionModelDao.class);

	public synchronized static ActionModel queryActions(int id, int phase) {
		ActionModel action = null;
		String sql = "select * from " + ActionModel.tableName + " where id = ? and phase = ?";
		
		PreparedStatement ps = null;
		Connection con = connectionmgr.getInstance().getConnection();
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, id);
			ps.setInt(2, phase);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				action = new ActionModel();
				action.setId(rs.getInt("id"));
				action.setPhase(rs.getInt("phase"));
				action.setPrefix_actions(rs.getString("prefix_actions"));
				action.setSuffix_actions(rs.getString("suffix_actions"));
			}
			rs.close();
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		} finally {
			connectionmgr.getInstance().closeConnection(con, ps);
		}
		
		return action;
	}

}
