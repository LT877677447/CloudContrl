package com.kilotrees.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.kilotrees.model.po.appuserinfo;
import com.kilotrees.services.ErrorLog_service;

/**
 * 粉丝用户帐号
 *
 */
public class appuserinfodao {
	private static Logger log = Logger.getLogger(appuserinfodao.class);
	/*养粉丝，一般设备进行工作时，一个个下发粉线信息，所以查询也是一个个查讲，因为u_id是递增的，所以可以用最后一个查到用户的u_id查找下一个
	 * 如果一次把用户全部查出来，占内存可能太大了．
	*/
	public static synchronized appuserinfo getNextUser(int lastuid,int advid,String dev_tag)
	{
		//当前请求的dev_tag，正常情况下如果用原来的设备养
		appuserinfo user = null;
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "select top 1 * from tb_appuserinfo where adv_id=? and u_id > ?";
		if(dev_tag != null && dev_tag.length() > 0)
			sql += " and dev_tag=?";
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setInt(1, advid);
			ps.setInt(2, lastuid);
			if(dev_tag != null && dev_tag.length() > 0)
				ps.setString(3, dev_tag);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				user = new  appuserinfo();
				user.setU_id(rs.getInt("u_id"));
				user.setUserid(rs.getString("userid"));
				user.setUsername(rs.getString("username"));
				user.setPass(rs.getString("pass"));
				user.setEmail(rs.getString("email"));
				user.setPhonenum(rs.getString("phonenum"));
				user.setSmscode(rs.getString("smscode"));
				user.setQqcode(rs.getString("qqcode"));
				user.setWebchat(rs.getString("webchat"));
				user.setAdv_id(rs.getInt("adv_id"));
				user.setDev_tag(rs.getString("dev_tag"));
				user.setVipid(rs.getInt("vipid"));
				user.setDev_config(rs.getString("dev_config"));
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
		return user;				
	}
	
	public static synchronized void addUser(appuserinfo user)
	{
		Connection con = connectionmgr.getInstance().getConnection();
		String sql = "insert tb_appuserinfo values(?,?,?,?,?,?,?,?,?,?,?,?)";		
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(sql);
			ps.setString(1, user.getUserid());
			ps.setString(2, user.getUsername());
			ps.setString(3, user.getPass());
			ps.setString(4, user.getEmail());
			ps.setString(5, user.getPhonenum());
			ps.setString(6, user.getSmscode());
			ps.setString(7, user.getQqcode());
			ps.setString(8, user.getWebchat());
			ps.setInt(9, user.getAdv_id());
			ps.setString(10, user.getDev_tag());			
			ps.setInt(11, user.getVipid());
			ps.setString(12, user.getDev_config());
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
	
	public static void test()
	{
		appuserinfo user = new appuserinfo();
		user.setAdv_id(10);
		user.setUserid("userid_1");
		user.setUsername("a xin");
		user.setDev_tag("dev_001");
		user.setQqcode("1303893");
		user.setPhonenum("13512700019");
		user.setDev_config("xxxxxx");
		addUser(user);
		
		user.setAdv_id(10);
		user.setUserid("userid_2");
		user.setUsername("a xin");
		user.setDev_tag("dev_002");
		user.setQqcode("1303893");
		user.setPhonenum("13512700019");
		user.setDev_config("xxxxxx");
		addUser(user);
		
		user.setAdv_id(10);
		user.setUserid("userid_3");
		user.setUsername("a xin");
		user.setDev_tag("dev_003");
		user.setQqcode("1303893");
		user.setPhonenum("13512700019");
		user.setDev_config("xxxxxx");
		addUser(user);
		
		user.setAdv_id(11);
		user.setUserid("userid_4");
		user.setUsername("a xin");
		user.setDev_tag("dev_003");
		user.setQqcode("1303893");
		user.setPhonenum("13512700019");
		user.setDev_config("xxxxxx");
		addUser(user);
		
		user.setAdv_id(11);
		user.setUserid("userid_5");
		user.setUsername("a xin");
		user.setDev_tag("dev_003");
		user.setQqcode("1303893");
		user.setPhonenum("13512700019");
		user.setDev_config("xxxxxx");
		addUser(user);
	}
	
	public static void test1()
	{
		appuserinfo user = getNextUser(0,10,null);
		System.out.println(user.toString());
		
		user = getNextUser((int)user.getU_id(),10,null);
		System.out.println(user.toString());
		
		user = getNextUser((int)user.getU_id(),10,null);
		System.out.println(user.toString());
		
		user = getNextUser((int)user.getU_id(),10,null);
		System.out.println(user.toString());
	}
	
}