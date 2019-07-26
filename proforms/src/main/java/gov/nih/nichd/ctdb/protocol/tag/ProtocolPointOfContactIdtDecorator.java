package gov.nih.nichd.ctdb.protocol.tag;

import gov.nih.nichd.ctdb.protocol.domain.PointOfContact;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class ProtocolPointOfContactIdtDecorator extends IdtDecorator {
	PointOfContact poc = new PointOfContact();
	/**
	 * Default Constructor
	 */
	public ProtocolPointOfContactIdtDecorator() {
		super();
	}
	
	public int getPocId() {
		PointOfContact poc = (PointOfContact) getObject();
		return poc.getId();
	}	
	
	public String getPocFirstName() {
		PointOfContact poc = (PointOfContact) getObject();
		return poc.getFirstName();
	}
	
	public String getPocMiddleName() {
		PointOfContact poc = (PointOfContact) getObject();
		return poc.getMiddleName();
	}
	
	public String getPocLastName() {
		PointOfContact poc = (PointOfContact) getObject();
		return poc.getLastName();
	}
	
	public String getPocFullName() {
		PointOfContact poc = (PointOfContact) getObject();
		return poc.getFullName();
	}
	
	public String getPocPhone() {
		poc = (PointOfContact) getObject();
		return poc.getPhone();
	}
	
	public String getPocEmail() {
		PointOfContact poc = (PointOfContact) getObject();
		return poc.getEmail();
	}
		
	public String getAddress() {
		PointOfContact poc = (PointOfContact) getObject();
		return poc.getAddress().toString();
	}
	
	public String getPosition() {
		PointOfContact poc = (PointOfContact) getObject();
		return poc.getPosition();
	}
	
	public String getAddressId() {
		PointOfContact poc = (PointOfContact) getObject();
		return String.valueOf(poc.getAddress().getId());
	}
	
	public String getAddressOne() {
		PointOfContact poc = (PointOfContact) getObject();
		return poc.getAddress().getAddressOne();
	}
	
	public String getAddressTwo() {
		PointOfContact poc = (PointOfContact) getObject();
		return poc.getAddress().getAddressTwo();
	}
	
	public String getCity() {
		PointOfContact poc = (PointOfContact) getObject();
		return poc.getAddress().getCity();
	}
	
	public String getState() {
		PointOfContact poc = (PointOfContact) getObject();
		return String.valueOf(poc.getAddress().getState().getId());
	}
	
	public String getCountry() {
		PointOfContact poc = (PointOfContact) getObject();
		return String.valueOf(poc.getAddress().getCountry().getId());
	}
	
	public String getZipCode() {
		PointOfContact poc = (PointOfContact) getObject();
		return poc.getAddress().getZipCode();
	}
	
	public String getStatus() {
		PointOfContact poc = (PointOfContact) getObject();
		return poc.getStatus();
	}
}
