package gov.nih.nichd.ctdb.protocol.domain;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

public class IntervalClinicalPoint extends CtdbDomainObject {
	private static final long serialVersionUID = -5219912918863278446L;
	
	private ClinicalLocation clinicalLoc;
	private Procedure procedure;
	private PointOfContact pointOfContact;
	private String status = "";
	private String clinicalPntInfo = "";
	
	public IntervalClinicalPoint(){
		super();
		clinicalLoc = new ClinicalLocation();
		procedure = new Procedure();
		pointOfContact = new PointOfContact();
	}
	
	public IntervalClinicalPoint(ClinicalLocation clinicalLoc, Procedure procedure, PointOfContact pointOfContact) {
		super();
		this.clinicalLoc = clinicalLoc;
		this.procedure = procedure;
		this.pointOfContact = pointOfContact;
	}
	
	public IntervalClinicalPoint(IntervalClinicalPoint intervalLocationPoint) {
		super();
		this.clinicalLoc = intervalLocationPoint.clinicalLoc;
		this.procedure = intervalLocationPoint.procedure;
		this.pointOfContact = intervalLocationPoint.pointOfContact;
	}
	public ClinicalLocation getClinicalLoc() {
		return this.clinicalLoc;
	}

	public void setClinicalLoc(ClinicalLocation clinicalLoc){
		this.clinicalLoc = clinicalLoc;
	}
	
	public Procedure getProcedure() {
		return procedure;
	}

	public void setProcedure(Procedure procedure){
		this.procedure = procedure;
	}
		
	public PointOfContact getPointOfContact() {
		return pointOfContact;
	}

	public void setPointOfContact(PointOfContact pointOfContact){
		this.pointOfContact = pointOfContact;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status){
		this.status = status;
	}
	
	public String getClinicalPntInfo() {
		return this.clinicalLoc.getName() + " - " + this.procedure.getName() + " - " + this.pointOfContact.getFullName();
	}
	
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof IntervalClinicalPoint) {			
			IntervalClinicalPoint other = (IntervalClinicalPoint) obj;
			if(clinicalLoc == null && other.clinicalLoc == null){
				return procedure.equals(other.procedure) && pointOfContact.equals(other.pointOfContact);
			} else {
				return clinicalLoc.equals(other.clinicalLoc) && procedure.equals(other.procedure) && pointOfContact.equals(other.pointOfContact);
			}
		}
		return false;
	}
	
	@Override
	public Document toXML() throws TransformationException {
		try	{
			Document document = super.newDocument();
			Element root = super.initXML(document, "IntervalClinicalPoint");

			Element typeNode = document.createElement("Procedure");
			typeNode.appendChild(document.createTextNode(this.procedure.getName()));
			root.appendChild(typeNode);
			
			Element nameNode = document.createElement("ClinicalLocation");
			nameNode.appendChild(document.createTextNode(this.clinicalLoc.getName()));
			root.appendChild(nameNode);
			
			Element labelNode = document.createElement("PointOfContact");
			labelNode.appendChild(document.createTextNode(this.pointOfContact.getFirstName()+" "+this.pointOfContact.getLastName()));
			root.appendChild(labelNode);

			return document;

		} catch (Exception ex) {
			throw new TransformationException("Unable to transform object " + this.getClass().getName() + " with id = " + this.getId());
		}
	}

}



