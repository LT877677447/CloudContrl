package com.kilotrees.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.dao.advallocdevlogdao;
import com.kilotrees.dao.devactstatusdao;
import com.kilotrees.dao.deviceinfodao;
import com.kilotrees.model.bo.error_result;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.advallocdevlog;
import com.kilotrees.model.po.advgroup;
import com.kilotrees.model.po.devactstatus;
import com.kilotrees.model.po.deviceinfo;
import com.kilotrees.service.adv.runtime.api.ITaskRuntime;

/**
 * 1:活跃设备管理设备，记录连接上来的设备信息 2:对广告或广告组分配空闲设备并设置设备最新状态 3:对已经分配广告的设备，释放多余或没有活跃的设备
 * 4:定时检查设备分配的广告是否关停,如果关停,释放设备
 * 
 * @author Administrator
 *
 */
public class actdeviceinfo_service {
	private static Logger log = Logger.getLogger(actdeviceinfo_service.class);

	private static actdeviceinfo_service inst;

	/**[tb_devactstatus]表“正常登陆”的设备
	 * 所有deviceLogin的设备
	 */
	private HashMap<String, deviceinfo> actDevList = new HashMap<String, deviceinfo>();
	/**
	 * [tb_deviceinfo]表中所有nouse=0的记录
	 */
	private HashMap<String, deviceinfo> allDevList = new HashMap<String, deviceinfo>();

	int freeCount = 0;
	int allCount;
	static int cc;// test

	public actdeviceinfo_service() {

	}
  
	public static actdeviceinfo_service getInstance() {
		synchronized(actdeviceinfo_service.class){
			if (inst == null) {
				inst = new actdeviceinfo_service();
			}
		}
		return inst;
	}


	/**
	 * 刷新[tb_deviceinfo]表和allDevList、[tb_devactstatus]表和actDevList
	 */
	public void refresh() {
		this.checkHartbeat();

		HashMap<String, deviceinfo> allDeviceInfo = deviceinfodao.getAllDeviceInfo();
		synchronized (allDevList) {
			if (allDevList.size() == 0) {
				for (deviceinfo devInfo : allDeviceInfo.values()) {
					if (devInfo.getNouse() == 0) {
						allDevList.put(devInfo.getDevice_tag(), devInfo);
					}
				}
			} else {
				for (deviceinfo devInfo : allDeviceInfo.values()) {
					deviceinfo devInfoInMemory = allDevList.get(devInfo.getDevice_tag());
					if (devInfoInMemory != null) {
						if (devInfo.getNouse() == 1) {
							allDevList.remove(devInfoInMemory.getDevice_tag());
						} else {
							// 手动分配广告，能实时更新
							devInfoInMemory.copy(devInfo);
						}
					} else {
						if (devInfo.getNouse() == 0) {
							log.info("有新设备加入:" + devInfo.getDevice_tag());
							allDevList.put(devInfo.getDevice_tag(), devInfo);
						}
					}
					// 设备分配的广告onlineflag=0 || onlineflag=2 下掉所有分配该广告的设备
					this.checkAdvOffline(devInfo);
					// 2018-11-30
					// 设备掉线10分钟，下掉该设备
					this.freeAdvIfDeviceOffLineTimeOut(devInfo);
				}
			}
		} // end synchronized (allDevList)
		allCount = allDevList.size();
		freeCount = 0;
		synchronized (actDevList) {
			for (deviceinfo e : actDevList.values()) {
				if (e.isFree()) {
					freeCount++;
				}
			}
		}
		log.info("当前总设备数：" + allCount);
		log.info("当前活跃设备数：" + actDevList.size() + ";空闲设备数:" + freeCount);

	}
	
	
	
	/**
	 * 检查心跳 从actDevList中移除通信中断的设备
	 */
	private void checkHartbeat() {
		Date now = new Date();
		synchronized (actDevList) {
			// 2019-2-1删除时会出错，这里要改用游标
			Iterator<Map.Entry<String, deviceinfo>> it = actDevList.entrySet().iterator();
			while (it.hasNext()) {
				deviceinfo devInfo = it.next().getValue();
				devactstatus devStatus = devInfo.getDevactStatus();
				if (devStatus == null) {
					devStatus = devactstatusdao.getDevActStatus(devInfo.getDevice_tag());
					if (devStatus == null) {
						devStatus = new devactstatus();
						devStatus.setDev_tag(devInfo.getDevice_tag());
						devactstatusdao.addNewDevActStatus(devStatus);
					}
					devInfo.setDevactStatus(devStatus);
				}
				int speTimeout = ServerConfig.getLoginDaemonInterval();
				if (now.getTime() - devStatus.getLastlogintime().getTime() > 1000 * speTimeout * 5) {
					log.info(devInfo.getDevice_tag() + (speTimeout * 5) + " 秒没有活跃了,从设备中移除");
					devStatus.setOnline(0);
					devStatus.setStatus("通信中断");
					devactstatusdao.updateOnLine(devStatus);
					it.remove();
					log.info("当前活跃设备数:" + actDevList.size());
				}
			}
		}
	}

	/**从allDevList(nouse=0)中拿deviceinfo对象
	 * @param dev_tag 要拿的deviceinfo的dev_tag
	 * @return deviceinfo对象
	 */
	public deviceinfo getDeviceInfoInAll(String dev_tag) {
		deviceinfo di = null;
		synchronized (allDevList) {
			di = allDevList.get(dev_tag);
		}
		return di;
	}

	/**如果设备上的adv_id对应的advtaskinfo的onlineflag类型
	 * 则把所有分配了该adv_id的设备的alloc_adv、alloc_type重置为0
	 * 同时在[tb_advallocdevlog]表中插入释放设备记录
	 * @param devInfo 要释放的广告来源于此设备
	 */
	public void checkAdvOffline(deviceinfo devInfo) {
		if (devInfo.getAlloc_type() != 0 && devInfo.getAlloc_adv() != 0) {
			if (devInfo.getAlloc_type() == ServerConfig.adv_alloc_type_group) {//2
				int groupid = devInfo.getAlloc_adv();
				advgroup g = advgroup_service.getInstance().getGroup(groupid);
				if(g == null){
					this.freeDeviceForGroup(groupid, 0);
					return;
				}
				if (g.getOnlineflag() == 0 || g.getOnlineflag() == 2) {
					this.freeDeviceForGroup(groupid, 0);
				}
			} else if (devInfo.getAlloc_type() == ServerConfig.adv_alloc_type_remain_group) {//5
				int groupid = devInfo.getAlloc_adv();
				advgroup g = advgroup_service.getInstance().getGroup(groupid);
				if(g == null){
					this.freeDeviceForGroup(groupid, 0);
					return;
				}
				if (g.getOnlineflag() == 2) {
					this.freeDeviceForGroup(groupid, 0);
				}
			} else if (devInfo.getAlloc_type() == ServerConfig.adv_alloc_type_single) {//1
				int adv_id = devInfo.getAlloc_adv();
				ITaskRuntime ari = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id);
				//2018-12-14
				if(ari.getAdvinfo().getHandle_Locked() > 0) {
					return;
				}
				if (ari.getAdvinfo().getOnlineflag() == 0 || ari.getAdvinfo().getOnlineflag() == 2) {
					this.freeDeviceForSingleAdv(adv_id, 0, ServerConfig.adv_alloc_type_single);
				}
			} else if (devInfo.getAlloc_type() == ServerConfig.adv_alloc_type_remain) {//3
				int adv_id = devInfo.getAlloc_adv();
				ITaskRuntime ari = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id);
				//2018-12-14
				if(ari.getAdvinfo().getHandle_Locked() > 0)
					return;
				if (ari.getAdvinfo().getOnlineflag() == 2)
					this.freeDeviceForSingleAdv(adv_id, 0, ServerConfig.adv_alloc_type_remain);
			} else if (devInfo.getAlloc_type() == ServerConfig.adv_alloc_type_lockremain) {//4
				int adv_id = devInfo.getAlloc_adv();
				ITaskRuntime ari = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id);
				if (ari.getAdvinfo().getOnlineflag() == 2)
					this.freeDeviceForSingleAdv(adv_id, 0, ServerConfig.adv_alloc_type_lockremain);
			}
		}
	}
	
	/**
	 * //2018-11-30增加功能，如果这个设备断线很久(可能关机了)，释放它占有的广告
	 * 修改服务器bug，现在一个任务分配到某几台机器，但中间某些机器停机了，没有释放这些机器所分配的任务，
	 * 任务也不会找空闲的机器来分配了，现在要改成判断某些机器是否断线半小时以上，如果是，就释放其分配的广告任务(hand_locked不释放)
	 */
	void freeAdvIfDeviceOffLineTimeOut(deviceinfo e)
	{
		if(e.getAlloc_adv() == 0)
			return;
		devactstatus devstauts = e.getDevactStatus();		
		if(devstauts == null)
		{
			//e.setLastLoginTime(new Date());
			devstauts = devactstatusdao.getDevActStatus(e.getDevice_tag());
			if(devstauts == null){
			  devstauts = new devactstatus();
			  devstauts.setDev_tag(e.getDevice_tag());
			  devactstatusdao.addNewDevActStatus(devstauts);
			}			
			//stauts.setLastlogintime(new Date());
			//devactstatusdao.addNewDevActStatus(devstauts);
			e.setDevactStatus(devstauts);
		}
		Date now = new Date();
		long lastLoginTime = 0;
		if(devstauts.getLastlogintime() != null)
			lastLoginTime = devstauts.getLastlogintime().getTime();
		int speTimeout = ServerConfig.getLoginDaemonInterval();//60
		//一半20秒，如果10分种不活跃，就认为关机
		if (now.getTime() - lastLoginTime < 1000 * speTimeout * 10) {
			return;
		}
		
		log.warn("freeAdvIfDeviceOffLineTimeOut dev_tag=" + e.getDevice_tag());
		if (e.getAlloc_type() != 0 && e.getAlloc_adv() != 0) {
			
			if (e.getAlloc_type() == ServerConfig.adv_alloc_type_group) {//2
				int groupid = e.getAlloc_adv();				
				this.freeDeviceForGroup(groupid, 0);
			} else if (e.getAlloc_type() == ServerConfig.adv_alloc_type_remain_group) {//5
				int groupid = e.getAlloc_adv();				
				//if (g.getOnlineflag() == 2)
				this.freeDeviceForGroup(groupid, 0);
			} else if (e.getAlloc_type() == ServerConfig.adv_alloc_type_single) {//1
				int adv_id = e.getAlloc_adv();			
				this.freeSingleDeviceForSingleAdv(e);
			} else if (e.getAlloc_type() == ServerConfig.adv_alloc_type_remain) {//3
				int adv_id = e.getAlloc_adv();
				this.freeSingleDeviceForSingleAdv(e);
			} else if (e.getAlloc_type() == ServerConfig.adv_alloc_type_lockremain) {//4
				int adv_id = e.getAlloc_adv();
				this.freeDeviceForSingleAdv(adv_id, 0, ServerConfig.adv_alloc_type_lockremain);
			}
		}
	}

	/**
	 * 取已经分配组id的设备数量
	 * 
	 * @param groupid
	 * @return
	 */
	public int getAllocedGroupDevCount(int groupid) {
		int c = 0;
		synchronized (allDevList) {
			for (deviceinfo di : allDevList.values()) {
				if (di.getAlloc_type() == ServerConfig.adv_alloc_type_group) {
					if (di.getAlloc_adv() == groupid)
						c++;
				}
			}
		}
		return c;
	}

	/**
	 * 取单独的广告任务已经分配的设备数
	 * 
	 * @param adv_id
	 * @return
	 */
	public int getAllocedDevCount(int adv_id, int alloc_type) {
		int c = 0;
		synchronized (allDevList) {
			for (deviceinfo di : allDevList.values()) {
				if (di.getAlloc_type() == alloc_type) {
					if (di.getAlloc_adv() == adv_id)
						c++;
				}
			}
		}
		return c;
	}

	/**
	 * 取已经分配组id的活跃设备数量
	 * 
	 * @param groupid
	 * @return
	 */
	public int getAllocedGroupDevCountAct(int groupid) {
		int c = 0;
		synchronized (actDevList) {
			for (deviceinfo di : actDevList.values()) {
				if (di.getAlloc_type() == ServerConfig.adv_alloc_type_group) {
					if (di.getAlloc_adv() == groupid)
						c++;
				}
			}
		}
		return c;
	}

	/**
	 * 取单独的广告任务已经分配的活跃设备数
	 * 
	 * @param adv_id
	 * @param alloc_type
	 * @return
	 */
	public int getAllocedDevCountAct(int adv_id, int alloc_type) {
		int c = 0;
		synchronized (actDevList) {
			for (deviceinfo di : actDevList.values()) {
				if (di.getAlloc_type() == alloc_type) {
					if (di.getAlloc_adv() == adv_id)
						c++;
				}
			}
		}
		return c;
	}

	public void allocatDeviceForGroup(int groupid, int count) {
		if (this.freeCount < count)
			return;
		ArrayList<deviceinfo> list = new ArrayList<deviceinfo>();
		list.addAll(actDevList.values());
		Collections.sort(list);

		// synchronized (actDevList) {
		// Collections.sort(actDevList);
		for (deviceinfo e : list) {
			if (e.isFree()) {
				// e.backup();
				e.setAlloc_adv(groupid);
				e.setAlloc_type(ServerConfig.adv_alloc_type_group);
				advgroup g = advgroup_service.getInstance().getGroup(groupid);
				e.setExt(g.getAdvids());
				e.setAlloctime(new Date());
				deviceinfodao.updateDeviceInfo(e);
				// 写日志表
				advallocdevlog log = new advallocdevlog();
				log.setAdvid(groupid);
				log.setDev_tag(e.getDevice_tag());
				log.setAlloc_type(e.getAlloc_type());
				log.setAlloc_time(new Date());
				advallocdevlogdao.addAllocLog(log);
				count--;
				if (count == 0)
					break;
			}
		}
		// }
	}

	/**
	 * 因为活跃设备不足，重新指定新的活跃设备到这个广告组，并清除已经分配但没有活跃的设备状态 *
	 * 
	 * @param groupid
	 * @param count
	 */
	public void reallocatDeviceForGroup(int groupid, int count) {
		if (this.freeCount < count)
			return;
		int c = count;
		ArrayList<deviceinfo> list = new ArrayList<deviceinfo>();
		list.addAll(actDevList.values());
		Collections.sort(list);
		// synchronized (actDevList) {
		// Collections.sort((List<deviceinfo>) actDevList.values());
		for (deviceinfo e : list) {
			if (e.isFree()) {
				// e.backup();
				e.setAlloc_adv(groupid);
				e.setAlloc_type(ServerConfig.adv_alloc_type_group);
				advgroup g = advgroup_service.getInstance().getGroup(groupid);
				e.setExt(g.getAdvids());
				e.setAlloctime(new Date());
				deviceinfodao.updateDeviceInfo(e);
				// 写日志表
				advallocdevlog log = new advallocdevlog();
				log.setAdvid(groupid);
				log.setDev_tag(e.getDevice_tag());
				log.setAlloc_type(e.getAlloc_type());
				log.setAlloc_time(new Date());
				log.setFree(0);
				advallocdevlogdao.addAllocLog(log);
				count--;
				if (count == 0)
					break;
			}
		}
		// }
		// 把没有活跃的设备的状态清除
		synchronized (allDevList) {
			for (deviceinfo e : this.allDevList.values()) {
				boolean act_now = false;
				devactstatus devstauts = e.getDevactStatus();
				if(devstauts != null && devstauts.getOnline() == 1)
					act_now = true;
				if (e.getAlloc_type() == ServerConfig.adv_alloc_type_group && e.getAlloc_adv() == groupid
						&& act_now == false) {
					e.backup();
					e.setAlloc_type(0);
					e.setAlloc_adv(0);
					e.setExt("");
					// e.setAdvstatus_reset(1);
					deviceinfodao.updateDeviceInfo(e);
					advallocdevlog log = new advallocdevlog();
					log.setAdvid(groupid);
					log.setDev_tag(e.getDevice_tag());
					log.setAlloc_type(ServerConfig.adv_alloc_type_group);
					log.setFree(1);
					log.setAlloc_time(new Date());
					advallocdevlogdao.addAllocLog(log);
					c--;
					if (c == 0) {
						break;
					}
				}
			}
		}
	}

	/**
	 * 由于广告数量或时间变化，之前分配的设备太多，需要释放多余的设备 最好先排序，把最后分配的机器优先释放
	 * 
	 * @param groupid
	 * @param count
	 */
	public void freeDeviceForGroup(int groupid, int count) {
		ArrayList<deviceinfo> list = new ArrayList<deviceinfo>();
		list.addAll(allDevList.values());
		Collections.sort(list);
		// 先释放最迟注册的
		// synchronized (allDevList) {
		for (deviceinfo e : list) {
			// 手工分配，不能释放
			if(e.getHand_locked() == 1)
				continue;
			if (e.getAlloc_type() == ServerConfig.adv_alloc_type_group && e.getAlloc_adv() == groupid) {
				e.backup();
				e.setAlloc_type(0);
				e.setAlloc_adv(0);
				e.setExt("");
				// e.setAdvstatus_reset(1);
				deviceinfodao.updateDeviceInfo(e);
				advallocdevlog log = new advallocdevlog();
				log.setAdvid(groupid);
				log.setDev_tag(e.getDevice_tag());
				log.setAlloc_type(ServerConfig.adv_alloc_type_group);
				log.setFree(1);
				log.setAlloc_time(new Date());
				advallocdevlogdao.addAllocLog(log);
				if (count > 0) {
					count--;
					if (count == 0)
						break;
				}
			}
		}
		// }
	}

	/**释放单个广告设备，重置deviceinfo表的alloc_type、alloc_adv为0
	 * 同时在[tb_advallocdevlog]表中插入释放设备记录
	 * @param adv_id 要释放的广告id
	 * @param count 要释放机器数，count<=0时，从所有设备中释放
	 * @param alloc_type 要释放的广告类型
	 */
	public void freeDeviceForSingleAdv(int adv_id, int count, int alloc_type) {
		ArrayList<deviceinfo> list = new ArrayList<deviceinfo>();
		list.addAll(allDevList.values());
		Collections.sort(list);
		// 先释放最迟注册的
		// synchronized (allDevList) {
		for (deviceinfo e : list) {
			// 手工分配，不能释放
			if(e.getHand_locked() == 1) {
				continue;
			}
			
			if(e.getDevice_tag().equals("D6002")) {
				System.out.println(123);
			}
			
			if (e.getAlloc_type() == alloc_type) {
				if (e.getAlloc_adv() == adv_id) {
					e.backup();
					e.setAlloc_type(0);
					e.setAlloc_adv(0);
					e.setExt("");
					// setAlloc_type已经设置advStatus了，
					// 这里不需要再调用e.setAdvstatus_reset(1);
					deviceinfodao.updateDeviceInfo(e);
					advallocdevlog log = new advallocdevlog();
					log.setAdvid(adv_id);
					log.setDev_tag(e.getDevice_tag());
					log.setAlloc_type(alloc_type);
					log.setFree(1);
					log.setAlloc_time(new Date());
					advallocdevlogdao.addAllocLog(log);
					if (count > 0) {
						count--;
						if (count == 0)
							break;
					}
				}
			}
		}
		// }
	}
	
	
	/**设备很久没有登录，重置alloc_type,alloc_adv为0，handlock除外
	 * @param device
	 */
	public void freeSingleDeviceForSingleAdv(deviceinfo device) {
		if(device.getHand_locked() == 1) {
			return;
		}
		advallocdevlog log = new advallocdevlog();
		log.setAdvid(device.getAlloc_adv());
		log.setDev_tag(device.getDevice_tag());
		log.setAlloc_type(device.getAlloc_type());
		log.setFree(2);
		log.setAlloc_time(new Date());
		advallocdevlogdao.addAllocLog(log);
		
		device.backup();
		device.setAlloc_type(0);
		device.setAlloc_adv(0);
		device.setExt("");
//		以前：refreshdevactstaus时，发现通讯中断，重置alloc_adv,alloc_type=0
//		现在：发现通讯中断，也不重置alloc_adv,alloc_type,当它自己重新上线后，仍然做任务
//		deviceinfodao.updateDeviceInfo(device);
	}
	
	
	
	/**
	 * 为新广告(非组)分配空闲机器
	 * 
	 * @param ai
	 */
	public void allocatDeviceForSigleAdv(int adv_id, int count, int alloc_type) {
		if (this.freeCount < count)
			return;
		ArrayList<deviceinfo> list = new ArrayList<deviceinfo>();
		list.addAll(actDevList.values());
		Collections.sort(list);
		// synchronized (actDevList) {
		// Collections.sort(actDevList);
		for (deviceinfo e : list) {
			// log.info(e.toString());
			if(e.getHand_locked() == 1)
				continue;
			if (e.isFree()) {
				e.setAlloc_type(alloc_type);
				e.setAlloc_adv(adv_id);
				e.setExt("" + adv_id);
				e.setAlloctime(new Date());
				// e.setAdvstatus_reset(1);
				deviceinfodao.updateDeviceInfo(e);
				advallocdevlog log = new advallocdevlog();
				log.setAdvid(adv_id);
				log.setDev_tag(e.getDevice_tag());
				log.setAlloc_type(alloc_type);
				log.setAlloc_time(new Date());
				advallocdevlogdao.addAllocLog(log);
				count--;
				if (count == 0)
					break;
			}
		}
		// }

		/*
		 * int lineTimeId = -1; String remRule = ""; int dayCount =
		 * ai.getDaycount(); if (dayCount <= 0) { log.error("adv task:" +
		 * ai.getAdv_id() + " 日新增为0"); return; } // lineTimeId = if
		 * (ai.getApkid() > 0) { lineTimeId = ai.getTimeline(); } if
		 * (ai.getRemaintime() > 0) { remRule = ai.getRemain_rule(); } if
		 * (remRule == null) remRule = ""; int remCountPec = 0; int[] month_days
		 * = new int[serverconfig.max_remain_days]; if (remRule.length() > 0)
		 * remCountPec = caclMothRemainByRule(remRule,month_days); // 需要设备数 int
		 * reqdevcount = -1; int hour_count_per = 0;// 一台设备每小时能做的最大数量 int
		 * day_count_per = 0;// 一台设备每天能做的最大数量 // 计算每台设备在曲线和非曲线下最大能做多少量 //
		 * 一个广告和留存所占用的时间 int t = ai.getRequesttime() + remCountPec *
		 * ai.getRemaintime() / 100; hour_count_per = 3600 / t; if
		 * (hour_count_per == 0) { log.error("adv task:" + ai.getAdv_id() +
		 * " 占用时长太大"); return; } if (lineTimeId == -1) { // 非曲线 // 一台设备一天能做数量
		 * day_count_per = hour_count_per * 24; reqdevcount = dayCount /
		 * day_count_per; } else { // 有曲线 day_count_per =
		 * timeline_service.getInstance().getDayCountOnTimeLine(lineTimeId,
		 * hour_count_per); reqdevcount = dayCount / day_count_per; } if
		 * (reqdevcount < 1) reqdevcount = 1; log.info("adv task:" +
		 * ai.getAdv_id() + " 需要设备数：" + reqdevcount); if (this.freeCount <
		 * reqdevcount) { log.error("没有足够设备分配给广告" + ai.getAdv_id()); return; }
		 * for (deviceinfo e : this.actDevList) { if (reqdevcount == 0) break;
		 * if (e.isFree()) { e.setExt("" + ai.getAdv_id());
		 * deviceinfodao.updateDeviceInfo(e); // 写日志表 advallocdevlog log = new
		 * advallocdevlog(); log.setAdvid(ai.getAdv_id());
		 * log.setDev_tag(e.getDevice_tag()); log.setGrouptype(0);
		 * log.setAlloc_time(new Date()); advallocdevlogdao.addAllocLog(log);
		 * reqdevcount--; } }
		 */
	}

	/**
	 * 为一组准备上线的广告分配机器,之前定义过，广告组不需要曲线和留存计算
	 * 
	 * @param group
	 */
	// public void allocatDeviceforGroup(advgroup group) {
	// int reqdevcount = group.getMaxDoTimeOfHour() / 3600;
	// if (reqdevcount < 1)
	// reqdevcount = 1;
	// log.info("新广告组:" + group.getGroupid() + " 需要设备数：" + reqdevcount);
	// if (this.freeCount < reqdevcount) {
	// log.error("没有足够设备分配给广告组:" + group.getGroupid());
	// return;
	// }
	// for (deviceinfo e : this.actDevList) {
	// if (reqdevcount == 0)
	// break;
	// if (e.isFree()) {
	// e.setExt("" + group.getGroupid());
	// e.setAlloc_group(1);
	// deviceinfodao.updateDeviceInfo(e);
	// // 写日志表
	// advallocdevlog log = new advallocdevlog();
	// log.setAdvid(group.getGroupid());
	// log.setDev_tag(e.getDevice_tag());
	// log.setGrouptype(1);
	// log.setAlloc_time(new Date());
	// advallocdevlogdao.addAllocLog(log);
	// reqdevcount--;
	// }
	// }
	// }

	public int getFreeCount() {
		return freeCount;
	}

	public void setFreeCount(int freeCount) {
		this.freeCount = freeCount;
	}

	public int getAllCount() {
		return allCount;
	}

	public void setAllCount(int allCount) {
		this.allCount = allCount;
	}

	public JSONObject devRegist(JSONObject request) {
		error_result er = new error_result();
		deviceinfo di = getDeviceInfoInAll(request.optString("dev_tag"));
		try {
			// new deviceinfo();
			if (di != null) {
				er.setErr_code(error_result.device_regist_alreadyexist);
				er.setErr_info("设备已经注册");
				return er.toJSONObject();
			}
			di = new deviceinfo();
			di.setDevice_tag(request.optString("dev_tag"));
			di.setPhone_type(request.optString("phonetype"));
			di.setClientid(request.optInt("cid"));
			deviceinfodao.addDeviceInfo(di, er);

			if (er.getErr_code() == 0)
				er.setErr_info("注册成功");
//			devactlog devLog = new devactlog();
//			devLog.setDev_tag(di.getDevice_tag());
//			devLog.setDevstatus("regist");
//			devLog.setLastlogintime(new Date());
//			devactlogdao_nouse.insertDevActTime(devLog);
			return er.toJSONObject();
		} catch (JSONException e) {
			ErrorLog_service.system_errlog(e);	
			log.error(e.getMessage(), e);
		}
		return null;
	}
	

	public JSONObject deviceLogin(JSONObject request) {
		JSONObject response = new JSONObject();
		error_result er = new error_result();
		boolean isInActList = false;
		String dev_tag = request.optString("dev_tag");
//		log.info("device 登录:" + dev_tag);
		deviceinfo di = this.getDeviceInfoInAll(dev_tag);
		try {
			setupLoginServerParams(dev_tag,response);
			if (di == null) {
				er.setErr_code(error_result.device_not_exist);
				er.setErr_info("设备" + dev_tag + " 在数据库中不存在,请先注册设备");
				ErrorLog_service.system_errlog(getClass().getName()+er.getErr_info());
				log.error(er.getErr_info());
				response.put("err_result", er.toJSONObject());
				return response;
			}
			response.put("err_result", er.toJSONObject());
			// log.info(dev_tag + ":登录");
			// this.getDeviceInfo(dev_tag);
			synchronized (actDevList) {
				if (actDevList.get(dev_tag) != null) {
					isInActList = true;
//					log.info("在活跃列表中:" + dev_tag);
				}
			}

			/*
			 * if (isInActList == false) { log.info("不在活跃设备列表中"); di =
			 * this.getDeviceInfoInAll(dev_tag); if (di == null) {
			 * er.setErr_code(error_result.System_sql); er.setErr_info("设备" +
			 * dev_tag + " 在数据库中不存在,请先注册设备"); log.error(er.getErr_info());
			 * response.put("err_result", er.toJSonObject()); return response; }
			 * 
			 * // log.info(di.toString()); //
			 * 在加入actDevList表前设置好登录时间，上次没有设置时，由于处于调试状态，
			 * 结果在检查心跳中LastLoginTime为null抛异常 //
			 * di.setLastLoginId(request.optLong("loginid")); //
			 * di.setLastLoginTime(new Date()); // di.setAct_now(true); //
			 * synchronized(actDevList){ // this.actDevList.add(di); // } }
			 */
			// log.info(di.toString());
			di.setLastLoginId(request.optLong("loginid"));
			//di.setLastLoginTime(new Date());
			devactstatus devstauts = di.getDevactStatus();			
			if(devstauts == null)
			{
				//e.setLastLoginTime(new Date());
				devstauts = devactstatusdao.getDevActStatus(di.getDevice_tag());
				if(devstauts == null){
				  devstauts = new devactstatus();
				  devstauts.setDev_tag(di.getDevice_tag());
				  devactstatusdao.addNewDevActStatus(devstauts);
				}
				//devstauts = new devactstatus();				
				di.setDevactStatus(devstauts);
			}
			devstauts.setLastlogintime(new Date());
			devstauts.setOnline(1);
			devstauts.setStatus("正常登录");
			devactstatusdao.updateLastLoginTime(devstauts);
			//di.setAct_now(true);
			// log.info(di.toString());
			if (isInActList == false) {
				synchronized (actDevList) {
					this.actDevList.put(di.getDevice_tag(), di);
				}
			}
//			devactlog dl = new devactlog();
//			dl.setDev_tag(di.getDevice_tag());
//			dl.setLastlogintime(di.getLastLoginTime());
//			dl.setDevstatus("ok");
//			devactlogdao.updateDevActTime(dl);
			// log.info("response:" + response.toString());
		} catch (Exception e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		}
		return response;
	}

	/**
	 * 加上配置的信息
	 */
	private void setupLoginServerParams(String dev_tag,JSONObject response) throws JSONException {
		
		JSONObject configJson = ServerConfig.getConfigJson();
		
		// Logs
		response.put("log_config", configJson.optJSONObject("log_config"));
		
		// TimeMillis
		int loginDaemonInterval = configJson.optInt("loginDaemonInterval", 30);
		response.put("loginDaemonInterval", loginDaemonInterval);
		
		int taskDaemonInterval = configJson.optInt("taskDaemonInterval", 1);
		response.put("taskDaemonInterval", taskDaemonInterval);
		
		int taskDaemonWaitInterval = configJson.optInt("taskDaemonWaitInterval", 5);
		response.put("taskDaemonWaitInterval", taskDaemonWaitInterval);
		
		int taskDaemonExceptionInterval = configJson.optInt("taskDaemonExceptionInterval", 30);
		response.put("taskDaemonExceptionInterval", taskDaemonExceptionInterval);
		
		// URLS
		String baseURL = ServerConfig.getRooturl();
		
		JSONObject urlsJsonObject = new JSONObject();
		response.put("urls", urlsJsonObject);
		
//		urlsJsonObject.put("baseURL", baseURL);
		urlsJsonObject.put("taskRequestPath", "/taskcenter");
		urlsJsonObject.put("taskReportPath", "/taskreport");
		urlsJsonObject.put("loginPath", "/devlogin");
		urlsJsonObject.put("loggerPath", "/DeviceLogMessage");
	}

}
