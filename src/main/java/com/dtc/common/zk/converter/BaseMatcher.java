package com.dtc.common.zk.converter;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

/**
 * 提供單一 entity、透過 {@link #setDataStore(List)} 設定比對的資料集，
 * 來轉換指定 field / getter 與 entity instance 的基礎架構。
 * <p>
 * <b>注意：</b>
 * <ul>
 * 	<li>當 entity 的資料筆數很多，不建議用這個方式</li>
 * </ul>
 * @author MontyPan
 *
 * @param <E> entity
 * @param <U> UI 顯示的資料型態
 */
public abstract class BaseMatcher<E, U> implements Converter<U, E, Component> {
	private ArrayList<E> dataStore = new ArrayList<E>();
	
	/**
	 * @return UI 呈現的值對應到 entity 的 field / getter 值
	 */
	protected abstract U getUiValue(E data);
	
	/**
	 * 設定 macher 所比對的資料集
	 */
	public void setDataStore(List<E> dataStore) {
		this.dataStore.clear();
		this.dataStore.addAll(dataStore);
	}

	@Override
	public U coerceToUi(E beanProp, Component component, BindContext ctx) {
		return beanProp == null ? null : getUiValue(beanProp);
	}
	
	@Override
	public E coerceToBean(U compAttr, Component component, BindContext ctx) {
		if (compAttr == null) { return null; }
		
		for (E data : dataStore) {
			if (getUiValue(data).equals(compAttr)) {
				return data;
			}
		}
		
		return null;
	}
}