package com.kilotrees.model.po;

import java.util.Calendar;
import java.util.Date;

import com.kilotrees.dao.sqlcommon;

/**
 * 留存任务日志表,到时分月表,如果数据库没有建月表，由代码中自动创建 
CREATE TABLE [dbo].[tb_advremainlog](
	[log_id] [int] IDENTITY(1,1) NOT NULL,
	[autoid] [bigint] NULL,
	[adv_id] [int] NOT NULL,
	[dev_tag] [varchar](50) NOT NULL,
	[vpnid] [int] NOT NULL,
	[step] [int] NOT NULL,
	[result] [int] NOT NULL,
	[loginfo] [varchar](500) NOT NULL,
	[logtime] [datetime] NOT NULL,
	[ip] [varchar](50) NULL,
	[area] [varchar](50) NULL,
 CONSTRAINT [PK_tb_advremainlog] PRIMARY KEY CLUSTERED 
(
	[log_id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

 *
 */
public class advremainlog {
	public final static String tablenamepre = "tb_advremainlog";
	int log_id;
	long autoid;
	int adv_id;
	String dev_tag;//做这个留存的设备标签
	int vpnid;
	//中间执行步骤,0表示结束
	int step;
	//结果0：正常，其它为异常
	int result;
	//日志内容
	String loginfo;
	Date logtime;
	//执行任务时vpn所有地ip
	String ip = "";
	// vpn所有地区
	String area = "";
	
	public static String getCurTableName()
	{		
		Calendar cal = Calendar.getInstance();		
		int month = cal.get(Calendar.MONTH )+1;		
		return sqlcommon.getMonthTableName(tablenamepre,month);
	}
	
	public static String getMonthTableName(int month)
	{		
		//Calendar cal = Calendar.getInstance();		
		//int month = cal.get(Calendar.MONTH )+1;
		if(month < 1 || month > 12)
			return "";
		return sqlcommon.getMonthTableName(tablenamepre,month);
	}
	
	public static String getCreateTableSql(int month)
	{
		String newName = sqlcommon.getMonthTableName(tablenamepre,month);
		String sql = "CREATE TABLE [dbo].[" + newName + "](\r\n";
		sql += "[log_id] [int] IDENTITY(1,1) NOT NULL,\r\n";		
		sql += "[autoid] [bigint] NULL,\r\n";
		sql += "[adv_id] [int] NOT NULL,\r\n";
		sql += "[dev_tag] [varchar](50) NOT NULL,\r\n";
		sql += "[vpnid] [int] NOT NULL,\r\n";
		sql += "[step] [int] NOT NULL,\r\n";
		sql += "[result] [int] NOT NULL,\r\n";
		sql += "[loginfo] [text] NULL,\r\n";
		sql += "[logtime] [datetime] NOT NULL,\r\n";
		sql += "[ip] [varchar](50) NULL,\r\n";
		sql += "[area] [varchar](50) NULL,\r\n";
		sql += "CONSTRAINT [PK_"+ newName + "] PRIMARY KEY CLUSTERED\r\n";
		sql += "(\r\n";
		sql += "[log_id] ASC\r\n";
		sql += ")WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]\r\n";
		sql += ") ON [PRIMARY]\r\n";
		return sql;
	}
	
	public Date getLogtime() {
		return logtime;
	}
	public void setLogtime(Date logtime) {
		this.logtime = logtime;
	}
	public int getLog_id() {
		return log_id;
	}
	public void setLog_id(int log_id) {
		this.log_id = log_id;
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
	public int getStep() {
		return step;
	}
	public void setStep(int step) {
		this.step = step;
	}
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	public String getLoginfo() {
		return loginfo;
	}
	public void setLoginfo(String loginfo) {
		this.loginfo = loginfo;
	}
	public int getVpnid() {
		return vpnid;
	}
	public void setVpnid(int vpnid) {
		this.vpnid = vpnid;
	}

	public long getAutoid() {
		return autoid;
	}

	public void setAutoid(long autoid) {
		this.autoid = autoid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}	
	
}
