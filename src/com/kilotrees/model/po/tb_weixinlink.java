/**
 * @author Administrator
 * 2019年1月18日 下午10:44:28
 * CREATE TABLE [dbo].[tb_weixinlink](
	[id] [int] IDENTITY(1,1) NOT FOR REPLICATION NOT NULL,
	[link_url] [varchar](70) NOT NULL,
	[need_count] [int] NULL,
	[current_count] [int] NULL,
	[add_time] [datetime] NULL,
	[comment] [varchar](100) NULL,
 CONSTRAINT [PK_tb_weixinlink] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
 */
package com.kilotrees.model.po;

import java.util.Date;

public class tb_weixinlink {
	public final static String tablename = "tb_weixinlink";
	private int id;
	private String linkUrl;
	private int needCount;
	private int currentCount;
	private Date addTime;
	private String comment;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLinkUrl() {
		return linkUrl;
	}
	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
	}
	public int getNeedCount() {
		return needCount;
	}
	public void setNeedCount(int needCount) {
		this.needCount = needCount;
	}
	public int getCurrentCount() {
		return currentCount;
	}
	public void setCurrentCount(int currentCount) {
		this.currentCount = currentCount;
	}
	public Date getAddTime() {
		return addTime;
	}
	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	@Override
	public String toString() {
		return "tb_weixinlink [id=" + id + ", linkUrl=" + linkUrl + ", needCount=" + needCount + ", currentCount="
				+ currentCount + ", addTime=" + addTime + ", comment=" + comment + "]";
	}
	
	
	
	
}
