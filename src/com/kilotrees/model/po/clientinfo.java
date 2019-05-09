package com.kilotrees.model.po;

/**
 * 记录租用我们云控系统的客户信息．或不同区域分布的组信息


CREATE TABLE [dbo].[tb_clientinfo](
	[c_id] [int] NOT NULL,
	[ctype] [int] NOT NULL,
	[name] [varchar](64) NOT NULL,
	[loginname] [varchar](32) NULL,
	[pass] [varchar](32) NULL,
	[phonenum] [varchar](32) NULL,
	[nouse] [int] NULL,
	[ext] [varchar](255) NULL,
 CONSTRAINT [PK_tb_countinfo] PRIMARY KEY CLUSTERED 
(
	[c_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

 　　　记录租用我们云控系统的客户信息．
 */
public class clientinfo {
	long c_id;//客户id
	int ctype;//0表示组，自己可能有几组不同地方的手机．1:表示租我们系统的真实客户
	String name;//客户名称
	String loginname;//登录名
	String pass;//登录密码
	String phonenum;//
	int nouse;//客户是否不再有效,１表示客户不再使作
	String ext;//扩展参数
	public long getC_id() {
		return c_id;
	}
	public void setC_id(long c_id) {
		this.c_id = c_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLoginname() {
		return loginname;
	}
	public void setLoginname(String loginname) {
		this.loginname = loginname;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public String getPhonenum() {
		return phonenum;
	}
	public void setPhonenum(String phonenum) {
		this.phonenum = phonenum;
	}
	public int getNouse() {
		return nouse;
	}
	public void setNouse(int nouse) {
		this.nouse = nouse;
	}
	public String getExt() {
		return ext;
	}
	public void setExt(String ext) {
		this.ext = ext;
	}	
}
