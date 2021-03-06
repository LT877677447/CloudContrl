/**
 * @author Administrator
 * 2019年4月30日 下午6:09:31 
 */
package com.kilotrees.model.bo;

import com.kilotrees.model.po.advtaskinfo;

public class TaskWinXinActive extends TaskBase {
	// private Integer autoid; in super
	private String weixinAccount;
	private String phoneNumber;
	private String password;
	private long registTime;
	private long firstFetchTime;
	private long lastFetchTime;
	private long lastLoginTime;
	// private String appinfo; in super
	// private String phoneInfo; in super
	private Integer status;
	private String comment;
	private String isOverSeas;

	/**
	 * 
	 */
	public TaskWinXinActive() {
		super();
	}
	
	public TaskWinXinActive(String weixinAccount, String phoneNumber, String password, long registTime, long firstFetchTime, long lastFetchTime, long lastLoginTime, Integer status,
			String comment, String isOverSeas) {
		super();
		this.weixinAccount = weixinAccount;
		this.phoneNumber = phoneNumber;
		this.password = password;
		this.registTime = registTime;
		this.firstFetchTime = firstFetchTime;
		this.lastFetchTime = lastFetchTime;
		this.lastLoginTime = lastLoginTime;
		this.status = status;
		this.comment = comment;
		this.isOverSeas = isOverSeas;
	}



	public String getWeixinAccount() {
		return weixinAccount;
	}

	public void setWeixinAccount(String weixinAccount) {
		this.weixinAccount = weixinAccount;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getPassword() {
		return password;
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

	public void setPassword(String password) {
		this.password = password;
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

	public String getIsOverSeas() {
		return isOverSeas;
	}

	public void setIsOverSeas(String isOverSeas) {
		this.isOverSeas = isOverSeas;
	}

	@Override
	public String toString() {
		return "TaskWinXinActive [weixinAccount=" + weixinAccount + ", phoneNumber=" + phoneNumber + ", password=" + password + ", registTime=" + registTime + ", firstFetchTime="
				+ firstFetchTime + ", lastFetchTime=" + lastFetchTime + ", lastLoginTime=" + lastLoginTime + ", status=" + status + ", comment=" + comment + ", isOverSeas="
				+ isOverSeas + "]";
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
