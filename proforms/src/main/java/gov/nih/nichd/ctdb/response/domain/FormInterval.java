package gov.nih.nichd.ctdb.response.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

public class FormInterval extends CtdbDomainObject {
	private static final long serialVersionUID = -2885103793104882551L;
	
	private int formId;
	private String formNameLink;
	private String formName;
	private String formNameLeftNav;
	private String formNameLinkLeftNav;
	private String dataCollectionStatus;
	private String required;
	private int intervalOrder;
	private int userId;
	
	public int getIntervalOrder() {
		return intervalOrder;
	}

	public void setIntervalOrder(int intervalOrder) {
		this.intervalOrder = intervalOrder;
	}

	public String getFormNameLink() {
		return formNameLink;
	}
	public void setFormNameLink(String formNameLink) {
		this.formNameLink = formNameLink;
	}
	public int getFormId() {
		return formId;
	}
	public void setFormId(int formId) {
		this.formId = formId;
	}
	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}
	public String getDataCollectionStatus() {
		return dataCollectionStatus;
	}
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public void setDataCollectionStatus(String dataCollectionStatus) {
		this.dataCollectionStatus = dataCollectionStatus;
	}
	public String getRequired() {
		return required;
	}
	public void setRequired(String required) {
		this.required = required;
	}

	public String getFormNameLeftNav() {
		return formNameLeftNav;
	}

	public void setFormNameLeftNav(String formNameLeftNav) {
		this.formNameLeftNav = formNameLeftNav;
	}

	public String getFormNameLinkLeftNav() {
		return formNameLinkLeftNav;
	}

	public void setFormNameLinkLeftNav(String formNameLinkLeftNav) {
		this.formNameLinkLeftNav = formNameLinkLeftNav;
	}

	public Document toXML() throws TransformationException {
		throw new UnsupportedOperationException("toXML() not supported in FormInterval.");
	}

}
