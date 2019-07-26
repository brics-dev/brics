package gov.nih.nichd.ctdb.protocol.tag;

import gov.nih.nichd.ctdb.protocol.domain.IntervalClinicalPoint;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class IntervalClinicalPointIdtDecorator extends IdtDecorator {

	/**
	 * Default Constructor
	 */
	public IntervalClinicalPointIdtDecorator() {
		super();
	}
	
	public int getId() {
		IntervalClinicalPoint intervalCP = (IntervalClinicalPoint) getObject();
		return intervalCP.getId();
	}
	
	public int getProcedureId() {
		IntervalClinicalPoint intervalCP = (IntervalClinicalPoint) getObject();
		int procId = intervalCP.getProcedure().getId();
		return procId;
	}
	
	public String getProcedureName() {
		IntervalClinicalPoint intervalCP = (IntervalClinicalPoint) getObject();
		String procName = intervalCP.getProcedure().getName();
		return procName;
	}
	
	public int getClinicalLocationId() {
		IntervalClinicalPoint intervalCP = (IntervalClinicalPoint) getObject();
		int locId = intervalCP.getClinicalLoc().getId();
		return locId;
	}
	
	public String getClinicalLocationName() {
		IntervalClinicalPoint intervalCP = (IntervalClinicalPoint) getObject();
		String locName = intervalCP.getClinicalLoc().getName();
		return locName;
	}
	
	public int getPointOfContactId() {
		IntervalClinicalPoint intervalCP = (IntervalClinicalPoint) getObject();
		int pocId = intervalCP.getPointOfContact().getId();
		return pocId;
	}
	
	public String getPointOfContactName() {
		IntervalClinicalPoint intervalCP = (IntervalClinicalPoint) getObject();
		String pocName = intervalCP.getPointOfContact().getFullName();
		return pocName;
	}
	public String getStatus() {
		IntervalClinicalPoint intervalCP = (IntervalClinicalPoint) getObject();
		return intervalCP.getStatus();
	}
}

