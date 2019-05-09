package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

public class connectionmgr {
	private static Logger log = Logger.getLogger(connectionmgr.class);
	private static DataSource datasource;
	private static connectionmgr inst;

	private connectionmgr() throws NamingException {
		if (datasource == null) {
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
			datasource = (DataSource) envContext.lookup("jdbc/YunDB");
		}
	}

	public static connectionmgr getInstance() {
		if (inst == null) {
			try {
				inst = new connectionmgr();
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}

		return inst;
	}

	public Connection getConnection() {
		Connection con = null;
		try {
			con = datasource.getConnection();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
		return con;
	}

	public void webStop() {
		log.info("注销jdbc驱动");
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			try {
				DriverManager.deregisterDriver(driver);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void closeConnection(Connection con, PreparedStatement ps) {
		try {
			//datasource.
			if (con != null) {
				con.close();
			}
			if (ps != null) {
				ps.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
