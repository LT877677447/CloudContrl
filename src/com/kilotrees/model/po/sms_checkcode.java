package com.kilotrees.model.po;

import java.util.Date;

/**
 *
CREATE TABLE [dbo].[tb_smscheckcode](
	[seqid] [int] NOT NULL,
	[req_dev] [varchar](50) NOT NULL,
	[adv_id] [int] NOT NULL,
	[mobile] [varchar](50) NULL,
	[issendsms] [int] NULL,
	[smstext] [varchar](500) NULL,
	[smscode] [varchar](50) NULL,
	[check_code_url] [varchar](300) NULL,
	[getmo_time] [datetime] NULL,
	[getcode_time] [datetime] NULL,
	[errcode] [int] NOT NULL,
	[errinfo] [varchar](100) NULL,
	[fetch_time] [datetime] NULL,
	[send_time] [datetime] NULL,
	[proc_result] [int] NOT NULL,
 CONSTRAINT [PK_tb_smscheckcode] PRIMARY KEY CLUSTERED 
(
	[seqid] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

 * @author Administrator
 *
 */
public class sms_checkcode {
	public static String tablename = "tb_smscheckcode";
	private long seqid = -1;//唯一充列号,-1表示有错
	private String req_dev;
	private int adv_id;//业务id，要转成平台方要求的业务名字，比如weixin
	private String mobile = "";//手机号
	//是否发短信
	private int issendsms = 0;
	private String smstext = "";//短信内容
	private String smscode = "";//验证码	
	private String check_code_url = "";//使用的第三方验证码对接平台
	//取得手机号时间
	private Date getmo_time;
	private Date getcode_time;	
	//0：无错误 1：超时
	private int errcode = 0;
	private String errinfo = "";
	private Date fetch_time; //客户提取验证码时间
	private Date send_time;//发短信时间
	//处理结果,0:返回手机号,对方还未响应验证码 1:对方响应验证码失败  2：响应验证码成功
	private int proc_result;
	
	
	public long getSeqid() {
		return seqid;
	}
	public void setSeqid(long seqid) {
		this.seqid = seqid;
	}
	public String getReq_dev() {
		return req_dev;
	}
	public void setReq_dev(String req_dev) {
		this.req_dev = req_dev;
	}
	public int getAdv_id() {
		return adv_id;
	}
	public void setAdv_id(int adv_id) {
		this.adv_id = adv_id;
	}
	public String getSmscode() {
		return smscode;
	}
	public void setSmscode(String smscode) {
		this.smscode = smscode;
	}
	public String getCheck_code_url() {
		return check_code_url;
	}
	public void setCheck_code_url(String check_code_url) {
		this.check_code_url = check_code_url;
	}
	public Date getGetmo_time() {
		return getmo_time;
	}
	public void setGetmo_time(Date getmo_time) {
		this.getmo_time = getmo_time;
	}
	public Date getGetcode_time() {
		return getcode_time;
	}
	public void setGetcode_time(Date getcode_time) {
		this.getcode_time = getcode_time;
	}
	public String getErrinfo() {
		return errinfo;
	}
	public void setErrinfo(String errinfo) {
		this.errinfo = errinfo;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	public int getIssendsms() {
		return issendsms;
	}
	public void setIssendsms(int issendsms) {
		this.issendsms = issendsms;
	}
	public int getErrcode() {
		return errcode;
	}
	public void setErrcode(int errcode) {
		this.errcode = errcode;
	}
	public String getSmstext() {
		return smstext;
	}
	public void setSmstext(String smstext) {
		this.smstext = smstext;
	}
	public Date getFetch_time() {
		return fetch_time;
	}
	public void setFetch_time(Date fetch_time) {
		this.fetch_time = fetch_time;
	}
	public Date getSend_time() {
		return send_time;
	}
	public void setSend_time(Date send_time) {
		this.send_time = send_time;
	}
	public int getProc_result() {
		return proc_result;
	}
	public void setProc_result(int proc_result) {
		this.proc_result = proc_result;
	}
	
}
