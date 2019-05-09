package com.kilotrees.model.po;

import java.util.Date;

/**
 * 设备活跃日志，主要记录设置最后活跃时间，客户端主控程序登录后，启动一个心跳线程，定时向服务器报告
 * 服务器可以监控设备运作
GO

CREATE TABLE [dbo].[tb_devactlog](
	[actid] [int] IDENTITY(1,1) NOT NULL,
	[dev_tag] [varchar](50) NOT NULL,
	[lastlogintime] [datetime] NOT NULL,
	[devstatus] [varchar](100) NOT NULL
) ON [PRIMARY]



 */
//2018-12-11 这个以后不用,由tb_devactstatus代替
public class devactlog {
	//自增值
	int actid;
	String dev_tag;
	Date lastlogintime;
	//用于描述设备状态,这里暂时用不到
	String  devstatus = "";
	
	public String getDev_tag() {
		return dev_tag;
	}
	public void setDev_tag(String dev_tag) {
		this.dev_tag = dev_tag;
	}
	public Date getLastlogintime() {
		return lastlogintime;
	}
	public void setLastlogintime(Date lastlogintime) {
		this.lastlogintime = lastlogintime;
	}
	public String getDevstatus() {
		return devstatus;
	}
	public void setDevstatus(String devstatus) {
		this.devstatus = devstatus;
	}		
}
