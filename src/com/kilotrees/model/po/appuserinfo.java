package com.kilotrees.model.po;
/**
 * app自媒体粉丝信息表
 * 

CREATE TABLE [dbo].[tb_appuserinfo](
	[u_id] [int] IDENTITY(1,1) NOT NULL,
	[userid] [varchar](50) NOT NULL,
	[username] [varchar](50) NULL,
	[pass] [varchar](50) NULL,
	[email] [varchar](50) NULL,
	[phonenum] [varchar](50) NULL,
	[smscode] [varchar](50) NULL,
	[qqcode] [varchar](50) NULL,
	[webchat] [varchar](50) NULL,
	[adv_id] [int] NOT NULL,
	[dev_tag] [varchar](50) NULL,
	[vipid] [int] NULL,
	[dev_config] [varchar](1000) NULL,
 CONSTRAINT [PK_tb_appuserinfo] PRIMARY KEY CLUSTERED 
(
	[u_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
 *
 */
public class appuserinfo {
	long u_id;
	String userid;
	String username;
	String pass;
	String email;
	String phonenum;
	String smscode;//注册时验证码
	String qqcode;
	String webchat;//
	int adv_id;
	String dev_tag;//注册时的设备号，防止不能转到其它机器上
	int vipid;//
	public int getVipid() {
		return vipid;
	}
	public void setVipid(int vipid) {
		this.vipid = vipid;
	}
	String dev_config;//注册时设备的所有信息(包括强匹配和随机修改的)
	public long getU_id() {
		return u_id;
	}
	public void setU_id(long u_id) {
		this.u_id = u_id;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhonenum() {
		return phonenum;
	}
	public void setPhonenum(String phonenum) {
		this.phonenum = phonenum;
	}
	public String getSmscode() {
		return smscode;
	}
	public void setSmscode(String smscode) {
		this.smscode = smscode;
	}
	public String getQqcode() {
		return qqcode;
	}
	public void setQqcode(String qqcode) {
		this.qqcode = qqcode;
	}
	public String getWebchat() {
		return webchat;
	}
	public void setWebchat(String webchat) {
		this.webchat = webchat;
	}
	public int getAdv_id() {
		return adv_id;
	}
	public void setAdv_id(int adv_id) {
		this.adv_id = adv_id;
	}
	public String getDev_tag() {
		return dev_tag;
	}
	public void setDev_tag(String dev_tag) {
		this.dev_tag = dev_tag;
	}
	public String getDev_config() {
		return dev_config;
	}
	public void setDev_config(String dev_config) {
		this.dev_config = dev_config;
	}
	
	public String toString()
	{
		String s = "u_id=" + u_id + "\r\n";
		s += "userid=" + userid + "\r\n";
		s += "username=" + username + "\r\n";
		s += "pass=" + pass + "\r\n";
		s += "email=" + email + "\r\n";
		s += "phonenum=" + phonenum + "\r\n";
		s += "smscode=" + smscode + "\r\n";
		s += "qqcode=" + qqcode + "\r\n";
		s += "webchat=" + webchat + "\r\n";
		s += "adv_id=" + adv_id + "\r\n";
		s += "dev_tag=" + dev_tag + "\r\n";
		s += "vipid=" + vipid + "\r\n";
		s += "dev_config=" + dev_config + "\r\n";
		return s;
	}
	
}
