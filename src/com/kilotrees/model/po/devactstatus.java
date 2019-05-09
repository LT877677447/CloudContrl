package com.kilotrees.model.po;

import java.util.Date;

/**
 *  
CREATE TABLE [dbo].[tb_devactstatus](
	[actsid] [int] IDENTITY(1,1) NOT NULL,
	[dev_tag] [varchar](50) NOT NULL,
	[lastlogintime] [datetime] NULL,
	[lastfetchtasktime] [datetime] NULL,
	[online] [int] NOT NULL,
	[status] [varchar](150) NOT NULL,
 CONSTRAINT [PK_tb_devactstatus] PRIMARY KEY CLUSTERED 
(
	[dev_tag] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]


 * 设备状态活跃日志类，用于扩展devactlog，记录每台最后登录时间，最后取任务时间，断线时间,当前是否在线
 * 以后在设备信息tb_deviceinfo中把广告分配那部分移到这里来或者另建一个类来处理广告分配和释放
 * 用于后台监控设备状态
 */
public class devactstatus {
	public final static String tablename = "tb_devactstatus";
	int actsid;
	String dev_tag = "";//主键	
	Date lastlogintime;
	Date lastfetchtasktime;
	// 设备是否在线  
	int online;
	String status = "";
	public devactstatus()
	{
		
	}

	public int getActsid() {
		return actsid;
	}

	public void setActsid(int actsid) {
		this.actsid = actsid;
	}

	public String getDev_tag() {
		return dev_tag;
	}

	public void setDev_tag(String dev_tag) {
		this.dev_tag = dev_tag;
	}

	public int getOnline() {
		return online;
	}

	/**
	 * @param online 0:下线  
	 */
	public void setOnline(int online) {
		this.online = online;
	}

	

	public Date getLastlogintime() {
		return lastlogintime;
	}

	public void setLastlogintime(Date lastlogintime) {
		this.lastlogintime = lastlogintime;
	}

	public Date getLastfetchtasktime() {
		return lastfetchtasktime;
	}

	public void setLastfetchtasktime(Date lastfetchtasktime) {
		this.lastfetchtasktime = lastfetchtasktime;
	}

	public String getStatus() {
		return status;
	}

	/**
	 * @param status 设备状态说明
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	// 下面现在是放在tb_deviceinfo中，以后再放到这里处理，
	// //预先分配好的广告id或广告组id,
	// int alloc_adv;
	// //临时分配的广告id，用于空闲时去做其它广告，每分配一次就加到此字段中,用分号分开.
	// String alloc_adv_temp;
	// //分配好的广告或组里的广告id，如果是组id，把组里的广告id放在里面，用分号分开，主要用于设备分配的广告组里面的广告改变时，能把知道之前组的内容
	// String ext = "";
	// //广告分配改变时，保留上面ext内容，用于状态清理
	// String ext_old = "";
	// //分配广告时间
	// Date alloctime;
	// //之前广告旧分配标识,
	// int alloc_type_old;
	// //
	// 当前广告分配标识，1：分配单一广告;2:分配给广告组；3:分配给单一的留存广告;4:广告和留存同时分到此设备上;5:分配留存组(多个留存同时做)
	// int alloc_type;
	// //此设备用到的vpn
	// int vpnid;
	// // 当广告状态(停新增，停留存或减少新增)，设备可能要换新的广告或置为空闲,服务器首先把这个位设为1
	// //如果这个数值不为0,那么服务器首先清理数据(卸载之前的旧广告)或安装新广告
	// int advstatus_reset;
	// //是否手动分配广告，如果是，程序不能释放设备，只能手工释放,一般用于自动充值这种专做某种特别任务机器
	// int hand_locked;
}
