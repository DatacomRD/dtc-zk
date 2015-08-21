package com.dtc.common.zk.converter;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

/**
 * 提供單一 entity、透過 {@link #setDataStore(List)} 設定比對的資料集，
 * 來轉換某兩個 field / getter 的 convert 基礎架構。
 * <p>
 * <b>注意：</b>
 * <ul>
 * 	<li>{@link #getBeanValue(Object)} 與 {@link #getUiValue(Object)} 的值在 data store 當中都必須具備唯一性 </li>
 * 	<li>當 entity 的資料筆數很多，不建議用這個方式</li>
 * </ul>
 * @author MontyPan
 *
 * @param <E> entity
 * @param <U> UI 顯示的資料型態
 * @param <B> bean property 的資料型態
 */
public abstract class BaseConverter<E, U, B> implements Converter<U, B, Component> {
	private ArrayList<E> dataStore = new ArrayList<E>();
	
	/**
	 * @return bean 對應到 entity 的 field / getter 值
	 */
	protected abstract B getBeanValue(E data);
	
	/**
	 * @return UI 呈現的值對應到 entity 的 field / getter 值
	 */
	protected abstract U getUiValue(E data);
	
	/**
	 * 設定 convert 所比對的資料集
	 */
	public void setDataStore(List<E> dataStore) {
		this.dataStore.clear();
		this.dataStore.addAll(dataStore);
	}
	
	@Override
	public U coerceToUi(B beanProp, Component component, BindContext ctx) {
		if (beanProp == null) { return null; }
		
		for (E data : dataStore) {
			if (getBeanValue(data).equals(beanProp)) {
				return getUiValue(data);
			}
		}
		
		return null;
	}

	@Override
	public B coerceToBean(U compAttr, Component component, BindContext ctx) {
		if (compAttr == null) { return null; }
		
		for (E data : dataStore) {
			if (getUiValue(data).equals(compAttr)) {
				return getBeanValue(data);
			}
		}
		
		return null;
	}
}