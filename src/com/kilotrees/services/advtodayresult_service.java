package com.kilotrees.services;

import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.kilotrees.dao.advtodayresultdao;
import com.kilotrees.model.bo.TaskBase;
import com.kilotrees.model.bo.TaskCPARemain;
import com.kilotrees.model.bo.ad_task_report;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.advtodayresult;

/**
 * 当天广告结果状态处理，当重新开机时，要从数据库中导入今天日期的所有广告结果状态，０点时清除各种状态，重新计算
 * 每个设备做完新增或留存时，都调用此服务，更新对应广告的状态．
 *　这里用更小的表来统计的方式记录每个任务当天状态，不需要每次查询新增日志和留存日志表(这２个表太大)
 * @author Administrator
 *
 */
/**
 * @author Administrator
 * 2019年4月19日 下午6:14:29 
 */
public class advtodayresult_service {
	private static Logger log = Logger.getLogger(advtodayresult_service.class);

	private static advtodayresult_service inst;

	private HashMap<String, advtodayresult> resultList = new HashMap<String, advtodayresult>();

	private advtodayresult_service() {

	}

	public static advtodayresult_service getInstance() {
		synchronized(advtodayresult_service.class){
			if (inst == null) {
				inst = new advtodayresult_service();
			}
		}
		return inst;
	}

	public void refresh() {
		// 清空当天执行结果
		if (!main_service.getInstance().isSystem_ready()) {
			HashMap<String, advtodayresult> ls = advtodayresultdao.getAdvTodayResultSet();
			if (ls != null) {
				// 服务器重启时设置广告状态
				synchronized (resultList) {
					resultList = ls;
				}
				resetAdvResult();
			}
		} else {
			// do nothing
		}
	}

	void resetAdvResult() {
		advnewtask_service.getInstance().resetAdvResult();
		advremaintask_service.getInstance().resetAdvResult();
	}

	public advtodayresult getAdvtodayResult(int advid, int isRemain) {
		synchronized (resultList) {
			String key = "" + advid + "_" + isRemain;
			return resultList.get(key);
		}
	}

	/**
	 * 设备新增任务完成
	 * 
	 * @param dev_tag
	 * @param adv_id
	 * @param isSuccess
	 */
	public void advNewResultReport(ad_task_report report) {
		advNewResultReportForParent(report);
		boolean bFound = false;
		advtodayresult adr = null;
		String key = "" + report.getTask().getAdv_id() + "_0";
		advtodayresult e = null;
		synchronized (resultList) {
			e = resultList.get(key);
		}
		if (e != null) {
			bFound = true;
			if (report.getResult() == ServerConfig.result_success_flag){
				//2018-10-25，如果第二次留存活跃，不增加成功数
				//2019-1-6 不再区分二次活跃，由下面advNewResultReportForSecondOpenu单独处理
				//if(report.getTask().getRid() == 0)
				e.setNewuser_success_count(e.getNewuser_success_count() + 1);
				e.setNewuser_success_opentcount(e.getNewuser_success_opentcount() + 1);
			}
			else{
				//if(report.getTask().getRid() == 0)
				e.setNewuser_err_count(e.getNewuser_err_count() + 1);
				e.setNewuser_err_opentcount(e.getNewuser_err_opentcount() + 1);
			}
			e.setResult_time(report.getReportTime());
			advtodayresultdao.updateAdvTodayResult(e);
			adr = e;
		}
		//跨日时可能有微小差别
		if (!bFound /*&& report.getTask().getRid() == 0*/) {
			adr = new advtodayresult();
			adr.setAdvid(report.getTask().getAdv_id());
			if (report.getResult() == ServerConfig.result_success_flag){
				//if(!(report.getTask().getTaskid() == -9 && report.getTask().isNewuser_today()))
				adr.setNewuser_success_count(1);
				adr.setNewuser_success_opentcount(1);
			}
			else{
				//if(!(report.getTask().getTaskid() == -9 && report.getTask().isNewuser_today()))
				adr.setNewuser_err_count(1);
				adr.setNewuser_err_opentcount(1);
			}
			adr.setResult_time(report.getReportTime());
			synchronized (resultList) {
				resultList.put(adr.getKey(), adr);
			}
			advtodayresultdao.newAdvTodayResult(adr);
		}
		//if(report.getTask().getRid() == 0)
		//if(report.getTask().getRid() == 0)
		advnewtask_service.getInstance().advNewTaskResultReport(report, adr);
	}
	/**更新todayResult表做新增多次打开的opentcount结果
	 * @param report
	 */
	public void advNewResultReportForSecondOpen(ad_task_report report) {
		//boolean bFound = false;
		//advtodayresult adr = null;
		String key = "" + report.getTask().getAdv_id() + "_0";
		advtodayresult e = null;
		synchronized (resultList) {
			e = resultList.get(key);
		}
		if (e != null) {			
			if (report.getResult() == ServerConfig.result_success_flag){
				//2018-10-25，如果第二次留存活跃，只增加成功打开数数				
				e.setNewuser_success_opentcount(e.getNewuser_success_opentcount() + 1);
			}
			else{
//				if(report.getTask().getRid() == 0)
//					e.setNewuser_err_count(e.getNewuser_err_count() + 1);
				e.setNewuser_err_opentcount(e.getNewuser_err_opentcount() + 1);
			}
			e.setResult_time(report.getReportTime());
			advtodayresultdao.updateAdvTodayResult(e);
			//adr = e;
		}
	}
	/**
	 * 设置充值任务结果集
	 * @param report
	 */
	public void advNewResultReportForParent(ad_task_report report) {
		if(report.getTask().getParent_advid() <= 0)
			return;
		boolean bFound = false;
		advtodayresult adr = null;
		int adv_id = report.getTask().getParent_advid();
		String key = "" + adv_id + "_0";
		advtodayresult e = null;
		synchronized (resultList) {
			e = resultList.get(key);
		}
		if (e != null) {
			bFound = true;
			if (report.getResult() == ServerConfig.result_success_flag){
				//2018-10-25，如果当天第二次以上的留存活跃，不增加用户成功数
				//if(!(report.getTask().getTaskid() == -9 && report.getTask().isNewuser_today()))
					e.setNewuser_success_count(e.getNewuser_success_count() + 1);
				e.setNewuser_success_opentcount(e.getNewuser_success_opentcount() + 1);
			}
			else{
				//if(!(report.getTask().getTaskid() == -9 && report.getTask().isNewuser_today()))
					e.setNewuser_err_count(e.getNewuser_err_count() + 1);
				e.setNewuser_err_opentcount(e.getNewuser_err_opentcount() + 1);
			}
			e.setResult_time(report.getReportTime());
			advtodayresultdao.updateAdvTodayResult(e);
			adr = e;
		}
		//跨日时可能有微小差别
		if (!bFound && report.getTask().getTaskid() != -9) {
			adr = new advtodayresult();
			adr.setAdvid(adv_id);
			if (report.getResult() == ServerConfig.result_success_flag){
				//if(!(report.getTask().getTaskid() == -9 && report.getTask().isNewuser_today()))
					adr.setNewuser_success_count(1);
				adr.setNewuser_success_opentcount(1);
			}
			else{
				//if(!(report.getTask().getTaskid() == -9 && report.getTask().isNewuser_today()))
				adr.setNewuser_err_count(1);
				adr.setNewuser_err_opentcount(1);
			}
			adr.setResult_time(report.getReportTime());
			synchronized (resultList) {
				resultList.put(adr.getKey(), adr);
			}
			advtodayresultdao.newAdvTodayResult(adr);
		}		
		advnewtask_service.getInstance().advNewTaskParentResultReport(report, adr);
	}

	/**
	 * 设备留存任务完成．
	 * 
	 * @param dev_tag
	 * @param rid
	 * @param adv_id
	 * @param isSuccess
	 */
	public void advRemainResultReport(ad_task_report report) {
		boolean bFound = false;
		//modify 2018-10-25 如果这个是当天新增第二次之后的打开,修改新增的打开数
		
		TaskBase task = report.getTask();
		TaskCPARemain taskRemain = (TaskCPARemain) task;
		
		if(taskRemain.isNewuser_today()) {
			advNewResultReportForSecondOpen(report);
		}
		//如果是充值任务
		advNewResultReportForParent(report);
		
		advtodayresult adr = null;
		String key = "" + taskRemain.getAdv_id() + "_1";
		advtodayresult e = null;
		synchronized (resultList) {
			e = resultList.get(key);
		}
		if (e != null) {
			bFound = true;
			if (report.getResult() == ServerConfig.result_success_flag){
				//success_count 只记录第一成功活跃，如果第一次不成功，dotoday是不减少的
				if(taskRemain.getDotoday() == taskRemain.getTodayopencount())
				{
					if(taskRemain.isNewuser_today())//当天做新增
					{
						e.setRemain_newuser_success_count(e.getRemain_newuser_success_count() + 1);
						e.setRemain_newuser_success_opentcount(e.getRemain_newuser_success_opentcount() + 1);
					}
					else{
						e.setRemain_olduser_success_count(e.getRemain_olduser_success_count() + 1);
						e.setRemain_olduser_success_opentcount(e.getRemain_olduser_success_opentcount() + 1);
					}
				}
				else
				{
					if(taskRemain.isNewuser_today()){
						e.setRemain_newuser_success_opentcount(e.getRemain_newuser_success_opentcount() + 1);
					}
					else{
						e.setRemain_olduser_success_opentcount(e.getRemain_olduser_success_opentcount() + 1);
					}
				}
			//	e.setr
//					e.setSuccess_count(e.getSuccess_count() + 1);
//				e.setSuccess_opentcount(e.getSuccess_opentcount() + 1);
			}
			else{
				e.setRemain_err_opentcount(e.getRemain_err_opentcount() + 1);
				
//				if(report.getTask().getDotoday() == report.getTask().getDayopencount())
//					e.setErr_count(e.getErr_count() + 1);
//				e.setErr_opentcount(e.getErr_opentcount() + 1);
				
			}
			e.setResult_time(new Date());
			advtodayresultdao.updateAdvTodayResult(e);
			adr = e;
		}
		//跨日时可能有微小差别
		if (!bFound) {
			adr = new advtodayresult();
			adr.setIsremain(1);
			adr.setAdvid(taskRemain.getAdv_id());
			if (report.getResult() == ServerConfig.result_success_flag){
				if(taskRemain.isNewuser_today()){
					adr.setRemain_newuser_success_count(1);
					adr.setRemain_newuser_success_opentcount(1);
				}
				else{
					adr.setRemain_olduser_success_count(1);
					adr.setRemain_olduser_success_opentcount(1);
				}
			}
			else{
				adr.setRemain_err_opentcount(1);
				//adr.setErr_opentcount(1);
			}
			adr.setResult_time(new Date());
			adr.setIsremain(1);
			synchronized (resultList) {
				resultList.put(adr.getKey(), adr);
			}
			advtodayresultdao.newAdvTodayResult(adr);
		}
		advremaintask_service.getInstance().advRemainTaskResultReport(report, adr);
	}
}
