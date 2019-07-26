package gov.nih.nichd.ctdb.form.tag;

import java.text.SimpleDateFormat;
import java.util.Date;

import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class DateFormatDecorator extends IdtDecorator {
	
	String field = "";
	
	public DateFormatDecorator(String field) {
		this.field = field;
	}
	
	public String getDate() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(SysPropUtil.getProperty("default.system.datetimeformat"));
		Object fieldProp = getProperty(field);
		return dateFormat.format((Date)fieldProp);
	}
}
