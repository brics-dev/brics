package gov.nih.nichd.ctdb.protocol.domain;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

public class Procedure extends CtdbDomainObject {
	private static final long serialVersionUID = -5219912918863278446L;
	
	private ProcedureType procedureType;
	private String name;
	private String label;
	private Boolean isNew = false;
	
	public Procedure(){
		super();
	}
	
	public Procedure(ProcedureType procedureType, String name, String label, Boolean isNew) {
		super();
		this.procedureType = procedureType;
		this.name = name;
		this.label = label;
		this.isNew = isNew;
	}
	
	public ProcedureType getProcedureType() {
		return procedureType;
	}

	public void setProcedureType(ProcedureType procedureType){
		this.procedureType = procedureType;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name){
		this.name = name;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label){
		this.label = label;
	}
	
	public Boolean getIsNew() {
		return isNew;
	}
	
	public void setIsNew(Boolean isNew){
		this.isNew = isNew;
	}
	
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Procedure other = (Procedure) obj;
		if (procedureType == null) {
			if (other.procedureType != null)
				return false;
		} else if (!procedureType.equals(other.procedureType)) {
			return false;
		}
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name)){
			return false;
		}
		return true;
	}
	
	@Override
	public Document toXML() throws TransformationException {
		try	{
			Document document = super.newDocument();
			Element root = super.initXML(document, "Procedure");

			Element typeNode = document.createElement("ProcedureType");
			typeNode.appendChild(document.createTextNode(this.procedureType.getName()));
			root.appendChild(typeNode);
			
			Element nameNode = document.createElement("name");
			nameNode.appendChild(document.createTextNode(this.name));
			root.appendChild(nameNode);
			
			Element labelNode = document.createElement("lable");
			labelNode.appendChild(document.createTextNode(this.label));
			root.appendChild(labelNode);

			return document;

		} catch (Exception ex) {
			throw new TransformationException("Unable to transform object " + this.getClass().getName() + " with id = " + this.getId());
		}
	}

}


