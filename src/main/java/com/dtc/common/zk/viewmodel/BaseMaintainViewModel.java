package com.dtc.common.zk.viewmodel;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Form;
import org.zkoss.bind.FormStatus;
import org.zkoss.bind.SimpleForm;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

import com.dtc.boundary.Entity;
import com.dtc.common.zk.exception.UIException;
import com.dtc.common.zk.util.MessageBoxUtil;

/**
 * 「單一 entity 資料維護」頁面的基礎 ViewModel，以 {@link BaseEntityViewModel} 為基礎再添加維護功能。
 * 頁面上會包含：
 * <ul>
 * 	<li>一個有掛 form binding 的編輯區</li>
 * 	<li>操作按鈕：新增、存檔、取消、刪除</li>
 * </ul>
 * <p>
 * 提供下列功能：
 * <ul>
 * 	<li>控制按鈕 disable 的邏輯，例如 {@link #isDisableAdd()}</li>
 * 	<li>控制編輯區是否顯示的邏輯，參見 {@link #isEditorVisible()}</li>
 * 	<li>資料列表元件選取某筆資料、按鈕按下的共通邏輯。各頁面特有的邏輯則在 do____ 各自實作，例如 {@link #doAdd()}</li>
 * 	<li>存檔、刪除時可處理 {@link #doSave()}、{@link #doDelete()} 拋出的 {@link UIException}。</li>
 * </ul>
 * <p>
 * ZUL 對應 command 清單：
 * <ul>
 * 	<li>資料列表選取某筆資料：selectData</li>
 * 	<li>操作按鈕的 onClick：add, save, cancel, delete</li>
 * </ul>
 * <p>
 * ZUL 其他配合事項：
 * <ul>
 * 	<li>
 * 		在編輯區內（嚴格說法：在掛 form binding 的 component 的 id space 內），
 * 		需要有一個恆為顯示的 component，其 visible 的值為<code>@load(vm.triggerStatus(fxStatus.dirty))</code>。
 * 		參見 {@link #triggerStatus(boolean)}。
 * 	</li>
 * 	<li>
 * 		form binding 的 middle object 為 {@link #currentData}，save 的寫法為
 * 		<code>@save(vm.currentData, before='save')</code>
 * 	</li>
 * </ul>
 * <p>
 * <b>注意：</b>child class 必須要有掛 <code>@Init(superclass=true)</code> 的 method。
 * 
 * @author MontyPan
 */
public abstract class BaseMaintainViewModel<T extends Entity> extends BaseEntityViewModel<T> {
	private T backupData;
	
	private String status;
	private boolean newFlag;
	private boolean editFlag;
	private boolean deleteFlag;
	private SimpleForm editorForm = new SimpleForm();
		
	//==== Confirm 的 EventListener 區 ====//
	private EventListener<Event> selectConfirm = new EventListener<Event>() {
		@Override
		public void onEvent(Event event) throws Exception {
			if(Messagebox.ON_YES.equals(event.getName())) {
				processSelect();
			}
		}
	};
	
	private EventListener<Event> cancelConfirm = new EventListener<Event>() {
		@Override
		public void onEvent(Event event) throws Exception {
			if(Messagebox.ON_YES.equals(event.getName())) {
				processCancel();
			}
		}
	};
	
	private EventListener<Event> deleteConfirm = new EventListener<Event>() {
		@Override
		public void onEvent(Event event) throws Exception {
			if(Messagebox.ON_YES.equals(event.getName())) {
				processDelete();
			}
		}
	};
	// ======== //
		
	@Init(superclass=true)
	public void baseMaintainInit() {}
	
	protected T newEntity() {
		try {
			return entityClass.newInstance();
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	@Command
	public void selectData() {
		if (editFlag && getEditorForm().isDirty()) {	//在編輯中又選了資料
			MessageBoxUtil.confirm(Labels.getLabel("dtc.zk.confirm.editorDirty"), selectConfirm);
			return;
		}
		processSelect();
	}
	
	private void processSelect() {
		currentData = getListModel().getSelection().iterator().next();
		backupData = currentData.<T>cloneEntity();
		
		status = Labels.getLabel("dtc.zk.ui.status.update");
		newFlag = false;
		editFlag = true;
		deleteFlag = true;
		
		doSelectData();
		refreshStatus();
		notifyChange("currentData");
	}
		
	/**
	 * 資料列表元件選取後的客製邏輯。
	 */
	protected abstract void doSelectData();
	
	/**
	 * 因為 {@link BaseEntityViewModel#search()} 沒有處理 editFlag，
	 * 所以這裡呼叫完 super 之後，會將 {@link #editFlag} 設定為 false 然後呼叫 {@link #refreshStatus()}。
	 */
	@Override
	@Command
	public void search() {
		super.search();
		editFlag = false;
		refreshStatus();
	}
	
	@Command
	@NotifyChange("currentData")
	public void add() {
		//避免因為還沒 onBlur 導致 fxStatus 沒更新導致按鈕還沒 disable 就按下去的哏
		if (getEditorForm().isDirty()) { return; }
		
		getListModel().clearSelection();
		currentData = newEntity();
		backupData = null;

		status = Labels.getLabel("dtc.zk.ui.status.add");
		newFlag = true;
		editFlag = true;
		deleteFlag = false;
		
		doAdd();
		refreshStatus();
	}
	
	/**
	 * 按下「新增」按鈕後的客製邏輯。
	 */
	protected abstract void doAdd();
	
	@Command
	@NotifyChange("currentData")
	public void delete() {
		//避免因為還沒 onBlur 導致 fxStatus 沒更新導致按鈕還沒 disable 就按下去的哏
		if (getEditorForm().isDirty()) { return; }
		MessageBoxUtil.confirm(Labels.getLabel("dtc.zk.confirm.delete"), deleteConfirm);
	}
	
	private void processDelete() {
		try {
			doDelete();
			currentData = null;
			status = null;
			newFlag = false;
			deleteFlag = false;
			//newFlag = false 跟 refreshStatus() 改成在 search() 裡頭作
			search();
		} catch (UIException e) {
			MessageBoxUtil.info(e.getMessage());
		} catch (Exception e) {
			MessageBoxUtil.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 按下「刪除」按鈕後的客製邏輯。
	 * 如果有已知的例外狀況，直接丟出 {@link UIException} 即可。
	 * @throws Exception
	 */
	protected abstract void doDelete() throws Exception;
	
	@Command
	public void cancel() {
		if (getEditorForm().isDirty()) {
			MessageBoxUtil.confirm(Labels.getLabel("dtc.zk.confirm.editorDirty"), cancelConfirm);
			return;
		}
		processCancel();
	}
	
	private void processCancel() {
		currentData = backupData;
		
		status = null;
		newFlag = false;
		editFlag = false;
		deleteFlag = false;
		
		doCancel();
		refreshStatus();
		notifyChange("currentData");
	}
	
	/**
	 * 按下「取消」按鈕後的客製邏輯。
	 */
	protected abstract void doCancel();
	
	@Command
	@NotifyChange("currentData")
	public void save() {
		try {
			doSave();
						
			//doSave() 可以炸 exception 來表示新增 / 儲存的時候遇到的狀況
			//所以下面這些東西必須在 doSave()（成功）之後才能作
			currentData = null;
			status = null;
			newFlag = false;
			deleteFlag = false;
			//newFlag = false 跟 refreshStatus() 改成在 search() 裡頭作
			search();
		} catch (UIException e) {
			MessageBoxUtil.info(e.getMessage());
		} catch (Exception e) {
			MessageBoxUtil.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 按下「存檔」按鈕後的客製邏輯。
	 * 如果有已知的例外狀況，直接丟出 {@link UIException} 即可。
	 * @throws Exception
	 */
	protected abstract void doSave() throws Exception;

	//注意：不能包含 currentData，否則 triggerStatus 會造成無窮迴圈
	//所以currentData要由save,cancel,delete,add,selectData行為時自己去控制notifyChange
	private static final String[] STATUS_FIELD = {
		"status", "disableAdd", "disableSave", "disableCancel", "disableDelete", "editorVisible"
	};
	private void refreshStatus() {
		notifyChange(STATUS_FIELD);
	}

	/**
	 * 參數無用、回傳值也無意義，
	 * 純粹就是為了讓 zul 有辦法在 fxStatus 的 loader 觸發時作 {@link #refreshStatus()}。
	 * <p><b>建議用在一個一定會顯示的 widget 的 visible 屬性上。</b></p>
	 */
	public boolean triggerStatus(boolean dirty) {
		refreshStatus();
		return true;
	}
	
	/**
	 * 以程式去設定 {@link #editorForm}（Form binding 的 middle object）的內容值，
	 * 並且連帶對 {@link #editorForm} 以及其 {@link FormStatus}（的 dirty）發 notify change。
	 * 
	 * @param name
	 * @param value
	 */
	protected void setEditorFormField(String name, Object value) {
		getEditorForm().setField(name, value);
		BindUtils.postNotifyChange(null, null, editorForm, name);
		BindUtils.postNotifyChange(null, null, editorForm.getStatus(), "dirty");
	}

	//==== setter / getter 區 ====//
	public boolean isNewFlag() {
		return newFlag;
	}
	
	public Form getEditorForm() {
		return editorForm;
	}

	public String getStatus() {
		return status;
	}

	protected void setStatus(String status) {
		this.status = status;
	}

	public boolean isEditorVisible() {
		return editFlag;
	}
	
	public boolean isDisableAdd() {
		return getEditorForm().isDirty();
	}
	
	public boolean isDisableSave() {
		return !getEditorForm().isDirty();
	}
	
	public boolean isDisableCancel() {
		return !getEditorForm().isDirty();
	}
	
	public boolean isDisableDelete() {
		return getEditorForm().isDirty() || !deleteFlag;
	}
}