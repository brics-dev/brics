package gov.nih.nichd.ctdb.drugDevice.domain;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Developed by CIT.
 * @author Shashi Rudrappa
 * Date: May 01, 2012
 * @version 1.0
 */
public class DrugDevice extends CtdbDomainObject implements Serializable{
	private String fdaInd;
	private String sponsor;
	private int protocolId;
    private String drugDeviceActionFlag;//To store user action of adding/editing/deleting drug and device. 

	public String getFdaInd() {
		return fdaInd;
	}
	public void setFdaInd(String fdaInd) {
		this.fdaInd = StringUtils.trim(fdaInd);
	}
	public String getSponsor() {
		return sponsor;
	}
	public void setSponsor(String sponsor) {
		this.sponsor = StringUtils.trim(sponsor);
	}
	public int getProtocolId() {
		return protocolId;
	}
	public void setProtocolId(int protocolId) {
		this.protocolId = protocolId;
	}

	public Document toXML() throws TransformationException, UnsupportedOperationException
	{
		throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in Address.");
	}
	public String getDrugDeviceActionFlag() {
		return drugDeviceActionFlag;
	}
	public void setDrugDeviceActionFlag(String drugDeviceActionFlag) {
		this.drugDeviceActionFlag = StringUtils.trim(drugDeviceActionFlag);
	}	
}
