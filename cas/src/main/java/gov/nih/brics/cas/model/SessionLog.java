package gov.nih.brics.cas.model;

import java.util.Date;

public class SessionLog {
	private String tgt;
	private String username;
	private Date timeIn;
	private Date timeOut;
	private String fullName;
	private String email;
	public String getTgt() {
		return tgt;
	}
	public void setTgt(String tgt) {
		this.tgt = tgt;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Date getTimeIn() {
		return timeIn;
	}
	public void setTimeIn(Date timeIn) {
		this.timeIn = timeIn;
	}
	public Date getTimeOut() {
		return timeOut;
	}
	public void setTimeOut(Date timeOut) {
		this.timeOut = timeOut;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
