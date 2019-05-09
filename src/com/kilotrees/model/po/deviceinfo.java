package com.kilotrees.model.po;

import java.util.Date;

import com.kilotrees.util.DateUtil;
import com.kilotrees.util.StringUtil;

/*
 * 云控中正式使用的手机信息，一般在每台机贴个标签编号，在手机主控程序中保存这个标签，每次访问时带这个参数

CREATE TABLE [dbo].[tb_deviceinfo](
	[device_tag] [varchar](50) NOT NULL,
	[clientid] [int] NULL,
	[phone_type] [varchar](32) NULL,
	[alloc_adv] [int] NULL,
	[alloc_adv_temp] [varchar](200) NULL,
	[ext] [varchar](200) NULL,
	[ext_old] [varchar](200) NULL,
	[registtime] [datetime] NULL,
	[alloctime] [datetime] NULL,
	[alloc_type_old] [int] NULL,
	[alloc_type] [int] NULL,
	[vpnid] [int] NULL,
	[advstatus_reset] [int] NULL,
	[nouse] [int] NULL,
	[hand_locked] [int] NULL,
 CONSTRAINT [PK_tb_deviceinfo] PRIMARY KEY CLUSTERED 
(
	[device_tag] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
 */
public class deviceinfo implements Comparable<deviceinfo>{
	public final static String tablename = "tb_deviceinfo";
	// 机器编号，这个编号前面字符最好表示客户，后面是数字编号，不同的编号字符机器可能在不同的地方部置
	//对于用pc模拟的任务，用固定pc_id开头不需要保存在数据库中，只把他们放在活跃列表中，对于模拟器用simulator_id
	String device_tag;
	// 此机器所属客户，暂时用不到，当商业化后租给别人时才用到
	int clientid;
	String phone_type;
	// 预先分配好的广告id或广告组id,
	int alloc_adv;
	//临时分配的广告id，用于空闲时去做其它广告，每分配一次就加到此字段中,用分号分开.
	String alloc_adv_temp;
	//分配好的广告或组里的广告id，如果是组id，把组里的广告id放在里面，用分号分开，主要用于设备分配的广告组里面的广告改变时，能把知道之前组的内容	
	String ext = "";
	//广告分配改变时，保留上面ext内容，用于状态清理
	String ext_old = "";	
	//设备注册时间，新的设备必须先注册，加到此表中才能使用
	Date registtime = new Date();
	//分配广告时间
	Date alloctime;
	//之前广告旧分配标识,
	int alloc_type_old;
	// 当前广告分配标识，1：分配单一广告;2:分配给广告组；3:分配给单一的留存广告;4:广告和留存同时分到此设备上;5:分配留存组(多个留存同时做)
	int alloc_type;
	//此设备用到的vpn
	int vpnid;
	// 当广告状态(停新增，停留存或减少新增)，设备可能要换新的广告或置为空闲,服务器首先把这个位设为1
	//如果这个数值不为0,那么服务器首先清理数据(卸载之前的旧广告)或安装新广告
	int advstatus_reset;
	// 设备是否停用，如果是1，表示停用(设备故障)
	int nouse;
	//是否手动分配广告，如果是，程序不能释放设备，只能手工释放,一般用于自动充值这种专做某种特别任务机器
	int hand_locked;
	//以下为自定义数据
	//2018-12-11 不再使用lastLoginTime,由下面devactStatus记录
	//Date lastLoginTime;
	//是否处于活跃
	//boolean act_now;
	
	//最后状态报告的reportid,防止客户端由于网络或服务器处理速度问题，多次发送同一个状态
	long lastReportId;
	//最后一个任务请求orderid，防止客户端由于网络或服务器处理速度问题，多次发送同一个请求，orderid由客户端生成，但要注意客户端重启时
	//orderid变成1
	long lastReqOrderId;
	//登录loginid,如果发现lastLoginId为1,表示客户重启，要把lastReportId,lastReqOrderId都置为0
	long lastLoginId;
	
	int update_count = 0;
	//2018-12-11 加上tb_devactstatus 来记录设备的离线
	devactstatus devactStatus; 

	public String getDevice_tag() {
		return device_tag;
	}

	public void setDevice_tag(String device_tag) {
		this.device_tag = device_tag;
	}

	public String getPhone_type() {
		return phone_type;
	}

	public void setPhone_type(String phone_type) {
		this.phone_type = phone_type;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		if (ext == null)
			ext = "";
		this.ext = ext;
	}

	public int getClientid() {
		return clientid;
	}

	public void setClientid(int clientid) {
		this.clientid = clientid;
	}
	
	/**
	 * @return 1:已经停用
	 */
	public int getNouse() {
		return nouse;
	}

	/**
	 * @param nouse 1:已经停用
	 */
	public void setNouse(int nouse) {
		this.nouse = nouse;
	}
	

	public int getAdvstatus_reset() {
		return advstatus_reset;
	}

	public void setAdvstatus_reset(int advstatus_reset) {
		this.advstatus_reset = advstatus_reset;
	}
	
	
	/**
	 * isFree()：用来判断设备是否可以开始分配新的任务
	 * nouse=1表示设备坏了
	 */
	public boolean isFree() {
		if (this.nouse == 1)
			return false;
		if(this.hand_locked > 0)   //hand_locked>0 表示要先手工释放设备，再用程序分配任务
			return false;
		if (alloc_type == 0 && alloc_adv == 0) //如果还没有分配设备、任务，就表示空闲，可以分配新任务
			return true;
		return false;
	}

	
	public int getAlloc_adv() {
		return alloc_adv;
	}

	public void setAlloc_adv(int alloc_adv) {
		if(this.alloc_adv != alloc_adv) {
			this.ext_old = this.alloc_adv+"";
			this.setAdvstatus_reset(1);   //改变分配广告时，把状态置为1
			this.alloc_adv = alloc_adv;
		}
	}

	public String getAlloc_adv_temp() {
		return alloc_adv_temp;
	}

	public void setAlloc_adv_temp(String alloc_adv_temp) {
		if(StringUtil.isStringEmpty(alloc_adv_temp))
			alloc_adv_temp = "";
		this.alloc_adv_temp = alloc_adv_temp;
	}
	
	public void addAllocAdvTemp(int adv_id)
	{
		if(alloc_adv_temp == null)
			alloc_adv_temp = "";
		if(alloc_adv_temp.length() > 0)
			alloc_adv_temp += ";";
		alloc_adv_temp += adv_id;
		while(alloc_adv_temp.length() > 90)  //如果 临时分配的任务长度>90，把最前面的一个任务清掉
		{
			int pos = alloc_adv_temp.indexOf(";");
			alloc_adv_temp = alloc_adv_temp.substring(pos + 1);
		}
	}

	public String getExt_old() {
		return ext_old;
	}

	public void setExt_old(String ext_old) {
		this.ext_old = ext_old;
	}

	public Date getRegisttime() {
		return registtime;
	}

	public void setRegisttime(Date registtime) {
		this.registtime = registtime;
	}
	
	

	public Date getAlloctime() {
		return alloctime;
	}

	public void setAlloctime(Date alloctime) {
		this.alloctime = alloctime;
	}

	public int getAlloc_type_old() {
		return alloc_type_old;
	}

	public void setAlloc_type_old(int alloc_type_old) {
		this.alloc_type_old = alloc_type_old;
	}

	public int getAlloc_type() {
		return alloc_type;
	}

	public void setAlloc_type(int alloc_type) {
		if(this.alloc_type != alloc_type)
		{
			this.setAdvstatus_reset(1);    //当分配类型改变时，把advstatus_reset置为1
			this.alloc_type_old = this.alloc_type;
			this.alloc_type = alloc_type;
		}
	}

	public int getVpnid() {
		return vpnid;
	}

	public void setVpnid(int vpnid) {
		this.vpnid = vpnid;
	}

	/**ext_old = ext
	 * alloc_type_old = alloc_type
	 */
	public void backup()
	{
		if(ext.length() > 0 && alloc_type > 0)   //当deviceinfo的ext和alloc_type都有值时，才做操作
		{
			this.ext_old = ext;
			this.alloc_type_old = this.alloc_type;
		}
	}
	/**
	 * 用于释放设备排序，优先越迟注册的越早释放
	 */
	@Override
	public int compareTo(deviceinfo o) {
		if(this.registtime.getTime() == o.registtime.getTime())
			return 0;
		if(this.registtime.after(o.registtime))
			return 1;
		else
			return -1;
	}
	
	
	public long getLastReportId() {
		return lastReportId;
	}

	public void setLastReportId(long lastReportId) {
		this.lastReportId = lastReportId;
	}

	public long getLastReqOrderId() {
		return lastReqOrderId;
	}

	public void setLastReqOrderId(long lastReqOrderId) {
		this.lastReqOrderId = lastReqOrderId;
	}
	
	public long getLastLoginId() {
		return lastLoginId;
	}

	public void setLastLoginId(long lastLoginId) {
		this.lastLoginId = lastLoginId;
		if(lastLoginId == 1)
		{
			lastReqOrderId = 0;
			lastReportId = 0;
		}
	}
	
	public int getUpdate_count() {
		return update_count;
	}

	public void setUpdate_count(int update_count) {
		this.update_count = update_count;
	}

	public int getHand_locked() {
		return hand_locked;
	}

	public void setHand_locked(int hand_locked) {
		this.hand_locked = hand_locked;
	}
	
	public void copy(deviceinfo di)
	{
		this.alloc_adv = di.getAlloc_adv();
		this.alloc_type = di.getAlloc_type();
		this.hand_locked = di.getHand_locked();
		this.alloc_adv_temp = di.getAlloc_adv_temp();
		this.alloc_type_old = di.getAlloc_type_old();
		this.ext = di.getExt();
		this.ext_old = di.getExt_old();
	}

	
	public devactstatus getDevactStatus() {
		return devactStatus;
	}

	public void setDevactStatus(devactstatus devactStatus) {
		this.devactStatus = devactStatus;
	}

	public String toString()
	{
		String s = "";
		s += "dev_tag=" + this.device_tag + "\r\n";
		s += "phone_type=" + this.phone_type + "\r\n";
				
		s += "alloc_adv=" + this.alloc_adv + "\r\n";
		s += "ext=" + this.ext + "\r\n";
		s += "registtime=" + DateUtil.getDateString(this.registtime) + "\r\n";
		s += "alloctime=" + DateUtil.getDateString(alloctime) + "\r\n";
		s += "alloc_type=" + this.alloc_type + "\r\n";
		s += "nouse=" + this.nouse + "\r\n";
		//s += "lastLoginTime=" + DateUtil.getDateString(lastLoginTime) + "\r\n";
		s += "lastLoginId=" + lastLoginId + "\r\n";
		s += "lastReqOrderId=" + lastReqOrderId + "\r\n";
		s += "lastReportId=" + lastReportId + "\r\n";
		return s;
		
	}
}
