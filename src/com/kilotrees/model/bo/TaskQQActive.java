package com.kilotrees.model.bo;

import org.json.JSONObject;

import com.kilotrees.dao.task.QQActiveModel;
import com.kilotrees.model.po.advtaskinfo;

public class TaskQQActive extends TaskBase {

	private String qqnum;
	private String pass;
	private long lastLoginTime;
	private long lastFetchTime;
	private String phoneNum;
	private String email;
	// private String appinfo; // in Super
	// private String phoneInfo; // in Super
	private int status;
	private int priceLevel;
	private String comment;

	public String getQqnum() {
		return qqnum;
	}

	public void setQqnum(String qqnum) {
		this.qqnum = qqnum;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public long getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(long lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public long getLastFetchTime() {
		return lastFetchTime;
	}

	public void setLastFetchTime(long lastFetchTime) {
		this.lastFetchTime = lastFetchTime;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getPriceLevel() {
		return priceLevel;
	}

	public void setPriceLevel(int priceLevel) {
		this.priceLevel = priceLevel;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setTaskInfo(advtaskinfo advinfo) {
		this.setAdv_id(advinfo.getAdv_id());
		this.setAdv_type(advinfo.getAdv_type());
		this.setLock_dev(advinfo.getRemain_lock_dev());
		this.setAlias(advinfo.getAlias());
	}

	public void setModleInfo(QQActiveModel model) {
			
		this.setAutoid(model.getAutoid());
		this.setQqnum(model.getQqnum());
		this.setPass(model.getPass());

		this.setLastLoginTime(model.getLastLoginTime());
		this.setLastFetchTime(model.getLastFetchTime());

		this.setPhoneNum(model.getPhoneNum());
		this.setEmail(model.getEmail());

		try {
			if (model.getAppinfo() != null) {
				this.setAppInfo(new JSONObject(model.getAppinfo()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (model.getPhoneInfo() != null) {
			
			try {
				this.setPhoneInfo(new JSONObject(model.getPhoneInfo()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			this.setTaskid(TASK_ID_REMAIN_TASK);

			this.setTaskPhase(TASK_PHASE_REMAIN);
		} else {
			this.setTaskid(TASK_ID_NEWLY_TASK);

			this.setTaskPhase(TASK_PHASE_NEWLY);
		}

		this.setStatus(model.getStatus());
		this.setPriceLevel(model.getPriceLevel());

		this.setComment(model.getComment());

	}

}
