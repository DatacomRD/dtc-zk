package com.dtc.common.zk.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.util.TimeZones;
import org.zkoss.zk.ui.Component;

/**
 * DICOM 的日期是字串，格式是 <code>yyyyMMddHHmmss.SSSSSS</code>
 * 這個 converter 可以在 ZUL 當中指定 format，
 * 作 DICOM 日期（bean）與字串（component attribute）之間的轉換。
 * 例如要轉換成「yyyy 年 MM 月」：
 * <pre>
 * &lt;label value="@load(vm.dicomDate) @converter(vm.converter, format='yyyy 年 MM 月')" /&gt;
 * </pre>
 * 如果沒有給 <code>format</code>，則預設使用 <code>yyyy/MM/dd</code>。
 * <p>
 * 如果 DICOM 日期長度不足，不足的部份會自動填上 0 跟「.」才進行轉換。
 * 如果 DICOM 日期轉字串失敗，會回傳空字串。
 * <p>
 * 字串轉 DICOM 日期時可以限定長度，例如只想取到日期，則
 * <pre>
 * &lt;label value="@load(vm.dicomDate) @converter(vm.converter, format='yyyy/MM/dd', limit=8)" /&gt;
 * </pre>
 * 如果字串轉 DICOM 日期失敗，會回傳 null。
 * <p>
 * 備註：
 * <ol>
 * 	<li>以字串的角度 VR.DT 包含 VR.DA，所以直接使用 VR.DT 的格式</li>
 * 	<li>VR.DT 的 UTC offset 不處理</li>
 * </ol>
 */
public class DicomDateToStringConverter implements Converter<String, String, Component>{
	public static final DicomDateToStringConverter INSTANCE = new DicomDateToStringConverter();

	private static final String VR_DT_FORMAT = "yyyyMMddHHmmss.SSSSSS";
	private static final SimpleDateFormat DICOM_PATTERN = new SimpleDateFormat(VR_DT_FORMAT);
	private static final String DEFAULT_PATTERN = "yyyy/MM/dd";

	@Override
	public String coerceToUi(String beanProp, Component component, BindContext ctx) {
		try {
			return getFormat(ctx).format(DICOM_PATTERN.parse(fill(beanProp)));
		} catch (Exception e) {
			return "";
		}
	}

	@Override
	public String coerceToBean(String compAttr, Component component, BindContext ctx) {
		int endIndex = VR_DT_FORMAT.length();

		try {
			if (ctx.getConverterArg("limit") != null) {
				endIndex = Integer.valueOf(ctx.getConverterArg("limit").toString());
			}
		} catch (Exception e) {}

		try {
			return DICOM_PATTERN.format(getFormat(ctx).parse(compAttr)).substring(0, endIndex);
		} catch (ParseException e) {
			return null;
		}
	}

	private static SimpleDateFormat getFormat(BindContext ctx) {
		final String format = (String) ctx.getConverterArg("format");
		SimpleDateFormat result = new SimpleDateFormat(
			format == null ? DEFAULT_PATTERN : format
		);
		result.setTimeZone(TimeZones.getCurrent());
		return result;
	}

	private static String fill(String beanProp) {
		StringBuffer sb = new StringBuffer(VR_DT_FORMAT.length());
		sb.append(beanProp);

		for (int i = beanProp.length(); i < 14; i++) {
			sb.append(0);
		}

		if (beanProp.length() < 15) {
			sb.append(".");
		}

		for (int i = Math.max(sb.length(), beanProp.length()); i < VR_DT_FORMAT.length(); i++) {
			sb.append(0);
		}

		return sb.toString();
	}
}
