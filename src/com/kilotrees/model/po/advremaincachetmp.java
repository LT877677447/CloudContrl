package com.kilotrees.model.po;

import java.util.Calendar;
import java.util.Date;

import com.kilotrees.util.DateUtil;

/**
 * 当天留存缓存数据，每个广告任务完成后，如果需要做留存，则先把数据写入此表，后面由advremaintask_service
 * 定时取出数据计算其所有留存时间．
 * 

CREATE TABLE [dbo].[tb_advremaincachetmp](
	[cacheid] [int] IDENTITY(1,1) NOT NULL,
	[autoid] [bigint] NULL,
	[adv_id] [int] NOT NULL,
	[dev_tag] [varchar](50) NOT NULL,
	[vpnid] [int] NOT NULL,
	[lock_dev] [int] NOT NULL,
	[phoneinfo] [text] NOT NULL,
	[usedtime] [int] NOT NULL,
	[procok] [int] NOT NULL,
	[adv_finish_time] [datetime] NOT NULL,
	[appinfo] [text] NULL,
	[retopencount] [int] NOT NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

 * @author Administrator
 *
 */
public class advremaincachetmp {
	public static final String tablename = "tb_advremaincachetmp";
	int cacheid;
	int adv_id;
	String dev_tag;
	int vpnid;
	int lock_dev;
	//2018-11-14 把phonetypeinfo改成phoneinfo
	String phoneInfo;
	// 广告在设备上注册时使用时长,为了节约时间，我们一般下发任务时指定时长，在注册新广告时选择35％的用户打开不超过25秒或远小于要求时间，
	// 而且这些用户基本不考虑做留存
	int usedtime;
	// 是否已经处理  0：未处理  1：已处理
	int procok;
	// 任务执行结束时间，那么从第二天开始做留存
	Date adv_finish_time = new Date();
	// 第一天要做留存的时间
	Date firstremaintime;
	//压缩文件序列号
	long autoid;

	// 设置一个最大留存日期的数组，从adv_finish_time第二天开始计算，如果某个数值为１，表示当天要做留存，０表示不做，-１表示结束了
	int[] act_days_flag = new int[ServerConfig.max_remain_days];
	boolean setremain;
	//增加一个字段，appinfo，分离之前的phonetypinfo，把那些留存中经常变化的放在appinfo中
	String appinfo = "";
	//2018-11-30 增加一下字段，记录今天剩下的打开次数(活跃数)，对于新增不需要做留存，但打开次数大于1时，或者为0时，随机计算出剩下的打开次数
	int retopencount;
	

	
	public long getAutoid() {
		return autoid;
	}

	public void setAutoid(long autoid) {
		this.autoid = autoid;
	}

	public int getCacheid() {
		return cacheid;
	}

	public void setCacheid(int cacheid) {
		this.cacheid = cacheid;
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

	public int getVpnid() {
		return vpnid;
	}

	public void setVpnid(int vpnid) {
		this.vpnid = vpnid;
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

	public int getProcok() {
		return procok;
	}

	public void setProcok(int procok) {
		this.procok = procok;
	}

	public Date getAdv_finish_time() {
		return adv_finish_time;
	}

	public void setAdv_finish_time(Date adv_finish_time) {
		this.adv_finish_time = adv_finish_time;
	}

	public int getUsedtime() {
		return usedtime;
	}

	public void setUsedtime(int usedtime) {
		this.usedtime = usedtime;
	}
	
	

	public int getRetopencount() {
		return retopencount;
	}

	public void setRetopencount(int retopencount) {
		this.retopencount = retopencount;
	}

	/**
	 * @param index month_days[]数组中值为0的下标
	 * @param flag
	 */
	public void setAct_days_flag(int index, int flag) {
		if (index < 0 || index > this.act_days_flag.length)
			return;
		act_days_flag[index] = flag;
		if (flag == 1)
			setremain = true;
	}

	public boolean isSetremain() {
		return setremain;
	}
	
	public String getAppinfo() {
		return appinfo;
	}

	public void setAppinfo(String appinfo) {
		this.appinfo = appinfo;
	}

	// 按act_days_flag设置返回advremaintask表中的remaininfo格式
	public String generateRemainInfo() {
		if (!setremain)
			return "";
		String s = "";
		Calendar date = Calendar.getInstance();
		date.setTime(this.adv_finish_time);
		int len = 0,max = 0;
		for (int i = 0; i < act_days_flag.length; i++) {
			if (act_days_flag[i] == -1)
				break;
			if (act_days_flag[i] == 1) {
//				if (this.firstremaintime == null) {
//					date.add(Calendar.DATE, i + 1);
//					date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE), 0, 0, 0);
//					this.firstremaintime = date.getTime();
//				}
				s += "1";
				len++;
				max = len;
			} else if(s.length() > 0)
			{
				s += "0";
				len++;
			}

		}
		s = s.substring(0, max);
		return s;
		// 之前的方式是把日期组合在一个字符串中，这种查询效率差，字符串也太长
		// Calendar date = Calendar.getInstance();
		// date.setTime(this.adv_finish_time);
		// //date.add(Calendar.DATE, 1);
		// String s = "";
		// for(int i = 0; i < act_days_flag.length; i++)
		// {
		// if(act_days_flag[i] == -1)
		// break;
		// if(act_days_flag[i] == 1)
		// {
		// date.add(Calendar.DATE, i+1);
		// s += DateUtil.getShortDateString(date.getTime());
		// s += ";";
		// //重置为广告完成时间
		// date.setTime(this.adv_finish_time);
		// }
		// }
		// if(s.endsWith(";"))
		// s = s.substring(0, s.lastIndexOf(";"));
		// return s;

	}

	public Date getFirstremaintime() {
		if (!setremain)
			return null;
		if (firstremaintime == null) {
			Calendar date = Calendar.getInstance();
			date.setTime(this.adv_finish_time);
			for (int i = 0; i < act_days_flag.length; i++) {
				if (act_days_flag[i] == -1)
					break;
				if (act_days_flag[i] == 1) {
					date.add(Calendar.DATE, i + 1);
					date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE), 0, 0, 0);
					//2018/11/16 要设置豪秒，advremaintaskdao.proc_init()会出问题
					date.set(Calendar.MILLISECOND, 0);
					this.firstremaintime = date.getTime();
					break;
				}
			}
		}
		return firstremaintime;
	}

	public void setFirstremaintime(Date firstremaintime) {
		this.firstremaintime = firstremaintime;
	}

	public Date getLastRemainTime() {
		Calendar date = Calendar.getInstance();
		date.setTime(this.adv_finish_time);
		int max = 0;
		for (int i = 0; i < act_days_flag.length; i++) {
			if (act_days_flag[i] == -1)
				break;
			if (act_days_flag[i] == 1) {
				max = i;
			}
		}
		date.add(Calendar.DATE, max + 2);
		date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE), 0, 0, 0);
		//2018/11/16 要设置豪秒，advremaintaskdao.proc_init()会出问题
		date.set(Calendar.MILLISECOND, 0);
		return date.getTime();
	}

	// 用插花方式随机分布几条数据,以每个月插入3条
	public void arrangeFlower(int endIdex) {
		int month = 30;// 一个月30天
		int count = 3 * endIdex / month;
		if (count < 1)
			count = 1;
		for (int i = 0; i < count; i++) {
			int pos = new java.util.Random().nextInt(endIdex);
			this.setAct_days_flag(pos, 1);
		}
	}

	public static void main(String[] argv) {
		advremaincachetmp app = new advremaincachetmp();
		app.setAct_days_flag(0, 1);
		app.setAct_days_flag(2, 1);
		app.setAct_days_flag(5, -1);
		String s = app.generateRemainInfo();
		System.out.println(s);

		Date d = app.getLastRemainTime();
		System.out.println(d);
		s = DateUtil.getShortDateString(d);
		System.out.println("lastremaindate = " + s);
	}

}
