package gov.nih.nichd.ctdb.protocol.domain;

import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

public class ProtocolClosingOut extends CtdbDomainObject {
	private static final long serialVersionUID = -5219912918863278446L;
	
	private Integer protocolId;
	private String bricsStudyId;
	private String protocolName;
	private String protocolNumber;
	private Boolean protoEnableEsignature;
	private Integer closingUserId;
	private Long closingBricsUserId;
	private String closingUserName;
	private String closingUserFullName;
	private Date closingOutDate;
	private Boolean reopenStatus;
	private Date reopenDate;
	
	public ProtocolClosingOut() {
		
	}
	
	public Integer getProtocolId() {
		return this.protocolId;
	}
	public void setProtocolId(Integer protocolId) {
		this.protocolId = protocolId;
	}
	public String getBricsStudyId() {
		return this.bricsStudyId;
	}
	public void setBricsStudyId(String bricsStudyId) {
		this.bricsStudyId = bricsStudyId;
	}
	public String getProtocolName() {
		return this.protocolName;
	}
	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
	}
	public String getProtocolNumber() {
		return this.protocolNumber;
	}
	public void setProtocolNumber(String protocolNumber) {
		this.protocolNumber = protocolNumber;
	}
	public Boolean getProtoEnableEsignature() {
		return this.protoEnableEsignature;
	}
	public void setProtoEnableEsignature(Boolean protoEnableEsignature) {
		this.protoEnableEsignature = protoEnableEsignature;
	}
	public Integer getClosingUserId() {
		return this.closingUserId;
	}
	public void setClosingUserId(Integer closingUserId) {
		this.closingUserId = closingUserId;
	}
	public Long getClosingBricsUserId() {
		return this.closingBricsUserId;
	}
	public void setClosingBricsUserId(Long closingBricsUserId) {
		this.closingBricsUserId = closingBricsUserId;
	}
	public String getClosingUserName() {
		return this.closingUserName;
	}
	public void setClosingUserName(String closingUserName) {
		this.closingUserName = closingUserName;
	}
	public String getClosingUserFullName() {
		return this.closingUserFullName;
	}
	public void setClosingUserFullName(String closingUserFullName) {
		this.closingUserFullName = closingUserFullName;
	}
	public Date getClosingOutDate() {
		return this.closingOutDate;
	}
	public void setClosingOutDate(Date closingOutDate) {
		this.closingOutDate = closingOutDate;
	}

	public Boolean getReopenStatus() {
		return reopenStatus;
	}

	public void setReopenStatus(Boolean reopenStatus) {
		this.reopenStatus = reopenStatus;
	}

	public Date getReopenDate() {
		return reopenDate;
	}

	public void setReopenDate(Date reopenDate) {
		this.reopenDate = reopenDate;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		if (obj instanceof ProtocolClosingOut) {
			ProtocolClosingOut other = (ProtocolClosingOut) obj;

			return protocolId == other.protocolId;
		}

		return false;
	}
	@Override
	public Document toXML() throws TransformationException {
		try	{
			Document document = super.newDocument();
			Element root = super.initXML(document, "ProtocolClosingOut");

			Element protocolNode = document.createElement("protocolId");
			protocolNode.appendChild(document.createTextNode(String.valueOf(this.getProtocolId())));
			root.appendChild(protocolNode);
			
			Element protocolNameNode = document.createElement("protocolName");
			protocolNameNode.appendChild(document.createTextNode(this.getProtocolName()));
			root.appendChild(protocolNameNode);
			
			Element protocolNumNode = document.createElement("protocolNumber");
			protocolNumNode.appendChild(document.createTextNode(this.getProtocolNumber()));
			root.appendChild(protocolNumNode);
			
			Element bricsStudyNode = document.createElement("bricsStudyId");
			bricsStudyNode.appendChild(document.createTextNode(this.getBricsStudyId()));
			root.appendChild(bricsStudyNode);
			
			Element userNode = document.createElement("closinUserId");
			userNode.appendChild(document.createTextNode(String.valueOf(this.getClosingUserId())));
			root.appendChild(userNode);
			
			Element userNameNode = document.createElement("closinUserName");
			userNameNode.appendChild(document.createTextNode(this.getClosingUserName()));
			root.appendChild(userNameNode);
			
			Element userFullNameNode = document.createElement("closinUserFullName");
			userFullNameNode.appendChild(document.createTextNode(this.getClosingUserFullName()));
			root.appendChild(userFullNameNode);
			
			Element bricsUserNode = document.createElement("bricsUserId");
			bricsUserNode.appendChild(document.createTextNode(String.valueOf(this.getClosingBricsUserId())));
			root.appendChild(bricsUserNode);
			
			Element closingOutDateNode = document.createElement("closingOutDate");
			closingOutDateNode.appendChild(document.createTextNode(String.valueOf(this.getClosingOutDate())));
			root.appendChild(closingOutDateNode);

			return document;

		} catch (Exception ex) {
			throw new TransformationException("Unable to transform object " + this.getClass().getName() + " with id = " + this.getId());
		}
	}
}
