package com.kilotrees.model.po;

import java.util.Date;

import org.apache.log4j.Logger;

import com.kilotrees.dao.advgroupdao;
import com.kilotrees.dao.advtaskinfodao;
import com.kilotrees.service.adv.runtime.api.ITaskRuntime;
import com.kilotrees.services.actdeviceinfo_service;
import com.kilotrees.services.advnewtask_service;
import com.kilotrees.services.main_service;
import com.kilotrees.util.StringUtil;

/**
 * 

CREATE TABLE [dbo].[tb_advgroup](
	[groupid] [int] NOT NULL,
	[name] [varchar](50) NOT NULL,
	[advids] [varchar](500) NOT NULL,
	[type] [int] NULL,
	[maxdotime] [int] NOT NULL,
	[modify_time] [datetime] NOT NULL,
	[onlineflag] [int] NOT NULL,
 CONSTRAINT [PK_tb_advgroup] PRIMARY KEY CLUSTERED 
(
	[groupid] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

 * 
 * 广告分组最先应用于大点击广告业务，以后可以考虑到cpa中
 * 广告按是否有组分类：A:类似大点击能同时执行，由注入脚本调用，可以在窗口非顶层也能执行．B:象触摸精灵那种，只能单一执行．
 * 按是否有时间曲线又分成2类，一类是要有时间曲线的，一类是冲量，不走时间曲线，不需要做留存 象养粉这种以后再考虑．
 * 在广告建立时，我们先按广告类别手工分好组，一般A中的广告要么按时间曲线，要么都是冲量．
 * 把相似类型的广告分组，只有能并行执行的广告才能归到一组，但如果每个广告的执行时间不一样，所有的总时长算起来其实很麻烦．
 * 另外不同的广告id，可能apk是一样的包，只不过不同的渠道，这种也要避免的．
 * 分组的广告有利于提高单台机器的效率，但分配任务和做留存或组内的广告变动时，极其麻烦，
 * 系统开始初始期，广告组只做大点击的链接任务，以后稳定了，再优化其它类别的分组逻辑．
 *
 */
public class advgroup {
	private static Logger log = Logger.getLogger(advgroup.class);
	public final static String tablename = "tb_advgroup";
	// 组id，如果是留存话，最好id从1000开始
	int groupid;
	String name;
	String advids;
	// 类型0:广告分组,1:留存分组
	int type;
	//整个组完成的时间，秒
	int maxdotime;
	Date modify_time;
	// 广告组的上线标识，一般手工上线并设置为１，
	//如果建立广告组设置为3，并把里面的广告在tb_advtaskinfo的onlineflag也设为3,让程序自行分配，
	//这里的标识和广告任务表的onlineflag是分开的，如果某个任务onlineflag停了，这里对应的广告任务也不会执行，但其它照样执行
	int onlineflag;

	int[] adv_ids;

	// ArrayList<advtaskruntimeinfo> advidList = new
	// ArrayList<advtaskruntimeinfo>();
	Date act_alloc_timeout;
	boolean alloc_init = false;
	//需求设备数
	int reqDevDoCount;

	// public void addToGroup(advtaskruntimeinfo ai) {
	// for (advtaskruntimeinfo e : advidList)
	// if (e.getAdvinfo().getAdv_id() == ai.getAdvinfo().getAdv_id())
	// return;
	// advidList.add(ai);
	// }
	//
	// public void remove(advtaskruntimeinfo ai) {
	// advidList.remove(ai);
	// }

	public int getGroupid() {
		return groupid;
	}

	public void setGroupid(int groupid) {
		this.groupid = groupid;
	}

	public String getAdvids() {
		return advids;
	}

	public void setAdvids(String advids) {
		this.advids = advids;
		if(StringUtil.isStringEmpty(advids))
		{
			adv_ids = new int[0];
			return;
		}
		String[] s = this.advids.split(";");
		adv_ids = new int[s.length];
		for (int i = 0; i < adv_ids.length; i++)
			adv_ids[i] = Integer.parseInt(s[i]);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Date getModify_time() {
		return modify_time;
	}

	public void setModify_time(Date modify_time) {
		this.modify_time = modify_time;
	}

	public boolean isIngroup(int adv_id) {
		for (int e : adv_ids)
			if (e == adv_id)
				return true;
		return false;
	}

	public int getOnlineflag() {
		return onlineflag;
	}

	public void setOnlineflag(int onlineflag) {
		this.onlineflag = onlineflag;
	}	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMaxdotime() {
		return maxdotime;
	}

	public void setMaxdotime(int maxdotime) {
		this.maxdotime = maxdotime;
	}
	/**
	 * 某广告停了，更新advids
	 * @param adv_id
	 */
	public void setAdvOffline(int adv_id)
	{
		boolean bFound = false;
		String s = "";
		for (int i = 0; i < adv_ids.length; i++){
			int e = adv_ids[i];
			if (e == adv_id)
			{
				bFound = true;
				continue;
			}
			s += e;
			if(i != adv_ids.length -1)
				s += ";";
		}
		if(bFound)
		{
			//int[] temp = new int[adv_ids.length -1];
			//for(int i = 0; i < adv_ids.len)
			this.setAdvids(s);
			advgroupdao.updateAdvs(this);
					
		}
	}

	/**
	 * 返回这组广告最大那个小时用时量 这里算法比较复杂，比如一组有3个广告，某个小时分别要做1个，２个，１０个广告，但每个广告做完时长为3,2,1分钟．
	 * 如果几个广告必须都完成后才能做下一轮新增，则一台机器要做完的时间为： 3(第一轮耗时)+2(第二轮时间)+(10-2)(剩下８个１分钟广告) =
	 * 13分钟 所以分组的广告，每个耗时最好在写脚本时就指定一样，或者相差极少． 或者是不需要留存的冲量包如果一起并行执行，效率会高很多
	 * 
	 * 直接使用表中maxdotime，不考虑每个广告时长
	 */
	public int getMaxDoTimeOfHour() {
		int maxCount = 0;
		int maxDoTime = maxdotime;
		//if (this.type == 0) {
			for (int i = 0; i < this.adv_ids.length; i++) {
				int adv_id = adv_ids[i];
				ITaskRuntime e = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id);
				int max = e.caclMaxHoureCountOfAllDate();
				if (max > maxCount)
					maxCount = max;
//				if (e.getAdvinfo().getRequesttime() > maxDoTime)
//					maxDoTime = e.getAdvinfo().getRequesttime();
			}
//		} else {
//			for (int i = 0; i < this.adv_ids.length; i++) {
//				int adv_id = adv_ids[i];
//				advremainruntimeinfo remain = new advremainruntimeinfo();
//				remain.setAdv_id(adv_id);
//				// 预先设置docount,对于第一次分配留存时，用新增同样的数量
//				int docount = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo().getDaycount();
//				//如果已经在运行，则取今天的留存数，并加上今天执行数/2
//				int todayRemain = advremaintaskdao.getTodayRemainTaskCountByAdvId(adv_id);
//				if(todayRemain > 0)
//				{
//					//之所以除以2，是因为第二天留存一般不超过50%,明天的留存比今天留存数不超过当天执行数的留存．
//					//准确的话应该按广告的remRule规则算出来
//					docount = todayRemain + docount/2;
//				}
//				remain.setTodocount(docount);
//				int max_ofhoure = remain.caclMaxHoureCountOfAllDate();
//				//remain.caclHouursCount(true);
//				if (max_ofhoure > maxCount)
//					maxCount = max_ofhoure;
//				if (advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo()
//						.getRemaintime() > maxDoTime)
//					maxDoTime = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo()
//							.getRemaintime();
//			}
//		}
		
		//maxDoTime += serverconfig.adv_extend_time;
		//直接使用
		return maxDoTime * maxCount;
	}

	void print() {
		// for (int i = 0; i < advidList.size(); i++) {
		// System.out.println("index_" + i + ":" + advidList.get(i));
		// }
	}
	/**
	 * 广告上线后第一次分配机器数量
	 * @return
	 */
	public boolean allocDevForNew() {
		if (alloc_init)
			return true;
		reqDevDoCount = getMaxDoTimeOfHour() / 3600 + 1;
		actdeviceinfo_service.getInstance().freeDeviceForGroup(groupid, reqDevDoCount);
		if (actdeviceinfo_service.getInstance().getFreeCount() < reqDevDoCount) {
			int c = reqDevDoCount - actdeviceinfo_service.getInstance().getFreeCount();
			log.error("当前空闲设备设备不足，请增加机器:" + c + "台");
			return false;
		}
		actdeviceinfo_service.getInstance().allocatDeviceForGroup(groupid, reqDevDoCount);		
		setOnlineflag(1);
		advgroupdao.updateOnlineFlag(this);
		if (getType() == 0) {
			String[] advids = getAdvids().split(";");
			int[] adv = new int[advids.length];
			for (int i = 0; i < adv.length; i++)
				adv[i] = Integer.parseInt(advids[i]);

			for (int i = 0; i < adv.length; i++) {
				advtaskinfo task = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv[i])
						.getAdvinfo();
				task.setOnlineflag(1);
				task.setOnline_time(new Date());
				advtaskinfodao.updateOnlineFlag(task);
			}
		}
		alloc_init = true;
		return alloc_init;
	}

	public void reAllocDev(boolean inited) {
		//log.info("任务组:" + this.name + "reAllocDev");
		reqDevDoCount = getMaxDoTimeOfHour() / 3600 + 1;
		int alloc_count = actdeviceinfo_service.getInstance().getAllocedGroupDevCount(this.groupid);
		int alloc_act_count = actdeviceinfo_service.getInstance().getAllocedGroupDevCountAct(groupid);
		if (reqDevDoCount == alloc_act_count)
			return;
		int diff = 0;
		if (reqDevDoCount > alloc_count) {
			diff = reqDevDoCount - alloc_count;
			log.error("广告组:" + groupid + ",需求机器数量：" + reqDevDoCount + ";现在总分配数量：" + alloc_count);
			log.error("需要增加" + diff);

			if (actdeviceinfo_service.getInstance().getFreeCount() < diff) {
				int c = diff - actdeviceinfo_service.getInstance().getFreeCount();
				log.error("当前空闲设备设备不足，请增加机器:" + c + "台");
				return;
			}
			actdeviceinfo_service.getInstance().allocatDeviceForGroup(groupid, diff);
		} else if (alloc_act_count > reqDevDoCount) {
			diff = alloc_act_count - reqDevDoCount;
			log.warn("广告组" + groupid + "分配的设备活跃数大于需求数,释放设备台数:" + alloc_act_count);
			actdeviceinfo_service.getInstance().freeDeviceForGroup(groupid, diff);
		} else if (alloc_act_count < reqDevDoCount) {
			// 计算超时时间，
			int freecount = actdeviceinfo_service.getInstance().getFreeCount();
			diff = reqDevDoCount - alloc_act_count;
			if(freecount >= diff){
				actdeviceinfo_service.getInstance().reallocatDeviceForGroup(groupid, diff);
				return;
			}				
			if (act_alloc_timeout == null)
				act_alloc_timeout = new Date();
			else {
				if (main_service.getInstance().startTimeout() < ServerConfig.act_realloc_wait)
					return;
				
				Date now = new Date();
				long timeout = (now.getTime() - act_alloc_timeout.getTime()) / 1000;
				log.warn("广告组:" + groupid + "活跃设备不足，等待超时分配:" + diff);
				if (actdeviceinfo_service.getInstance().getFreeCount() < diff)
					return;
				if (timeout > ServerConfig.act_realloc_timeout) {
					actdeviceinfo_service.getInstance().reallocatDeviceForGroup(groupid, diff);
					act_alloc_timeout = null;
				}
			}
		}
	}
	
	public String getTaskRuntimeInfo()
	{
		String sruntime = "任务组:" + name + "(" + this.groupid + ")\r\n";
		int alloc_count = actdeviceinfo_service.getInstance().getAllocedGroupDevCount(this.groupid);
		int alloc_act_count = actdeviceinfo_service.getInstance().getAllocedGroupDevCountAct(groupid);
		sruntime += "需求机器数量：" + reqDevDoCount + ";已经总分配数量：" + alloc_count + "\r\n";
		sruntime += "活跃分配设备:" + alloc_act_count;
		log.info(sruntime);
		return sruntime;
	}

	public static void main(String[] argv) {
	//	advgroup app = new advgroup();

		// app.print();
		String advids = "";
		String[] s = advids.split(";");
		int[] adv_ids = new int[s.length];
		System.out.println("adv_ids.length=" + adv_ids.length);
		for (int i = 0; i < adv_ids.length; i++){
			adv_ids[i] = Integer.parseInt(s[i]);
			System.out.println(adv_ids[i]);
		}
	}
}
