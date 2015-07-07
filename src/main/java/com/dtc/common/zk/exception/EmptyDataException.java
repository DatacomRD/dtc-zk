package com.dtc.common.zk.exception;

public class EmptyDataException extends UIException {
	private static final long serialVersionUID = -6691061384383130972L;

	public EmptyDataException(String customMessage) {
		super("dtc.zk.error.data.empty", new String[]{customMessage});
	}
}