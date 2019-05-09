package com.kilotrees.services;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.kilotrees.dao.advtaskinfodao;
import com.kilotrees.model.po.advgroup;
import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.service.adv.runtime.advruntimefactory;
import com.kilotrees.service.adv.runtime.cparemainruntime;
import com.kilotrees.service.adv.runtime.api.ITaskRuntime;

/**
 * 广告分配给设备策略 计算所有任务和留存要分配需求机器数量，动态计算分配任务数量和活跃设备分配策略
 * 对广告分配设备，对于新加的广告，onlineflag设置为3的，自行分配设备，并设置onlineflag=0
 * 从下面的main函数测试中，如果按一般的留存45;25;15;10;1，这种来看,每天100新增，一个月后，差不700个留存要做
 * 
 * @author Administrator
 *
 */
public class advsalloc_service {
	private static Logger log = Logger.getLogger(advsalloc_service.class);
	private static advsalloc_service inst;

	private advsalloc_service() {

	}

	public static advsalloc_service getInstance() {
		synchronized (advsalloc_service.class) {
			if (inst == null) {
				inst = new advsalloc_service();
			}
		}
		return inst;
	}

	public void refresh() {
		// 因为用户可能会修改任务每天的数量和时长，每次都要计算每小时工作量
		advnewtask_service.getInstance().advDisptchReady();
		advremaintask_service.getInstance().advDisptchReady();
		if (!main_service.getInstance().isSystem_ready()) {
			reCacleAllTaskDevAlloc(true);
		} else {
			reCacleAllTaskDevAlloc(false);
		}
	}

	/**
	 * 计算每个广告或广告组所需要的设备数量． 留存现在有个问题，如果用做新增的机器来做，做留存的机器的vpn指向地区一定要和新增一致
	 * 
	 */
	public void reCacleAllTaskDevAlloc(boolean inited) {

		advnewtask_service.getInstance().reAllocDev(inited);
		advremaintask_service.getInstance().reAllocDev(inited);
		advgroup_service.getInstance().reAllocDev(inited);
		allocDevForNewTask();

	}

	/**
	 * 暂时没有用
	 */
	public void allocDevForNewTask() {
		// 新加的任务可能已经分成组了，先调用组的分配方法
		ArrayList<advtaskinfo> l = advtaskinfodao.getAdvtaskListOnlineflag3();
		advgroup_service.getInstance().allocDevForNew();
		// 新加的任务，由程序自动分配机器

		// 为空闲设备分配新广告
		for (advtaskinfo e : l) {
			advgroup g = advgroup_service.getInstance().getGroupIncludeAdv(e.getAdv_id(), false);
			if (g != null) {
				// 前面已经调用了
				// g.allocDevForNew();
				// e.setOnlineflag(0);
				continue;
			}
			if (e.getAdv_type() >= 30) {
				log.warn("allocDevForNewTask 例行任务不能由程序自行分配!");
				continue;
			}
			ITaskRuntime ari = advruntimefactory.createAdvRuntime(e, false);
			ari.caclRuntimeHouursCount();

			if (e.getRemain_lock_dev() == 1)
				ari.allocDevForNew();
			else {
				// 为新增计算所需要的机器数量
				ari.allocNewReady();
				if (ari.getAdvinfo().getRemaintime() > 0) {
					// 为留存计算所需要机器数量
					cparemainruntime ami = (cparemainruntime) advruntimefactory.createAdvRuntime(e, true);// new
																											// cparemainruntime();
					// ami.setAdv_id(e.getAdv_id());
					ami.allocNewReady();
					int c = ari.getReqDevDoCount() + ami.getReqDevDoCount();
					if (actdeviceinfo_service.getInstance().getFreeCount() < c) {
						ErrorLog_service.system_errlog(getClass().getName() + "新广告分配设备失败，空闲数不足(新增需求："
								+ ari.getReqDevDoCount() + ",留存需求:" + ami.getReqDevDoCount() + ",空闲机器："
								+ actdeviceinfo_service.getInstance().getFreeCount());
						log.error("新广告分配设备失败，空闲数不足(新增需求：" + ari.getReqDevDoCount() + ",留存需求:" + ami.getReqDevDoCount()
								+ ",空闲机器：" + actdeviceinfo_service.getInstance().getFreeCount());
						continue;
					}
					ami.allocDevForNew();
				}
				ari.allocDevForNew();
				// ami.allocDevForNew();
			}
		}
	}
}
