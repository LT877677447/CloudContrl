/**
 * @author Administrator
 * 2019年2月14日 上午10:23:11 
 */
package com.kilotrees.model.po;

import java.util.Date;

public class tb_weixinlinkFail {
	public static final String tablename = "tb_weixinlinkFail";
	private Integer idAccount;
	private String account;
	private Integer idLink;
	private String linkUrl;
	private Integer failureNumber;
	private Date lastFailTime;
	public Integer getIdAccount() {
		return idAccount;
	}
	public void setIdAccount(Integer idAccount) {
		this.idAccount = idAccount;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public Integer getIdLink() {
		return idLink;
	}
	public void setIdLink(Integer idLink) {
		this.idLink = idLink;
	}
	public String getLinkUrl() {
		return linkUrl;
	}
	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
	}
	public Integer getFailureNumber() {
		return failureNumber;
	}
	public void setFailureNumber(Integer failureNumber) {
		this.failureNumber = failureNumber;
	}
	public Date getLastFailTime() {
		return lastFailTime;
	}
	public void setLastFailTime(Date lastFailTime) {
		this.lastFailTime = lastFailTime;
	}
	@Override
	public String toString() {
		return "tb_weixinlinkFail [idAccount=" + idAccount + ", account=" + account + ", idLink=" + idLink + ", linkUrl=" + linkUrl + ", failureNumber=" + failureNumber
				+ ", lastFailTime=" + lastFailTime + "]";
	}
	
	
	
}
