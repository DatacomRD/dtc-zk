package com.dtc.common.zk.exception;

public class DeleteFailedByReference extends UIException {
	private static final long serialVersionUID = -3376922232834207093L;
	
	public DeleteFailedByReference() {
		super("dtc.zk.error.delete.reference", "");
	}
}