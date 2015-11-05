package com.dtc.common.zk.converter;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

/**
 * 在 constructor 時設定 true / false 對應的值（ {@link #trueValue} / {@link #falseValue}），
 * 在轉換到 UI 時只會判斷 bean property 值否與 {@link #trueValue} 相同。
 * 
 * @param <B> bean property 的資料型態
 */
public class BooleanConverter<B> implements Converter<Boolean, B, Component>{
	private final B trueValue;
	private final B falseValue;

	public BooleanConverter(B trueValue, B falseValue) {
		if (trueValue == null || falseValue == null) {
			throw new IllegalArgumentException();
		}
		
		this.trueValue = trueValue;
		this.falseValue = falseValue;
	}

	@Override
	public Boolean coerceToUi(B beanProp, Component component, BindContext ctx) {
		return trueValue.equals(beanProp);
	}

	@Override
	public B coerceToBean(Boolean compAttr, Component component, BindContext ctx) {
		if (compAttr == null) { return falseValue; }
		
		return compAttr ? trueValue : falseValue;
	}
}
