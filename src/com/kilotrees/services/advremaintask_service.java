package com.kilotrees.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.dao.advremaincachetmpdao;
import com.kilotrees.dao.advremainlogdao;
import com.kilotrees.dao.advremaintaskdao;
import com.kilotrees.dao.advtaskinfodao;
import com.kilotrees.dao.delautoiddao;
import com.kilotrees.model.bo.TaskBase;
import com.kilotrees.model.bo.TaskCPARemain;
import com.kilotrees.model.bo.ad_task_report;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.advremaincachetmp;
import com.kilotrees.model.po.advremainlog;
import com.kilotrees.model.po.advremaintask;
import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.model.po.advtodayresult;
import com.kilotrees.model.po.deviceinfo;
import com.kilotrees.service.adv.runtime.advruntimefactory;
import com.kilotrees.service.adv.runtime.cparemainruntime;
import com.kilotrees.service.adv.runtime.api.ITaskRuntime;
import com.kilotrees.util.DateUtil;
import com.kilotrees.util.StringUtil;

/**
 * 留存任务管理服务 实现留存策略，计算每个新增任务的留存数据，按留存规则用正态分布方式计算出后面的每天的留存数值
 * 对于一个需要做留存的广告来说，我们假定一台机器产生的新增用户数据，以后都在这台机器上做留存，预先就分配好机器的数量和每台能做的数量
 * 
 * @author Administrator
 *
 */
public class advremaintask_service {
	private static Logger log = Logger.getLogger(advremaintask_service.class);

	private static advremaintask_service inst;
	/**
	 * advremaintask表中要做
	 */
	HashMap<Integer, ITaskRuntime> remainTaskList = new HashMap<Integer, ITaskRuntime>();

	private advremaintask_service() {
		// new TestThread().start();
	}

	public static advremaintask_service getInstance() {
		synchronized (advremaintask_service.class) {
			if (inst == null) {
				inst = new advremaintask_service();
			}
		}
		return inst;
	}

	static int cc;// test

	public void refresh() {

		if (!main_service.getInstance().isSystem_ready()) {
			// 如果是零点统计当天要做的留存
			// if (main_service.getInstance().isNewDayBegine()) {
			// 读取留存临时表数据，处理昨天没有完成的留存,清空临时表
			// 重启服务器时进入
			this.procRemainCacheData(true);
			// }
			// 留存表太大，在系统初始化时一次计算所有要做留存的任务，后面那些一天要活跃多次的新增之后，余下的活跃实时加到留存任务中．
			synchronized (remainTaskList) {
				log.info("refresh init advremaintaskdao.getTodayRemainTaskRunTime ");
				ArrayList<Integer> ls = advremaintaskdao.getTodayRemainTaskRunTimeIds();
				remainTaskList.clear();
				for (Integer i : ls) {
					advtaskinfo advinfo = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(i).getAdvinfo();
					ITaskRuntime remainri = advruntimefactory.createAdvRuntime(advinfo, true);
					remainTaskList.put(i, remainri);
				}
				// remainTaskList = advremaintaskdao.getTodayRemainTaskRunTime();

			}
		} else {
			procRemainCacheData(false);
		}

		this.delOfflineTask();
		ArrayList<Integer> ls = advtaskinfodao.getAdvtaskListOnlineflag2();
		for (Integer i : ls) {
			synchronized (remainTaskList) {
				ITaskRuntime e = remainTaskList.get(i);
				if (e != null) {
					// 暂时不删除，先设置标记
					e.setOffline(true);
					log.info("advid=" + i + "强制关停留存!");
					advremaintaskdao.updateRemainTaskoffline2(i);
				}
			}
		}
		// 设备留存时间 2019-1-16，因为现在已经改了接口，advruntimeimpl已经包含advtaskinfo这个了，不需在设置了
		synchronized (remainTaskList) {
			for (ITaskRuntime e : remainTaskList.values()) {
				if (!e.isOffline()/* && e.getRemaintime() == 0 */) {
					// advruntimeimpl a =
					// advnewtask_service.getInstance().getAdvTaskRunTimeInfo(e.getAdv_id());
					// if (a == null) {
					// log.error("advnewtask_service.getInstance().getAdvTaskRunTimeInfo return null
					// advid="
					// + e.getAdv_id());
					// continue;
					// }
					// if(e.getAdvinfo().getRemaintime() > 0)
					// e.setRemaintime(e.getAdvinfo().getRemaintime());
					// 2018-12-3
					// else if(a.getAdvinfo().getDayopencount() != 1)
					// e.setRemaintime(a.getAdvinfo().getRequesttime());
					// e.setFirstDoDay(advtodayresultdao.getFirstRemainDay(e.getAdv_id()));
				}
			}
		}
		// if(main_service.getInstance().isSystem_ready())

		log.info("留存任务数量:" + remainTaskList.size());
	}

	// 增加和删除列表由同一个refresh线程处理
	void delOfflineTask() {
		synchronized (remainTaskList) {
			Iterator<Map.Entry<Integer, ITaskRuntime>> it = remainTaskList.entrySet().iterator();
			for (; it.hasNext();) {
				ITaskRuntime e = it.next().getValue();
				if (e.isOffline()) {
					// remainTaskList.remove(e);
					it.remove();
				}
			}
		}
	}

	public void resetAdvResult() {
		// 重启时设置当天完成的任务状态
		// 设备广告状态
		synchronized (remainTaskList) {
			for (ITaskRuntime e : remainTaskList.values()) {
				int advid = e.getAdvinfo().getAdv_id();
				advtodayresult as = advtodayresult_service.getInstance().getAdvtodayResult(advid, 1);
				e.setResult(as);
			}
		}
	}

	public ITaskRuntime getRemainRuntimeInfo(int adv_id) {
		synchronized (remainTaskList) {
			ITaskRuntime e = remainTaskList.get(adv_id);
			return e;
		}
	}

	public void advRemainTaskResultReport(ad_task_report report, advtodayresult as) {

		TaskBase task = report.getTask();
		TaskCPARemain taskRemain = (TaskCPARemain) task;

		synchronized (remainTaskList) {
			// for (advremainruntimeinfo e : remainTaskList) {
			cparemainruntime e = (cparemainruntime) remainTaskList.get(taskRemain.getAdv_id());
			if (e != null) {
				if (e.getResult() == null)
					e.setResult(as);
				if (report.getResult() == ServerConfig.result_success_flag) {
					// e.setTodocount(e.getTodocount() - 1);
					// 下面其实也没有必要，因为上面定时刷新时，会重新从表中计算一次
					if (taskRemain.isNewuser_today()) {
						e.setNew_todocount(e.getNew_todocount() - 1);
					} else {
						e.setOld_todocount(e.getOld_todocount() - 1);
					}
					// 更改表字段
					advremaintaskdao.finisthTodayRemainTask(taskRemain);
				}
			}
			// }
		}
		// 是否加日志信息表?
		advremainlog newlog = new advremainlog();
		newlog.setAutoid(taskRemain.getAutoid());
		newlog.setAdv_id(taskRemain.getAdv_id());
		newlog.setDev_tag(taskRemain.getDev_tag());
		newlog.setStep(report.getStep());
		newlog.setResult(report.getResult());
		newlog.setLoginfo(report.getResult_info());
		newlog.setLogtime(report.getReportTime());
		newlog.setIp(report.getIp());
		newlog.setArea(report.getArea());
		advremainlogdao.addNewRemainLog(newlog);
	}

	/*
	 * 对每个广告留存计算一天每小时需要执行的数量
	 */
	public void advDisptchReady() {
		// １：计算当前每个广告需要的设备数量包括留存，机器第一次启动和０点都要重新计算，如果有新机器加入，也要计算
		synchronized (remainTaskList) {
			for (ITaskRuntime advi : remainTaskList.values()) {
				// advruntimeimpl a =
				// advnewtask_service.getInstance().getAdvTaskRunTimeInfo(e.getAdv_id());
				// e.setRem_rule(a.getAdvinfo().getRemain_rule());
				// e.setLock_dev(a.getAdvinfo().getRemain_lock_dev());
				// e.setTimelineid(a.getAdvinfo().getRem_timeline());
				// 2019-1-17,要刷新advtaskinfo
				cparemainruntime e = (cparemainruntime) advi;
				advtaskinfo new_ai = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(advi.getAdvinfo().getAdv_id()).getAdvinfo();
				e.getAdvinfo().clone(new_ai);

				int[] count = advremaintaskdao.getTodayRemainOldUserCount(e.getAdvinfo().getAdv_id());
				e.setOld_user_count(count[0]);
				e.setOld_todocount(count[1]);
				count = advremaintaskdao.getTodayRemainNewUserCount(e.getAdvinfo().getAdv_id());
				e.setToday_user_count(count[0]);
				e.setNew_todocount(count[1]);
				// 2019-1-17 下面暂时不实现
				int[] days_7c = advremaintaskdao.getSpectDaysRemainCount(e.getAdvinfo().getAdv_id(), 7);
				// modify 2018-10-25,因为dayopencount规则改了，这里大部分是0，所以这里暂时设为1，以后再计算
				for (int i = 0; i < days_7c.length; i++)
					days_7c[i] *= 1;// a.getAdvinfo().getDayopencount();

				e.setDo_count_af7days(days_7c);
				// Date d = advremaintaskdao.getFirstRemainDay(e.getAdv_id());
				// e.setFirstDoDay(d);
				// e.caclHouursCount(false);
				e.caclRuntimeHouursCount();
				// log.info("advDisptchReady 留存广告:" + e.getAdv_id() +
				// "FirstDoDay=" + DateUtil.getShortDateString(d));
				// log.info("remrule=" + a.getAdvinfo().getRemain_rule());
				// String info = "7天剩余留存数据:";
				// for (int i = 0; i < 7; i++) {
				// info = info + days_7c[i] + ";";
				// }
				// log.info(info);
				e.getTaskRuntimeInfo();
			}
		}
	}
	//
	// public ArrayList<advremainruntimeinfo> getRemainTaskList() {
	// return remainTaskList;
	// }

	/**
	 * 每个要做留存的广告完成后，先把数据放在缓存表中，程序定时扫描此表，如果数量达到一定(如100)左右，马上计算留存，
	 * 把最终生成的留存数据插入tb_advremaintask表中 * 如果零点时，即使留存没有达到，也要把昨天的数据全部计算出来并清空缓存表
	 * 
	 * @param reset
	 *            是否mainservice凌晨初始化
	 */
	public void procRemainCacheData(boolean reset) {
		// 拿advremaintmp表procok=0的adv_id
		ArrayList<Integer> advids = advremaincachetmpdao.getAdvIdList();
		// 初始化时，若没有留存，直接删除一个月前
		if (advids.size() == 0 && !main_service.getInstance().isSystem_ready()) {
			log.info("留存缓存数为0，系统刚开始，清了缓存表");
			advremaincachetmpdao.resetCacheTable(30);
			return;
		}
		// 处理每个adv_id
		for (int ii = 0; ii < advids.size(); ii++) {
			int adv_id = advids.get(ii);
			// 不存在advtaskinfo的话就结束了
			if (advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id) == null) {
				ErrorLog_service.system_errlog(getClass().getName() + "procRemainCacheData找不到广告,id:" + adv_id);
				log.error("procRemainCacheData找不到广告,id:" + adv_id);
				continue;
			}
			// 拿advremaintmp表该adv_id的list
			ArrayList<advremaincachetmp> ls = advremaincachetmpdao.getCacheList(adv_id);
			// 不是项目初始化，数量不够结束
			if (!reset && ls.size() < ServerConfig.remain_cache_count) {
				log.info("广告" + adv_id + "留存数量不足：" + ls.size());
				continue;
			}

			int max_cache_id = ls.get(ls.size() - 1).getCacheid();
			final int size = ls.size();// 对应任务未处理（prock=0）的advremaincachetmp集合size

			// 2018-11-30 这里判断是否真留存还是多次打开
			// 拿广告的remainTime
			int remainTime = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo().getRemaintime();
			if (remainTime <= 0) {
				// 不是真实做留存，只是做当天活跃
				handle_advmultiopencount(ls, adv_id);
				// advremaincachetmpdao.finishCacheProc(max_cache_id, adv_id);
				continue;
			}
			// remainTime>0 是真实做留存的
			String remRule = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo().getRemain_rule();
			// 不符合留存规则，下一个
			if (judgeRemRule(remRule) == false) {
				ErrorLog_service.system_errlog((getClass().getName() + "procRemainCacheData remRul=" + remRule));
				log.error("procRemainCacheData remRul=" + remRule);
				continue;
			}
			/*
			 * month_days为每天活跃用户比例
			 */
			int[] month_days = new int[ServerConfig.max_remain_days];
			// 根据remRule填充month_days[]
			cparemainruntime.caclMothRemainByRule(remRule, month_days);
			if (month_days[0] == 0) {
				// 2018/11/30，如果广告一天活跃多次，那么这里有问题。
				ErrorLog_service.system_errlog(getClass().getName() + "procRemainCacheData 次日留存为0,remRul=" + remRule);
				log.error("procRemainCacheData 次日留存为0,remRul=" + remRule);
				continue;
			}
			// 2019-1-6从下面提到这里执行，防止下面处理出错时，cache的数据重复处理。
			// 把advremaincachetmp表该adv_id的<max_cache_id的set procok=1
			advremaincachetmpdao.finishCacheProc(max_cache_id, adv_id);

			int req_time = 60;
			// ret_days表示没有给删除的用户，除了活跃用户，还包括沉默用户．这里保留比设定的留存值增加30%的用户数。
			// 我们假定次日留存为40，次日有40%的用户是再次使用，那么实质上可能还有30%的用户次日并没有活跃
			// 这个跟以前的想法是不一样的，以前就是只留下40%，并且后面每天都活跃，明显不合理．

			int[] ret_days = new int[ServerConfig.max_remain_days];
			int endIndex = ret_days.length - 1;
			// 2019-1-31，由于象妈妈网要求一月后还要有50%的用户，但活跃率可以很低，只好改成这个ret_days由配置文件写死
			String ret_days_def = "";
			ret_days_def = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo().getExtJso().optString("remainret_days", "");
			// if(advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id))

			if (ret_days_def.length() > 0) {
				ret_days_def = ret_days_def.replaceAll(",", ";");
				String[] s = ret_days_def.split(";");
				for (int j = 0; j < ret_days.length; j++) {
					ret_days[j] = Integer.parseInt(s[j]);
					if (month_days[j] <= 0) {
						endIndex = j;
						break;
					}
				}
			} else {
				// 默认把month_day[] * 4/3 赋给 ret_days[] ,记录endIndex
				for (int j = 0; j < ret_days.length; j++) {
					ret_days[j] = month_days[j] * 4 / 3;
					// log.info("month_days[" + j + "]=" + month_days[j] +
					// ";ret_days[j]=" + ret_days[j]);
					if (month_days[j] <= 0) {
						endIndex = j;
						break;
					}
				}
			}
			// 2018-12-5
			// cut是在做第二天留存时,需要减去的数量
			int cut = ls.size() - ret_days[0] * size / 100;// (100 - ret_days[0]) * size / 100;
			req_time = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id).getAdvinfo().getRequesttime();
			// log.info("ls size=" + ls.size() + ",cut=" + cut + ";req_time=" +
			// req_time);
			// 去掉那些执行时间小于25秒并用时长,这个在任务分派时取一部分用户只打开几十秒，但后面没有用上
			// 先去掉之前打开时间极短的用户

			advremaincachetmp delCache;
			if (req_time > 25 * 3) {
				// int c = cut;//防止大部分给去掉了
				for (int k = 0; k < ls.size(); k++) {
					if (ls.get(k).getUsedtime() <= 25) {
						// log.info("用时太短删除 " + ls.get(k).getPhoneInfo());
						delCache = ls.remove(k);
						handle_delReamaincache(delCache, "procRemainCacheData:1");
						k--;
						cut--;
					}
					if (cut == 0)
						break;
				}
			}
			// log.info("清除用时短用户后，ls size=" + ls.size() + ",cut=" + cut);
			// 按留存remRule计算后面的数据
			// 处理次日留存,先把用户减到ret_days[0]比例的数据
			while (cut > 0) {
				int pos = new java.util.Random().nextInt(ls.size());
				delCache = ls.remove(pos);
				handle_delReamaincache(delCache, "procRemainCacheData:2");
				cut--;
			}
			//至此，ls中就是第二天做次留的量
			// log.info("now ls size=" + ls.size());
			// 先设置所有记录的停止时间
			for (advremaincachetmp e : ls) {
				e.setAct_days_flag(endIndex, -1);
			}
			int arrangeFlowerCount = size / 20;// 随机抽5%的量插花
			// log.info("arrangeFlowerCount = " + arrangeFlowerCount);
			while (arrangeFlowerCount-- > 0) {
				int pos = new java.util.Random().nextInt(ls.size());
				advremaincachetmp e = ls.get(pos);
				e.arrangeFlower(endIndex);
			}

			// 按ret_days每天留下的活跃和留存数，不断缩减ls的大小，并且按减小的数目随机选择记录设置结束留存，并且按month_days值设置活跃日期
			int jj = 0;
			for (jj = 0; jj < endIndex; jj++) {
				// 第jj天留下的留存和沉默用户数
				int today_retcount = ret_days[jj] * size / 100;
				// 当天必须活跃的用户数
				int act_count = month_days[jj] * size / 100;// ??
				if (today_retcount < 1) {
					if (ls.size() > 0) {
						// 保留一个直到endIndex?
						today_retcount = 1;
					} else
						break;
				}

				if (act_count < 1 && ls.size() >= 1) {
					// 保留一个直到endIndex?
					act_count = 1;
				}

				if (jj > 0) {
					// jj=0时，前面已经处理了cut
					// 这里也有可能后面的数值比前面大(时间越后，随机性越大)
					// cut = ret_days[j-1] - ret_days[j];
					cut = ls.size() - today_retcount;//
					// 缩表删除cut数量记录，如果删除的记录有留存日期就插入到留存任务表中
					while (cut > 0 && ls.size() > 0) {
						int pos = new java.util.Random().nextInt(ls.size());
						advremaincachetmp e = ls.get(pos);
						handle_advremaincache(e);
						ls.remove(pos);
						cut--;
					}

				}
				// 随机抽act_count个记录设置advremaincachetmp的数组序号jj为1，表示第jj天是活跃的
				if (act_count == 0) {
					// 可能size本身太小，随机抽一条插花
					// if(ls.size() >= 1)
					// {
					// int pos = new java.util.Random().nextInt(ls.size());
					// advremaincachetmp e = ls.get(pos);
					// e.arrangeFlower(endIndex);
					// }
					break;
				} else {
					// if (act_count > ls.size())
					// act_count = ls.size();
					// 为保障随机数不重复，用临时列表处理
					ArrayList<advremaincachetmp> lstmp = new ArrayList<advremaincachetmp>();
					for (advremaincachetmp ac : ls)
						lstmp.add(ac);
					while (act_count > 0) {
						int pos = new java.util.Random().nextInt(lstmp.size());
						advremaincachetmp e = lstmp.get(pos);
						e.setAct_days_flag(jj, 1);
						lstmp.remove(pos);
						act_count--;
					}
				}
			}
			// log.info("完成处理后 ls size=" + ls.size() + ";jj=" + jj);
			for (advremaincachetmp e : ls) {
				handle_advremaincache(e);
			}
			// 2019-1-6 将下面这句提到前面去
			// advremaincachetmpdao.finishCacheProc(max_cache_id, adv_id);
		}
		if (reset) {
			// 2019-1-6这里先不清空表，方便跟踪数据，按配置文件指定日期清空。
			int delcachedays = ServerConfig.getConfigJson().optInt("delcachedays", 30);
			advremaincachetmpdao.resetCacheTable(delcachedays);
		}
	}

	/**
	 * 把不需要做留存但当天活跃数大于1的，放到留存表执行
	 * 
	 * @param ls
	 */
	void handle_advmultiopencount(ArrayList<advremaincachetmp> ls, int adv_id) {
		int max_cache_id = ls.get(ls.size() - 1).getCacheid();
		for (advremaincachetmp cache : ls) {
			// 把cache的开始时间设为今天，结束时间设为今天23:59:59
			// 因为缓存表的 数据是隔一段时间才处理，如果是刚好是0点系统初始化，就要把昨天没有完成的放到今天处理了，所以留存表数据对不上
			Date firstRemainTime = new Date();
			Date LastRemainTime;
			Calendar date = Calendar.getInstance();
			date.setTime(new Date());
			date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE), 23, 59, 59);
			LastRemainTime = date.getTime();

			advremaintask task = new advremaintask();
			task.setAutoid(cache.getAutoid());
			task.setAdv_id(cache.getAdv_id());
			task.setDev_tag(cache.getDev_tag());
			task.setLock_dev(cache.getLock_dev());
			task.setVpnid(cache.getVpnid());
			task.setPhoneInfo(cache.getPhoneInfo());
			task.setFirstremaintime(firstRemainTime);
			task.setRemaininfo("1");
			task.setLastremaintime(LastRemainTime);
			task.setTodayopencount(cache.getRetopencount());
			task.setLastfetchday(cache.getAdv_finish_time());
			task.setLastfinishday(cache.getAdv_finish_time());
			task.setNewregtime(cache.getAdv_finish_time());
			task.setAppinfo(cache.getAppinfo());
			advremaintaskdao.addNewRemainTask(task);

			if (main_service.getInstance().isSystem_ready()) {// 系统未初始化时会有问题
				synchronized (remainTaskList) {
					cparemainruntime e = (cparemainruntime) remainTaskList.get(task.getAdv_id());
					if (e == null) {
						// e = new cparemainruntime();
						// e.setAdv_id(task.getAdv_id());
						// //
						// int remaintime =
						// advnewtask_service.getInstance().getAdvTaskRunTimeInfo(task.getAdv_id()).getAdvinfo().getRequesttime();
						// e.setRemaintime(remaintime);
						advtaskinfo ati = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(task.getAdv_id()).getAdvinfo();
						e = (cparemainruntime) advruntimefactory.createAdvRuntime(ati, true);
						remainTaskList.put(task.getAdv_id(), e);
					}
					// 下面其实也没有必要，因为上面定时刷新时，会重新从表中计算一次
					int docount = e.getNew_todocount();
					// docount += dayopencount - 1;
					docount += cache.getRetopencount();
					e.setNew_todocount(docount);
				}
			}
		}
		advremaincachetmpdao.finishCacheProc(max_cache_id, adv_id);
	}

	/**
	 * 把临时留存表中计算出来要做留存的广告加到留存任务表中
	 * 
	 * @param cache
	 */
	void handle_advremaincache(advremaincachetmp cache) {
		if (cache.isSetremain() == false || StringUtil.isStringEmpty(cache.generateRemainInfo())) {
			handle_delReamaincache(cache, "handle_advremaincache");
			return;
		}

		// int dayopencount = 1;
		// 2018-11-30 在缓存时早已经计算好剩下的打开次数，下面不需要再减1了
		int dayopencount = cache.getRetopencount();
		// advtaskinfo ai =
		// advnewtask_service.getInstance().getAdvTaskRunTimeInfo(cache.getAdv_id()).getAdvinfo();
		// if (ai != null)
		// dayopencount = ai.getDayOpenCountRand();
		//// // modify
		// 2018-10-22,正常情况下，dayopencount=0，我们随机让用户做1-5次，按json文件定义好的比重来处理
		// // 格式如下:ad_dayopencount:"50;20;20;7;3"表示打开1次的占50%，打开2次的占20，依次类推
		// if (dayopencount == 0) {
		// //这里暂时用通用的，可能每个任务不一样。
		// String ad_dayopencount =
		// serviceconfig_service.getInstance().getConfig().getExtJso()
		// .optString("ad_dayopencount_0", "50;20;20;7;3");
		// String[] as = ad_dayopencount.split(";");
		// int[] c = new int[as.length];
		// Integer[] values = new Integer[as.length];
		//
		// for (int i = 0; i < c.length; i++) {
		// c[i] = Integer.parseInt(as[i]);
		// values[i] = i + 1;
		// }
		// dayopencount = (int)InfoGenUtils.randOnSpecScale(values, c);
		// }

		advremaintask task = new advremaintask();
		task.setAutoid(cache.getAutoid());
		task.setAdv_id(cache.getAdv_id());
		task.setDev_tag(cache.getDev_tag());
		task.setLock_dev(cache.getLock_dev());
		task.setVpnid(cache.getVpnid());
		task.setPhoneInfo(cache.getPhoneInfo());
		task.setFirstremaintime(cache.getFirstremaintime());
		task.setRemaininfo(cache.generateRemainInfo());
		task.setLastremaintime(cache.getLastRemainTime());
		task.setTodayopencount(dayopencount);
		task.setLastfetchday(cache.getAdv_finish_time());
		task.setLastfinishday(cache.getAdv_finish_time());
		task.setNewregtime(cache.getAdv_finish_time());
		task.setAppinfo(cache.getAppinfo());
		advremaintaskdao.addNewRemainTask(task);
		// //modify
		// 2018-10-22,正常情况下，dayopencount=0，我们随机让用户做1-5次，按json文件定义好的比重来处理
		// //格式如下:ad_dayopencount:"50;20;20;7;3"表示打开1次的占50%，打开2次的占20，依次类推
		// if(dayopencount == 0)
		// {
		// String ad_dayopencount =
		// serviceconfig_service.getInstance().getConfig().getExtJso().optString("ad_dayopencount","50;20;20;7;3");
		// String[] as = ad_dayopencount.split(";");
		// int[] c = new int[as.length];
		// int[] values = new int[as.length];
		//
		// for(int i = 0; i < c.length; i++){
		// c[i] = Integer.parseInt(as[i]);
		// values[i] = i+1;
		// }
		// dayopencount = InfoGenUtils.randOnSpecScale(values,c);
		// }
		// 如果广告每天活跃数大于１，立即把余下的活跃加到remainTaskList表中并且假到数据库时设定dotoday为dayopencount
		// - 1
		if (main_service.getInstance().isSystem_ready() && dayopencount > 0) {// 系统未初始化时会有问题
			synchronized (remainTaskList) {
				cparemainruntime e = (cparemainruntime) remainTaskList.get(task.getAdv_id());
				if (e == null) {
					// e = new cparemainruntime();
					// e.setAdv_id(task.getAdv_id());
					// //如果是当天
					// remainTaskList.put(e.getAdv_id(), e);
					advtaskinfo ati = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(task.getAdv_id()).getAdvinfo();
					e = (cparemainruntime) advruntimefactory.createAdvRuntime(ati, true);
					remainTaskList.put(task.getAdv_id(), e);
				}
				// 下面其实也没有必要，因为上面定时刷新时，会重新从表中计算一次
				int docount = e.getNew_todocount();
				// docount += dayopencount - 1;
				docount += dayopencount;
				e.setNew_todocount(docount);
			}
		}
	}

	/**
	 * 把要删除的advremaincachetmp放到[tb_advremaintask]表
	 * 
	 * @param cache
	 * @param delinfo
	 */
	void handle_delReamaincache(advremaincachetmp cache, String delinfo) {
		if (cache == null)
			return;
		// 2019-1-11 对于一天打开多次的用户，不能立即删除，还是做多一天，明天才不做。
		if (cache.getRetopencount() > 0) {
			addCacheTmpToRemainTask(cache, 1);
		} else {
			long autoid = cache.getAutoid();
			delautoiddao.addDelAutoid(autoid, delinfo);
		}
	}

	/**
	 * 新增任务完成后，如果需要做留存，或者当天要做多次打开，加到留存缓存表中，等待处理留存
	 * 
	 * @param report
	 */
	public void finishNewTaskSuccess(ad_task_report report) {
		if (report.getResult() != ServerConfig.result_success_flag) {
			ErrorLog_service.system_errlog(getClass().getName() + "finishNewTaskSuccess report.getResult() != serverconfig.result_success_flag");
			log.error("finishNewTaskSuccess report.getResult() != serverconfig.result_success_flag");
		}

		TaskBase task = report.getTask();
		advremaincachetmp tmp = new advremaincachetmp();
		// 2018-11-30
		boolean req_remain = false;
		int dayopencount = 1;
		ITaskRuntime adr = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(task.getAdv_id());
		if (adr != null) {
			if (adr.getAdvinfo().getRemaintime() > 0 && adr.getAdvinfo().getAdv_type() < 10)
				req_remain = true;
			// dayopencount = adr.getAdvinfo().getDayOpenCountRand();
			// 2018-12-26
			dayopencount = adr.getDayOpenCountRand(0);
		}
		// 减去新增时的第一次
		int retopencount = dayopencount - 1;
		tmp.setAdv_id(task.getAdv_id());
		tmp.setAutoid(task.getAutoid());
		tmp.setDev_tag(task.getDev_tag());
		tmp.setLock_dev(task.getLock_dev());

		JSONObject phoneInfo = task.getPhoneInfo();
		JSONObject appInfo = task.getAppInfo();
		tmp.setPhoneInfo(phoneInfo != null ? phoneInfo.toString() : "{}");
		tmp.setAppinfo(appInfo != null ? appInfo.toString() : "{}");

		tmp.setUsedtime(task.getScriptTimeout());
		tmp.setRetopencount(retopencount);

		if (req_remain || retopencount > 0)
			advremaincachetmpdao.addCache(tmp);
		else
			// remaintime=0 且 dayopencount=1的情况
			this.handle_delReamaincache(tmp, "finishNewTaskSuccess 1");
	}

	/**
	 * false: 数字位数<4 || 前面数字<后面数字
	 * 
	 * @param remRule
	 *            数据库的remain_rule
	 * @return
	 */
	public static boolean judgeRemRule(String remRule) {
		if (remRule.charAt(0) == '$') {
			remRule = remRule.substring(1);
		}
		remRule = remRule.replaceAll(",", ";");
		String[] s = remRule.split(";");
		if (s.length < 4) {
			ErrorLog_service.system_errlog("advremaintask_service:" + "留存规则不对，格式为：次日;周;双周;月;2月或$开头");
			log.error("留存规则不对，格式为：次日;周;双周;月;2月或$开头");
			return false;
		}
		for (int i = 0; i < s.length - 1; i++) {
			if (Integer.parseInt(s[i]) < Integer.parseInt(s[i + 1])) {
				ErrorLog_service.system_errlog("advremaintask_service:" + "前面数据要大于后面数据:i=" + i + ";s[i]=" + s[i] + ";s[i+1]=" + s[i + 1]);
				log.warn("前面数据要大于后面数据:i=" + i + ";s[i]=" + s[i] + ";s[i+1]=" + s[i + 1]);
				// 2019-1-31，有些广告后面留存数有可能大于前一天的，这里改为警告
				// return false;
			}
		}
		return true;
	}

	public TaskBase[] fetchTasks(deviceinfo di) {
		TaskBase[] adt = new TaskBase[0];
		ArrayList<TaskBase> list = new ArrayList<TaskBase>();
		// HashMap<Integer, advremainruntimeinfo> remainTaskList
		synchronized (remainTaskList) {
			ITaskRuntime e = remainTaskList.get(di.getAlloc_adv());// 通过预先分配好的广告id或广告组id获取留存任务

			// 之前做新增的广告，以后留存必须在同样的机器上执行，但这个限制非常大，而且分配和释放设备很复杂，尽量不用．
			// if (di.getAlloc_type() == serverconfig.adv_alloc_type_lockremain) {
			// if (e != null && e.isLock_dev() == 0) {
			// log.warn(
			// "fetchTasks di.= serverconfig.adv_alloc_type_lockremain,but
			// advremainruntimeinfo is not lock");
			// }
			// }
			// for (advremainruntimeinfo e : remainTaskList) {
			if (e != null) {
				// 通过Device_tag提取任务
				TaskBase[] tasks = e.fetchTask(di.getDevice_tag());
				if (tasks != null && tasks.length > 0) {
					for (TaskBase task : tasks) {
						list.add(task);
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
	 * 为暂时空闲的设备分配任务,先看每个任务当前小时是否按时完成任务,否则会让没有分配的空闲机器去做无谓的
	 * 
	 * @param di
	 * @return
	 */
	public TaskBase[] fetchUrgentTempTask(deviceinfo di) {
		TaskBase[] adt = new TaskBase[0];
		ArrayList<TaskBase> list = new ArrayList<TaskBase>();
		synchronized (remainTaskList) {
			for (ITaskRuntime e : remainTaskList.values()) {
				// 2018-12-13分配空闲任务时，任务不能 hand_locked,这是留存任务，如果hand_locked=1不给执行，2可以执行
				advtaskinfo ai = e.getAdvinfo();// advnewtask_service.getInstance().getAdvTaskRunTimeInfo(e.getAdv_id()).getAdvinfo();
				if (ai != null && ai.getHandle_Locked() == 1)
					continue;
				int curMin = Calendar.getInstance().get(Calendar.MINUTE);
				// 当前小时剩下的时间秒数
				int retSecTime = (60 - curMin - 1) * 60;
				retSecTime -= 10;
				// 时间是否充足
				boolean bTimeOK = retSecTime > ai.getRemaintime();
				if (ai.getRem_timeline() < 0)
					bTimeOK = true;
				// if (/*retSecTime > e.getRemaintime()*/bTimeOK && e.checkCurHourFinishStatus()
				// == false)
				// 2018-12-23 游戏工作时间太多，导致过了30分钟都不分配了
				if (e.checkCurHourFinishStatus() == false) {
					// if (e.isLock_dev() == 1)
					// continue;
					TaskBase[] tasks = e.fetchTask(di.getDevice_tag());
					if (tasks != null && tasks.length > 0) {
						for (TaskBase task : tasks) {
							list.add(task);
						}
						break;
					}
				}
			}
		}

		adt = new TaskBase[list.size()];
		list.toArray(adt);
		return adt;
	}

	public void reAllocDev(boolean inited) {
		for (ITaskRuntime e : remainTaskList.values()) {
			e.reAllocDev(inited);
		}
	}

	/**
	 * 系统零点初始化留存今天打开次数，对于大部分广告来说，每天做的活跃是随机的。在这里计算每个留存的todayopencount和dotoday的值
	 * 每个广告一次取1000条记录更新,直到完成为止 跟存储过程proc_init一样，只能一天执行一次,以后取消这个存储过程，在这里实现存储过程的功能
	 * 留存因为是固定的，所以一开始时全部计算好，但新增可能用户改变新增数目，暂时在每次做完第一次的时动态计算
	 */
	public void systemInit() {
		ServerConfig sc = new ServerConfig();// serviceconfig_service还没有生成,直接用new serverconfig;
		sc.refresh();
		// 这里有问题，因为存储过程已经完成，并更新了remainjobdoday,要换个字段
		if (DateUtil.isSameDate(sc.getRemainjobdoday(), new Date()))
			return;
		log.warn("@@@@systemInit begin@@@");
		// 在下面用批执行实现之前的proc_init存储过程,如果当天要做留存，暂时把todayopencount,dotoday设为1
		if (advremaintaskdao.proc_init() == false) {
			log.warn("@@@@systemInit advremaintaskdao.proc_init()=false@@@");
			return;
		}
		// 有些任务每天打开数是大于1的，有些是动态在1-N之间随机的，所以下面重新计算todayopencount,dotoday的值
		// hm:dotoday >= 1的 留存
		// HashMap<Integer, cparemainruntime> hm =
		// advremaintaskdao.getTodayRemainTaskRunTime();
		ArrayList<Integer> advidList = advremaintaskdao.getTodayRemainTaskRunTimeIds();
		final int fetchCount = 1000;
		for (Integer e : advidList) {
			int maxrid = -1;
			int adv_id = e;// .getAdv_id();
			advtaskinfo ai = advtaskinfodao.getAdvtaskbyid(adv_id);

			if (ai == null) {
				log.warn("@@@systemInit err: getAdvtaskbyid = null adv_id=" + adv_id);
				continue;
			}
			ITaskRuntime advi = advruntimefactory.createAdvRuntime(ai, true);
			// 对每个广告一次取1000个留存记录，直到取完为止
			while (true) {
				ArrayList<advremaintask> list = advremaintaskdao.getTodayRemainTaskList(adv_id, maxrid, fetchCount);
				if (list.size() == 0)
					break;
				for (advremaintask art : list) {
					int daydiff = 0;
					daydiff = DateUtil.differDayQty(art.getNewregtime(), new Date());
					int opencount = advi.getDayOpenCountRand(daydiff);// ai.getDayOpenCountRandRemain();
					art.setTodayopencount(opencount);
					art.setDotoday(opencount);
					if (maxrid < art.getRid())
						maxrid = art.getRid();
				}
				advremaintaskdao.initTodayRemainTaskOpenCount(list);
			}
		}
		try {
			sc.setRemainjobdoday(new Date());
		} catch (JSONException e1) {
			ErrorLog_service.system_errlog(e1);
			log.error(e1.getMessage(), e1);
		}
		log.warn("@@@systemInit end@@@");
	}

	void addCacheTmpToRemainTask(advremaincachetmp cache, int handType) {
		Date firstRemainTime = new Date();
		Date LastRemainTime;
		Calendar date = Calendar.getInstance();
		date.setTime(new Date());
		date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE), 23, 59, 59);
		LastRemainTime = date.getTime();

		advremaintask task = new advremaintask();
		task.setAutoid(cache.getAutoid());
		task.setAdv_id(cache.getAdv_id());
		task.setDev_tag(cache.getDev_tag());
		task.setLock_dev(cache.getLock_dev());
		task.setVpnid(cache.getVpnid());
		task.setPhoneInfo(cache.getPhoneInfo());
		task.setFirstremaintime(firstRemainTime);
		task.setRemaininfo("1");
		task.setLastremaintime(LastRemainTime);
		task.setTodayopencount(cache.getRetopencount());
		task.setLastfetchday(cache.getAdv_finish_time());
		task.setLastfinishday(cache.getAdv_finish_time());
		task.setNewregtime(cache.getAdv_finish_time());
		task.setAppinfo(cache.getAppinfo());
		advremaintaskdao.addNewRemainTask(task);

		if (handType == 2 && cache.getRetopencount() <= 0) {
			return;
		}

		if (main_service.getInstance().isSystem_ready()) {// 系统未初始化时会有问题
			synchronized (remainTaskList) {
				cparemainruntime e = (cparemainruntime) remainTaskList.get(task.getAdv_id());
				if (e == null) {
					// e = new cparemainruntime();
					// e.setAdv_id(task.getAdv_id());
					// int remaintime =
					// advnewtask_service.getInstance().getAdvTaskRunTimeInfo(task.getAdv_id())
					// .getAdvinfo().getRemaintime();
					// e.setRemaintime(remaintime);
					advtaskinfo adv = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(task.getAdv_id()).getAdvinfo();
					e = (cparemainruntime) advruntimefactory.createAdvRuntime(adv, true);
					remainTaskList.put(e.getAdvinfo().getAdv_id(), e);
				}
				int docount = e.getNew_todocount();
				docount += cache.getRetopencount();
				e.setNew_todocount(docount);
			}
		}

	}

	public void getTempTaskByPrior(List<ITaskRuntime> list) {
		for (ITaskRuntime e : remainTaskList.values()) {
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
