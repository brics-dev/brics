package gov.nih.nichd.ctdb.protocol.tag;

import gov.nih.nichd.ctdb.protocol.domain.ClinicalLocation;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class ProtocolClinicalLocationIdtDecorator extends IdtDecorator {
	
	public ProtocolClinicalLocationIdtDecorator() {
		super();
	}
	
	public int getId() {
		ClinicalLocation clinicalLoc = (ClinicalLocation) getObject();
		return clinicalLoc.getId();
	}	
	
	public String getName() {
		ClinicalLocation clinicalLoc = (ClinicalLocation) getObject();
		return clinicalLoc.getName();
	}
	
	public String getAddress() {
		ClinicalLocation clinicalLoc = (ClinicalLocation) getObject();
		return clinicalLoc.getAddress().toString();
	}
	
	public String getAddressId() {
		ClinicalLocation clinicalLoc = (ClinicalLocation) getObject();
		return String.valueOf(clinicalLoc.getAddress().getId());
	}
	
	public String getAddressOne() {
		ClinicalLocation clinicalLoc = (ClinicalLocation) getObject();
		return clinicalLoc.getAddress().getAddressOne();
	}
	
	public String getAddressTwo() {
		ClinicalLocation clinicalLoc = (ClinicalLocation) getObject();
		return clinicalLoc.getAddress().getAddressTwo();
	}
	
	public String getCity() {
		ClinicalLocation clinicalLoc = (ClinicalLocation) getObject();
		return clinicalLoc.getAddress().getCity();
	}
	
	public String getState() {
		ClinicalLocation clinicalLoc = (ClinicalLocation) getObject();
		return String.valueOf(clinicalLoc.getAddress().getState().getId());
	}
	
	public String getCountry() {
		ClinicalLocation clinicalLoc = (ClinicalLocation) getObject();
		return String.valueOf(clinicalLoc.getAddress().getCountry().getId());
	}
	
	public String getZipCode() {
		ClinicalLocation clinicalLoc = (ClinicalLocation) getObject();
		return clinicalLoc.getAddress().getZipCode();
	}
	
	public String getStatus() {
		ClinicalLocation clinicalLoc = (ClinicalLocation) getObject();
		return clinicalLoc.getStatus();
	}
}
