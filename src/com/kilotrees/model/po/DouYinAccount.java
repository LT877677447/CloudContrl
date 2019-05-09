/**
 * @author Administrator
 * 2019年4月18日 下午10:11:51 
 */
package com.kilotrees.model.po;

import java.io.Serializable;
import java.util.Date;

/**
 * CREATE TABLE [dbo].[tb_DouYinAccount](
	[autoid] [bigint] IDENTITY(1,1) NOT NULL,
	[phoneNumber] [varchar](50) NOT NULL,
	[pass] [varchar](50) NOT NULL,
	[registTime] [datetime] NULL,
	[appinfo] [text] NULL,
	[phoneInfo] [text] NULL,
	[status] [int] NULL,
	[comment] [varchar](100) NULL,
 CONSTRAINT [PK_tb_DouYinAccount] PRIMARY KEY CLUSTERED 
(
	[phoneNumber] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
 * 
 * @author Administrator
 * 2019年4月18日 下午10:22:43 
 */
public class DouYinAccount implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long autoid;
	private String phoneNumber;
	private String pass;
	private Date registTime;
	private String appinfo;
	private String phoneInfo;
	private int status;
	private String comment;

	public DouYinAccount() {
		super();
	}

	public DouYinAccount(Long autoid, String phoneNumber, String pass, Date registTime, String appinfo, String phoneInfo, int status, String comment) {
		super();
		this.autoid = autoid;
		this.phoneNumber = phoneNumber;
		this.pass = pass;
		this.registTime = registTime;
		this.appinfo = appinfo;
		this.phoneInfo = phoneInfo;
		this.status = status;
		this.comment = comment;
	}

	public Long getAutoid() {
		return autoid;
	}

	public void setAutoid(Long autoid) {
		this.autoid = autoid;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public Date getRegistTime() {
		return registTime;
	}

	public void setRegistTime(Date registTime) {
		this.registTime = registTime;
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

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "DouYinAccount [autoid=" + autoid + ", phoneNumber=" + phoneNumber + ", pass=" + pass + ", registTime=" + registTime + ", appinfo=" + appinfo + ", phoneInfo="
				+ phoneInfo + ", status=" + status + ", comment=" + comment + "]";
	}

}
