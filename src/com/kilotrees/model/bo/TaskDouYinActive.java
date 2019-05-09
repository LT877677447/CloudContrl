/**
 * @author Administrator
 * 2019年4月27日 下午6:10:39 
 */
package com.kilotrees.model.bo;

import com.kilotrees.model.po.advtaskinfo;

/**
 * @author Administrator 2019年4月27日 下午6:19:17
 */
public class TaskDouYinActive extends TaskBase {
	// private Integer autoid; in super
	private String phoneNumber;
	private String pass;
	private long registTime;
	private long firstFetchTime;
	private long lastFetchTime;
	private long lastLoginTime;
	// private String appinfo; in super
	// private String phoneInfo; in super
	private Integer status;
	private String comment;

	/**
	 * 
	 */
	public TaskDouYinActive() {
		super();
	}

	/**
	 * @param phoneNumber
	 * @param pass
	 * @param registTime
	 * @param firstFetchTime
	 * @param lastFetchTime
	 * @param lastLoginTime
	 * @param status
	 * @param comment
	 */
	public TaskDouYinActive(String phoneNumber, String pass, long registTime, long firstFetchTime, long lastFetchTime, long lastLoginTime, Integer status, String comment) {
		super();
		this.phoneNumber = phoneNumber;
		this.pass = pass;
		this.registTime = registTime;
		this.firstFetchTime = firstFetchTime;
		this.lastFetchTime = lastFetchTime;
		this.lastLoginTime = lastLoginTime;
		this.status = status;
		this.comment = comment;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getPass() {
		return pass;
	}

	public long getRegistTime() {
		return registTime;
	}

	public long getFirstFetchTime() {
		return firstFetchTime;
	}

	public long getLastFetchTime() {
		return lastFetchTime;
	}

	public long getLastLoginTime() {
		return lastLoginTime;
	}

	public Integer getStatus() {
		return status;
	}

	public String getComment() {
		return comment;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public void setRegistTime(long registTime) {
		this.registTime = registTime;
	}

	public void setFirstFetchTime(long firstFetchTime) {
		this.firstFetchTime = firstFetchTime;
	}

	public void setLastFetchTime(long lastFetchTime) {
		this.lastFetchTime = lastFetchTime;
	}

	public void setLastLoginTime(long lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "TaskDouYinActive [phoneNumber=" + phoneNumber + ", pass=" + pass + ", registTime=" + registTime + ", firstFetchTime=" + firstFetchTime + ", lastFetchTime="
				+ lastFetchTime + ", lastLoginTime=" + lastLoginTime + ", status=" + status + ", comment=" + comment + "]";
	}

	public void setTaskInfo(advtaskinfo advinfo) {
		this.setAdv_id(advinfo.getAdv_id());
		this.setAdv_type(advinfo.getAdv_type());
		this.setLock_dev(advinfo.getRemain_lock_dev());
		this.setAlias(advinfo.getAlias());
	}

	public void setModleInfo() {
		this.setTaskid(TASK_ID_REMAIN_TASK);
		this.setTaskPhase(TASK_PHASE_REMAIN);
	}
}
