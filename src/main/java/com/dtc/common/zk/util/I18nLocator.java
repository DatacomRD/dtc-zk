package com.dtc.common.zk.util;

import java.io.InputStream;
import java.util.Locale;

import org.zkoss.util.resource.LabelLocator2;
import org.zkoss.util.resource.Labels;

public class I18nLocator implements LabelLocator2 {
	private static final String HEADER = "i18n";
	private static final String TAIL = ".properties";
	private static I18nLocator instance;
	
	public static void register() {
		if (instance != null) { return; }
		
		instance = new I18nLocator();
		Labels.register(instance);
	}
	
	private I18nLocator() {}
	
	@Override
	public InputStream locate(Locale locale) {
		String file = locale == null ? HEADER + TAIL : 
			HEADER + "_" + locale + TAIL;
		return I18nLocator.class.getClassLoader().getResourceAsStream(file);
	}

	@Override
	public String getCharset() {
		return "UTF-8";
	}
}