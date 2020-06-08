package gov.nih.nichd.ctdb.btris.domain;

import java.util.Date;


import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

public class BtrisObject extends CtdbDomainObject {
	private static final long serialVersionUID = -8118559881991376370L;

	private String btrisObservationName = "";
	private String btrisRedCode = "";
	private String btrisSpecimenType = "";
	private String btrisUnitOfMeasure = "";
	private String btrisRange = "";
	private String btrisTable = "";
	private String btrisValueText = "";
	private String btrisValueNumeric = "";
	private String btrisValueNameComment="";
	private Date btrisPrimaryDateTime;
	

	public BtrisObject() {}

	public BtrisObject(BtrisObject bo) {
		this.btrisObservationName = bo.btrisObservationName;
		this.btrisRedCode = bo.btrisRedCode;
		this.btrisSpecimenType = bo.btrisSpecimenType;
		this.btrisUnitOfMeasure = bo.btrisUnitOfMeasure;
		this.btrisRange = bo.btrisRange;
		this.btrisValueText = bo.btrisValueText;
		this.btrisValueNumeric = bo.btrisValueNumeric;
		this.btrisValueNameComment = bo.btrisValueNameComment;
		this.btrisPrimaryDateTime = bo.btrisPrimaryDateTime;
	}

	public String getBtrisObservationName() {
		return btrisObservationName;
	}

	public void setBtrisObservationName(String btrisObservationName) {
		this.btrisObservationName = btrisObservationName;
	}

	public String getBtrisRedCode() {
		return btrisRedCode;
	}

	public void setBtrisRedCode(String btrisRedCode) {
		this.btrisRedCode = btrisRedCode;
	}

	public String getBtrisSpecimenType() {
		return btrisSpecimenType;
	}

	public void setBtrisSpecimenType(String btrisSpecimenType) {
		this.btrisSpecimenType = btrisSpecimenType;
	}

	public String getBtrisUnitOfMeasure() {
		return btrisUnitOfMeasure;
	}

	public void setBtrisUnitOfMeasure(String btrisUnitOfMeasure) {
		this.btrisUnitOfMeasure = btrisUnitOfMeasure;
	}

	public String getBtrisRange() {
		return btrisRange;
	}

	public void setBtrisRange(String btrisRange) {
		this.btrisRange = btrisRange;
	}

	public String getBtrisTable() {
		return btrisTable;
	}

	public void setBtrisTable(String btrisTable) {
		this.btrisTable = btrisTable;
	}

	public String getBtrisValueText() {
		return btrisValueText;
	}

	public void setBtrisValueText(String btrisValueText) {
		this.btrisValueText = btrisValueText;
	}

	public String getBtrisValueNumeric() {
		return btrisValueNumeric;
	}

	public void setBtrisValueNumeric(String btrisValueNumeric) {
		this.btrisValueNumeric = btrisValueNumeric;
	}
	
	public String getBtrisValueNameComment() {
		return btrisValueNameComment;
	}

	public void setBtrisValueNameComment(String btrisValueNameComment) {
		this.btrisValueNameComment = btrisValueNameComment;
	}
	
	public Date getBtrisPrimaryDateTime() {
		return btrisPrimaryDateTime;
	}

	public void setBtrisPrimaryDateTime(Date btrisPrimaryDateTime) {
		this.btrisPrimaryDateTime = btrisPrimaryDateTime;
	}

	@Override
	public Document toXML() throws TransformationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return "BtrisObject [btrisObservationName=" + btrisObservationName + ", btrisRedCode=" + btrisRedCode
				+ ", btrisSpecimenType=" + btrisSpecimenType + ", btrisUnitOfMeasure=" + btrisUnitOfMeasure
				+ ", btrisRange=" + btrisRange + ", btrisTable=" + btrisTable + ", btrisValueText=" + btrisValueText
				+ ", btrisValueNumeric=" + btrisValueNumeric + ", btrisValueNameComment=" + btrisValueNameComment
				+ ", btrisPrimaryDateTime=" + btrisPrimaryDateTime + "]";
	}

}
