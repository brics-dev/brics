package gov.nih.nichd.ctdb.response.domain;

import java.util.Date;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.protocol.domain.ClinicalLocation;
import gov.nih.nichd.ctdb.protocol.domain.PointOfContact;
import gov.nih.nichd.ctdb.protocol.domain.Procedure;
import gov.nih.tbi.commons.util.BRICSStringUtils;

public class ScheduleReport extends CtdbDomainObject {
	private static final long serialVersionUID = 2955962442255393014L;

	private Date visitDate = null;
	private Integer protocolId;
	private String protocolNumber = "";
	private Integer patientId;
	private String patientMRN = "";
	private String patientGuid = "";
	private String patientSubjectId = "";
	private String patientFirstName = "";
	private String patientMidName = "";
	private String patientLastName = "";
	private Integer visitTypeId;
	private String visitTypeName = "";
	private ClinicalLocation clinicalLocation = new ClinicalLocation();;
	private Procedure procedure = new Procedure();
	private PointOfContact pointOfContact = new PointOfContact();
	private String comments = "";

	public ScheduleReport(){
		super();
	}
	
	public Date getVisitDate() {
		return this.visitDate;
	}
	public void setVisitDate(Date visitDate){
		this.visitDate = visitDate;
	}
	public Integer getProtocolId() {
		return this.protocolId;
	}
	public void setProtocolId(Integer protocolId){
		this.protocolId = protocolId;
	}
	public String getProtocolNumber() {
		return this.protocolNumber;
	}
	public void setProtocolNumber(String protocolNumber){
		this.protocolNumber = protocolNumber;
	}
	public Integer getPatientId() {
		return this.patientId;
	}
	public void setPatientId(Integer patientId){
		this.patientId = patientId;
	}
	public String getPatientMRN() {
		return this.patientMRN;
	}
	public void setPatientMRN(String patientMRN){
		this.patientMRN = patientMRN;
	}
	public String getPatientGuid() {
		return this.patientGuid;
	}
	public void setPatientGuid(String patientGuid){
		this.patientGuid = patientGuid;
	}
	public String getPatientSubjectId() {
		return this.patientSubjectId;
	}
	public void setPatientSubjectId(String patientSubjectId){
		this.patientSubjectId = patientSubjectId;
	}
	public String getPatientFirstName() {
		return this.patientFirstName;
	}
	public void setPatientFirstName(String patientFirstName){
		this.patientFirstName = patientFirstName;
	}
	public String getPatientMidName() {
		return this.patientMidName;
	}
	public void setPatientMidName(String patientMidName){
		this.patientMidName = patientMidName;
	}
	public String getPatientLastName() {
		return this.patientLastName;
	}
	public void setPatientLastName(String patientLastName){
		this.patientLastName = patientLastName;
	}
	public Integer getVisitTypeId() {
		return this.visitTypeId;
	}
	public void setVisitTypeId(Integer visitTypeId){
		this.visitTypeId = visitTypeId;
	}
	public String getVisitTypeName() {
		return this.visitTypeName;
	}
	public void setVisitTypeName(String visitTypeName) {
		this.visitTypeName = visitTypeName;
	}
	public ClinicalLocation getClinicalLocation() {
		return this.clinicalLocation;
	}
	public void setClinicalLocation(ClinicalLocation clinicalLocation) {
		this.clinicalLocation = clinicalLocation;
	}
	public Procedure getProcedure() {
		return this.procedure;
	}
	public void setProcedure(Procedure procedure){
		this.procedure = procedure;
	}
	public PointOfContact getPointOfContact() {
		return this.pointOfContact;
	}
	public void setPointOfContact(PointOfContact poc){
		this.pointOfContact = poc;
	}
	public String getComments() {
		return this.comments;
	}
	public void setComments(String comments){
		this.comments = comments;
	}
	public String getPatientFullName() {
		String firstName = this.getPatientFirstName();
		String middleName = this.getPatientMidName();
		String lastName = this.getPatientLastName();

		// capitalize the first characters in the first and last names
		String first = firstName == null ? "" : BRICSStringUtils.capitalizeFirstCharacter(firstName.trim());
		String middle = middleName == null ? "" : BRICSStringUtils.capitalizeFirstCharacter(middleName.trim());
		String last = lastName == null ? "" : BRICSStringUtils.capitalizeFirstCharacter(lastName.trim());

		String fullName = last + ", " + first + " " + middle;
		if(last.isEmpty() && first.isEmpty()){
			fullName = "";
		}
		return fullName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		if (obj instanceof ScheduleReport) {
			ScheduleReport other = (ScheduleReport) obj;

			return visitDate.equals(other.visitDate) && protocolNumber.equals(other.protocolNumber) 
					&& patientId == other.patientId && visitTypeId == other.visitTypeId 
					&& clinicalLocation.getId() == other.getClinicalLocation().getId() && procedure.getId() == other.getProcedure().getId() 
					&& pointOfContact.getId() == other.getPointOfContact().getId();
		}

		return false;
	}
	
	@Override
	public Document toXML() throws TransformationException {
		throw new TransformationException("Unable to transform object " + this.getClass().getName() + " with id = " + this.getId());
	}

}
