package com.kilotrees.model.po;
/**
 * 时间曲线表
 * 

CREATE TABLE [dbo].[tb_timeline](
	[timelineid] [int] NOT NULL,
	[distribute_hours] [varchar](500) NOT NULL,
	[mark] [varchar](200) NULL,
 CONSTRAINT [PK_tb_timeline] PRIMARY KEY CLUSTERED 
(
	[timelineid] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

 *
 */
public class timelineinfo {
	public static final String tablename = "tb_timeline";
	//曲线id
	int timelineid;
	//第x小时分布百分比，用分号分开
	String distribute_hours;
	//2018-12-29 增加一个说明属性
	String mark = "";
	
	public String getMark() {
		return mark;
	}
	public void setMark(String mark) {
		if(mark != null)
			this.mark = mark;
	}
	public int getTimelineid() {
		return timelineid;
	}
	public void setTimelineid(int timelineid) {
		this.timelineid = timelineid;
	}
	public String getDistribute_hours() {
		return distribute_hours;
	}
	public void setDistribute_hours(String distribute_hours) {
		this.distribute_hours = distribute_hours;
	}
	
}
