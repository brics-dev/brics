package gov.nih.nichd.ctdb.protocol.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

public class ConfigureEformAuditDetail extends CtdbDomainObject {
	
	private int sectionId = -1;
	private int questionId = -1;
	private String action;
	private String sectionText;
	private String questionText;
	private String username;
	
	
	
	
	
	

	public String getSectionText() {
		return sectionText;
	}







	public void setSectionText(String sectionText) {
		this.sectionText = sectionText;
	}







	public String getQuestionText() {
		return questionText;
	}







	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}







	public String getUsername() {
		return username;
	}







	public void setUsername(String username) {
		this.username = username;
	}







	public int getSectionId() {
		return sectionId;
	}







	public void setSectionId(int sectionId) {
		this.sectionId = sectionId;
	}







	public int getQuestionId() {
		return questionId;
	}







	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}







	public String getAction() {
		return action;
	}







	public void setAction(String action) {
		this.action = action;
	}







	@Override
	public Document toXML() throws TransformationException {
		throw new UnsupportedOperationException("toXML() is not supported in ConfigureEformAuditDetail.");
	}

}
