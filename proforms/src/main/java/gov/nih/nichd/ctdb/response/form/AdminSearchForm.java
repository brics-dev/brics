package gov.nih.nichd.ctdb.response.form;

import gov.nih.nichd.ctdb.common.CtdbForm;

public class AdminSearchForm extends CtdbForm{

	public AdminSearchForm() {
		super();
	}

	private static final long serialVersionUID = -2268723755718107237L;
	
	private String studyPrefixId;
	private String protocolNum;
	private int adminFormId;
	private String guid;
	private String shortName;
	private String visitType;
	
	public String getStudyPrefixId() {
		return studyPrefixId;
	}
	public void setStudyPrefixId(String studyPrefixId) {
		this.studyPrefixId = studyPrefixId;
	}
	public String getProtocolNum() {
		return protocolNum;
	}
	public void setProtocolNum(String protocolNum) {
		this.protocolNum = protocolNum;
	}
	public int getAdminFormId() {
		return adminFormId;
	}
	public void setAdminFormId(int adminFormId) {
		this.adminFormId = adminFormId;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getVisitType() {
		return visitType;
	}
	public void setVisitType(String visitType) {
		this.visitType = visitType;
	}
	
	
}