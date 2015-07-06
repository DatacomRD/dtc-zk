package com.dtc.common.zk.viewmodel;

/**
 * 搜尋功能的比對條件
 * 
 * @author MontyPan
 */
public enum Constraint {
	EQUAL(" = "),
	NOT_EQUAL(" != "),
	SMALLER(" < "),
	BIGGER(" > "),
	NOT_BIGGER(" <= "),
	NOT_SMALLER(" >= "),
	LIKE(" like ");
	
	private final String opString;
	
	Constraint(String op) {
		this.opString = op;
	}
	
	public String getOpString() {
		return opString;
	}
}