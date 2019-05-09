package com.kilotrees.model.po;

public class ActionModel {

	public static final String tableName = "Action";
	
	private int id;
	private int phase;
	private String prefix_actions;
	private String suffix_actions;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPhase() {
		return phase;
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}

	public String getPrefix_actions() {
		return prefix_actions;
	}

	public void setPrefix_actions(String prefix_actions) {
		this.prefix_actions = prefix_actions;
	}

	public String getSuffix_actions() {
		return suffix_actions;
	}

	public void setSuffix_actions(String suffix_actions) {
		this.suffix_actions = suffix_actions;
	}

}
