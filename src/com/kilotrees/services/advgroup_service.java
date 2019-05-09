package com.kilotrees.services;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.kilotrees.dao.advgroupdao;
import com.kilotrees.dao.apkinfodao;
import com.kilotrees.model.po.advgroup;
import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.model.po.apkinfo;

/**
 * 
 * 分组用于同一台机器同时执行一组任务，这个主要分以后扩展优化机器使用率设计，当前处理还是比较简单，开始时我们用来做大点击的链接广告，
 * adtaskdispath_center中allocUrgentTempTask(deviceinfo)用临时分配空闲机这个来实现
 * 广告组和留存组管理 组一般人工设定好，但不能把相同包不同渠道的apk放在同一组
 * 广告组这个比较复杂,先实现基本功能,主要是onlineflag和任务的onlineflag有冲突时处理麻烦,还有当要更改广告组里的id时,重新分配也麻烦.
 * 
 * @author Administrator
 *
 */
public class advgroup_service {
	private static Logger log = Logger.getLogger(advgroup_service.class);

	private static advgroup_service inst;
	private ArrayList<advgroup> grouplist = new ArrayList<advgroup>();

	private advgroup_service() {

	}

	public static advgroup_service getInstance() {
		synchronized(advgroup_service.class){
			if (inst == null) {
				inst = new advgroup_service();
			}
		}
		return inst;
	}

	public void refresh() {
		synchronized(grouplist){
			grouplist = advgroupdao.getAdvGroupList();
			check();
		}
	}

	/**
	 * 同一组广告的广告apk包名不能相同
	 */
	void check() {
		try {
			for (advgroup e : grouplist) {
				String[] advids = e.getAdvids().split(";");
				int[] adv = new int[advids.length];
				for (int i = 0; i < adv.length; i++)
					adv[i] = Integer.parseInt(advids[i]);
				String pkgs = "";
				String packagename = "";
				for (int i = 0; i < adv.length; i++) {
					advtaskinfo task = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv[i]).getAdvinfo();
					if (task == null) {
						log.warn("广告组内广告可能没有上线:" + adv[i]);
						continue;
					}
					//task.setGroupid(e.getGroupid());
					if (task.getApkid() <= 0)
						continue;
					apkinfo ai = apkinfodao.getApkInfo(task.getApkid());
					if(ai != null)
						packagename = ai.getPackagename();
					if (packagename.length() > 0) {
						if (pkgs.indexOf(packagename) > -1) {
							ErrorLog_service.system_errlog(getClass().getName()+"广告组有重复包名的id" + adv[i] + ";" + ai.getApkname());
							log.error("广告组有重复包名的id" + adv[i] + ";" + ai.getApkname());
							return;
						} else {
							pkgs += packagename;
							pkgs += ";";
						}
					}
				}

			}
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		}
	}

	public advgroup getGroupIncludeAdv(int adv_id, boolean remain) {
		int type = 0;
		if (remain)
			type = 1;
		synchronized(grouplist){
			for (advgroup e : grouplist) {
				if (e.getType() == type && e.isIngroup(adv_id)) {
					return e;
				}
			}
		}
		return null;
	}

	/**根据传入的groupid(就是adv_id)，从grouplist中查出对应advgroup对象
	 * @param groupid groupid就是adv_id
	 * @return
	 */
	public advgroup getGroup(int groupid) {
		synchronized(grouplist){
			for (advgroup e : grouplist) {
				if (e.getGroupid() == groupid)
					return e;
			}
		}
		return null;
	}

	public void allocDevForNew() {
		synchronized(grouplist){
			for (advgroup e : grouplist) {
				if (e.getOnlineflag() == 3) {
					// 新上线广告
					e.allocDevForNew();
				}
			}
		}
	}

	public void reAllocDev(boolean inited) {
		synchronized(grouplist){
			for (advgroup e : grouplist) {
				if (e.getOnlineflag() == 1)
					e.reAllocDev(inited);
					e.getTaskRuntimeInfo();
			}
		}
	}
}
