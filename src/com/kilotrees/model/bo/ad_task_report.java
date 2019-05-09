package com.kilotrees.model.bo;

import java.util.Date;

/**
 * 客户端返回任务状态报告
 * @author Administrator
 *
 */
public class ad_task_report {
	TaskBase task;
	
	//一个任务分好几个步聚，从1开始，我们设定0为最后
	int step = 1;
	
	//结果状态,0表示成功，其它为错误
	int result;
	
	String result_info;
	
	Date reportTime;
	
	//执行任务时vpn所有地ip
	String ip = "";
	
	//vpn所有地区
	String area = "";
	
	public TaskBase getTask() {
		return task;
	}
	public void setTask(TaskBase task) {
		this.task = task;
	}
	public int getStep() {
		return step;
	}
	public void setStep(int step) {
		this.step = step;
	}
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	public String getResult_info() {
		return result_info;
	}
	public void setResult_info(String result_info) {
		this.result_info = result_info;
	}
	public Date getReportTime() {
		return reportTime;
	}
	public void setReportTime(Date reportTime) {
		this.reportTime = reportTime;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	
}
