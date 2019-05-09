/**
 * @author Administrator
 * 2019年1月31日 下午2:38:03 
 */
package com.kilotrees.service.adv.runtime;

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.log4j.Logger;

import com.kilotrees.dao.task.ITaskDao;
import com.kilotrees.dao.task.QQActiveModel;
import com.kilotrees.dao.task.TaskDaoHandler;
import com.kilotrees.model.bo.TaskBase;
import com.kilotrees.model.bo.TaskDouYinActive;
import com.kilotrees.model.bo.TaskQQActive;
import com.kilotrees.model.bo.TaskWinXinActive;
import com.kilotrees.model.po.ServerConfig;
import com.kilotrees.model.po.advgroup;
import com.kilotrees.model.po.advtaskinfo;
import com.kilotrees.services.actdeviceinfo_service;
import com.kilotrees.services.advgroup_service;

/**
 * 自媒体QQ,抖音，快手之类的帐号定期活跃任务，任务advtype=10，此任务从自媒体原始帐号表（tb_qqacount）中按最后登录时间取出需要养的号，
 * 下发给设备执行。如果此帐号没有绑定设备信息，则新建一个 phoneinfo下发，如果有，则用数据表中原有的设备信息下发。
 * 任务做完之后，在logic中处理report报告，更新最后活跃时间和phoneinfo 这种任务没有时间曲线。也不需要做日志。
 * 
 * @author Administrator 2019年1月31日 下午2:38:08
 */
public class RuntimeFeedFans extends advruntimebase {
	
	private static Logger log = Logger.getLogger(RuntimeFeedFans.class);

	private ITaskDao dao = null;

	public void setAdvinfo(advtaskinfo _advinfo) {
		super.setAdvinfo(_advinfo);

		String alias = this.advinfo.getAlias();
		dao = TaskDaoHandler.getTaskLogicDaoInstance(alias);

		if (dao == null) {
			log.error("Fatal: Please check the Alias is set or not!!!");
		}
	}

	@Override
	public boolean isFinish() {
		// TODO Auto-generated method stub
		if (this.getAllTodocount() < 1) {
			return true;
		}
		return false;
	}

	@Override
	public int caclMaxHoureCountOfAllDate() {
		return dao.fetchActiveCount();
	}

	@Override
	public boolean allocNewReady() {
		// TODO Auto-generated method stub
		// 不需要实现
		return false;
	}

	@Override
	public boolean allocDevForNew() {
		// TODO Auto-generated method stub
		// 不需要实现
		return false;
	}

	@Override
	public void reAllocDev(boolean inited) {
		// TODO Auto-generated method stub
		// 不需要实现
	}

	@Override
	public TaskBase[] fetchTask(String dev_tag) {
		ArrayList<TaskBase> list = new ArrayList<TaskBase>();
		Object model = dao.fetchOneActiveAccount();
		if(model == null) {
			return null;
		}
		
		if(model instanceof QQActiveModel) {
			//QQ活跃
			QQActiveModel QQModel = (QQActiveModel) model;
			TaskQQActive taskFeedFans = new TaskQQActive();
			taskFeedFans.setTaskInfo(advinfo);
			taskFeedFans.setModleInfo(QQModel);
			list.add(taskFeedFans);
		}
		if(model instanceof TaskDouYinActive){
			//抖音活跃
			TaskDouYinActive douYinModel = (TaskDouYinActive) model;
			douYinModel.setTaskInfo(advinfo);
			douYinModel.setModleInfo();
			list.add(douYinModel);
		}
		if(model instanceof TaskWinXinActive) {
			TaskWinXinActive weiXinModel = (TaskWinXinActive) model;
			weiXinModel.setTaskInfo(advinfo);
			weiXinModel.setModleInfo();
			list.add(weiXinModel);
		}
		
		TaskBase[] adtasks = new TaskBase[list.size()];
		list.toArray(adtasks);
		return adtasks;
	}

	@Override
	public TaskBase fetchTaskStrong() {
		// TODO Auto-generated method stub
		// 不需要实现
		return null;
	}

	@Override
	public boolean checkCurHourFinishStatus() {
		// TODO Auto-generated method stub
		if (this.isFinish()) {
			return false;
		}
		return true;
	}

	@Override
	public String getTaskRuntimeInfo() {
		// TODO Auto-generated method stub
		//
		// return "";
		checkDoingTimeout();
		String sruntime = "";
		int curhour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int adv_id = 0;
		advgroup group = advgroup_service.getInstance().getGroupIncludeAdv(this.getAdvinfo().getAdv_id(), false);
		if (this.advinfo != null) {
			adv_id = this.advinfo.getAdv_id();
			if (group == null)
				sruntime += "新增任务:" + advinfo.getName() + "[id:" + advinfo.getAdv_id() + "]]\r\n";
			else
				sruntime += group.getName() + "组任务:" + advinfo.getName() + "[id:" + advinfo.getAdv_id() + "]]\r\n";
			int ret_count = advinfo.getDayusercount();
			if (this.result != null) {
				ret_count -= result.getNewuser_success_count();
			}
			sruntime += "状态:";
			if (ret_count <= 0)
				sruntime += "***完成***\r\n";
			else
				sruntime += "未完成(" + ret_count + ")\r\n";

			sruntime += "每天执行用户总数:" + advinfo.getDayusercount() + "\r\n";
			sruntime += "正在执行的数:" + this.getDoingCount() + "\r\n";

			if (this.result != null) {
				sruntime += "今天成功用户数:" + result.getNewuser_success_count() + "\r\n";
				sruntime += "今天失败用户数:" + result.getNewuser_err_count() + "\r\n";
				sruntime += "今天成功活跃数:" + result.getNewuser_success_opentcount() + "\r\n";
				sruntime += "今天失败活跃:" + result.getNewuser_err_opentcount() + "\r\n";
			} else {
				sruntime += "今天成功执行数:" + 0 + "\r\n";
				// sruntime += "今天失败执行数:" + 0 + "\r\n";
			}

			if (this.advinfo.getAdv_type() < 30) {
				// 如果当前接近整点，比如58分钟，当重启时，因为剩下的分钟数不足以做1个任务，很容易看到下面的数据为0，看起来象有问题
				// 最好列出后面小时个数.
				// 2018-12-8
				int show_hours = 24 - curhour;
				String s1 = "" + this.docount_hours[curhour], s2 = "" + this.retcount_hours[curhour];
				s1 += "［";
				s2 += "［";
				for (int i = 1; i < show_hours; i++) {
					s1 += this.docount_hours[curhour + i] + ",";
					s2 += this.retcount_hours[curhour + i] + ",";
				}
				s1 += "］";
				s2 += "］";
				sruntime += "后面小时分配任务数:" + s1/* this.docount_hours[curhour] */ + "\r\n";
				sruntime += "后面小时剩余任务数:" + s2/* this.retcount_hours[curhour] */ + "\r\n";
			}
		}
		if (group == null) {
			// 2018-12-8 加上this.reqDevDoCount，因 为第一次重启时并没有计算出需求(已经改过来）
			if (this.advinfo.getAdv_type() < 30 && this.reqDevDoCount > 0) {
				sruntime += "需求设备数:" + this.reqDevDoCount + "\r\n";
			}
			int alloc_type = ServerConfig.adv_alloc_type_single;
			if (this.advinfo.getRemain_lock_dev() == 1) {
				alloc_type = ServerConfig.adv_alloc_type_lockremain;
			}
			int act_alloced_count = actdeviceinfo_service.getInstance().getAllocedDevCountAct(adv_id, alloc_type);
			int all_allock_count = actdeviceinfo_service.getInstance().getAllocedDevCount(adv_id, alloc_type);

			if (this.advinfo.getAdv_type() < 30) {
				sruntime += "已经分配的总设备数:" + all_allock_count + "\r\n";
				sruntime += "已经分配的活跃设备数:" + act_alloced_count + "\r\n";
			}
		}
		log.info(sruntime);
		return sruntime;
	}

	@Override
	public int getAllTodocount() {
		return caclMaxHoureCountOfAllDate();
	}

}
