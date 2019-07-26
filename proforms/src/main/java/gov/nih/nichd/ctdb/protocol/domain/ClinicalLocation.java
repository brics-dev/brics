package gov.nih.nichd.ctdb.protocol.domain;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.util.domain.Address;

public class ClinicalLocation extends CtdbDomainObject {
	private static final long serialVersionUID = -5219912918863278446L;
	
	private String name;
	private Address address;
	private String status = "";
	
	public ClinicalLocation() {
		super();
		this.name = "";
		this.address = new Address();
		this.status = "";
	}
	
	public ClinicalLocation(String name, Address address){
		this.name = name;
		this.address = address;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    } 
    
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status){
		this.status = status;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		if (obj instanceof ClinicalLocation) {
			ClinicalLocation other = (ClinicalLocation) obj;

			return name.equals(other.name) && address.equals(other.address);
		}

		return false;
	}
	@Override
	public Document toXML() throws TransformationException {
		try	{
			Document document = super.newDocument();
			Element root = super.initXML(document, "ClinicalLocation");

			Element typeNode = document.createElement("name");
			typeNode.appendChild(document.createTextNode(this.getName()));
			root.appendChild(typeNode);
			
			Element nameNode = document.createElement("address");
			nameNode.appendChild(document.createTextNode(this.getAddress().toXML().toString()));
			root.appendChild(nameNode);

			return document;

		} catch (Exception ex) {
			throw new TransformationException("Unable to transform object " + this.getClass().getName() + " with id = " + this.getId());
		}
	}
}
