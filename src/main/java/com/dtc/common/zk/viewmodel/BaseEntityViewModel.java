package com.dtc.common.zk.viewmodel;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.ListModelList;

import com.dtc.boundary.Entity;
import com.dtc.common.core.date.DateUtil;

/**
 * 「單一 entity 資料顯示」頁面的基礎 ViewModel，
 * 頁面上會包含一個使用 {@link ListModelList} 為 model 的資料列表元件，並提供搜尋功能。
 * <p>
 * <b>注意：</b>child class 必須要有掛 <code>@Init(superclass=true)</code> 的 method。
 * <p>
 * 
 * <h1>搜尋功能</h1>
 * 前提假設：
 * <ul>
 * 	<li>操作按鈕的 onClick：search, resetSearch</li>
 * 	<li>各個搜尋條件之間用 AND 連接</li>
 * </ul>
 * ZUL form binding 的對象為 {@link #constraint}，
 * middle object 各個 binding 的 attribute 名稱就是 entity 要搜尋的 attribute（欄位）名稱，
 * view model 必須 override {@link #queryData(String, Map)}，
 * 這樣各欄位就會以「等於（{@link Constraint#EQUAL}）」作為比對條件。
 * 若是 entity 的 attribute 也是 entity（假設 attribute 名稱為 <code>foo</code>，
 * 則 {@link #constraint} 必須在 {@link #afterResetSearch()} 作
 * <code>getConstraint().put("foo", new HashMap<String, Object>());</code>。
 * 
 * <p>
 * 如果需要「等於」以外的比對條件，則對 {@link #getConstraintMap()} 增加資料，
 * key 值為 entity 要搜尋的 attribute 名稱、value 值則為指定的比對條件 {@link Constraint}。
 * 換言之，如果沒有指定比對條件，就等於是 {@link Constraint#EQUAL}。
 * <p>
 * 另外有三個特例 attribute 名稱結尾：
 * <ul>
 * 	<li>
 * 		{@value #CONSTRAINT_FROM}、{@value #CONSTRAINT_START}：代表搜尋欄位為去掉 {@value #CONSTRAINT_FROM} 的剩餘字串、搜尋條件為 {@link Constraint#NOT_SMALLER}
 * 		<ul>
 * 			<li>{@value #CONSTRAINT_FROM}：會使用 {@link #magicConvert(Object, Constraint)} 轉換</li>
 *		</ul>
 * 	</li>
 * 	<li>
 * 		{@value #CONSTRAINT_TO}、{@value #CONSTRAINT_END}：代表搜尋欄位為去掉 {@value #CONSTRAINT_TO} 的剩餘字串、搜尋條件為 {@link Constraint#NOT_BIGGER}
 * 		<ul>
 * 			<li>{@value #CONSTRAINT_TO}：會使用 {@link #magicConvert(Object, Constraint)} 轉換</li>
 *		</ul>
 *	</li>
 * 	<li>
 * 		{@value #CONSTRAINT_BETWEEN}：代表搜尋欄位為去掉 {@value #CONSTRAINT_BETWEEN} 的剩餘字串，
 * 		搜尋條件會變成兩個，分別為 {@link Constraint#NOT_SMALLER} 與 {@link Constraint#NOT_BIGGER}。
 * 	</li>
 * </ul>
 * <b>注意：</b>使用 {@link Constraint#LIKE}，比對值會是以該物件的 toString() 回傳值前後再加上「%」。
 * 
 * @author MontyPan
 */
public abstract class BaseEntityViewModel<T extends Entity> extends BaseViewModel {
	private static final String CONSTRAINT_FROM = "_from";
	private static final String CONSTRAINT_TO = "_to";
	private static final String CONSTRAINT_START = "_start";
	private static final String CONSTRAINT_END = "_end";
	private static final String CONSTRAINT_BETWEEN = "_between";
	
	//這幾個欄位就偷懶不設 private 了 [逃]
	protected final Class<T> entityClass;
	protected T currentData;
	
	private ListModelList<T> dataStore;
	private HashMap<String, Object> constraint = new HashMap<String, Object>();
	private HashMap<String, Constraint> opMap = new HashMap<String, Constraint>();
	
	/**
	 * 會做 {@link #afterResetSearch()}。
	 */
	@SuppressWarnings("unchecked")
	protected BaseEntityViewModel() {
		Type type = getClass().getGenericSuperclass();
		entityClass = (Class<T>)((ParameterizedType)type).getActualTypeArguments()[0];
		dataStore = new ListModelList<T>();
		afterResetSearch();
	}
	
	@Init(superclass=true)
	public void baseEntityInit() {}
	
	@AfterCompose
	public void baseEntityAfterCompose() {
		search();
	}
	
	/**
	 * @return 資料列表元件要顯示的資料
	 */
	protected abstract List<T> queryData() throws Exception;
	
	/**
	 * 重點：
	 * <ul>
	 * 	<li>掛 {@link Command} annotation</li>
	 * 	<li>設定 currentData：<code>currentData = dataStore.getSelection().iterator().next();</code></li>
	 * 	<li>notify change <code>currentData</code>
	 * </ul>
	 */
	//會作成 abstract 的原因是 BaseMaintainViewModel.selectData() 的實際邏輯太複雜
	//真正共通要作的事情又太簡單，就乾脆算啦... [茶]
	public abstract void selectData();

	// ======== 搜尋區 ======== //
	/**
	 * @return 資料列表元件要顯示的、符合特定搜尋條件的資料。
	 * 	基本上就是呼叫 entity 對應的 find(String, Map)。
	 */
	protected List<T> queryData(String hql, Map<String, Object> params) throws Exception {
		throw new UnsupportedOperationException("預設無此行為，請自行實作");
	}
	
	/**
	 * 在 {@link #resetSearch()} 中 {@link #constraint} 清空之後讓 child class 可以自訂一些操作的 method。
	 */
	protected void afterResetSearch() {}
	
	@Command
	@NotifyChange("constraint")
	public void resetSearch() {
		constraint.clear();
		afterResetSearch();
	}
	
	@Command
	public void search() {
		StringBuffer hql = new StringBuffer("from " + entityClass.getSimpleName() + " where 1=1 ");
		HashMap<String, Object> params = new HashMap<String, Object>();
		recursiveBuild(hql, params, getConstraint(), "");	//一開始根本沒有 childname
		
		dataStore.clear();
		try {
			if (params.size() == 0) {
				dataStore.addAll(queryData());
			} else {
				dataStore.addAll(queryData(hql.toString(), params));
			}
		} catch (Exception e) {
			handleDaoException(e);
		}
		
		afterSearch();
	}
	
	/**
	 * 在 {@link #search()} 完畢之後讓 child class 可以自訂一些操作的 method。
	 */
	protected void afterSearch() {}

	/**
	 * 用遞迴的方式解決 children binding 的 search 問題。
	 */
	@SuppressWarnings("unchecked")
	private void recursiveBuild(StringBuffer hql, HashMap<String, Object> params, HashMap<String, Object> valueMap, String childName) {
		HashMap<String, Constraint> opMap = getConstraintMap();
		
		for (String key : valueMap.keySet()) {
			Object value = valueMap.get(key);
			
			if (value == null) { continue; }
			if (value instanceof String && StringUtils.isEmpty(value.toString())) { continue; }
			
			String field = childName + key;
			
			if (value instanceof HashMap) {
				recursiveBuild(hql, params, (HashMap<String, Object>)value, field + ".");
				continue;
			}
			
			String paramName = convertParamName(childName + key);

			// ==== 區間特別處理區 ==== //
			//直接看 tail，無視 operatorMap
			if (key.endsWith(CONSTRAINT_FROM)) {
				String realField = childName + key.substring(0, key.indexOf(CONSTRAINT_FROM));
				hql.append("AND " + realField + " ");
				hql.append(Constraint.NOT_SMALLER.getOpString());
				hql.append(" :" + paramName + " ");
				params.put(paramName, magicConvert(value, Constraint.NOT_SMALLER));
				continue;
			}
			if (key.endsWith(CONSTRAINT_START)) {
				String realField = childName + key.substring(0, key.indexOf(CONSTRAINT_START));
				hql.append("AND " + realField + " ");
				hql.append(Constraint.NOT_SMALLER.getOpString());
				hql.append(" :" + paramName + " ");
				params.put(paramName, value);
				continue;
			}
			if (key.endsWith(CONSTRAINT_TO)) {
				String realField = childName + key.substring(0, key.indexOf(CONSTRAINT_TO));
				hql.append("AND " + realField + " ");
				hql.append(Constraint.NOT_BIGGER.getOpString());
				hql.append(" :" + paramName + " ");
				params.put(paramName, magicConvert(value, Constraint.NOT_BIGGER));
				continue;
			}
			if (key.endsWith(CONSTRAINT_END)) {
				String realField = childName + key.substring(0, key.indexOf(CONSTRAINT_END));
				hql.append("AND " + realField + " ");
				hql.append(Constraint.NOT_BIGGER.getOpString());
				hql.append(" :" + paramName + " ");
				params.put(paramName, value);
				continue;
			}
			if (key.endsWith(CONSTRAINT_BETWEEN)) {
				//就是把 _between 轉成 _from 跟 _to，懶得再抽共用 method 了（才兩次... 才兩次阿  [逃]）
				String realField = childName + key.substring(0, key.indexOf(CONSTRAINT_BETWEEN));
				String newFrom = convertParamName(realField + CONSTRAINT_FROM);
				hql.append("AND " + realField + " ");
				hql.append(Constraint.NOT_SMALLER.getOpString());
				hql.append(" :" + newFrom + " ");
				params.put(newFrom, magicConvert(value, Constraint.NOT_SMALLER));
				String newTo = convertParamName(realField + CONSTRAINT_TO);
				hql.append("AND " + realField + " ");
				hql.append(Constraint.NOT_BIGGER.getOpString());
				hql.append(" :" + newTo + " ");
				params.put(newTo, magicConvert(value, Constraint.NOT_BIGGER));
				continue;
			}
			// ======== //
			
			hql.append("AND " + field + " ");
			Constraint op = opMap.get(field);

			if (op == null) {	//沒有指定 operator 就是等於
				op = Constraint.EQUAL;
			}

			hql.append(op.getOpString());
			
			switch(op) {
			case NOT_BIGGER:
			case NOT_SMALLER:
			case SMALLER:
			case BIGGER:
				params.put(paramName, magicConvert(value, op));
				break;
			case LIKE:
				params.put(paramName, "%" + value + "%");
				break;
			case EQUAL:
			case NOT_EQUAL:
				params.put(paramName, value);
				break;
			
			}
			hql.append(" :" + paramName + " ");
		}
	}
	
	/**
	 * HQL 的 param name 遇到「.」會出問題，所以轉換成「_」
	 */
	private static String convertParamName(String fieldName) {
		return fieldName.replace(".", "_");
	}
	/**
	 * 用各種黑魔法把搜尋的值調整為想要的值，例如處理日期區間必須將 UI 傳入的日期改為當天第一秒 / 最後一秒。
	 */
	private Object magicConvert(Object obj, Constraint op) {
		if (obj instanceof Date) {
			if (op == Constraint.NOT_BIGGER || op == Constraint.SMALLER) {
				return DateUtil.getEndDate((Date)obj);
			}
			if (op == Constraint.BIGGER || op == Constraint.NOT_SMALLER) {
				return DateUtil.getStartDate((Date)obj);
			}
		}
		return obj;
	}
	// ======== 搜尋區結束 ======== //
	
	// ==== getter / setter 區 ==== //
	public final ListModelList<T> getListModel() {
		return dataStore;
	}
		
	public final T getCurrentData() {
		return currentData;
	}

	public final void setCurrentData(T currentData) {
		this.currentData = currentData;
	}

	/**
	 * @return 搜尋欄位名稱與搜尋值的 map
	 * @see #getConstraintMap()
	 */
	public final HashMap<String, Object> getConstraint() {
		return constraint;
	}
	
	/**
	 * @return key 值為搜尋欄位、value 值為比對條件
	 * @see #getConstraint()
	 */
	public final HashMap<String, Constraint> getConstraintMap() {
		return opMap;
	}
}