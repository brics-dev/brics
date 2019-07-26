package gov.nih.nichd.ctdb.patient.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;


/**
 * PatientVisit DomainObject for the NICHD CTDB Application
 *
 */
public class PatientVisit extends CtdbDomainObject
{
	private static final long serialVersionUID = 8574066094376239992L;
	
	
    private int protocolId = Integer.MIN_VALUE;
    private String protocolNumber = null;
    private int intervalId = Integer.MIN_VALUE; // interval
    private String intervalName = null;
	private Date visitDate = null;
	private String comments = null;
	private int intervalClinicalPointId = Integer.MIN_VALUE;
	//PP
	private String subjectId = null;
	//Patient
	private String mrn = null;
	private String guid = null;
	private int patientId = Integer.MIN_VALUE;
	
	private String patientFirstName = null;
    private String patientLastName = null;
    private String token = null;
    

	public String getIntervalName() {
		return intervalName;
	}

	public void setIntervalName(String intervalName) {
		this.intervalName = intervalName;
	}

	public String getMrn() {
		return mrn;
	}

	public void setMrn(String mrn) {
		this.mrn = mrn;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

    public int getIntervalId() {
		return intervalId;
	}

	public void setIntervalId(int visitTypeId) {
		this.intervalId = visitTypeId;
	}

    public PatientVisit(){
    }
    
	public int getPatientId() {
		return patientId;
	}

	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}

	public Date getVisitDate() {
		return visitDate;
	}

	public void setVisitDate(Date visitDate) {
		this.visitDate = visitDate;
	}

	public String getFormattedVisitDate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return df.format(visitDate);
	}

	public void setFormattedVisitDate(String formattedVisitDate) {
	}

	public int getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(int protocolId) {
		this.protocolId = protocolId;
	}



	public String getPatientFirstName() {
		return patientFirstName;
	}

	public void setPatientFirstName(String patientFirstName) {
		this.patientFirstName = patientFirstName;
	}

	public String getPatientLastName() {
		return patientLastName;
	}

	public void setPatientLastName(String patientLastName) {
		this.patientLastName = patientLastName;
	}

	public void setProtocolNumber(String protocolNumber) {
		this.protocolNumber = protocolNumber;
	}

	public String getProtocolNumber() {
		return protocolNumber;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public Document toXML() throws TransformationException, UnsupportedOperationException {
		throw new UnsupportedOperationException("toXML() not supported in PatientVisit.");
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}
	
	public String getComments() {
		return this.comments;
	}
	
	public void setComments(String comments){
		this.comments = comments;
	}
	
	public int getIntervalClinicalPointId() {
		return this.intervalClinicalPointId;
	}
	
	public void setIntervalClinicalPointId(int pntId) {
		this.intervalClinicalPointId = pntId;
	}
}
