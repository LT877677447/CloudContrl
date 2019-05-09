package com.kilotrees.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.action.TaskActionHandler;
import com.kilotrees.dao.advallocdevlogdao;
import com.kilotrees.dao.apkinfodao;
import com.kilotrees.dao.devactstatusdao;
import com.kilotrees.dao.deviceinfodao;
import com.kilotrees.model.bo.TaskBase;
import com.kilotrees.model.bo.ad_task_report;
import com.kilotrees.model.bo.error_result;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.advallocdevlog;
import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.model.po.apkinfo;
import com.kilotrees.model.po.devactstatus;
import com.kilotrees.model.po.deviceinfo;
import com.kilotrees.serverbean.ServerBeanBase;
import com.kilotrees.service.adv.runtime.api.ITaskRuntime;
import com.kilotrees.util.JSONObjectUtil;

/**
 * 广告派发分配中心，功能：
 * 
 * 2:设备请求任务首先进入这里，分配好任务
 * 3:我们这套云控系统，最终是做安桌soe的为主，一般app的cpa广告为辅，但开始时以cpa开展，尽量做到细致稳定．
 * 
 */
public class adtaskdispath_center {
	private static Logger log = Logger.getLogger(adtaskdispath_center.class);
	private static adtaskdispath_center instance = null;

	public static adtaskdispath_center getInstance() {
		if (instance != null) {
			return instance;
		}
		synchronized (adtaskdispath_center.class) {
			if (instance == null) {
				instance = new adtaskdispath_center();
			}
		}
		return instance;
	}

	/**
	 * 设备请求任务 1:如果设备处于任务清理状态，先清理任务 2:如果分配了留存任务，优先做留存 3:看是否分配新增任务
	 */
	public JSONObject handleTaskRequest(JSONObject request) throws IOException {
		log.info("请求任务:" + request.toString());

		JSONObject response = new JSONObject();
		JSONArray prefix_task_actions = new JSONArray();
		JSONArray suffix_task_actions = new JSONArray();
		error_result errorResult = new error_result();

		String dev_tag = request.optString("dev_tag");

		/**
		 * 检查是否需要更新RomFiles及RomServer
		 */
		if (checkUpdateRomServer(request, response)) {
			log.warn("升级romserver");
			return response;
		}
		if (checkUpdateRomFiles(request, response)) {
			log.warn("升级romfiles");
			return response;
		}

		/**
		 * 检查设备是否存在
		 */
		deviceinfo devInfo = actdeviceinfo_service.getInstance().getDeviceInfoInAll(dev_tag);
		if (devInfo == null) {
			try {
				errorResult.setErr_code(error_result.device_not_exist);
				errorResult.setErr_info("找不到设备:" + dev_tag);
				response.put("err_result", errorResult.toJSONObject());
			} catch (JSONException e) {
				ErrorLog_service.system_errlog(e);
				log.error(e.getMessage(), e);
			}
			ErrorLog_service.system_errlog(getClass().getName() + "找不到设备,dev_tag=" + dev_tag);
			log.error("找不到设备,dev_tag=" + dev_tag);
			return response;
		}

		/**
		 * 更新设备device状态
		 */
		devactstatus devstauts = devInfo.getDevactStatus();
		if (devstauts == null) {
			devstauts = devactstatusdao.getDevActStatus(devInfo.getDevice_tag());
			if (devstauts == null) {
				devstauts = new devactstatus();
				devstauts.setDev_tag(devInfo.getDevice_tag());
				devactstatusdao.addNewDevActStatus(devstauts);
			}
			devInfo.setDevactStatus(devstauts);
		}
		devstauts.setLastfetchtasktime(new Date());
		devactstatusdao.updateLastFetchTaskTime(devstauts);

		/**
		 * 检查客户端的请求唯一ID:orderid
		 */
		long orderid = request.optInt("orderid");
		if (orderid <= devInfo.getLastReqOrderId()) {
			// 重复发送,这里orderid由客户端生成，最少为1,但如果客户端重启而服务器没有重启，就会出错,
			// 客户第一次登录时，要带上一个login_orderid,如果发现此id=1，重置di的lastorderid和lastreportid
			try {
				errorResult.setErr_code(error_result.orderid_same_as_last);
				errorResult.setErr_info("重复发送orderid:" + orderid);
				response.put("err_result", errorResult.toJSONObject());
				response.put("lastorderid", devInfo.getLastReqOrderId());
			} catch (JSONException e) {
				ErrorLog_service.system_errlog(e);
				log.error(e.getMessage(), e);
			}
			ErrorLog_service.system_errlog(getClass().getName() + "重复发送请求,tag=" + dev_tag + ";orderid=" + orderid + ",di.LastReqOrderId=" + devInfo.getLastReqOrderId());
			log.error("重复发送请求,tag=" + dev_tag + ";orderid=" + orderid + ",di.LastReqOrderId=" + devInfo.getLastReqOrderId());
			return response;
		}
		// 给deviceinfo赋最后一个任务请求orderid
		devInfo.setLastReqOrderId(orderid);

		try {
			// 当广告状态(停新增，停留存或减少新增)，设备可能要换新的广告或置为空闲,服务器首先把这个位设为1
			// 如果这个数值不为0,那么服务器首先清理数据(卸载之前的旧广告)或安装新广告
			if (devInfo.getAdvstatus_reset() == 1) {
				// 删除旧安装的广告,添加taskactions

				// 广告分配改变时，保留上面ext内容，用于状态清理————之前广告旧分配标识,
				if (devInfo.getExt_old().length() > 0 && devInfo.getAlloc_type_old() > 0) {
					// 之前的任务id列表
					String[] advids = devInfo.getExt_old().split(";");
					for (int i = 0; i < advids.length; i++) {
						int adv_id = Integer.parseInt(advids[i]);
						ITaskRuntime advTaskRunTime = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(adv_id);
						if (advTaskRunTime == null) {
							continue;
						}
						advtaskinfo task = advTaskRunTime.getAdvinfo();
						if (task == null || task.getApkid() <= 0) {
							continue;
						}
						apkinfo ai = apkinfodao.getApkInfo(task.getApkid());
						if (ai == null) {
							continue;
						}
						String packagename = ai.getPackagename();
						JSONObject json = new JSONObject();
						json.put("action", "UNINSTALL_APP");
						json.put("packageName", packagename);
						prefix_task_actions.put(json);
					}
				}
				devInfo.setAdvstatus_reset(0);
			}

			TaskBase[] adtasks = null;

			/*
			 * 所有设备先执行例行任务
			 */
			TaskBase[] routineTasks = advnewtask_service.getInstance().fetchRoutineTasks(devInfo);

			/**
			 * 原则上按之前分配的广告id和类别来取广告
			 */
			if (devInfo.getAlloc_adv() > 0 && devInfo.getAlloc_type() > 0) {
				// Alloc_type：当前广告分配标识，1：分配单一广告;2:分配给广告组；3:分配给单一的留存广告;4:广告和留存同时分到此设备上;5:分配留存组(多个留存同时做)

				if (devInfo.getAlloc_type() == ServerConfig.adv_alloc_type_single) {// di.getAlloc_type()==1
					adtasks = advnewtask_service.getInstance().fetchTasks(devInfo);
				} else if (devInfo.getAlloc_type() == ServerConfig.adv_alloc_type_group) {// di.getAlloc_type()==2
					adtasks = advnewtask_service.getInstance().fetchTasks(devInfo);

				} else if (devInfo.getAlloc_type() == ServerConfig.adv_alloc_type_remain) {// di.getAlloc_type()==3
					// 通过设备信息提取到的留存任务
					adtasks = advremaintask_service.getInstance().fetchTasks(devInfo);

				} else if (devInfo.getAlloc_type() == ServerConfig.adv_alloc_type_lockremain) {// di.getAlloc_type()==4
					// 首先做留存，再做新增，如果留存数太多这里也有问题，会让新增很久都不能做

					boolean dorem = true;
					ITaskRuntime adr_rem = advremaintask_service.getInstance().getRemainRuntimeInfo(devInfo.getAlloc_adv());
					ITaskRuntime adr_new = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(devInfo.getAlloc_adv());

					if (adr_new != null && adr_rem != null) {
						// 新增有可能已经停止了，adr的getCurRetCount为null，这里会有空指针异常
						if (adr_new.isOffline() == false && adr_rem.getCurRetCount() < adr_new.getCurRetCount())
							dorem = false;
					}
					if (dorem) {
						adtasks = advremaintask_service.getInstance().fetchTasks(devInfo);
						// 虽然当前小时有留存要做，但今天活跃要半小时后才能做下一次，有可能没有取得任务，为了不让这半小时空闲，先执行新增
						if (adtasks.length == 0)
							adtasks = advnewtask_service.getInstance().fetchTasks(devInfo);
					}
					// if(adtasks.length == 0)
					else {
						adtasks = advnewtask_service.getInstance().fetchTasks(devInfo);
						if (adtasks.length == 0)
							adtasks = advremaintask_service.getInstance().fetchTasks(devInfo);
					}

				} else if (devInfo.getAlloc_type() == ServerConfig.adv_alloc_type_remain_group) {// di.getAlloc_type()==5
					adtasks = advremaintask_service.getInstance().fetchTasks(devInfo);
				}

				if (adtasks.length == 0) {
					// 当前是不是分配的设备的任务是不是按曲线任务或者冲量任务已经完成，如果是则可以临时分配其它广告
					// adtasks = allocUrgentTempTask(devInfo);
				}

			} else {
				// 2018-12-12 空闲没有分配任务的设备也要做剩下的任务
				// adtasks = allocUrgentTempTask(devInfo);
			}

			if (dev_tag.equals("B6012")) {
				log.debug("debug");
			}

			if (routineTasks != null && routineTasks.length > 0) {
				ArrayList<TaskBase> list = new ArrayList<TaskBase>();
				for (TaskBase e : routineTasks) {
					list.add(e);
				}
				for (TaskBase e1 : adtasks) {
					list.add(e1);
				}
				adtasks = new TaskBase[list.size()];
				list.toArray(adtasks);
			}

			// 配置前置后置动作action
			JsonActionService.createCommonActionJSON(prefix_task_actions, suffix_task_actions, adtasks);

			// 生成广告任务列表JSON
			JSONArray json_tasks = JsonTaskService.createCommonTaskJSON(adtasks, dev_tag);

			response.put("prefix_actions", prefix_task_actions);
			response.put("suffix_actions", suffix_task_actions);
			response.put("err_result", errorResult.toJSONObject());
			response.put("tasks", json_tasks);
			response.put("dev_tag", dev_tag);

			// 处理每个任务的特别逻辑
			for (int i = 0; adtasks != null && i < adtasks.length; i++) {
				TaskBase task = adtasks[i];
				String alias = task.getAlias();

				TaskActionHandler.requestLogic(alias, request, response);
			}

		} catch (JSONException e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		}

		return response;
	}

	private boolean checkUpdateRomServer(JSONObject request, JSONObject response) {
		int romserver_version = request.optInt("romserver_version", 0);
		JSONObject jsoExt = ServerConfig.getConfigJson();
		if (jsoExt != null) {
			JSONObject romserverJson = jsoExt.optJSONObject("update_rom_server");
			if (romserverJson != null) {
				int latestVersion = romserverJson.optInt("latest_version", 0);
				String latestUrl = romserverJson.optString("update_url", "");
				if (latestVersion > romserver_version && latestUrl.startsWith("http")) {
					try {
						response.put("update_rom_server", romserverJson);
						return true;
					} catch (JSONException ex) {
						ErrorLog_service.system_errlog(ex);
						log.error(ex.getMessage(), ex);
					}
				}
			}
		}
		return false;
	}

	private boolean checkUpdateRomFiles(JSONObject request, JSONObject response) {
		int romfiles_version = request.optInt("romfiles_version", 0);
		JSONObject jsoExt = ServerConfig.getConfigJson();
		if (jsoExt != null) {
			JSONObject romfilesJson = jsoExt.optJSONObject("update_rom_files");
			if (romfilesJson != null) {
				int latestVersion = romfilesJson.optInt("latest_version", 0);
				if (latestVersion > romfiles_version) {
					try {
						response.put("update_rom_files", romfilesJson);
						return true;
					} catch (JSONException ex) {
						ErrorLog_service.system_errlog(ex);
						log.error(ex.getMessage(), ex);
					}
				}
			}
		}
		return false;
	}

	/**
	 * 为空闲的设备临时分配广告任务并记录设备和分配记录信息
	 */
	private TaskBase[] allocUrgentTempTask(deviceinfo di) {
		// 查看哪些广告任务最为紧迫,一般为冲量任务和留存任务

		TaskBase[] ads = new TaskBase[0];
		// 先看当前小时剩余是否超过10分钟,少于10分钟则不做了
		// int curMin = Calendar.getInstance().get(Calendar.MINUTE);
		// if(curMin > 50)
		// return ads;

		// 2019-2-25:构造list容器，把当前小时做不完的新增、留存任务放进来，然后随机拿一个任务
		List<ITaskRuntime> list = new ArrayList<>();
		advnewtask_service.getInstance().getTempTaskByPrior(list);
		advremaintask_service.getInstance().getTempTaskByPrior(list);
		int size = list.size();
		if (size >= 1) {
			ITaskRuntime iTaskRunTime = list.get(new Random().nextInt(size));
			ads = iTaskRunTime.fetchTask(di.getDevice_tag());
		}
		// ads = advremaintask_service.getInstance().fetchUrgentTempTask(di);
		// if (ads.length == 0) {
		// ads = advnewtask_service.getInstance().fetchUrgentTempTask(di);
		// }

		for (TaskBase ad : ads) {
			// 设置临时记录字段，如果是留存，则负数表示
			int taskid = ad.getTaskid();
			int adv_id = ad.getAdv_id();
			if (taskid == -9)
				adv_id *= -1;
			di.addAllocAdvTemp(adv_id);
			// 2018-12-20 添加分配alloctime
			di.setAlloctime(new Date());
			deviceinfodao.updateDeviceInfo(di);

			advallocdevlog log = new advallocdevlog();
			log.setAdvid(adv_id);
			log.setDev_tag(di.getDevice_tag());
			log.setAlloc_type(ServerConfig.adv_alloc_type_temp);
			log.setAlloc_time(new Date());
			advallocdevlogdao.addAllocLog(log);
		}
		return ads;
	}

	/**
	 * 设备完成所有任务后，每个任务的报告请求
	 */
	public JSONObject handleTaskReport(JSONObject request) {
		// 如果服务器处理速度慢，比如调试时，一台设备会连续发几个状态报告，
		// 如果量很大时，可能也出现这种情况，这里用reportid跟orderid一样处理
		JSONObject response = new JSONObject();
		error_result err = new error_result();

		log.info("任务完成:" + JSONObjectUtil.getNewJsonWithoutKey(request, "phoneInfo").toString());

		String dev_tag = request.optString("dev_tag");
		long reportid = request.optLong("reportid");
		deviceinfo devInfo = actdeviceinfo_service.getInstance().getDeviceInfoInAll(dev_tag);
		if (devInfo != null) {
			if (reportid <= devInfo.getLastReportId()) {
				try {
					err = new error_result();
					err.setErr_code(error_result.reportid_same_as_last);
					err.setErr_info("重复发送reportid:" + reportid);
					response.put("err_result", err.toJSONObject());
					response.put("lastreportid", devInfo.getLastReportId());
				} catch (JSONException e) {
					ErrorLog_service.system_errlog(e);
					log.error(e.getMessage(), e);
				}
				ErrorLog_service.system_errlog(getClass().getName() + "重复发送报告,tag=" + dev_tag + ";reportid=" + reportid + ",di.getLastReportId=" + devInfo.getLastReportId());
				log.error("重复发送报告,tag=" + dev_tag + ";reportid=" + reportid + ",di.getLastReportId=" + devInfo.getLastReportId());
				return response;
			}
			devInfo.setLastReportId(reportid);
		}

		TaskBase task = null;
		try {
			String typeClass = request.optString("taskClass");
			task = (TaskBase) Class.forName(typeClass).newInstance();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		task.setWithJSONObject(request);

		advtaskinfo adtask = advnewtask_service.getInstance().getAdvTaskRunTimeInfo(task.getAdv_id()).getAdvinfo();
		task.setLock_dev(adtask.getRemain_lock_dev());

		int step = request.optInt("step");
		int result = request.optInt("result");
		String result_info = request.optString("result_info");

		ad_task_report report = new ad_task_report();
		report.setTask(task);
		report.setStep(step);
		report.setResult(result);
		report.setResult_info(result_info);
		report.setReportTime(new Date());

		// 新增地区和ip
		report.setArea(request.optString("area", ""));
		report.setIp(request.optString("ip", ""));

		int rid = request.optInt("rid");
		if (rid != 0) {
			advtodayresult_service.getInstance().advRemainResultReport(report);
		} else {
			advtodayresult_service.getInstance().advNewResultReport(report);
		}

		String alias = adtask.getAlias();
		TaskActionHandler.reportLogic(alias, request, response);

		try {
			ServerBeanBase sbean = (ServerBeanBase) ServerBeanBase.getServerBean(adtask.getServerbeanid());
			if (sbean != null) {
				sbean.handleTaskResport(request);
			}
			response.put("err_result", err.toJSONObject());
		} catch (JSONException e) {
			ErrorLog_service.system_errlog(e);
			log.error(e.getMessage(), e);
		}
		return response;
	}
}
