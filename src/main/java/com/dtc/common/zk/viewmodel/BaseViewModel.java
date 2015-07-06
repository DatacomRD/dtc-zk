package com.dtc.common.zk.viewmodel;

import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Window;

import com.dtc.common.zk.util.MessageBoxUtil;

/**
 * <ul>
 * 	<li>提供 {@link #postCommand(String)}。</li>
 *  <li>
 *  	提供 {@link #getBinder()}。
 *  	<b>注意：</b>child class 必須要有掛 <code>@Init(superclass=true)</code> 的 method。
 *  </li>
 * </ul>
 * @author MontyPan
 */
public class BaseViewModel {
	private Binder binder;
	
	protected BaseViewModel() {}
	
	@Init(superclass=true)
	public void baseInit(@ContextParam(ContextType.BINDER) final Binder binder) {
		this.binder = binder;
	}
	
	/**
	 * 以程式的方式觸發 NotifyChange，
	 * 基本上就是 {@link BindUtils#postNotifyChange(String, String, Object, String)} 的 wrapper。
	 */
	protected void notifyChange(String[] properties) {
		for (String property : properties) {
			BindUtils.postNotifyChange(null, null, this, property);
		}
	}
	
	/**
	 * 以程式的方式觸發 NotifyChange，
	 * 基本上就是 {@link BindUtils#postNotifyChange(String, String, Object, String)} 的 wrapper。
	 */
	protected void notifyChange(String property) {
		BindUtils.postNotifyChange(null, null, this, property);
	}
	
	/**
	 * 以程式的方法觸發 Command，
	 * 基本上就是 {@link Binder#postCommand(String, Map)} 的 wrapper。
	 */
	protected void postCommand(String command) {
		postCommand(command, null);
	}
	
	/**
	 * 以程式的方法觸發 Command，
	 * 基本上就是 {@link Binder#postCommand(String, Map)} 的 wrapper。
	 */
	protected void postCommand(String command, Map<String, Object> params) {
		binder.postCommand(command, params);
	}
	
	/**
	 * 以程式的方法觸發 GlobalCommand，
	 * 基本上就是 {@link BindUtils#postGlobalCommand(String, String, String, Map)} 的 wrapper。
	 */
	protected void postGlobalCommand(String command) {
		postGlobalCommand(command, null);
	}
	
	/**
	 * 以程式的方法觸發 GlobalCommand，
	 * 基本上就是 {@link BindUtils#postGlobalCommand(String, String, String, Map)} 的 wrapper。
	 */
	protected void postGlobalCommand(String command, Map<String, Object> params) {
		BindUtils.postGlobalCommand(null, null, command, params);
	}
	
	protected void handleDaoException(Exception e) {
		MessageBoxUtil.error(Labels.getLabel("error.dao.exception") + "\n" + e.getLocalizedMessage());
		e.printStackTrace();
	}
	
	protected void openDialog(String uri) {
		openDialog(uri, null);
	}
	
	protected void openDialog(String uri, Map<String, Object> params) {
		Window window = (Window)Executions.createComponents(uri, null, params);
		window.doModal();
	}

	// ==== 以下為 getter / setter 區 ==== //
	protected Binder getBinder() {
		return binder;
	}
}