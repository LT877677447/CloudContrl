package com.kilotrees.model.po;

import java.util.Date;

/**
 * 
每个业务都建一张不同的表，按业务名称加上业务别名，比如dslh表示大圣轮回

CREATE TABLE [dbo].[tb_qqaccount_dslh](
	[qqnum] [varchar](50) NOT NULL,
	[pass] [varchar](50) NOT NULL,
	[gettime] [datetime] NULL,
	[stauts] [int] NOT NULL,
	[autoid] [bigint] NOT NULL,
	[result] [int] NOT NULL,
	[info] [varchar](50) NOT NULL,
 CONSTRAINT [PK_tb_qqaccount_dslh] PRIMARY KEY CLUSTERED 
(
	[qqnum] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
 *
 */
public class qqaacount_info {
	String qqnum = "";
	String pass = "";
	Date gettime;
	//当前状态，0表示未使用，1表示已经取过
	int stauts;
	//
	long autoid;
	//结果值
	int result;
	//结果描述
	String info = "";
	public String getQqnum() {
		return qqnum;
	}
	public void setQqnum(String qqnum) {
		this.qqnum = qqnum;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public Date getGettime() {
		return gettime;
	}
	public void setGettime(Date gettime) {
		this.gettime = gettime;
	}
	public int getStauts() {
		return stauts;
	}
	public void setStauts(int stauts) {
		this.stauts = stauts;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public long getAutoid() {
		return autoid;
	}
	public void setAutoid(long autoid) {
		this.autoid = autoid;
	}
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	
	
}
