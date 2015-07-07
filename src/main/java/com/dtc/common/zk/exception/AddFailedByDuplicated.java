package com.dtc.common.zk.exception;

/**
 * 新增資料時資料重複的 exception。
 * @author MontyPan
 */
public class AddFailedByDuplicated extends UIException {
	private static final long serialVersionUID = -6022635640160640361L;

	public AddFailedByDuplicated(String customMessage) {
		super("dtc.zk.error.add.duplicated", new String[]{customMessage});
	}
}