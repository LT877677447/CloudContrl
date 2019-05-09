package com.kilotrees.model.po;

import org.json.JSONObject;

/*
 * 	O


CREATE TABLE [dbo].[tb_vpninfo](
	[vpnid] [int] IDENTITY(1,1) NOT NULL,
	[vpnurl] [varchar](150) NOT NULL,
	[account] [varchar](50) NOT NULL,
	[pass] [varchar](50) NOT NULL,
	[static_flag] [int] NOT NULL,
	[areas] [varchar](1000) NOT NULL,
	[vpntype] [int] NOT NULL,
	[nouse] [int] NOT NULL,
	[device_count] [int] NULL,
 CONSTRAINT [PK_tb_vpninfo] PRIMARY KEY CLUSTERED 
(
	[vpnid] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

 */


public class vpninfo {
	int vpnid;
	//如果用无极vpn，url无效
	String vpnurl;
	String account;
	String pass;
	//是否静态ip,1表示静态ip
	int static_flag;
	//此vpn所有地区
	String areas = "";
	//vpn类型，0无极20元，1无极60元
	int vpntype;
	//是否有效
	int nouse;
	
	int deviceCount;
	
	
	
	public int getDeviceCount() {
		return deviceCount;
	}
	public void setDeviceCount(int deviceCount) {
		this.deviceCount = deviceCount;
	}
	public int getVpnid() {
		return vpnid;
	}
	public void setVpnid(int vpnid) {
		this.vpnid = vpnid;
	}
	public String getVpnurl() {
		return vpnurl;
	}
	public void setVpnurl(String vpnurl) {
		this.vpnurl = vpnurl;
	}
	public int getNouse() {
		return nouse;
	}
	public void setNouse(int nouse) {
		this.nouse = nouse;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public int getStatic_flag() {
		return static_flag;
	}
	public void setStatic_flag(int static_flag) {
		this.static_flag = static_flag;
	}
		
	public String getAreas() {
		return areas;
	}
	public void setAreas(String areas) {
		if(areas == null)
			areas = "";
		this.areas = areas;
	}
	public JSONObject toJson()
	{
		JSONObject jso = new JSONObject();
		try {
		jso.put("vpnid", vpnid);
		jso.put("vpnurl", vpnurl);
		jso.put("account", account);
		jso.put("pass", pass);
		jso.put("static_flag", static_flag);
		jso.put("vpntype", vpntype);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return jso;
	}
	
	
	
	public int getVpntype() {
		return vpntype;
	}
	public void setVpntype(int vpntype) {
		this.vpntype = vpntype;
	}
	public String toString()
	{
		String s = "vpnid=" + vpnid + "\r\n";
		s += "vpnurl=" + vpnurl + "\r\n";
		s += "account=" + account + "\r\n";
		s += "pass=" + pass + "\r\n";
		s += "static_flag=" + static_flag + "\r\n";
		s += "nouse=" + nouse + "\r\n";
		return s;
	}
}
