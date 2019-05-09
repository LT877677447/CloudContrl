package com.kilotrees.model.po;

import java.util.Date;

/**
 * 广告动态分配设备的日志信息
 *

CREATE TABLE [dbo].[tb_advallocdevlog](
	[advid] [int] NOT NULL,
	[dev_tag] [varchar](50) NOT NULL,
	[alloc_type] [int] NOT NULL,
	[free] [int] NOT NULL,
	[alloc_time] [datetime] NOT NULL
) ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO

 * @author Administrator
 *
 */
public class advallocdevlog {
	public static final String tablename = "tb_advallocdevlog";
	int advid;
	String dev_tag;
	//参考deviceinfo的alloc_type
	int alloc_type;
	Date alloc_time;
	//释放广告的原因 1：广告下线 2：设备长时间离线
	int free;
	
	public int getAdvid() {
		return advid;
	}
	public void setAdvid(int advid) {
		this.advid = advid;
	}
	public String getDev_tag() {
		return dev_tag;
	}
	public void setDev_tag(String dev_tag) {
		this.dev_tag = dev_tag;
	}
	
	public int getAlloc_type() {
		return alloc_type;
	}
	public void setAlloc_type(int alloc_type) {
		this.alloc_type = alloc_type;
	}
	public Date getAlloc_time() {
		return alloc_time;
	}
	public void setAlloc_time(Date alloc_time) {
		this.alloc_time = alloc_time;
	}
	public int getFree() {
		return free;
	}
	public void setFree(int free) {
		this.free = free;
	}	
}
