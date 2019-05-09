package com.kilotrees.action;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kilotrees.util.StringUtil;

public class TaskActionHandler {
	private static Logger log = Logger.getLogger(ITaskAction.class);
	private static final String LogicPackageNamePrefix = "com.kilotrees.action";

	public static void requestLogic(String alias, JSONObject request, JSONObject response) {
		ITaskAction taskLogic = newTaskLogicInstance(alias);
		if (taskLogic == null) {
			return;
		}
		try {
			taskLogic.handleTaskRequest(request, response);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public static void reportLogic(String alias, JSONObject request, JSONObject response) {
		ITaskAction taskLogic = newTaskLogicInstance(alias);
		if (taskLogic == null) {
			return;
		}
		try {
			taskLogic.handleTaskReport(request, response);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private static ITaskAction newTaskLogicInstance(String alias) {
		if (StringUtil.isStringEmpty(alias) == false) {
			try {
				Class<?> logicClazz = Class.forName(LogicPackageNamePrefix + ".task." + alias + "TaskAction");
				ITaskAction taskLogic = (ITaskAction) logicClazz.newInstance();
				return taskLogic;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return null;
	}
}
