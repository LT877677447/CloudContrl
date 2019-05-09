package com.kilotrees.dao.task;

import org.apache.log4j.Logger;

import com.kilotrees.util.StringUtil;

public class TaskDaoHandler {
	private static Logger log = Logger.getLogger(TaskDaoHandler.class);
	
	private static final String LogicPackageNamePrefix = "com.kilotrees.dao.task.";
	
	public static ITaskDao getTaskLogicDaoInstance(String alias) {
		if (StringUtil.isStringEmpty(alias) == false) {
			try {
				Class<?> specialLogicClazz = Class.forName(LogicPackageNamePrefix + alias + "TaskDao");
				ITaskDao taskDao = (ITaskDao) specialLogicClazz.newInstance();
				return taskDao;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return null;
	}
	
}
