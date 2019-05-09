/**
 * @author Administrator
 * 2019年1月18日 下午10:44:45 
 * CREATE TABLE [dbo].[tb_weixinlog](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[type] [varchar](50) NULL,
	[message] [varchar](200) NULL,
	[comment] [varchar](200) NULL,
	[logtime] [datetime] NULL,
 CONSTRAINT [PK_tb_weixinlog] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
 */
package com.kilotrees.model.po;

import java.util.Date;

public class tb_weixinlog {
	public final static String tablename = "tb_weixinlog";
	private int id;
	private String type;
	private String message;
	private String comment;
	private Date logTime;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Date getLogTime() {
		return logTime;
	}
	public void setLogTime(Date logTime) {
		this.logTime = logTime;
	}
	@Override
	public String toString() {
		return "tb_weixinlog [id=" + id + ", type=" + type + ", message=" + message + ", comment=" + comment
				+ ", logTime=" + logTime + "]";
	}
	
	
	

}
