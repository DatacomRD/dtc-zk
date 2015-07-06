package com.dtc.common.zk.util;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

public class MessageBoxUtil {
	static {
		//FIXME
		Messagebox.setTemplate("/WEB-INF/pages/zul/zk/zul/html/MioboxMessagebox.zul");
	}

	public static void error(String message) {
		String title = Labels.getLabel("msg.error.title");
		Messagebox.show(message, title, Messagebox.OK, Messagebox.ERROR);
	}

	public static void info(String message) {
		String title = Labels.getLabel("msg.info.title");
		Messagebox.show(message, title, Messagebox.OK, Messagebox.INFORMATION);
	}

	public static void confirm(String message, EventListener<Event> listener) {
		String title = Labels.getLabel("msg.confirm.title");
		Messagebox.show(
			message, 
			title, 
			Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, 
			listener
		);
	}

	public static boolean confirm(String message) {
		String title = Labels.getLabel("msg.confirm.title");
		int buttonIndex = Messagebox.show(message, title, Messagebox.YES | Messagebox.NO, Messagebox.QUESTION);
		return (buttonIndex == Messagebox.OK);
	}
}
