package com.kilotrees.service.adv.runtime.api;

import com.kilotrees.model.bo.TaskBase;
import com.kilotrees.model.bo.ad_task_report;
import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.model.po.advtodayresult;

/**
 * 广告实时状态处理接口，主要实现计算时间曲线，每小时分配量，之前主要是针对cpa类广告，后面针对养粉和用粉做运营业务 比之前增加动态处理结果回传。
 * 
 * @author elememt
 *
 */
public interface ITaskRuntime {
	boolean isRemain();

	void setRemain(boolean r);

	boolean isFinish();

	public boolean isOffline();

	public void setOffline(boolean isOffline);

	public void setAdvinfo(advtaskinfo _advinfo);

	public advtaskinfo getAdvinfo();

	public advtodayresult getResult();

	public void setResult(advtodayresult result);

	public int getReqDevDoCount();

	// 取当天24小时的任务分布量
	public int[] getDoCountOf24Houurs();

	public int getCurRetCount();

	public void caclRuntimeHouursCount();

	public int caclMaxHoureCountOfAllDate();

	// 准备为新任务分配设备，一般不用
	public boolean allocNewReady();

	public boolean allocDevForNew();

	// 2019-1-31，程序自动分配和释放设备，这个仅对于简单的CPA之类的任务可行，但后面时长会动态变化，打开次数也是动态变的，越来越不可行，以后我们还是用手机分配和释放为主，做个页面，手动分配。
	// 以后用于估算设备需求数量
	public void reAllocDev(boolean inited);

	// 2019-1-14 针对阅读这种，一次可能要取多条任务
	public TaskBase[] fetchTask(String dev_tag);

	// 不管时间曲线或者任务完成，都强制返回任务,主要用于充值
	public TaskBase fetchTaskStrong();

	public boolean checkCurHourFinishStatus();

	public String getTaskRuntimeInfo();

	// 新增时随机时长
	public int getDayOpenCountRand(int daydiff);

	// 留存随机打开次数，取消下面的接口，为了更好统一
	// public int getDayOpenCountRandRemain(int daydiff);
	// 取平均一天一个用户打开次数
	public float getDayOpenCountAver();

	// 取一个新增平均时长
	public float getWorkingTimeAver(int daydiff);

	// public float getRequestTimeAverForRemain(int daydiff);
	public int getWorkingTimeRand();

	// public int getRequestTimeRandForRemain(int daydiff);
	public void incDoingCount(String device_tag);

	public void decDoingCount(String dev_tag);

	public int getAllTodocount();

	// 对于养粉等非cpa类的新增广告，可以自身处理结果
	public boolean handleTaskReport(ad_task_report report);

}
