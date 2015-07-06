package com.dtc.common.zk.exception;

import org.zkoss.util.resource.Labels;

/**
 * UI 操作時產生的自定義 exception 的 base class。
 * @author MontyPan
 */
public class UIException extends Exception {
	private static final long serialVersionUID = 4637912036842340682L;
	
	public UIException(String i18nKey, String customMessage) {
		super(Labels.getLabel(i18nKey) + "\n" + customMessage);
	}
	
	public UIException(String i18nKey, String[] values) {
		super(Labels.getLabel(i18nKey, values));
	}
}