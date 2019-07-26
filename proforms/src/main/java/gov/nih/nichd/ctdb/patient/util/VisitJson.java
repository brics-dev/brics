package gov.nih.nichd.ctdb.patient.util;

import gov.nih.nichd.ctdb.patient.domain.PatientVisit;

import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

public class VisitJson extends JSONObject {
	private Date date;
	private String description;
	private String guid;
	private String subjectId;
	private String type;
	private String protocolId;
	private String protocolNumber;
	
	
	public static VisitJson fromPatientVisit(PatientVisit visit) {
		VisitJson jsonVisit = new VisitJson();
		jsonVisit.setDate(visit.getVisitDate());
		jsonVisit.setDescription(visit.getLabel());
		jsonVisit.setGuid(visit.getGuid());
		jsonVisit.setType(visit.getIntervalName());
		jsonVisit.setSubjectId(String.valueOf(visit.getPatientId()));
		jsonVisit.setProtocolId(String.valueOf(visit.getProtocolId()));
		jsonVisit.setProtocolNumber(visit.getProtocolNumber());
		return jsonVisit;
	}


	public Date getDate() {
		return date;
	}
	
	protected String getCalendarField(int calendarFieldValue) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return String.valueOf(cal.get(calendarFieldValue));
	}
	
	public String getYear() {
		return getCalendarField(Calendar.YEAR);
	}
	
	public String getMonth() {
		return String.valueOf(Integer.decode(getCalendarField(Calendar.MONTH)) + 1);
	}
	
	public String getDay() {
		return getCalendarField(Calendar.DATE);
	}
	
	public String getHour() {
		return getCalendarField(Calendar.HOUR_OF_DAY);
	}
	
	public String getMinute() {
		String minute = getCalendarField(Calendar.MINUTE);
		if (Integer.decode(minute) < 10) {
			minute = "0" + minute;
		}
		return minute;
	}


	public void setDate(Date date) {
		this.date = date;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getGuid() {
		return guid;
	}


	public void setGuid(String guid) {
		this.guid = guid;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}
	
	public String getSubjectId() {
		return subjectId;
	}


	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}


	public void setProtocolId(String protocolId) {
		this.protocolId = protocolId;
	}


	public String getProtocolId() {
		return protocolId;
	}


	public String getProtocolNumber() {
		return protocolNumber;
	}


	public void setProtocolNumber(String protocolNumber) {
		this.protocolNumber = protocolNumber;
	}


	public String toString() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("year", Integer.decode(this.getYear()));
			obj.put("month", Integer.decode(this.getMonth()));
			obj.put("day", Integer.decode(this.getDay()));
			obj.put("hour", this.getHour());
			obj.put("minute", this.getMinute());
			obj.put("description", this.getDescription());
			obj.put("subject", this.getGuid());
			obj.put("subjectId", this.getSubjectId());
			obj.put("type", this.getType());
			obj.put("protocolId", this.getProtocolId());
			obj.put("protocolNumber", this.getProtocolNumber());
			return obj.toString();
		}
		catch(Exception e) {
			return "{}";
		}
	}
}
