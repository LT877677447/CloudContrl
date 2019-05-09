package com.kilotrees.model.po;

/**
 * 商务信息
 * 

CREATE TABLE [dbo].[tb_bdinfo](
	[bdid] [int] NOT NULL,
	[bdname] [varchar](50) NOT NULL,
	[phonenum] [varchar](50) NULL,
 CONSTRAINT [PK_tb_bdinfo] PRIMARY KEY CLUSTERED 
(
	[bdid] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

 *
 */
public class bdinfo {
	int bdid;
	String bdname;
	String phonenum;
	
	public int getBdid() {
		return bdid;
	}
	public void setBdid(int bdid) {
		this.bdid = bdid;
	}
	public String getBdname() {
		return bdname;
	}
	public void setBdname(String bdname) {
		this.bdname = bdname;
	}
	public String getPhonenum() {
		return phonenum;
	}
	public void setPhonenum(String phonenum) {
		this.phonenum = phonenum;
	}	
}
