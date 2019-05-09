/**
 * @author Administrator
 * 2019年1月18日 下午10:44:00 
 * CREATE TABLE [dbo].[tb_weixinaccount](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[account] [varchar](60) NULL,
	[complete] [int] NULL,
	[lastfetchtime] [datetime] NULL,
	[jointime] [datetime] NULL,
	[comment] [varchar](50) NULL,
 CONSTRAINT [PK_tb_weixinaccount] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
 */
package com.kilotrees.model.po;

import java.util.Date;

public class tb_weixinaccount {
	public final static String tablename = "tb_weixinaccount";
	private int id;
	private String account;
	private int complete;
	private Date lastFetchTime;
	private Date joinTime;
	private String comment;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public int getComplete() {
		return complete;
	}
	public void setComplete(int complete) {
		this.complete = complete;
	}
	public Date getLastFetchTime() {
		return lastFetchTime;
	}
	public void setLastFetchTime(Date lastFetchTime) {
		this.lastFetchTime = lastFetchTime;
	}
	public Date getJoinTime() {
		return joinTime;
	}
	public void setJoinTime(Date joinTime) {
		this.joinTime = joinTime;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	@Override
	public String toString() {
		return "tb_weixinaccount [id=" + id + ", account=" + account + ", complete=" + complete + ", lastFetchTime="
				+ lastFetchTime + ", joinTime=" + joinTime + ", comment=" + comment + "]";
	}
	
	
	
}
