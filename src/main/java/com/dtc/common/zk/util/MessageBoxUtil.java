package com.dtc.common.zk.util;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

public class MessageBoxUtil {
	static { I18nLocator.register(); }
	
	public static void error(String message) {
		String title = Labels.getLabel("dtc.zk.error.title");
		Messagebox.show(message, title, Messagebox.OK, Messagebox.ERROR);
	}

	public static void info(String message) {
		String title = Labels.getLabel("dtc.zk.info.title");
		Messagebox.show(message, title, Messagebox.OK, Messagebox.INFORMATION);
	}

	public static void confirm(String message, EventListener<Event> listener) {
		String title = Labels.getLabel("dtc.zk.confirm.title");
		Messagebox.show(
			message, 
			title, 
			Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, 
			listener
		);
	}

	public static boolean confirm(String message) {
		String title = Labels.getLabel("dtc.zk.confirm.title");
		int buttonIndex = Messagebox.show(message, title, Messagebox.YES | Messagebox.NO, Messagebox.QUESTION);
		return (buttonIndex == Messagebox.OK);
	}
}
