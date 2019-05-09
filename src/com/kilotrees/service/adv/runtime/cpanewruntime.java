package com.kilotrees.service.adv.runtime;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kilotrees.dao.advtaskinfodao;
import com.kilotrees.model.bo.TaskBase;
import com.kilotrees.model.bo.TaskCPANewly;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.advgroup;
import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.services.actdeviceinfo_service;
import com.kilotrees.services.advgroup_service;
import com.kilotrees.services.advremaintask_service;
import com.kilotrees.services.main_service;
import com.kilotrees.services.timeline_service;
import com.kilotrees.util.DateUtil;
import com.kilotrees.util.StringUtil;

/**
 * cpa新增广告实时状态信息 为新任务分配机器 定时重新计算分配设备
 * 
 * @author Administrator
 *
 */
public class cpanewruntime extends advruntimebase{
	private static Logger log = Logger.getLogger(cpanewruntime.class);
//	advtaskinfo advinfo;
//	advtodayresult result;
//	// 需要多少台手机做新增
//	int reqDevDoCount;
//	// 预先分配的机器
//	// String[] devTags;
//	// 一天每小时要做的数量,这个数量是随广告每天要做的数量(用户后台修改)advinfo.getDateCount()变化而不断变化
//	int[] docount_hours;
//	// 每小时剩余的数量，初始值和docount_hours一样，但实时计算提取任务，每提取一个就少一个．
//	int[] retcount_hours;
//	// docount_hours 24小时中最大量的
//	int max_docountOfHours;
//	//是否下线
//	boolean isOffline;
	// 分配超时
	Date act_alloc_timeout;
	// 不知为何当时加这个
	// int todaycount;
	// 是否冲量包，不做留存，不做曲线(不做留存任务，只做新增)
	//boolean isRush;	
	//是否需要重新计算docount_hours,如果系统启动或者任务的每天新增数量，任务打开时间，留存时间，时间曲线有变化，则需要重新计算．
	//boolean need_reCaclRuntime;
	//2018-12-3 有些任务只做很小个数，比如充值，但有时机器上来取的比较多，之前是成功后才算完成一个任务，这样会有可能某些机器正在做，但没有做完
	//另外有别的机器上来取，就会出现做多任务情况。现在增加一个字段，记录当前已经分配给机器，但没有完成的任务，当然，这个数值在服务器重启时有问题
	//或者客户端设备卡死时也有问题,这里最好用一个HashMap或vector来记录某台设备的id和取任务时间，并定时检查是否超时，超时就要删除
	//int doingCount;
	//2018-12-9，太容易卡死了，还是用容器来记录超时  <设备，超时时间>
	//HashMap<String, Long> doingDevs = new HashMap<String, Long>();
	

	boolean rush;
	public boolean isNeed_reCaclRuntime() {
		return need_reCaclRuntime;
	}

	public void setNeed_reCaclRuntime(boolean need_reCaclRuntime) {
		this.need_reCaclRuntime = need_reCaclRuntime;
	}

//	public advtaskinfo getAdvinfo() {
//		return advinfo;
//	}

	/**设置advruntimebase对象绑定的advtaskinfo对象,同时如果和之
	 * 前绑定的advtaskinfo对象不同，把need_reCaclRuntime设为true
	 * @param _advinfo 要拿来设置的advinfo对象
	 */
	public void setAdvinfo(advtaskinfo _advinfo) {
		if(this.advinfo == null)
		{
//			this.advinfo = _advinfo;
			super.setAdvinfo(_advinfo);
			//系统第一次启动
			need_reCaclRuntime = true;
		}
		else{			
			if(advinfo.getDayusercount() != _advinfo.getDayusercount())
			{
				log.warn("重置need_recaclRuntime,getDayusercount改变了：" + _advinfo.getDayusercount());
				need_reCaclRuntime = true;
			}
			if(advinfo.getRequesttime() != _advinfo.getRequesttime()){
				log.warn("重置need_recaclRuntime,getRequesttime改变了：" + _advinfo.getRequesttime());
				need_reCaclRuntime = true;
			}
			if(advinfo.getTimeline() != _advinfo.getTimeline()){
				log.warn("重置need_recaclRuntime,getTimeline改变了：" + _advinfo.getTimeline());
				need_reCaclRuntime = true;
			}
			if(advinfo.getRemaintime() != _advinfo.getRemaintime()){
				log.warn("重置need_recaclRuntime,getTimeline改变了：" + _advinfo.getRemaintime());
				need_reCaclRuntime = true;
			}
			this.advinfo.clone(_advinfo);
		}
		//2018-12-7
		//this.advinfo = _advinfo;
	}
	
	

	/**
	 * @return 返回advtodayresult对象，包含今日广告的执行信息
	 */
//	public advtodayresult getResult() {
//		return result;
//	}
//
//	public void setResult(advtodayresult result) {
//		this.result = result;
//	}

	
	// public String[] getDevTags() {
	// return devTags;
	// }
	//
	// public void setDevTags(String[] devTags) {
	// this.devTags = devTags;
	// }

	public int[] getDocount_hours() {
		return docount_hours;
	}

	// public void setDocount_hours(int[] do_hours) {
	// // this.docount_hours = do_hours;
	// int curhour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
	// // 第一次设置
	// if (retcount_hours == null)
	// this.retcount_hours = new int[docount_hours.length];
	// // 当前小时之前的数据不重置，否则之前的数据可能为0
	// for (int i = curhour; i < do_hours.length; i++){
	// docount_hours[i] = do_hours[i];
	// retcount_hours[i] = docount_hours[i];
	// }
	//
	// for (int i = 0; i < docount_hours.length; i++) {
	// if (docount_hours[i] > this.max_docountOfHours)
	// this.max_docountOfHours = docount_hours[i];
	// }
	// }

	
	
//	public boolean isOffline() {
//		return isOffline;
//	}

//	public void setOffline(boolean isOffline) {
		//
//		super.setOffline(isOffline);
		//下面这个不知当初为何加上去，actdeviceinfo_service.checkAdvOffline()好象已经实现了这个功能，先留着
		//2019-1-16
		//if (isOffline)
		//	this.freeDevWhileOffline();
//	}	

//	public boolean isRush() {
//		return isRush;
//	}
//
//	public void setRush(boolean isRush) {
//		this.isRush = isRush;
//	}

	

//	public synchronized void setDoingCount(int doingCount) {
//		this.doingCount = doingCount;
//	}
	
	// public int getTodaycount() {
	// return todaycount;
	// }
	//
	// public void setTodaycount(int todaycount) {
	// this.todaycount = todaycount;
	// }
	/**
	 * 实时计算当前小时之后每小时的任务量,如果任务量不变，每次计算
	 * 增加一个变量need_reCaclRuntime来处理，只有任务变化或者第一次启动时才计算
	 * 2018/6/8加上时间限制，如果一个任务规定只能在哪些时间段做(比如0-6点不能做,这时不需考虑时间曲线)
	 */
	public void caclRuntimeHouursCount() {
		if(this.offline){
			log.error("caclRuntimeHouursCount 任务已经下线");
			return;
		}
		//调用父类advruntimebase实现refresh()刷新作用
		super.caclRuntimeHouursCount();
		if(this.isFinish()) {
			return;
		}
		if(this.getAdvinfo().getAdv_type() >= 30)
		{//如果是例行任务
			//docount_hours = new int[24];
			//retcount_hours = new int[24];
			return;
		}
		
		int count = advinfo.getDayusercount();
		int timelineid = advinfo.getTimeline();
		//这里有点问题，如果不按时间曲线的话，是要重新计算的
		if(need_reCaclRuntime == false && timelineid > -1)
		{
			log.warn("caclRuntimeHouursCount 上次已经计算过，任务没有变化，直接返回");
			return;
		}		
        /**
         * 这里用remaintime可能不太合适，之前一个任务，很想放大量快点完成，但由于要做小小留存，所以分配的机器数很少,
         */
		//2019-1-28 
		if (timelineid == -1/* && this.advinfo.getRemaintime() <= 0*/) {
			rush = true;
		}

		if (advinfo.getOnline_time() != null) {
			// 如果是今天才开始分配任务,则按当前时间计算今天执行数,也就是说如果12点上线任务，数量为100，那么我们后面只做50个
			Date today = new Date();
			Date onlineDate = getAdvinfo().getOnline_time();
			if (DateUtil.isSameDate(today, onlineDate)) {
				// 今天上线的任务，按当前时间截取要做的数量．
				Calendar date = Calendar.getInstance();
				/*2018-12-20   放弃用数据库查出来的onlineDate
				 *            改用下面拿的today，实时计算数量
				 * date.setTime(onlineDate);
				 * */
				date.setTime(today);
				int onlinehour = date.get(Calendar.HOUR_OF_DAY);
				// 直接按比例,不再按曲线走了
				count = count * (24 - onlinehour - 1) / 24;
			}
		}
		if (getResult() != null) {
			count -= getResult().getNewuser_success_count();
		}
		int h[] = new int[24];
		JSONObject jsoAdvExt = advinfo.getExtJso();
		//之前为了实现0点到6点不做任何任务，加了时间限制曲线，但实质上，我们的时间曲线可以设定任意小时为0个，也是同样的目的
		if(jsoAdvExt != null && StringUtil.isStringEmpty(jsoAdvExt.optString("limite_hours")) == false)
		{
			//任务有时间段限制,按当前时间算起每小时需要做的任务量	
			String strTmp = jsoAdvExt.optString("limite_hours");
			//防止不小心用了逗号
			strTmp = strTmp.replaceAll(",", ";");
			String[] limitStr = strTmp.split(";");
			h = caclHourCountByLimitHours(advinfo.getAdv_id(),limitStr,false,count);
		}
		else
			h = timeline_service.getInstance().caclDistributeOfHours(timelineid, false, count);

		if (this.docount_hours == null)
			this.docount_hours = new int[h.length];
		if (this.retcount_hours == null)
			retcount_hours = new int[h.length];
		this.max_docountOfHours = advruntimebase.setDocount_hours(h, this.docount_hours, this.retcount_hours);
		//2018-12-8在这里计算reqDevDoCount，不然重启时，每一次总是为0
		int max_hour_count = max_docountOfHours;
		int maxdotime = this.advinfo.getRequesttime() * max_hour_count;
		this.reqDevDoCount = maxdotime / 3600 + 1;
		need_reCaclRuntime = false;
	}
	
	

	public int caclMaxHoureCountOfAllDate() {
		if(offline){
			log.error("caclMaxHoureCountOfAllDate 任务已经下线");
			return 0;
		}
		int count = advinfo.getDayusercount();
		int timelineid = advinfo.getTimeline();
		int maxCount = 0;
		int h[] = new int[24];
		JSONObject jsoAdvExt = advinfo.getExtJso();
		if(jsoAdvExt != null && StringUtil.isStringEmpty(jsoAdvExt.optString("limite_hours")) == false)
		{
			//任务有时间段限制,按当前时间算起每小时需要做的任务量	
			String strTmp = jsoAdvExt.optString("limite_hours");
			strTmp = strTmp.replaceAll(",", ";");
			String[] limitStr = strTmp.split(";");
			//String[] limitStr = jsoAdvExt.optString("limite_hours").split(";");
			h = caclHourCountByLimitHours(advinfo.getAdv_id(),limitStr,true,count);
		}
		else {
		  h = timeline_service.getInstance().caclDistributeOfHours(timelineid, true, count);
		}
		for (int i = 0; i < h.length; i++)
			if (h[i] > maxCount)
				maxCount = h[i];
		return maxCount;
	}

	
    /**
     * 这里好象没有多大作用,actdeviceinfo_service定期做此工作,不过也不影响系统正常工作,先保留
     */
	void freeDevWhileOffline() {
		if (this.advinfo.getRemain_lock_dev() > 0) {
			if (this.advinfo.getOnlineflag() != 2)
				return;
		}
		if(this.advinfo.getHandle_Locked() > 0)
			return;
		int adv_id = this.getAdvinfo().getAdv_id();
		//log.info("freeDevWhileOffline 任务下线，释放广告" + adv_id + "所占用设备");
		actdeviceinfo_service.getInstance().freeDeviceForSingleAdv(adv_id, -1, ServerConfig.adv_alloc_type_single);
		advgroup g = advgroup_service.getInstance().getGroupIncludeAdv(adv_id, false);
		if(g != null)
			g.setAdvOffline(adv_id);
	}
	/**
	 * 第一次分配时以全天计算
	 * @return
	 */
	public boolean allocNewReady() {
		if(this.getAdvinfo().getAdv_type() >= 30)
		{
			return false;
		}
		int adv_id = this.getAdvinfo().getAdv_id();
		if (advgroup_service.getInstance().getGroupIncludeAdv(adv_id, false) != null) {
			log.error("allocDevForNew　此广告属于组，应该在组分配,adv_id=" + adv_id);
			return false;
		}
		if (this.getResult() != null) {
			log.error("allocDevForNew　新分配广告设备，不应该有getResult!=null");
			return false;
		}
		int alloc_type = ServerConfig.adv_alloc_type_single;
		if (this.getAdvinfo().getRemain_lock_dev() > 0 && this.getAdvinfo().getRemaintime() > 0)
			alloc_type = ServerConfig.adv_alloc_type_lockremain;

		actdeviceinfo_service.getInstance().freeDeviceForSingleAdv(adv_id, -1, alloc_type);
		int maxCount_ofH = this.caclMaxHoureCountOfAllDate();
		if (alloc_type == ServerConfig.adv_alloc_type_single) {
			int maxdotime = this.getAdvinfo().getRequesttime() * maxCount_ofH;
			this.reqDevDoCount = maxdotime / 3600 + 1;
		} else {
			// 留存和新增锁定同一台机，这种情况比较复杂，而且真的锁定的话，机器用开之后，free就比较麻烦了
			// 计算一周后的留存数量
			String remrule = this.getAdvinfo().getRemain_rule();
			String[] s = remrule.split(";");
			remrule = s[0] + ";" + s[1] + ";-1;-1;-1";
			int sum = cparemainruntime.caclMothRemainByRule(remrule, null);

			sum = sum * this.advinfo.getDayusercount() / 100;
			int max_remain_ofhours = caclMaxHoureCountOfAllDateByTimeline(sum, advinfo.getRem_timeline());
			//不能用max_docountOfHours，这时还没有分配，没有执行caclRuntimeHouursCount
			//int maxdotime = this.getAdvinfo().getRequesttime() * this.max_docountOfHours;
			int maxdotime = this.getAdvinfo().getRequesttime() * maxCount_ofH;
			maxdotime += this.getAdvinfo().getRemaintime() * max_remain_ofhours;
			this.reqDevDoCount = maxdotime / 3600 + 1;
			if (reqDevDoCount > max_docountOfHours || reqDevDoCount > max_remain_ofhours) {
				log.error("广告" + adv_id + "每小时分配的设备台数大于需求数，不能按小时分配");
				// this.reqDevDoCount = 0;
				return false;
			}
		}
		return true;
	}
	/**
	 * 只有当作务onlineflag=3时调用
	 * @return
	 */
	public boolean allocDevForNew() {
		// if(alloc_init)
		if(this.getAdvinfo().getAdv_type() >= 30)
		{
			log.warn("例行性广告，allocDevForNew　不处理,advid=" + this.advinfo.getAdv_id());
			return false;
		}
		if(this.getAdvinfo().getHandle_Locked() > 0)
			return false;
		if (advgroup_service.getInstance().getGroupIncludeAdv(this.getAdvinfo().getAdv_id(), false) != null) {
			return false;
		}
		allocNewReady();
		
		int freeCount = actdeviceinfo_service.getInstance().getFreeCount();
		if (freeCount == 0) {
			log.error("allocDevForNew　没有空闲设备，分配失败,advid=" + this.advinfo.getAdv_id());
			return false;
		}
		int alloc_type = ServerConfig.adv_alloc_type_single;
		if (this.getAdvinfo().getRemain_lock_dev() > 0 && this.getAdvinfo().getRemaintime() > 0)
			alloc_type = ServerConfig.adv_alloc_type_lockremain;

		int adv_id = this.getAdvinfo().getAdv_id();
		log.info("广告" + this.getAdvinfo().getAdv_id() + "新分配需求设备数：" + reqDevDoCount);

		if (freeCount < reqDevDoCount) {			
			if (this.rush == false){
				log.error("广告分配设备不足,空闲设备数：" + actdeviceinfo_service.getInstance().getFreeCount());
				return false;
			}				
		}
		// 如果是冲量包或空闲设备足够，继续分配
		actdeviceinfo_service.getInstance().allocatDeviceForSigleAdv(adv_id, this.reqDevDoCount, alloc_type);
		advinfo.setOnlineflag(1);
		advinfo.setOnline_time(new Date());
		advtaskinfodao.updateOnlineFlag(advinfo);
		return true;
		//
		// return alloc_init;
	}

	/*
	 * 由于广告的参数(每天新增，使用时长)都可以手工调整，需要实时计算当前广告的设备需求，如果不足，超时后再次分配 如果已经分配的设备数大于需求数，释放多余设备
	 */
	public void reAllocDev(boolean inited) {
		
		if(this.getAdvinfo().getAdv_type() >= 30)
		{
			return;
		}
		if(this.getAdvinfo().getHandle_Locked() > 0)
			return;
		if (advgroup_service.getInstance().getGroupIncludeAdv(this.getAdvinfo().getAdv_id(), false) != null) {
			return;
		}
		int ret_cont = advinfo.getDayusercount();
		if (this.getResult() != null)
			ret_cont -= this.getResult().getNewuser_success_count();
		if (inited) {			
			String info = "新增广告任务(" + this.advinfo.getAdv_id() + ")今天需要执行总数:" + advinfo.getDayusercount() + ",余下执行数:"
					+ ret_cont;
			if (this.getResult() != null)
				info += ";今天已经执行数:" + this.getResult().getNewuser_success_count();
			if (advgroup_service.getInstance().getGroupIncludeAdv(advinfo.getAdv_id(), false) == null) {
				int alloc_type = ServerConfig.adv_alloc_type_single;
				if (this.advinfo.getRemain_lock_dev() == 1)
					alloc_type = ServerConfig.adv_alloc_type_lockremain;
				int all_alloced_count = actdeviceinfo_service.getInstance().getAllocedDevCount(advinfo.getAdv_id(),
						alloc_type);
				info += ";已经分配的设备总数:" + all_alloced_count;
			}
			log.info(info);
			return;
		}
//		//当初为何要返回？
//		if(ret_cont == 0)
//		{
//			log.warn("reAllocDev :今天任务已经完成");
//			return;
//		}
		// static final Date beginTime = new Date();
		int diff = 0, freecount = 0;
		int adv_id = advinfo.getAdv_id();
		int alloc_type = ServerConfig.adv_alloc_type_single;
		//this.caclRuntimeHouursCount();
		int max_hour_count = max_docountOfHours;
		int maxdotime = this.advinfo.getRequesttime() * max_hour_count;
		this.reqDevDoCount = maxdotime / 3600 + 1;
		//adv_alloc_type_lockremain暂时不考虑了下面不用看
		{
//		if (this.advinfo.getRemain_lock_dev() == 1 && advinfo.getRemaintime() > 0) {
//			alloc_type = serverconfig.adv_alloc_type_lockremain;
//			// 加上留存,这里应该象之前一样重新计算？
//			advremainruntimeinfo remRuntimeInfo = advremaintask_service.getInstance().getRemainRuntimeInfo(adv_id);
////			if(remRuntimeInfo != null){
////				//第一天没有留存任务
////				int rem_max_hour_count = remRuntimeInfo.getMax_docountOfHours();
////				maxdotime += advinfo.getRemaintime() * rem_max_hour_count;	
////			}
//			String remrul = advinfo.getRemain_rule();
//			String[] s = remrul.split(";");
//			remrul = s[0] + ";" + s[1] + ";-1;-1;-1";
//			int sum = advremainruntimeinfo.caclMothRemainByRule(remrul,null);
//			sum = sum * this.advinfo.getDaycount() /100;		
//			int max_remain_ofhours = advtaskruntimeinfo.caclMaxHoureCountOfAllDateByTimeline(sum,advinfo.getRem_timeline());
//			maxdotime += advinfo.getRemaintime() * max_remain_ofhours;
//		}
		}
		int act_alloced_count = actdeviceinfo_service.getInstance().getAllocedDevCountAct(adv_id, alloc_type);
		int all_alloced_count = actdeviceinfo_service.getInstance().getAllocedDevCount(adv_id, alloc_type);
		
		log.info("广告" + adv_id + "realldev 需求设备数:" + this.reqDevDoCount);
		log.info("广告" + adv_id + "已经分配设备数:" + all_alloced_count + ";当前分配并活跃设备数:" + act_alloced_count);

		if (all_alloced_count < this.reqDevDoCount) {
			// 可能广告每天任务改变，需要再分配多些机器，或者由于服务器关闭或断网之类，后面堆积的任务太多
//			log.error("广告" + adv_id + "realldev 之前分配设备数量:" + all_alloced_count);
			freecount = actdeviceinfo_service.getInstance().getFreeCount();
			diff = this.reqDevDoCount - all_alloced_count;
			
			if (actdeviceinfo_service.getInstance().getFreeCount() >= diff) {
				log.error("需要添加分配:" + diff);
				
//				actdeviceinfo_service.getInstance().allocatDeviceForSigleAdv(adv_id, diff, alloc_type);
			} else if (freecount > 0 && this.advinfo.getOnlineflag() != 3) {
				// 2018-12-6,如果Onlineflag不等于3，有多少就做多少，之前是没有足够空闲就不分配去做
				
//				actdeviceinfo_service.getInstance().allocatDeviceForSigleAdv(adv_id, diff, alloc_type);
				// log.error("广告" + adv_id + "realldev 没有足够设备分配，diff=" + diff +
				// ";freecount=" + freecount);
			} else {
//				log.error("新增广告" + adv_id + " 没有足够设备分配，diff=" + diff + ";freecount=" + freecount);
			}
			return;
		}
		//当任务变小，之前分配的机器数太多，预留多一台空闲
		/*
		 * 
		 */
		int reservedDeviceCount = reqDevDoCount + 1;
		if (act_alloced_count >= reqDevDoCount && act_alloced_count < reservedDeviceCount)
			return;
		else if (act_alloced_count > reservedDeviceCount) {
			// 可能任务每天数减少，需要释放部分
			diff = act_alloced_count - this.reqDevDoCount - 1;
			if (alloc_type == ServerConfig.adv_alloc_type_single) {
				//alloc_type = serverconfig.adv_alloc_type_lockremain不释放
				log.error("广告" + adv_id + "realldev 空闲设备大于实质需求,释放:" + diff);
				
				//下面一行导致 程序主动释放多余设备：计算需要6台，手动分配10台，会释放4台， 因为要求全手动控制，故注释
//				actdeviceinfo_service.getInstance().freeDeviceForSingleAdv(adv_id, diff, alloc_type);
			}
			return;
		}
		//return;
		///2018-12-6改回来（下面是多余，2018-10-16？？？？）
//		diff = reqDevDoCount - act_alloced_count;
//		if(diff <= 0)
//		{
//			return;
//		}
		if (main_service.getInstance().startTimeout() < ServerConfig.act_realloc_wait)
			return;
		// 活跃的分配设备不足时等超时
		if (reqDevDoCount > act_alloced_count) {			
			diff = reqDevDoCount - act_alloced_count;			
			log.error("新增广告id=" + adv_id + "需求设备数量:" + reqDevDoCount + ",当前活跃:" + act_alloced_count + "等待超时分配");
			if (this.act_alloc_timeout == null) {
				act_alloc_timeout = new Date();
				return;
			}
			Date now = new Date();
			long timeout = (now.getTime() - act_alloc_timeout.getTime()) / 1000;
			log.warn("新增广告:" + adv_id + "活跃设备不足，等待超时,现在超时时间:" + timeout);

			if (timeout > ServerConfig.act_realloc_timeout) {
				//2018-12-6
				freecount = actdeviceinfo_service.getInstance().getFreeCount();
				if(freecount >= diff){
					actdeviceinfo_service.getInstance().allocatDeviceForSigleAdv(adv_id, diff, alloc_type);
					act_alloc_timeout = null;
					return;
				}
				else if(freecount > 0 && this.advinfo.getOnlineflag() != 3)
					actdeviceinfo_service.getInstance().allocatDeviceForSigleAdv(adv_id, freecount, alloc_type);
				if(freecount == 0)
					log.error("超时时间已过，但空闲设备不足,请减少任务数或增加设备");
				return;				
			}
		}
//		freecount = actdeviceinfo_service.getInstance().getFreeCount();		
//		if(freecount >= diff){
//			//之前不知为何发神经变成释放，造成总是反复分配和释放
//			actdeviceinfo_service.getInstance().allocatDeviceForSigleAdv(adv_id, diff, alloc_type);
//			return;
//		}				
//		// 计算超时
//		if (this.act_alloc_timeout == null) {
//			act_alloc_timeout = new Date();
//			return;
//		}
//		if (main_service.getInstance().startTimeout() < serverconfig.act_realloc_wait)
//			return;
//
//		Date now = new Date();
//		long timeout = (now.getTime() - act_alloc_timeout.getTime()) / 1000;
//		log.warn("广告:" + adv_id + "活跃设备不足，等待超时,现在超时时间:" + timeout);
//
//		if (timeout > serverconfig.act_realloc_timeout) {
//			if (actdeviceinfo_service.getInstance().getFreeCount() < diff) {
//				log.error("超时是间已过，但空闲设备不足,请减少任务数或增加设备");
//				return;
//			}
//			actdeviceinfo_service.getInstance().allocatDeviceForSigleAdv(adv_id, diff, alloc_type);
//			act_alloc_timeout = null;
//		}
	}

	/**
	 * 提取任务,对于例行性广告，每个设备只执行一次
	 * 
	 * @return
	 * @throws SQLException
	 */
	public TaskBase[] fetchTask(String dev_tag) {
		if(offline){
			int adv_id = 0;
			if(advinfo != null) {
				adv_id = advinfo.getAdv_id();
			}
			log.error("任务已经下线,advid=" + adv_id);
			return new TaskBase[0];
		}
		if(this.getAdvinfo().getAdv_type() >= 30)
		{
			// 例行性任务，每个设备只执行一次,不考虑每小时或每天的执行数
			TaskBase[] adtasks = new TaskBase[1];
			adtasks[0] = new TaskCPANewly();
			((TaskCPANewly) adtasks[0]).setTaskInfo(advinfo);
			this.incDoingCount(dev_tag);
			return adtasks;
		}
		int retc = advinfo.getDayusercount();   //每天要做的新增
		int donecount = 0;
		if (result != null){
			donecount = result.getNewuser_success_count();   //减去今天已经完成的
		}
		retc -= donecount;
		if (retc <= 0)
		{
			log.warn("fetchTask　任务已经完成了，没有可分配");
			return new TaskBase[0];
		}
		//2018-12-3
		retc -= this.getDoingCount();			  //减去正在做的
		if (retc <= 0)
		{
			log.warn("fetchTask　任务已经没有可分配了,已经完成:" + donecount + "正在执行：" + this.getDoingCount());
			return new TaskBase[0];
		}
		TaskBase[] adtasks = new TaskBase[1];//null;
		Calendar date = Calendar.getInstance();
		int curhour = date.get(Calendar.HOUR_OF_DAY);
		try {
			if (advinfo.getTimeline() > -1) {
				// 如果是要求留存曲线，就按小时分布
				if (this.retcount_hours[curhour] <= 0) {
					return new TaskBase[0];
				}
			}
			//2018-12-21 在分发留存任务时验证 remain_rule字段是否符合规则
			//remaintime = 0 或-1  就是不做留存的
			if(advinfo.getRemaintime() != 0 && advinfo.getRemaintime() != -1) {
				if(!advremaintask_service.judgeRemRule(advinfo.getRemain_rule())) {
					// 不符合留存规则
					throw new RuntimeException(advinfo.getAdv_id() + " 分发留存任务，remain_rule不符合留存规则，抛出异常......");
				}
			}
			adtasks[0] = new TaskCPANewly();
			
			log.info("cpanewruntime.fetchTask ::: start  ");
			if(advinfo.getAdv_id() == 3) {
				log.info("cpanewruntime.fetchTask ::: advinfo " + advinfo);
			}
			
			((TaskCPANewly)adtasks[0]).setTaskInfo(advinfo);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			return new TaskBase[0];
		}
		this.retcount_hours[curhour] -= 1;
		this.incDoingCount(dev_tag);
		return adtasks;
	}

	public int getAllTodocount()
	{
		return this.advinfo.getDayusercount();
	}
	
	/**
	 * 检测当前小时能否按时完成任务
	 */
	public boolean checkCurHourFinishStatus()
	{		
//		if(this.advinfo.getRemain_lock_dev() == 1)这里由上层判断，
//			return true;
		boolean canFinish = true;
		//检查是否冲量任务或例行性任务
		int retCount = getCurRetCount();
		
		if(retCount > 0)
		{
			if(this.rush)//冲量包，越快做完越好,让更多的设备空闲
				return false;
			int adv_id = this.getAdvinfo().getAdv_id();
			//不考虑分组?
			if (advgroup_service.getInstance().getGroupIncludeAdv(adv_id, false) != null) {
				return true;
			}
			int alloc_type = ServerConfig.adv_alloc_type_single;		
			if (this.advinfo.getRemain_lock_dev() == 1) {
				alloc_type = ServerConfig.adv_alloc_type_lockremain;			
			}
			int act_alloced_count = actdeviceinfo_service.getInstance().getAllocedDevCountAct(advinfo.getAdv_id(), alloc_type);
			int curMin = Calendar.getInstance().get(Calendar.MINUTE);
			int curSec = Calendar.getInstance().get(Calendar.SECOND);
			int sectime = (60-curMin - 1) * 60 + 60 - curSec;
			if(act_alloced_count * sectime < retCount * advinfo.getRequesttime())
				return false;			
		}
		return canFinish;
	}
	
	public String getTaskRuntimeInfo()
	{
		checkDoingTimeout();
		String sruntime = "";
		int curhour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int adv_id = 0;
		advgroup group = advgroup_service.getInstance().getGroupIncludeAdv(this.getAdvinfo().getAdv_id(), false);
		if(this.advinfo != null)
		{
			adv_id = this.advinfo.getAdv_id();
			if(group == null)
				sruntime  += "新增任务:" + advinfo.getName() + "[id:" + advinfo.getAdv_id() + "]]\r\n";
			else
				sruntime  += group.getName() + "组任务:"+ advinfo.getName() + "[id:" + advinfo.getAdv_id() + "]]\r\n";
			int ret_count = advinfo.getDayusercount();
			if(this.result != null)
			{
				ret_count -= result.getNewuser_success_count();
			}
			sruntime += "状态:";
			if(ret_count <= 0)
				sruntime += "***完成***\r\n";
			else 
				sruntime += "未完成(" + ret_count + ")\r\n";
				
			sruntime  += "每天执行用户总数:" + advinfo.getDayusercount() + "\r\n";
			sruntime += "正在执行的数:" + this.getDoingCount() + "\r\n";
			
			if(this.result != null)
			{
				sruntime  += "今天成功用户数:" + result.getNewuser_success_count() + "\r\n";
				sruntime  += "今天失败用户数:" + result.getNewuser_err_count() + "\r\n";
				sruntime  += "今天成功活跃数:" + result.getNewuser_success_opentcount() + "\r\n";
				sruntime  += "今天失败活跃:" + result.getNewuser_err_opentcount() + "\r\n";
			}
			else
			{
				sruntime += "今天成功执行数:" + 0 + "\r\n";
				//sruntime += "今天失败执行数:" + 0 + "\r\n";
			}
			
			if(this.advinfo.getAdv_type() < 30){
				//如果当前接近整点，比如58分钟，当重启时，因为剩下的分钟数不足以做1个任务，很容易看到下面的数据为0，看起来象有问题
				//最好列出后面小时个数.
				//2018-12-8
				int show_hours = 24-curhour;
				String s1 = "" + this.docount_hours[curhour],s2 = "" +  this.retcount_hours[curhour];
				s1 += "［";
				s2 += "［";
				for(int i = 1; i < show_hours; i++)
				{
					s1 += this.docount_hours[curhour + i] + ",";
					s2 += this.retcount_hours[curhour + i] + ",";
				}
				s1 += "］";
				s2 += "］";
				sruntime += "后面小时分配任务数:" + s1/*this.docount_hours[curhour]*/ + "\r\n";
				sruntime += "后面小时剩余任务数:" + s2/*this.retcount_hours[curhour]*/ + "\r\n";
			}
		}
		if (group == null) {
			//2018-12-8 加上this.reqDevDoCount，因 为第一次重启时并没有计算出需求(已经改过来）
			if(this.advinfo.getAdv_type() < 30 && this.reqDevDoCount > 0){
				sruntime  += "需求设备数:" + this.reqDevDoCount + "\r\n";
			}
			int alloc_type = ServerConfig.adv_alloc_type_single;		
			if (this.advinfo.getRemain_lock_dev() == 1) {
				alloc_type = ServerConfig.adv_alloc_type_lockremain;			
			}
			int act_alloced_count = actdeviceinfo_service.getInstance().getAllocedDevCountAct(adv_id, alloc_type);
			int all_allock_count = actdeviceinfo_service.getInstance().getAllocedDevCount(adv_id, alloc_type);
			
			if(this.advinfo.getAdv_type() < 30){
				sruntime  += "已经分配的总设备数:" + all_allock_count + "\r\n";
				sruntime  += "已经分配的活跃设备数:" + act_alloced_count + "\r\n";
			}
		}
		log.info(sruntime);
		return sruntime;
	}
	
	@Override
	public TaskBase fetchTaskStrong() {
		// TODO Auto-generated method stub
		// return null;
		TaskCPANewly adtask = new TaskCPANewly();
		adtask.setTaskInfo(advinfo);
		return adtask;
	}
	
	@Override
	public boolean isFinish() {
		// TODO Auto-generated method stub
		int retc = advinfo.getDayusercount();   //每天要做的新增
		int donecount = 0;
		if (result != null){
			donecount = result.getNewuser_success_count();   //减去今天已经完成的
		}
		retc -= donecount;
		if(retc <= 0) {
			return true;
		}
		return false;
	}
}
