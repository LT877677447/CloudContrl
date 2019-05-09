package com.kilotrees.dao.task;

public class QQActiveModel {

	private long autoid;
	private String qqnum;
	private String pass;
	private long lastLoginTime;
	private long lastFetchTime;
	private String phoneNum;
	private String email;
	private String appinfo;
	private String phoneInfo;
	private int status;
	private int priceLevel;
	private String comment;
	
	public long getAutoid() {
		return autoid;
	}
	public void setAutoid(long autoid) {
		this.autoid = autoid;
	}
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
	public String getAppinfo() {
		return appinfo;
	}
	public void setAppinfo(String appinfo) {
		this.appinfo = appinfo;
	}
	public String getPhoneInfo() {
		return phoneInfo;
	}
	public void setPhoneInfo(String phoneInfo) {
		this.phoneInfo = phoneInfo;
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
	
}
