package gov.nih.nichd.ctdb.patient.domain;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.protocol.domain.PrepopDataElement;

public class PatientVisitPrepopValue extends CtdbDomainObject {
	private static final long serialVersionUID = 6966151441388036139L;
	
	private long patientVisitId;
	private long prepopDataElementId;
	private long prepopDEIntervalId;
	private long intervalId;
	private String prepopValue;
	private PrepopDataElement prepopDataElement;
	
	public PatientVisitPrepopValue() {
		super();
		patientVisitId = -1L;
		prepopDataElementId = -1L;
		
		prepopValue = "";
		prepopDataElement = new PrepopDataElement();
	}
	
	public long getPatientVisitId() {
		return patientVisitId;
	}

	public void setPatientVisitId(long patientVisitId) {
		this.patientVisitId = patientVisitId;
	}

	public long getPrepopDataElementId() {
		return prepopDataElementId;
	}

	public void setPrepopDataElementId(long prepopDataElementId) {
		this.prepopDataElementId = prepopDataElementId;
	}
	
	public long getPrepopDEIntervalId() {
		return prepopDEIntervalId;
	}

	public void setPrepopDEIntervalId(long prepopDEIntervalId) {
		this.prepopDEIntervalId = prepopDEIntervalId;
	}

	public long getIntervalId() {
		return intervalId;
	}

	public void setIntervalId(long intervalId) {
		this.intervalId = intervalId;
	}
	
	public String getPrepopvalue() {
		return prepopValue;
	}

	public void setPrepopvalue(String prepopValue) {
		this.prepopValue = prepopValue;
	}
	
	public PrepopDataElement getPrepopDataElement() {
		return prepopDataElement;
	}

	public void setPrepopDataElement(PrepopDataElement prepopDataElement) {
		this.prepopDataElement = prepopDataElement;
	}
	
	@Override
	public Document toXML() throws TransformationException, UnsupportedOperationException {
		try	{
			Document document = super.newDocument();
			Element root = super.initXML(document, "patientvisitprepopvalue");

			Element prepopDEIdNode = document.createElement("prepopDEId");
			prepopDEIdNode.appendChild(document.createTextNode(String.valueOf(this.prepopDataElementId)));
			root.appendChild(prepopDEIdNode);

			Element intervalIdNode = document.createElement("intervalId");
			intervalIdNode.appendChild(document.createTextNode(String.valueOf(this.intervalId)));
			root.appendChild(intervalIdNode);
			
			Element prepopValueNode = document.createElement("prepopValue");
			prepopValueNode.appendChild(document.createTextNode(String.valueOf(this.prepopValue)));
			root.appendChild(prepopValueNode);
						
			Element nameNode = document.createElement("prepopDEShortName");
			nameNode.appendChild(document.createTextNode(this.prepopDataElement.getShortName()));
			root.appendChild(nameNode);
			
			Element titleNode = document.createElement("prepopDETitle");
			titleNode.appendChild(document.createTextNode(this.prepopDataElement.getTitle()));
			root.appendChild(titleNode);
			
			Element valueTypeNode = document.createElement("prepopDEValueType");
			valueTypeNode.appendChild(document.createTextNode(this.prepopDataElement.getValueType()));
			root.appendChild(valueTypeNode);

			return document;

		} catch (Exception ex) {
			throw new UnsupportedOperationException("toXML() not supported in PatientVisitPrepopValue.");
		}
	}

}
