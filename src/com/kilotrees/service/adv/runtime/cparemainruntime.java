package com.kilotrees.service.adv.runtime;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.kilotrees.dao.advremaintaskdao;
import com.kilotrees.model.bo.TaskBase;
import com.kilotrees.model.bo.TaskCPARemain;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.advremaintask;
import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.model.po.advtodayresult;
import com.kilotrees.service.adv.runtime.api.ITaskRuntime;
import com.kilotrees.services.ErrorLog_service;
import com.kilotrees.services.actdeviceinfo_service;
import com.kilotrees.services.advgroup_service;
import com.kilotrees.services.advnewtask_service;
import com.kilotrees.services.advremaintask_service;
import com.kilotrees.services.main_service;
import com.kilotrees.services.timeline_service;
import com.kilotrees.util.StringUtil;
/**
 * 2019-1-15 修改成统一继承advruntimeimpl接口
 * @author elememt
 *
 */
public class cparemainruntime extends advruntimebase{
	private static Logger log = Logger.getLogger(cparemainruntime.class);

	int taskid = -9;// 留存广告任务的taskid
	//int adv_id;
	// 对应广告id的剩余要做留存记录数据，这个跟新增的不一样，每次重启后都实时从数据库留存任务表查到，不需要再减去result的成功数据,
	// 每完成一个留存任务就相应减少并在数据库留存表中记录，这个和新增任务不相同，新增任务是固定不变的．
	// 这里是对应广告id的数据库字段dotoday总和,每条记录乘以每天打开数量
	// 2018-12-3，把todocount拆分为旧用户，新用户(已经加到留存表），今天没有完成的新增三部分中的dotoday总和
	// int todocount;
	
	
	//advtodayresult result;

//	// 需要多少台手机做留存
//	int reqDevDoCount;
//	// 是否强制下线
//	boolean offline2;
//	// 平均留存时间
//	int remaintime;
//	// 留存描述字串
//	String rem_rule;
//	// 留存曲线
//	int timelineid = -2;	
//	// 一天每小时要做的数量
//	int[] docount_hours;
//	// 每小时剩余的数量，初始值和docount_hours一样，但实时计算提取任务，每提取一个就少一个．
//	int[] retcount_hours;
//	// 24小时中最大量的
//	int max_docountOfHours;
	// 是否lock_dev,不再考虑，无用。
	//2018-12-26取消lock_dev
	//int lock_dev;
	// 从今天开始7天剩余要做的广告数量(直接从留存任务表查出，不包含这段时间的新增，主要用来当新增数量改变时，预测7天内机器空闲数量是否足够)
	// 当新增暂停或减少新增时，留存继续做时，对于释放设备也是有计算价值,因为今后活跃数是随机变化的，这里还要仔细考虑
	int[] do_count_af7days;
	// 分配超时，刚启动时，可能在线的客户端数量还有很多没有登录，所以给出一定的超时时间
	private Date act_alloc_timeout;
	// 是否需要重新计算docount_hours,如果系统启动或者留存时间，时间曲线有变化，则需要重新计算．
	//boolean need_reCaclRuntime;
	// modify 2018-10-25，把当日新增活跃当留存在这里分开统计
	int old_user_count;
	// 当天新增用户要做多次活跃的用户数
	int today_user_count;
	int old_todocount;
	// 当天新增用户，放到留存表中的当天活跃的dotoday总和
	int new_todocount;
	// 当天新增用户（还没有开始做）将要做的活跃数（估算)
	int newready_todocount;
	// 旧用户打开总次数(活跃总数）
	// int old_user_opencount;
	// 当天用户活跃总数(除去第一次)
	// int today_user_opencount;
	//String adv_name = "";

//	public boolean isNeed_reCaclRuntime() {
//		return need_reCaclRuntime;
//	}
//
//	public void setNeed_reCaclRuntime(boolean need_reCaclRuntime) {
//		this.need_reCaclRuntime = need_reCaclRuntime;
//	}

	// public int getTodocount() {
	// return todocount;
	// }
	//
	// public void setTodocount(int todocount) {
	// this.todocount = todocount;
	// need_reCaclRuntime = true;
	// }

	public int getOld_todocount() {
		return old_todocount;
	}

	public void setOld_todocount(int old_todocount) {
		this.old_todocount = old_todocount;
	}

	public int getNew_todocount() {
		return new_todocount;
	}

	public void setNew_todocount(int new_todocount) {
		this.new_todocount = new_todocount;
	}

	public int getNewready_todocount() {
		 
		ITaskRuntime ari = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(this.advinfo.getAdv_id());
		if(ari != null){
			advtaskinfo ai = ari.getAdvinfo();
			float adveropencount = 1;
			if (ai != null) {
				newready_todocount = ai.getDayusercount();
				//adveropencount = ai.getDayOpenCountAdver();
				adveropencount = ari.getDayOpenCountAver();
			}
			advtodayresult r = ari.getResult();
			if (r != null) {
				newready_todocount -= r.getNewuser_success_count();
			}
			adveropencount -= 1;// 减去第一次新增
			newready_todocount = (int) (newready_todocount * adveropencount);
		}
		return newready_todocount;
	}

	public void setNewready_todocount(int newready_todocount) {
		this.newready_todocount = newready_todocount;
	}

	public int getAllTodocount() {
		return (old_todocount + new_todocount + getNewready_todocount());
	}

//	
//
//	public boolean isOffline2() {
//		return offline2;
//	}
//
//	public void setOffline2(boolean offline2) {
//		this.offline2 = offline2;
//		if (offline2)
//			freeDevWhileOffline();
//	}

//	public String getRem_rule() {
//		return rem_rule;
//	}
//
//	public void setRem_rule(String rem_rule) {
//		this.rem_rule = rem_rule;
//	}

//	public int getTimelineid() {
//		return timelineid;
//	}
//
//	public void setTimelineid(int timelineid) {
//		if (this.timelineid != timelineid)
//			this.need_reCaclRuntime = true;
//
//		this.timelineid = timelineid;
//	}
//
//	public int getRemaintime() {
//		return remaintime;
//	}

//	public void setRemaintime(int remaintime) {
//		if (this.remaintime != remaintime) {
//			log.warn("重置need_recaclRuntime,remaintime改变了：" + remaintime);
//			this.need_reCaclRuntime = true;
//		}
//		this.remaintime = remaintime;
//	}
//
//	public int[] getDocount_hours() {
//		return docount_hours;
//	}

	// public void setDocount_hours(int[] do_hours) {
	// //this.docount_hours = do_hours;
	// int curhour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
	// //当前小时之前的数据不重置，否则之前的数据可能为0
	// for(int i = curhour; i < do_hours.length; i++)
	// docount_hours[i] = do_hours[i];
	// //第一次设置
	// if(retcount_hours == null){
	// this.retcount_hours = new int[docount_hours.length];
	// for (int i = 0; i < docount_hours.length; i++)
	// retcount_hours[i] = docount_hours[i];
	// }
	// for (int i = 0; i < docount_hours.length; i++) {
	// if( i > curhour){
	// //不能影响之前的数据
	// retcount_hours[i] = docount_hours[i];
	// }
	// if(docount_hours[i] > this.max_docountOfHours)
	// this.max_docountOfHours = docount_hours[i];
	// }
	// }

	

//	public Date getFirstDoDay() {
//		return firstDoDay;
//	}
//
//	public void setFirstDoDay(Date firstDoDay) {
//		//this.firstDoDay = firstDoDay;
//	}
//
//	public int getGroupid() {
//		return groupid;
//	}
//
//	public void setGroupid(int groupid) {
//		this.groupid = groupid;
//	}

	public int[] getDo_count_af7days() {
		return do_count_af7days;
	}

	@Override
	public void setOffline(boolean isOffline) {
		// TODO Auto-generated method stub
		super.setOffline(isOffline);
		if(isOffline)
			this.freeDevWhileOffline();
	}

	public void setDo_count_af7days(int[] do_count_af7days) {
		this.do_count_af7days = do_count_af7days;
	}

//	public int isLock_dev() {
//		return lock_dev;
//	}
//
//	public void setLock_dev(int lock_dev) {
//		this.lock_dev = lock_dev;
//	}

	public int getOld_user_count() {
		return old_user_count;
	}

	public void setOld_user_count(int old_user_count) {
		this.old_user_count = old_user_count;
	}

	public int getToday_user_count() {
		return today_user_count;
	}

	public void setToday_user_count(int today_user_count) {
		this.today_user_count = today_user_count;
	}

	//
	// public int getOld_user_opencount() {
	// return old_user_opencount;
	// }
	//
	// public void setOld_user_opencount(int old_user_opencount) {
	// this.old_user_opencount = old_user_opencount;
	// }
	//
	// public int getToday_user_opencount() {
	// return today_user_opencount;
	// }
	//
	// public void setToday_user_opencount(int today_user_opencount) {
	// this.today_user_opencount = today_user_opencount;
	// }

	public void caclRuntimeHouursCount() {
		if (isOffline())
			return;
		if (need_reCaclRuntime == false) {
			log.warn("caclRuntimeHouursCount 上次已经计算过，任务没有变化，直接返回");
			return;
		}
		int count = getAllTodocount();// getTodocount();
		int timelineid = this.advinfo.getRem_timeline();
		int h[] = timeline_service.getInstance().caclDistributeOfHours(timelineid, false, count);
		if (this.docount_hours == null)
			this.docount_hours = new int[h.length];
		if (this.retcount_hours == null)
			retcount_hours = new int[h.length];
		this.max_docountOfHours = advruntimebase.setDocount_hours(h, this.docount_hours, this.retcount_hours);
		//2018,在这里计算出reqDevDoCount，不然开机总是为０
		//int wor
		int max_dotime_hour = this.max_docountOfHours * this.advinfo.getRemaintime();
		this.reqDevDoCount = max_dotime_hour / 3600 + 1;
		need_reCaclRuntime = false;
	}

	public int caclMaxHoureCountOfAllDate() {
		if (isOffline())
			return 0;
		int maxCount = 0;
		int count = getAllTodocount();// getTodocount();
		int timelineid = this.advinfo.getRem_timeline();
		int h[] = timeline_service.getInstance().caclDistributeOfHours(timelineid, true, count);
		for (int i = 0; i < h.length; i++)
			if (h[i] > maxCount)
				maxCount = h[i];
		return maxCount;
	}
	/**
	 * 如果是online_flag=3的话，其实新增还没有开始，要预测这个后面留存数目，但后面没有做了，所以这个以后再考虑
	 * @return
	 */
	public boolean allocNewReady() {
		if (isOffline())
			return false;
		//先屏蔽了，以后再考虑
//		advtaskinfo ai = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(this.adv_id).getAdvinfo();
//		if (this.remaintime <= 0) {
//			// 2018-12-3
//			// if(ai.getDayopencount() == 1){
//			log.error("allocNewReady　advid=" + this.adv_id + "此任务不需要做留存！");
//			return false;
//			// }
//		}
//		if (advgroup_service.getInstance().getGroupIncludeAdv(this.adv_id, true) != null) {
//			log.error("allocDevForNew　此广告属于组，应该在组分配");
//			return false;
//		}
//
//		if (ai.isHandle_Locked())
//			return false;
//		if (ai.getRemain_lock_dev() == 1) {
//			log.error("allocDevForNew　此广告类型为lock_dev，不在此分配");
//			return false;
//		}
		// 第一次计算留存机器数量，单独分配时预计先分一周左右
		// 暂时不考虑那么多了，屏蔽先
		// int newDayCount = ai.getDaycount();
		// String remrul = ai.getRemain_rule();
		// String[] s = remrul.split(";");
		// remrul = s[0] + ";" + s[1] + ";-1;-1;-1";
		// int sum = caclMothRemainByRule(remrul, null);
		// sum = sum * newDayCount / 100;
		// 2018-12-3，还是用回最简的先
		//当初为了加上getAllTodocount才修改的，下面其实是有问题的。
		//int sum = this.getAllTodocount();

		//int max_remain_ofhours = advtaskruntimeinfo.caclMaxHoureCountOfAllDateByTimeline(sum, this.timelineid);
		//this.reqDevDoCount = ai.getRemaintime() * max_remain_ofhours / 3600 + 1;
		return true;

	}

	void freeDevWhileOffline() {
		log.info("freeDevWhileOffline 任务下线，释放广告" + this.advinfo.getAdv_id() + "所占用设备");
		int alloc_type = ServerConfig.adv_alloc_type_remain;
//		if (this.lock_dev == 1)
//			alloc_type = serverconfig.adv_alloc_type_lockremain;
		actdeviceinfo_service.getInstance().freeDeviceForSingleAdv( this.advinfo.getAdv_id(), -1, alloc_type);
	}

	public boolean allocDevForNew() {
		if (!allocNewReady())
			return false;
		if (isOffline())
			return false;
		int freeCount = actdeviceinfo_service.getInstance().getFreeCount();
		if (freeCount == 0) {
			log.error("没有空闲设备，分配失败");
			return false;
		}
		int alloc_type = ServerConfig.adv_alloc_type_remain;
		if (actdeviceinfo_service.getInstance().getFreeCount() < reqDevDoCount) {
			log.error("广告分配设备不足,空闲设备数：" + actdeviceinfo_service.getInstance().getFreeCount());
			return false;
		}
//		actdeviceinfo_service.getInstance().allocatDeviceForSigleAdv( this.advinfo.getAdv_id(), this.reqDevDoCount, alloc_type);
		return true;
		//
		// return alloc_init;
	}
	/**
	 * 2019-1-31 这里的计算有问题，之前没有考虑留存时长和打开次数是变化的，比如在某个范围变动，而且随着留存天数增大会缩小时长和打开次数。
	 * 以后改成预计设备数量，不再分配和释放，由人手指定
	 */
	public void reAllocDev(boolean inited) {
		if (isOffline()) {
			int alloc_type = ServerConfig.adv_alloc_type_remain;
//			if (this.lock_dev == 1)
//				alloc_type = serverconfig.adv_alloc_type_lockremain;
			int adv_id = this.advinfo.getAdv_id();
			int all_alloced_count = actdeviceinfo_service.getInstance().getAllocedDevCount(adv_id, alloc_type);
			if (all_alloced_count > 0) {
				freeDevWhileOffline();
			}
			return;
		}
		int adv_id =  this.advinfo.getAdv_id();
		int remaintime = this.advinfo.getRemaintime();
		if (remaintime <= 0) {
			log.error("reAllocDev　advid=" + adv_id + "此任务不需要做留存！");
			return;
		}
		if (inited) {
			String info = "留存广告任务(" + adv_id + ")今天余下执行总数:" + getAllTodocount();// +
																						// this.do_count_af7days[0]
																						// +
																						// ",余下执行数:"
			// + this.todocount;
			if (this.getResult() != null)
				info += ";今天已经执行数:" + (this.getResult().getRemain_newuser_success_opentcount()
						+ this.getResult().getRemain_olduser_success_opentcount());

			int alloc_type = ServerConfig.adv_alloc_type_remain;
//			if (this.lock_dev == 1)
//				alloc_type = serverconfig.adv_alloc_type_lockremain;
			int all_alloced_count = actdeviceinfo_service.getInstance().getAllocedDevCount(adv_id, alloc_type);
			info += ";已经分配的设备总数:" + all_alloced_count;
			log.info(info);
			return;
		}
		if (advgroup_service.getInstance().getGroupIncludeAdv(adv_id, true) != null)
			return;
		advtaskinfo ai = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo();
		int max_dotime_hour = this.max_docountOfHours * remaintime;
		this.reqDevDoCount = max_dotime_hour / 3600 + 1;
		if (ai.getHandle_Locked() == 1)
			return;
//		if (lock_dev == 1) {
//			// 同一台机上做新增留存时，在advtaskruntimeinfo中计算需要分配的设备数量．
//			return;
//		}
		int freecount = actdeviceinfo_service.getInstance().getFreeCount();
		// 需求和实质分配相差
		int diff = 0;
		int alloc_type = ServerConfig.adv_alloc_type_remain;
		// this.caclRuntimeHouursCount();
		int all_alloced_count = actdeviceinfo_service.getInstance().getAllocedDevCount(adv_id, alloc_type);
		int act_alloc_count = actdeviceinfo_service.getInstance().getAllocedDevCountAct(adv_id, alloc_type);

		// if(all_alloced_count == this.reqDevDoCount)
		// {
		// return;
		// }
		// 如果所有分配的设备数不足，有足够的设备空闲就立即分配
		if (all_alloced_count < this.reqDevDoCount/* && this.act_alloc_timeout == null*/) {
			// 可能广告每天任务改变，需要再分配多些机器，或者由于服务器关闭或断网之类，后面堆积的任务太多
			log.error("广告留存" + adv_id + "realldev 总共分配设备数量:" + all_alloced_count);
			//freecount = actdeviceinfo_service.getInstance().getFreeCount();
			diff = this.reqDevDoCount - all_alloced_count;
			if (actdeviceinfo_service.getInstance().getFreeCount() >= diff) {
				log.error("需要添加分配:" + diff);
//				actdeviceinfo_service.getInstance().allocatDeviceForSigleAdv(adv_id, diff, alloc_type);
			} else if (freecount > 0) {
				// 2018-12-6,留存任务是紧急任务，有多少就做多少，之前是没有足够空闲就不分配去做
//				actdeviceinfo_service.getInstance().allocatDeviceForSigleAdv(adv_id, freecount, alloc_type);
				// log.error("广告" + adv_id + "realldev 没有足够设备分配，diff=" + diff +
				// ";freecount=" + freecount);
			} else {
				log.error("广告" + adv_id + "realldev 没有足够设备分配，diff=" + diff + ";freecount=" + freecount);
			}
			return;
		}

		if (main_service.getInstance().startTimeout() < ServerConfig.act_realloc_wait)
			return;
		// 活跃的分配设备不足时等超时
		if (reqDevDoCount > act_alloc_count) {			
			diff = reqDevDoCount - act_alloc_count;			
			log.error("等广告留存id=" + adv_id + "需求设备数量:" + reqDevDoCount + ",当前活跃:" + act_alloc_count + "等待超时分配");
			if (this.act_alloc_timeout == null) {
				act_alloc_timeout = new Date();
				return;
			}
			Date now = new Date();
			long timeout = (now.getTime() - act_alloc_timeout.getTime()) / 1000;
			log.warn("广告留存:" + adv_id + "活跃设备不足，等待超时,现在超时时间:" + timeout);

			if (timeout > ServerConfig.act_realloc_timeout) {
				//2018-12-6
				freecount = actdeviceinfo_service.getInstance().getFreeCount();
				if(freecount >= diff){
//					actdeviceinfo_service.getInstance().allocatDeviceForSigleAdv(adv_id, diff, alloc_type);
					act_alloc_timeout = null;
					return;
				}
				else if(freecount > 0)
//					actdeviceinfo_service.getInstance().allocatDeviceForSigleAdv(adv_id, freecount, alloc_type);
				if(freecount == 0)
					log.error("留存超时时间已过，但空闲设备不足,请减少任务数或增加设备");
				return;				
			}
		}
		//end 2018-12-6
		// 预计后面几天的机器设备数量下面计算太静态,只有每天新增不变才可行 ,如果新增停了,或减少了,有可能要释放设备的
		// 这里有问题
		// int reqDev_after7 = caclAf7daysReqDevs(ai);
		// log.warn("广告留存[:" + adv_id + "]7天内预计最大机器需求数:" + reqDev_after7);
		// if (reqDev_after7 > act_alloc_count + freecount) {
		// log.warn("广告留存:" + adv_id + "7天内当前分配设备活跃数可能不足:" + act_alloc_count);
		// }
		// if (act_alloc_count > reqDev_after7) {
		// // 释放多出的设备
		// diff = act_alloc_count - reqDev_after7;
		// log.error("广告留存" + adv_id + "realldev 空闲设备大于实质需求,释放:" + diff +
		// "台设备");
		// actdeviceinfo_service.getInstance().freeDeviceForSingleAdv(adv_id,
		// diff, alloc_type);
		// }
	}

	/**
	 * 计算7天内最大需求设备数 这个以后再优化吧。
	 * 
	 * @param ai
	 */
	// int caclAf7daysReqDevs(advtaskinfo ai) {
	// int[] new_after_7day = new int[do_count_af7days.length];
	// for (int i = 0; i < do_count_af7days.length; i++)
	// new_after_7day[i] = do_count_af7days[i];
	//
	// if (ai.getDaycount() > 0 && ai.getOnlineflag() == 1) {
	// // 留存没有停,后面还要加上7天的新增产生的留存
	// int[] days = new int[serverconfig.max_remain_days];
	// String remrule = this.rem_rule;
	// caclMothRemainByRule(remrule, days);
	// for (int i = 0; i < days.length; i++) {
	// days[i] = days[i] * ai.getDaycount() / 100;
	// }
	// // i=0是当天要做留存数据
	// for (int i = 1; i < new_after_7day.length; i++) {
	// for (int j = 0; j < i; j++)
	// new_after_7day[i] += days[j];
	// }
	// } else {
	// // 新增停了,后面就是只有do_count_af7days的剩余留存了
	// }
	// int max_after_7day = 0;
	// for (int i = 0; i < new_after_7day.length; i++)
	// if (max_after_7day < new_after_7day[i])
	// max_after_7day = new_after_7day[i];
	// int max_h =
	// advtaskruntimeinfo.caclMaxHoureCountOfAllDateByTimeline(max_after_7day,
	// ai.getRem_timeline());
	// int reqDev_after7 = (max_h * remaintime) / 3600 + 1;
	// return reqDev_after7;
	// }

	/**
	 * 每天新增100个时根据留存规则算出二个月每天的留存数量．
	 * 有些渗量包只要求做周留存,这里扩展一下，除了第一个外，如果遇到-1,表示后面的不做了，比如40;20;-1,-1;-1表示做完周留存就不做了．
	 * 这里用简单的中间分值法，以后采用更好的算法，比如正态分布这种
	 * @param remRule
	 * @param month_days 
	 * @return month_days数组元素之和
	 */
	public static int caclMothRemainByRule(String remRule, int[] month_days) {
		// System.out.println(remRule);
		if (advremaintask_service.judgeRemRule(remRule) == false) {
			ErrorLog_service.system_errlog("caclMothRemainByRule remRul=" + remRule);
			log.error("caclMothRemainByRule remRul=" + remRule);
			return 0;
		}
		//防止不小心用了逗号
		remRule = remRule.replaceAll(",", ";");
		if (month_days == null) {
			month_days = new int[ServerConfig.max_remain_days];
		}
		// 2018-11-30
		// 这里扩展一下，如果第一个字符为$，后面让运营直接填定每天的留存数,这样不需要计算，还可以人工调整得更切实际
		if (remRule.charAt(0) == '$') {
			String ss = remRule.substring(1);
			String[] s = ss.split(";");
			for (int i = 0; i < month_days.length && i < s.length; i++) {
				int v = Integer.parseInt(s[i]);
				if(v <= 0) {
					break;
				}
				month_days[i] = v;
			}
		} else {
			String[] s = remRule.split(";");
			int dayRem, weekRem, weekRem2, monthRem, monthRem2;
			dayRem = Integer.parseInt(s[0]);
			weekRem = Integer.parseInt(s[1]);
			weekRem2 = Integer.parseInt(s[2]);
			monthRem = Integer.parseInt(s[3]);
			monthRem2 = Integer.parseInt(s[4]);

			// int[] month_days = new int[60];
			month_days[0] = dayRem;
			month_days[7] = weekRem;
			month_days[14] = weekRem2;
			month_days[29] = monthRem;
			month_days[59] = monthRem2;
			// float step = ((float)(dayRem - weekRem))/7;
			// for(int i = 1; i < 7; i++)
			// {
			// month_days[i] = dayRem -(int)(i*step);
			// }
			middleValues(month_days, 0, 7);
			middleValues(month_days, 7, 14);
			middleValues(month_days, 14, 29);
			middleValues(month_days, 29, 59);
		}
		int sum = 0;
		for (int i = 0; i < month_days.length; i++) {
			if(month_days[i] <= 0)
				break;
			sum += month_days[i];
		}
		return sum;
		// return debugInfo(month_days);

		// step = ((float)(weekRem - weekRem2))/7;
		// for(int i = 8; i < 14; i++)
		// {
		// month_days[i] = weekRem - (int)((i-7)*step);
		// }

		//
		// step = (float)((weekRem2 - monthRem))/15;
		// for(int i = 15; i < 29; i++)
		// {
		// month_days[i] = weekRem2 - (int)((i-14)*step);
		// }

	}

	static int debugInfo(int[] month_days) {
		for (int i = 0; i < month_days.length; i++) {
			if (i < 10)
				System.out.print("" + i + "   ");
			else
				System.out.print("" + i + "  ");
		}
		System.out.println();
		int sum = 0;
		for (int i = 0; i < month_days.length; i++) {
			System.out.print(month_days[i] + "  ");
			sum += month_days[i];
		}
		System.out.println("sum=" + sum);
		return sum;
	}

	static void middleValues(int[] values, int beginIndex, int endIndex) {
		int beginValue = values[beginIndex];
		int endValue = values[endIndex];
		if (endValue == -1 || endValue > beginValue) {
			values[endIndex] = 0;
			return;
		}
		int len = endIndex - beginIndex;
		float step = ((float) (beginValue - endValue)) / len;
		for (int i = beginIndex + 1; i < endIndex; i++)
			values[i] = beginValue - (int) ((i - beginIndex) * step);
	}

	/**
	 * 提取任务
	 * 
	 * @return
	 * @throws SQLException
	 */
	public TaskBase[] fetchTask(String dev_tag) {
		if (this.isOffline()) {
			return new TaskBase[0];
		}
		Calendar date = Calendar.getInstance();
		int curhour = date.get(Calendar.HOUR_OF_DAY);
		advremaintask at = null;
		int timelineid = this.advinfo.getRem_timeline();
		if (timelineid > -1) {
			// 如果是要求留存曲线，就按小时分布
			if (this.retcount_hours[curhour] < 0) {
				log.warn("fetchTask 当前小时分配数量为:" + docount_hours[curhour]);
				return new TaskBase[0];
			}
		}
		if (this.getAllTodocount() < 1) {
			log.warn("fetchTask　任务已经完成了，没有可分配");
			return new TaskBase[0];
		}
//		if (this.isLock_dev() == 1)
//			at = advremaintaskdao.getTodayRemainLockedTask(adv_id, dev_tag);
//		else
		//2018-12-29，对于活跃数大于1的留存，只要做过一次，也算是有留存，所以这里优先取今天没有做过的
		int adv_id = this.advinfo.getAdv_id();
		at = advremaintaskdao.getTodayRemainTaskNoFetch(adv_id);
		if(at ==null) {
			//然后取其它的
			at = advremaintaskdao.getTodayRemainTask(adv_id);
		}
		if (at == null) {
			return new TaskBase[0];
		}
		TaskCPARemain adtask = new TaskCPARemain();
		adtask.setTaskInfo(this.advinfo);
		adtask.setRemTask(at);
		this.retcount_hours[curhour] -= 1;
		// 这里很可能有问题，新规则活跃数在初始化时设置，现在不在这里设置
		// if(at.getDotoday() > 1)
		// {
		// //如果每天打开活跃数大于1，而且今天第一次取，随机今天活跃数目
		// Date lastFetchDate = at.getLastfetchday();
		// Date toDay = new Date();
		// log.info("fetchTask lastFetchDate:" +
		// DateUtil.getDateString(lastFetchDate));
		// if(DateUtil.isSameDate(lastFetchDate, toDay) == false)
		// {
		// int c = at.getDotoday();
		// java.util.Random rand = new java.util.Random();
		// int r = rand.nextInt(c+1);
		// at.setDotoday(r + 1);
		// log.info("fetchTask " + adv_id + "今天第一次取 old dotoday=" + c + ",rand
		// dotoday=" + at.getDotoday());
		// }
		// }
		advremaintaskdao.fetchTodayRemainTask(at);
		return new TaskBase[] {adtask};
	}

	/**
	 * 返回当前小时还有多少量要做
	 * 
	 * @return
	 */
	public int getCurRetCount() {
		Calendar date = Calendar.getInstance();
		int curhour = date.get(Calendar.HOUR_OF_DAY);
		return retcount_hours[curhour];
	}

	/**
	 * 检测当前小时能否按时完成任务
	 */
	public boolean checkCurHourFinishStatus() {
		boolean canFinish = true;
		if (getAllTodocount() <= 0) {
			return true;
		}
		if (this.advinfo.getRem_timeline() < 0)
			return false;
		int retCount = getCurRetCount();
		if (retCount > 0) {
			// 不考虑分组？
			if (advgroup_service.getInstance().getGroupIncludeAdv(this.advinfo.getAdv_id(), true) != null)
				return true;
			// if(this.timelineid < 0)
			// return false;
			int alloc_type = ServerConfig.adv_alloc_type_remain;
//			if (this.lock_dev == 1)
//				alloc_type = serverconfig.adv_alloc_type_lockremain;
			int act_alloced_count = actdeviceinfo_service.getInstance().getAllocedDevCountAct(this.advinfo.getAdv_id(), alloc_type);
			int curMin = Calendar.getInstance().get(Calendar.MINUTE);
			int curSec = Calendar.getInstance().get(Calendar.SECOND);
			int sectime = (60 - curMin - 1) * 60 + 60 - curSec;
			if (act_alloced_count * sectime < this.advinfo.getRemaintime() * retCount)
				return false;
		}
		return canFinish;
	}

	public String getTaskRuntimeInfo() {
		int curhour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		String sruntime = "";
		int adv_id = this.advinfo.getAdv_id();
		String adv_name = "";
		if (StringUtil.isStringEmpty(adv_name)) {
			advtaskinfo ai = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo();
			adv_name = ai.getName();
		}
		
		sruntime += "留存任务:" + adv_name + "[" + adv_id + "]]\r\n";
		sruntime += "状态：";
		if (this.getAllTodocount() < 1)
			sruntime += "***完成***\r\n";
		else {
			sruntime += "未完成(" + getAllTodocount() + ")\r\n";
		}
		// sruntime += "余下活跃数:" + this.getAllTodocount() + "\r\n";

		sruntime += "旧留存用户数:" + this.old_user_count + "\r\n";
		sruntime += "新活跃用户数:" + this.today_user_count + "\r\n";

		if (this.result != null) {
			sruntime += "今天成功活跃数:" + result.getAllSuccessOpenCount() + "\r\n";
			sruntime += "今天失败活跃数:" + result.getAllErrOpenCount() + "\r\n";
		} else {
			sruntime += "今天成功执行数:" + 0 + "\r\n";
			sruntime += "今天失败执行数:" + 0 + "\r\n";
		}
		sruntime += "当前小时分配任务数:" + this.docount_hours[curhour] + "\r\n";
		sruntime += "当前小时剩余任务数:" + this.retcount_hours[curhour] + "\r\n";

		sruntime += "需求设备数:" + this.reqDevDoCount + "\r\n";
		int alloc_type = ServerConfig.adv_alloc_type_remain;
//		if (this.lock_dev == 1)
//			alloc_type = serverconfig.adv_alloc_type_lockremain;
		int all_alloced_count = actdeviceinfo_service.getInstance().getAllocedDevCount(adv_id, alloc_type);
		sruntime += "已经分配的总设备数:" + all_alloced_count + "\r\n";
		int act_alloced_count = actdeviceinfo_service.getInstance().getAllocedDevCountAct(adv_id, alloc_type);
		sruntime += "已经分配的活跃设备数:" + act_alloced_count + "\r\n";
		log.info(sruntime);
		return sruntime;
	}

	public static void main(String[] argv) {
		String s = "$40;30;20;10;5;0";
		int[] c = new int[60];
		caclMothRemainByRule(s, c);
		for (int cc : c) {
			System.out.println(cc);
		}
	}

	@Override
	public boolean isFinish() {
		// TODO Auto-generated method stub
		if(this.getAllTodocount() < 1)
			return true;
		return false;
	}

	@Override
	public TaskBase fetchTaskStrong() {
		// TODO Auto-generated method stub
		return null;
	}
}
