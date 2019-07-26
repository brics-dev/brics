package gov.nih.nichd.ctdb.response.domain;

import java.util.Date;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

public class ScheduleReportFilter extends CtdbDomainObject {
	private static final long serialVersionUID = 2955962442255393014L;

	private Integer protocolId;
	private Integer patientId;
	private Integer clinicalLocId;
	private Date startDate;
	private Date endDate;

	public ScheduleReportFilter(){
		super();
	}
	public ScheduleReportFilter(Integer protocolId, Integer patientId, Integer clinicalLocId, 
			Date startDate, Date endDate){
		super();
		this.protocolId = protocolId;
		this.patientId = patientId;
		this.clinicalLocId = clinicalLocId;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public Integer getProtocolId() {
		return this.protocolId;
	}
	public void setProtocolId(Integer protocolId){
		this.protocolId = protocolId;
	}
	public Integer getPatientId() {
		return this.patientId;
	}
	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}
	public Integer getClinicalLocId() {
		return this.clinicalLocId;
	}
	public void setClinicalLocId(Integer clinicalLocId){
		this.clinicalLocId = clinicalLocId;
	}
	public Date getStartDate(){
		return this.startDate;
	}
	public void setStartDate(Date startDate){
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return this.endDate;
	}
	public void setEndDate(Date endDate){
		this.endDate = endDate;
	}
	
	@Override
	public Document toXML() throws TransformationException {
		// TODO Auto-generated method stub
		return null;
	}

}

