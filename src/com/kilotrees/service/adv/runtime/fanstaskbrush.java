/**
 * @author Administrator
 * 2019年1月31日 下午4:23:56 
 */
package com.kilotrees.service.adv.runtime;

import org.apache.log4j.Logger;

import com.kilotrees.model.bo.TaskBase;
/**
 * 粉丝任务，比如刷阅读之类，主要针对业务表，象大圣之类由原始帐号表建一张业务表，按任务量导入帐号，直至每个帐号做完
 * 这种任务跟年前做的微信阅读量不大一样，这种任务是帐号是我们的，微信那个帐号是人家的。
 * 一般情况下，我们取一个帐号并下发任务，并且把阅读链接之类由参数传入(可以由配置文件写好多条），也可以从另一张表中读取
 * @author Administrator
 * 2019年1月31日 下午4:24:02
 */
public class fanstaskbrush extends advruntimebase {
	private static Logger log = Logger.getLogger(fanstaskbrush.class);
	@Override
	public boolean isFinish() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int caclMaxHoureCountOfAllDate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean allocNewReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allocDevForNew() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reAllocDev(boolean inited) {
		// TODO Auto-generated method stub

	}

	@Override
	public TaskBase[] fetchTask(String dev_tag) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TaskBase fetchTaskStrong() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkCurHourFinishStatus() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getTaskRuntimeInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAllTodocount() {
		// TODO Auto-generated method stub
		return 0;
	}

}
