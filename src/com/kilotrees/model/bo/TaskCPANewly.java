package com.kilotrees.model.bo;

public class TaskCPANewly extends TaskBase {
	
	public TaskCPANewly() {
		super();
		
		this.setTaskPhase(TaskBase.TASK_PHASE_NEWLY);
		this.setTaskType("CPA");
	}
	
}
