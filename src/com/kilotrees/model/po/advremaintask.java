package com.kilotrees.model.po;

import java.util.Date;

import com.kilotrees.util.DateUtil;

/**
 * 留存任务表
 * 程序启动时读取此表，看哪些今天的留存没有完成的，有就执行．养粉不用这张表
 * dotoday字段每天0点由存储过程计算今天每条记录是否需要做留存，因为remainifno这个字段太长，如果每次由程序判断今天日期是否在remaininfo，效率太慢
 * remaininfo以前直接包含每天执行的年月日，后面改成0和1组成的字串，由另一个字段标记最开始执行留存日期，remaininfo每一位表示从最开始日期的开始的天数，
 * 第一位表示第一天，第二位表示第二天,0表示不需要做留存，1表示当天要做留存
 * 存储过程判断今天的日期对应的字符是否为1，如果是就把dotoday置为每天活跃数,否则置为0,如果所有留存做完了，后续不需要做留存了，就置为-1
 * 存储过程开始执行时，把表serverconfig中的remainjobprocwork置为1，完成后置为2
 * 具体看存储过程[proc_init]

CREATE TABLE [dbo].[tb_advremaintask](
	[rid] [int] IDENTITY(1,1) NOT NULL,
	[autoid] [bigint] NOT NULL,
	[adv_id] [int] NOT NULL,
	[dev_tag] [varchar](50) NOT NULL,
	[vpnid] [int] NOT NULL,
	[lock_dev] [int] NOT NULL,
	[phoneinfo] [text] NOT NULL,
	[lastfinishday] [datetime] NULL,
	[lastfetchday] [datetime] NULL,
	[todayopencount] [int] NOT NULL,
	[remaininfo] [varchar](1000) NOT NULL,
	[firstremaintime] [datetime] NOT NULL,
	[lastremaintime] [datetime] NOT NULL,
	[dotoday] [int] NOT NULL,
	[newregtime] [datetime] NULL,
	[stoptime] [datetime] NULL,
	[ext] [varchar](500) NULL,
	[appinfo] [text] NULL,
 CONSTRAINT [PK_tb_advremaintask] PRIMARY KEY CLUSTERED 
(
	[autoid] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
 *
 */
public class advremaintask {
	public static String tablename="tb_advremaintask";
	//自增长
	int rid;
	//用作留存压缩文件名序列号
	//2018-12-7 把autoid改为主键
	long autoid;
	int adv_id;
	String dev_tag;
	int vpnid;//最初使用vpn，防止ip变化太大	
	//是否锁定机器，如果是，只能用上面dev_tag的机器做留存
	int lock_dev;
	//新增时的机器信息,2018-11-14把phonetypeinfo改成phoneInfo
	String phoneInfo;
	//最后执行完成日期
	Date lastfinishday;
	//最后获取日期，防止机器重启时，如果重启前分发给客户端执行，就不再重新获取
	Date lastfetchday;
	//当天设备打开此广告打开次数，（****之前这个数据要和advtaskinfo的要求一致,现在改了，如果advtaskinfo的数值大于0，表示固定打开，否则按比例随机*****）
	//如果是新增时多次打开插入表时，按配置文件动态随机计算一个数值，一般情况下每天0点做留存计算时，按1-10次动态计算
	int todayopencount;
	//执行留存标识，用由数字1或0组成的字符串，从第一天开始，用1和０表示是否执行，半年内的字符串长度360个
	//字符串第一个字符肯定为1
	//这个用户整个留存周期内的所有要做活跃的日期
	String remaininfo;
	//此广告做留存的第一天时间
	Date firstremaintime;
	//这里用多一个字段记录最后一天留存时间，从上面rmaininfo最后一天加多一天．方便在系统启动或每天０点计算留存时直接比较此字段，判断这个记录是否已经完成，如果是设dotoday为-1
	Date lastremaintime;
	//modify 2018-10-24
	//增加一个字段表示新增时的完成时间，用于方便统计某天的留存率和方便区分当天是做留存还是当天新增的多次活跃，如果这个日期为今天，表示这个不是真实做留存，而是
	//做新增时，第二次以后的打开数
	Date newregtime;	
	//当天执行数量，初始值为dayopencount,大于０表示当天要执行，小于等于０都表示当天不需要执行
	//这个每天早上0点10秒由数据库作业定时更新，首先判断当前时间是否大于rmaininfo，如果是，表示此记录以后都不做留存了，把dotoday置为－１.
	//再判断今天的日期是否包含在remaininfo中，如果是就把dotoday置为dayopencount,否则置为0，表示今天此记录不需要做留存，
	//如果在任务表advtaskinfo中对应的adv_id的isoffline＝2表示此广告和留存都要停，这时dotoday值为－２，
	//程序执行完成后置为减1值到为0	
	//这个数值和dayopencount可以用于判断是否第一次做留存，如果不是第一次，则在
	int dotoday;
	//判断是否当天新增活跃
	boolean newuser_today;
	//增加一个字段，appinfo，分离之前的phonetypinfo，把那些留存中经常变化的放在appinfo中
	String appinfo = "";
	
	public int getVpnid() {
		return vpnid;
	}
	public void setVpnid(int vpnid) {
		this.vpnid = vpnid;
	}
	
	public int getRid() {
		return rid;
	}
	public void setRid(int rid) {
		this.rid = rid;
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
	public int getLock_dev() {
		return lock_dev;
	}
	public void setLock_dev(int lock_dev) {
		this.lock_dev = lock_dev;
	}
	
	public String getPhoneInfo() {
		return phoneInfo;
	}
	public void setPhoneInfo(String phoneInfo) {
		this.phoneInfo = phoneInfo;
	}
	public Date getLastfinishday() {
		return lastfinishday;
	}
	public void setLastfinishday(Date lastfinishday) {
		this.lastfinishday = lastfinishday;
	}
	public Date getLastfetchday() {
		return lastfetchday;
	}
	public void setLastfetchday(Date lastfetchday) {
		this.lastfetchday = lastfetchday;
	}
	
	public String getRemaininfo() {
		return remaininfo;
	}
	public void setRemaininfo(String remaininfo) {
		this.remaininfo = remaininfo;
	}
	
	
	public Date getLastremaintime() {
		return lastremaintime;
	}
	public void setLastremaintime(Date lastremaintime) {
		this.lastremaintime = lastremaintime;
	}	
	
	public Date getFirstremaintime() {
		return firstremaintime;
	}
	public void setFirstremaintime(Date firstremaintime) {
		this.firstremaintime = firstremaintime;
	}
	
	public Date getNewregtime() {
		return newregtime;
	}
	/** 设置用户注册完成时间，同时判断newregtime如果是今天，则设 newuser_today为true
	 * @param newregtime 注册完成时的时间
	 */
	public void setNewregtime(Date newregtime) {
		this.newregtime = newregtime;
		if(this.newregtime != null)
		{
			if(DateUtil.isSameDate(new Date(), newregtime))
				this.newuser_today = true;
		}
	}
	
	
	public int getTodayopencount() {
		return todayopencount;
	}
	public void setTodayopencount(int todayopencount) {
		if(todayopencount < 0)
			todayopencount = 0;
		this.todayopencount = todayopencount;
	}
	public boolean isNewuser_today() {
		return newuser_today;
	}
	public int getDotoday() {
		return dotoday;
	}
	public void setDotoday(int dotoday) {
		this.dotoday = dotoday;
	}
		
	public long getAutoid() {
		return autoid;
	}
	public void setAutoid(long autoid) {
		this.autoid = autoid;
	}
	
	
	public String getAppinfo() {
		return appinfo;
	}
	public void setAppinfo(String appinfo) {
		this.appinfo = appinfo;
	}
	
	public String toString()
	{
		String s = "";
		s += "rid=" + rid + "\r\n";		
		s += "adv_id=" + adv_id + "\r\n";
		s += "dev_tag=" + dev_tag + "\r\n";
		s += "vpnid=" + vpnid + "\r\n";
		s += "lock_dev=" + lock_dev + "\r\n";
		s += "phoneInfo=" + phoneInfo + "\r\n";
		s += "dayopencount=" + todayopencount + "\r\n";
		return s;
	}
}
