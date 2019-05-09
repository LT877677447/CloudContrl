package com.kilotrees.model.bo;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.kilotrees.model.po.advremaintask;
import com.kilotrees.util.DateUtil;

public class TaskCPARemain extends TaskBase {

	// 留存表的rid
	private int rid;

	// 给客户端传入从开始新增到现在的留存天数，客户端或服务器动态调整活跃时间
	private int remain_sep_days;

	// modify 2018-10-24增加dayopencount和留存中的dotoday和newuser_today
	private int todayopencount = 0;
	private int dotoday;
	private boolean newuser_today;
	
	public TaskCPARemain() {
		super();
		
		this.setTaskPhase(TaskBase.TASK_PHASE_REMAIN);
		this.setTaskType("CPA");
	}

	public int getRid() {
		return rid;
	}

	public void setRid(int rid) {
		this.rid = rid;
	}

	public int getRemain_sep_days() {
		return remain_sep_days;
	}

	public void setRemain_sep_days(int remain_sep_days) {
		this.remain_sep_days = remain_sep_days;
	}

	public int getTodayopencount() {
		return todayopencount;
	}

	public void setTodayopencount(int todayopencount) {
		this.todayopencount = todayopencount;
	}

	public int getDotoday() {
		return dotoday;
	}

	public void setDotoday(int dotoday) {
		this.dotoday = dotoday;
	}

	public boolean isNewuser_today() {
		return newuser_today;
	}

	public void setNewuser_today(boolean newuser_today) {
		this.newuser_today = newuser_today;
	}

	/**
	 * 设置留存任务
	 * 
	 * @param remainTask
	 *            要设置为留存任务的advremaintask对象
	 */
	public void setRemTask(advremaintask remainTask) {
		this.setTaskid(TASK_ID_REMAIN_TASK);

		this.setRid(remainTask.getRid());
		this.setAutoid(remainTask.getAutoid());
		this.setAdv_id(remainTask.getAdv_id());
		// this.setAdv_type(remainTask.getAdv_type());

		this.setLock_dev(remainTask.getLock_dev());

		try { 
			this.setPhoneInfo(new JSONObject(remainTask.getPhoneInfo()));
			this.setAppInfo(new JSONObject(remainTask.getAppinfo()));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		this.setTodayopencount(remainTask.getTodayopencount());
		this.setDotoday(remainTask.getDotoday());
		this.setNewuser_today(remainTask.isNewuser_today());
		
		int adv_id = remainTask.getAdv_id();

		if (remainTask.getNewregtime() != null) {
			this.setRemain_sep_days(DateUtil.differDayQty(remainTask.getNewregtime(), new Date()));
		}
	}
}
