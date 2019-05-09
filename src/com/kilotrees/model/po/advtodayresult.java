package com.kilotrees.model.po;

import java.util.Date;

/**
 * 记录每天广告的执行状况,方便后台快速查询，不需要查询执行日志表． 
CREATE TABLE [dbo].[tb_advtodayresult](
	[tid] [int] IDENTITY(1,1) NOT NULL,
	[advid] [int] NOT NULL,
	[isremain] [int] NOT NULL,
	[newuser_success_count] [int] NOT NULL,
	[newuser_err_count] [int] NOT NULL,
	[newuser_success_opentcount] [int] NOT NULL,
	[newuser_err_opentcount] [int] NOT NULL,
	[remain_olduser_success_count] [int] NULL,
	[remain_newuser_success_count] [int] NULL,
	[remain_olduser_success_opentcount] [int] NULL,
	[remain_newuser_success_opentcount] [int] NULL,
	[remain_err_opentcount] [int] NULL,
	[result_time] [datetime] NOT NULL
) ON [PRIMARY]
 *
 */
public class advtodayresult {
	//自增 today id
	int tid;
	int advid;
	//是否留存任务    值为0：新增任务   值为非0：留存任务  
	int isremain;
	//modify 2018-10-25之前不管新增还是留存，都共用success_count和err_count，有时会造成混乱，现在加多几个字段，完全分开来
	//新增用户成功数
	int newuser_success_count;
	//新增时失败数
	int newuser_err_count;
	//因为新增和留存都有可能一天打开多次，用户数和打开次数是不一样的，这个数据暂时混到了新增数和留存数中，要区分开来。
	int newuser_success_opentcount;
	int newuser_err_opentcount;
	//下面是留存用户用
	//旧的留存用户数
	int remain_olduser_success_count;
	//这个表示当天新增做活跃的用户数
	int remain_newuser_success_count;
	//旧用户打开次数
	int remain_olduser_success_opentcount;
	//当天新增用户之后的打开成功次数
	int remain_newuser_success_opentcount;
	//只记录失败打开数，不再分新还旧用户
	int remain_err_opentcount;
	public int getRemain_err_opentcount() {
		return remain_err_opentcount;
	}

	public void setRemain_err_opentcount(int remain_err_opentcount) {
		this.remain_err_opentcount = remain_err_opentcount;
	}

	Date result_time = new Date();
	
	//String key = "";
	
	public int getTid() {
		return tid;
	}

	public void setTid(int tid) {
		this.tid = tid;
	}

	public int getAdvid() {
		return advid;
	}

	public void setAdvid(int advid) {
		this.advid = advid;
	}

	public int getIsremain() {
		return isremain;
	}

	public void setIsremain(int isremain) {
		this.isremain = isremain;
	}
//
//	public int getSuccess_count() {
//		return success_count;
//	}
//
//	public void setSuccess_count(int success_count) {
//		this.success_count = success_count;
//	}
//	
//	public int getAllDoCount()
//	{
//		return (success_count + err_count);
//	}
//
//	public int getErr_count() {
//		return err_count;
//	}
//
//	public void setErr_count(int err_count) {
//		this.err_count = err_count;
//	}	
//
//	public int getSuccess_opentcount() {
//		return success_opentcount;
//	}
//
//	public void setSuccess_opentcount(int success_opentcount) {
//		this.success_opentcount = success_opentcount;
//	}
//
//	public int getErr_opentcount() {
//		return err_opentcount;
//	}
//
//	public void setErr_opentcount(int err_opentcount) {
//		this.err_opentcount = err_opentcount;
//	}

	public Date getResult_time() {
		return result_time;
	}

	public int getNewuser_success_count() {
		return newuser_success_count;
	}

	public void setNewuser_success_count(int newuser_success_count) {
		this.newuser_success_count = newuser_success_count;
	}

	public int getNewuser_err_count() {
		return newuser_err_count;
	}

	public void setNewuser_err_count(int newuser_err_count) {
		this.newuser_err_count = newuser_err_count;
	}

	public int getNewuser_success_opentcount() {
		return newuser_success_opentcount;
	}

	public void setNewuser_success_opentcount(int newuser_success_opentcount) {
		this.newuser_success_opentcount = newuser_success_opentcount;
	}

	public int getNewuser_err_opentcount() {
		return newuser_err_opentcount;
	}

	public void setNewuser_err_opentcount(int newuser_err_opentcount) {
		this.newuser_err_opentcount = newuser_err_opentcount;
	}

	public int getRemain_olduser_success_count() {
		return remain_olduser_success_count;
	}

	public void setRemain_olduser_success_count(int remain_olduser_success_count) {
		this.remain_olduser_success_count = remain_olduser_success_count;
	}

	public int getRemain_newuser_success_count() {
		return remain_newuser_success_count;
	}

	public void setRemain_newuser_success_count(int remain_newuser_success_count) {
		this.remain_newuser_success_count = remain_newuser_success_count;
	}

	public int getRemain_olduser_success_opentcount() {
		return remain_olduser_success_opentcount;
	}

	public void setRemain_olduser_success_opentcount(int remain_olduser_success_opentcount) {
		this.remain_olduser_success_opentcount = remain_olduser_success_opentcount;
	}

	public int getRemain_newuser_success_opentcount() {
		return remain_newuser_success_opentcount;
	}

	public void setRemain_newuser_success_opentcount(int remain_newuser_success_opentcount) {
		this.remain_newuser_success_opentcount = remain_newuser_success_opentcount;
	}

	public void setResult_time(Date result_time) {
		this.result_time = result_time;
	}
	
	public int getAllSuccessUserCount()
	{
		if(this.isremain == 0)
		{
			return this.newuser_success_count;
		}
		return this.remain_newuser_success_count + this.remain_olduser_success_count;
	}
	
	public int getAllErrUserCount()
	{
		if(this.isremain == 0)
		{
			return this.newuser_err_count;
		}
		return 0;//this.remain_err_opentcount;
	}
	
	public int getAllSuccessOpenCount()
	{
		if(this.isremain == 0)
		{
			return this.newuser_success_opentcount;
		}
		return this.remain_newuser_success_opentcount + this.remain_olduser_success_opentcount;
	}
	
	public int getAllErrOpenCount()
	{
		if(this.isremain == 0)
		{
			return this.newuser_err_opentcount;
		}
		return this.remain_err_opentcount;
	}
	
	/**
	 * @return this.advid + "_" + this.isremain;
	 */
	public String getKey()
	{
		String key = "" + this.advid + "_" + this.isremain;
		return key;
	}
	
	public String toString()
	{
		String s = "advid=" + advid + "\r\n";
//		s += "isremain=" + isremain + "\r\n";
//		s += "success_count=" + success_count + "\r\n";
//		s += "err_count=" + err_count + "\r\n";
//		s += "result_time=" + result_time + "\r\n";
//		
		return s;
	}
}
