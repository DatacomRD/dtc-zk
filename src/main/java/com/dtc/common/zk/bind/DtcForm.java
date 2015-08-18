package com.dtc.common.zk.bind;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.zkoss.bind.Form;
import org.zkoss.bind.FormExt;
import org.zkoss.bind.FormStatus;
import org.zkoss.bind.impl.FormImpl;
import org.zkoss.lang.Objects;

/**
 * 原先 ZK 的設計中，middle object 的 class 是固定的，
 * 如果 middle object 要改變 class，就會導致 form binding 出錯。
 * 所以提供 {@link #clear()}，在 middle object 改變 class 時呼叫。
 * 其餘邏輯以及程式碼都是直接複製 {@link FormImpl}。
 */
public class DtcForm implements Form,FormExt,Serializable {
	private static final long serialVersionUID = 1463169907348730644L;
	
	private final Set<String> _saveFieldNames; //field name for saving
	private final Set<String> _loadFieldNames; //field name for loading
	private final Map<String, Object> _fields; //field series -> value
	private final Map<String, Object> _initFields; //field series -> value
	private final Set<String> _dirtyFieldNames; //field name that is dirty
	private static final int INIT_CAPACITY = 32;
	
	private final FormStatus _status;
	
	public DtcForm() {
		_fields = new LinkedHashMap<String, Object>(INIT_CAPACITY);
		_initFields = new HashMap<String, Object>(INIT_CAPACITY);
		_saveFieldNames = new LinkedHashSet<String>(INIT_CAPACITY);
		_loadFieldNames = new LinkedHashSet<String>(INIT_CAPACITY);
		_dirtyFieldNames = new HashSet<String>(INIT_CAPACITY);
		_status = new FormStatusImpl();
	}
	
	private class FormStatusImpl implements FormStatus,Serializable{
		private static final long serialVersionUID = 1L;
		@Override
		public boolean isDirty() {
			return DtcForm.this.isDirty();
		}
	}

	public void clear() {
		_saveFieldNames.clear();
		_loadFieldNames.clear();
		_fields.clear();
		_initFields.clear();
		_dirtyFieldNames.clear();
	}
	
	@Override
	public void setField(String field, Object value) {
		_fields.put(field, value);
		final Object init = _initFields.get(field);
		if (!Objects.equals(init, value)) { //different from original
			_dirtyFieldNames.add(field);
		} else {
			_dirtyFieldNames.remove(field);
		}
	}
	
	@Override
	public void resetDirty() {
		_initFields.putAll(_fields);
		_dirtyFieldNames.clear();
	}
	
	@Override
	public Object getField(String field) {
		return _fields.get(field);
	}
	
	@Override
	public Set<String> getLoadFieldNames() {
		return _loadFieldNames;
	}

	@Override
	public Set<String> getSaveFieldNames() {
		return _saveFieldNames;
	}
	
	@Override
	public Set<String> getFieldNames() {
		return _fields.keySet();
	}

	@Override
	public boolean isDirty() {
		return !_dirtyFieldNames.isEmpty();
	}
	
	@Override
	public void addLoadFieldName(String fieldName) {
		_loadFieldNames.add(fieldName);
	}

	@Override
	public void addSaveFieldName(String fieldName) {
		_saveFieldNames.add(fieldName);
	}
	
	@Override
	public FormStatus getStatus() {
		return _status;
	}
}
