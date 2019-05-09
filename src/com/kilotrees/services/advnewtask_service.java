package com.kilotrees.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kilotrees.dao.advnewreglogdao;
import com.kilotrees.dao.advtaskinfodao;
import com.kilotrees.model.bo.TaskBase;
import com.kilotrees.model.bo.ad_task_report;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.advgroup;
import com.kilotrees.model.po.advnewreglog;
import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.model.po.advtodayresult;
import com.kilotrees.model.po.deviceinfo;
import com.kilotrees.serverbean.ServerBeanBase;
import com.kilotrees.service.adv.runtime.advruntimefactory;
import com.kilotrees.service.adv.runtime.api.ITaskRuntime;

/**
 * 广告新增任务管理,操作advTaskList注意同步问题
 * 
 * @author Administrator
 */
public class advnewtask_service {
	private static Logger log = Logger.getLogger(advnewtask_service.class);

	private static advnewtask_service inst;

	// ArrayList<advtaskruntimeinfo> advTaskList = new
	// ArrayList<advtaskruntimeinfo>();
	// 新增广告实时状态信息 为新任务分配机器 定时重新计算分配设备
	/**
	 * 当前上线onlineflag=1的advtaskruntimeinfo集合
	 */
	HashMap<Integer, ITaskRuntime> advTaskList = new HashMap<Integer, ITaskRuntime>();

	private advnewtask_service() {

	}

	public static advnewtask_service getInstance() {
		synchronized (advnewtask_service.class) {
			if (inst == null) {
				inst = new advnewtask_service();
			}
		}
		return inst;
	}

	static int cc;// test

	/**
	 * 从数据库拿onlineflag=1的advtaskinfo来更新advTaskList, 从advTaskList中清掉数据库下线的广告
	 */
	public void refresh() {

		HashMap<Integer, advtaskinfo> onlineAdvtaskList = advtaskinfodao.getOnlineAdvtaskList();
		if (onlineAdvtaskList == null) {
			onlineAdvtaskList = new HashMap<Integer, advtaskinfo>();
		}
		// 如果是第一次启动或凌晨开始重新初始化
		if (!main_service.getInstance().isSystem_ready()) {
			// 如果是零点统计当天没有完成的广告 // ...
			synchronized (advTaskList) {
				advTaskList = new HashMap<Integer, ITaskRuntime>();
				for (advtaskinfo taskInfo : onlineAdvtaskList.values()) {
					ITaskRuntime ari = advruntimefactory.createAdvRuntime(taskInfo, false);
					addNewTask(ari, true);
				}
			}
		} else {
			delOfflineTask();
			synchronized (advTaskList) {
				// 检查哪个adv已经被暂停或者新增了广告
				// onlineAdvtaskList有的而advTaskList中没有的要新增，有的要更新
				for (advtaskinfo taskInfo : onlineAdvtaskList.values()) {
					ITaskRuntime runtime = advTaskList.get(taskInfo.getAdv_id());
					if (runtime != null) {
						// 用户可能修改广告信息，重新设置
						runtime.setAdvinfo(taskInfo);
					} else {
						ITaskRuntime ari = advruntimefactory.createAdvRuntime(taskInfo, false);
						addNewTask(ari, false);
					}
				}
				// advTaskList有的advtaskinfo而onlineAdvtaskList没有的，说明已经下线
				for (ITaskRuntime e1 : advTaskList.values()) {
					advtaskinfo e = onlineAdvtaskList.get(e1.getAdvinfo().getAdv_id());
					if (e == null) {
						e1.setOffline(true);
					}
				}
			}
		}
		log.info("refresh 在线任务总数" + this.advTaskList.size());
	}

	public void resetAdvResult() {
		// 重启时设置当天完成的任务状态
		// 设备广告状态
		synchronized (advTaskList) {
			for (ITaskRuntime e1 : advTaskList.values()) {
				int advid = e1.getAdvinfo().getAdv_id();
				advtodayresult as = advtodayresult_service.getInstance().getAdvtodayResult(advid, 0);
				e1.setResult(as);
			}
		}
	}

	/**
	 * 根据adv_id从advTaskList（onlineflag=1）拿对应advruntimeimpl对象， 如果没有，说明可能下线
	 * 注意：如果adv_id参数为long，advTaskList取不到对应的对象
	 * 
	 * @param adv_id
	 * @return
	 */
	public ITaskRuntime getAdvTaskRunTimeInfo(int adv_id) {
		synchronized (advTaskList) {
			ITaskRuntime adr = advTaskList.get(adv_id);
			if (adr != null) {
				return adr;
			}
		}
		log.warn("getAdvTaskRunTimeInfo not in advTaskList adv_id=" + adv_id);
		// 在上线任务列表advTaskList中找不到，可能已经下线了,再在数据库中找
		advtaskinfo ai = advtaskinfodao.getAdvtaskbyid(adv_id);
		if (ai != null) {
			// advruntimeimplme ari = new cpanewruntime();
			// ari.setAdvinfo(ai);
			ITaskRuntime ari = advruntimefactory.createAdvRuntime(ai, false);
			ari.setOffline(true);
			return ari;
		}
		return null;
	}

	// 增加和删除列表由同一个refresh线程处理
	/**
	 * 把advtaskinfo对应的advruntimeimpl添加进advTaskList集合
	 * 
	 * @param ari
	 *            要添加的advtasktuntimeinfo对象
	 * @param init
	 *            系统是否正在初始化
	 */
	public void addNewTask(ITaskRuntime ari, boolean init) {
		int advid = ari.getAdvinfo().getAdv_id();
		if (!init) {
			log.warn("新加广告adv_id=" + ari.getAdvinfo().getAdv_id() + ";name=" + ari.getAdvinfo().getName());
		}
		synchronized (advTaskList) {
			advTaskList.put(advid, ari);
		}
		// advtaskruntimeinfo r1 = advTaskList.get(advid);
		log.info("当前上线广告数量：" + advTaskList.size());
	}

	/**
	 * 从advTaskList中remove掉advruntimeimpl的isOffline()=true的对象
	 */
	public void delOfflineTask() {
		synchronized (advTaskList) {
			for (Iterator<Map.Entry<Integer, ITaskRuntime>> it = advTaskList.entrySet().iterator(); it.hasNext();) {
				Map.Entry<Integer, ITaskRuntime> item = it.next();
				ITaskRuntime ari = item.getValue();
				if (ari.isOffline()) {
					log.warn("删除广告adv_id=" + ari.getAdvinfo().getAdv_id() + ";name=" + ari.getAdvinfo().getName());
					// advTaskList.remove(ari.getAdvinfo().getAdv_id());
					// i--;
					it.remove();
				}
			}
		}
		log.info("delOfflineTask()后广告数量：" + advTaskList.size());
	}

	/**
	 * 充值任务结果返回
	 * 
	 * @param report
	 * @param as
	 */
	public void advNewTaskParentResultReport(ad_task_report report, advtodayresult as) {
		synchronized (advTaskList) {

			ITaskRuntime adr = advTaskList.get(report.getTask().getParent_advid());
			if (adr != null) {
				adr.setResult(as);
				// if (adr.getAdvinfo().getRemaintime() > 0 && adr.getAdvinfo().getAdv_type() <
				// 10)
				// req_remain = true;
				// //2018-11-30，如果留存时间为0，但dayopencount不等1时，也要放天留存表中做剩下的活跃数
				// else if(adr.getAdvinfo().getDayopencount() != 1)
				// req_remain = true;
				// boolean test = true;
				if (report.getResult() == ServerConfig.result_success_flag) {
					int alldocount = adr.getAdvinfo().getAlldocount();
					adr.getAdvinfo().setAlldocount(alldocount + 1);
					advtaskinfodao.updateAllDocount(adr.getAdvinfo());
				}
				adr.decDoingCount(report.getTask().getDev_tag());
			}
		}
	}

	/**
	 * 设备返回任务完成报告
	 * 
	 * @param report
	 * @param as
	 */
	public void advNewTaskResultReport(ad_task_report report, advtodayresult as) {
		// 这里改成在留存服务处理，不然，不做留存的zip文件没有做删除
		// boolean req_remain = false;
		ITaskRuntime adr;
		synchronized (advTaskList) {

			adr = advTaskList.get(report.getTask().getAdv_id());
			if (adr != null) {
				adr.setResult(as);
				// if (adr.getAdvinfo().getRemaintime() > 0 && adr.getAdvinfo().getAdv_type() <
				// 10)
				// req_remain = true;
				// //2018-11-30，如果留存时间为0，但dayopencount不等1时，也要放天留存表中做剩下的活跃数
				// else if(adr.getAdvinfo().getDayopencount() != 1)
				// req_remain = true;
				// boolean test = true;
				if (report.getResult() == ServerConfig.result_success_flag) {
					int alldocount = adr.getAdvinfo().getAlldocount();
					adr.getAdvinfo().setAlldocount(alldocount + 1);
					advtaskinfodao.updateAllDocount(adr.getAdvinfo());
				}
				adr.decDoingCount(report.getTask().getDev_tag());

			} else
				adr = this.getAdvTaskRunTimeInfo(report.getTask().getAdv_id());
		}
		// 如果自身处理日志记录，就不需要下面记录log之类，这种对于非cpa类的广告
		if (adr.handleTaskReport(report))
			return;

		advnewreglog newlog = new advnewreglog();
		newlog.setAutoid(report.getTask().getAutoid());
		newlog.setAdv_id(report.getTask().getAdv_id());
		newlog.setDev_tag(report.getTask().getDev_tag());
		newlog.setStep(report.getStep());
		newlog.setResult(report.getResult());
		newlog.setLoginfo(report.getResult_info());

		// newlog.setPhoneInfo(report.getTask().getPhoneInfo());
		// 这里不需要保存phoneinfo，这个数据量太大，增加存储空间
		// 2018-12-19 把phoneInfo用来保存appinfo的信息,之前是空字符串
		JSONObject appInfo = report.getTask().getAppInfo();
		newlog.setAppInfo(appInfo != null ? appInfo.toString() : "");

		newlog.setLogtime(report.getReportTime());
		newlog.setIp(report.getIp());
		newlog.setArea(report.getArea());
		advnewreglogdao.addNewRegLog(newlog);

		if (report.getResult() == ServerConfig.result_success_flag) {
			// if(report.)
			// 新增完成后，如果需要做留存，则加到留存缓存表中
			// if (req_remain)
			advremaintask_service.getInstance().finishNewTaskSuccess(report);
		}

	}

	/*
	 * 对每个广告计算一天每小时需要执行的数量
	 */
	public void advDisptchReady() {
		synchronized (advTaskList) {
			for (ITaskRuntime e : advTaskList.values()) {
				// log.info("advDisptchReady adv_id=" +
				// e.getAdvinfo().getAdv_id());
				e.caclRuntimeHouursCount();
				e.getTaskRuntimeInfo();
				ServerBeanBase sbean = (ServerBeanBase) ServerBeanBase.getServerBean(e.getAdvinfo().getServerbeanid());
				if (sbean != null)
					sbean.refresh(e.getAdvinfo().getAdv_id());
			}
		}
	}

	/**
	 * 按设备预先分配的广告提取广告任务
	 */
	public TaskBase[] fetchTasks(deviceinfo di) {
		TaskBase[] adt = new TaskBase[0];
		ArrayList<TaskBase> list = new ArrayList<TaskBase>();

		synchronized (advTaskList) {
			// 不为广告组
			if (di.getAlloc_type() != ServerConfig.adv_alloc_type_group) {
				ITaskRuntime adr = advTaskList.get(di.getAlloc_adv());
				if (adr != null) {
					// 对于大点击任务，同时分配几个下去？
					int fetchcount = 1;
					JSONObject jsoExt = adr.getAdvinfo().getExtJso();
					if (jsoExt != null) {
						fetchcount = jsoExt.optInt("fetchcount", 1);
					}
					for (int i = 0; i < fetchcount; i++) {
						TaskBase[] tasks = adr.fetchTask(di.getDevice_tag());
						if (tasks != null && tasks.length > 0) {
							for (TaskBase task : tasks)
								if (task != null) {
									list.add(task);
								}
							// 2018-12-29
							// adr.incDoingCount(di.getDevice_tag());
							// break;
						} else {
							break;
						}
					}
				}

			} else // if (di.getAlloc_type() ==
					// serverconfig.adv_alloc_type_group)
			{
				int groupid = di.getAlloc_adv();
				advgroup g = advgroup_service.getInstance().getGroup(groupid);
				for (ITaskRuntime e : advTaskList.values()) {
					if (g.isIngroup(e.getAdvinfo().getAdv_id())) {
						TaskBase[] tasks = e.fetchTask(di.getDevice_tag());
						if (tasks != null && tasks.length > 0) {
							for (TaskBase task : tasks)
								if (task != null) {
									list.add(task);
								}
						}
					}
				}
			}
			// }
		}
		adt = new TaskBase[list.size()];
		list.toArray(adt);
		return adt;
	}

	/**
	 * 检查传入的deviceinfo对象是否已经上传了日志
	 * 
	 * @param di
	 *            要分配例行广告的deviceinfo对象
	 * @return 例行广告数组ad_task[]
	 */
	public TaskBase[] fetchRoutineTasks(deviceinfo di) {
		TaskBase[] adt = new TaskBase[0];
		ArrayList<TaskBase> list = new ArrayList<TaskBase>();
		if (di.getHand_locked() > 0) {
			// 对于自动充值之类的，是否执行例行广告?
		}
		synchronized (advTaskList) {
			for (ITaskRuntime adr : advTaskList.values()) {
				// 对于例行性广告，每台设备都要做
				if (adr.getAdvinfo().getAdv_type() >= 30) {
					boolean needDo = false;
					if (adr.getAdvinfo().getAdv_type() == 30) {
						// 每天做一次,查询日志表
						if (advnewreglogdao.checkDevLog(di, 1) == false)
							needDo = true;
					} else {
						// 以月为单位，如果只做一次，手动关了此任务
						if (advnewreglogdao.checkDevLog(di, 0) == false)
							needDo = true;
					}
					if (needDo) {
						TaskBase[] tasks = adr.fetchTask(di.getDevice_tag());
						if (tasks != null && tasks.length > 0) {
							for (TaskBase task : tasks)
								if (task != null) {
									list.add(task);
								}
						}
						// 2018-12-29直接在advruntimebase.fetch中实现了，不在这里incDoingCount
						// adr.incDoingCount(di.getDevice_tag());
					}
				}
			}
		}
		adt = new TaskBase[list.size()];
		list.toArray(adt);
		return adt;
	}

	/**
	 * 为暂时空闲的设备分配任务,先看每个任务当前小时是否按时完成任务,否则会让没有分配的空闲机器去做无谓的
	 * 
	 * @param di
	 * @return
	 */
	public TaskBase[] fetchUrgentTempTask(deviceinfo di) {
		TaskBase[] adt = new TaskBase[0];
		ArrayList<TaskBase> list = new ArrayList<TaskBase>();
		if (di.getHand_locked() > 0)
			return adt;
		synchronized (advTaskList) {
			for (ITaskRuntime adr : advTaskList.values()) {
				if (adr.getAdvinfo().getHandle_Locked() > 0)
					continue;
				int curMin = Calendar.getInstance().get(Calendar.MINUTE);
				// 当前小时剩下的时间秒数
				int retSecTime = (60 - curMin - 1) * 60;
				retSecTime -= 10;
				int reqSecTime = adr.getAdvinfo().getRequesttime();
				// 2018-12-23 游戏工作时间太多，导致过了30分钟都不分配了
				// if ((adr.isRush() || retSecTime > reqSecTime) &&
				// adr.checkCurHourFinishStatus() == false)
				if (adr.checkCurHourFinishStatus() == false) {
					// 如果新增和留存必须同一台机，则只能重新分配机器，不能由其它机器临时做
					if (adr.getAdvinfo().getRemain_lock_dev() == 1)
						continue;
					TaskBase[] tasks = adr.fetchTask(di.getDevice_tag());
					if (tasks != null && tasks.length > 0) {
						for (TaskBase task : tasks)
							if (task != null) {
								list.add(task);
							}
					}
				}
			}
		}
		adt = new TaskBase[list.size()];
		list.toArray(adt);
		return adt;
	}
	//
	// public ArrayList<advtaskruntimeinfo> getAdvTaskList() {
	// return advTaskList;
	// }

	public void reAllocDev(boolean inited) {
		synchronized (advTaskList) {
			for (ITaskRuntime e : advTaskList.values()) {
				e.reAllocDev(inited);
			}
		}
	}

	// 2018-12-29 当任务的json文件中的dayusercount和数据表中的不一样时，更新数据表的dayusercount字段的值
	public void updateAdvDayUserCountByJson(int adv_id, int newcount) {
		advtaskinfodao.updateAdvDayUserCountByJson(adv_id, newcount);
	}

	public void getTempTaskByPrior(List<ITaskRuntime> list) {
		for (ITaskRuntime e : advTaskList.values()) {
			int hand_lock = e.getAdvinfo().getHandle_Locked();
			if(hand_lock <= 0) {
				if (!e.checkCurHourFinishStatus()) {
					int prior = e.getAdvinfo().getPrior();
					prior = prior <= 1 ? 1 : prior;
					for(int i=0;i<prior;i++) {
						list.add(e);
					}
				}
			}
		}
	}
	
	
	
}
