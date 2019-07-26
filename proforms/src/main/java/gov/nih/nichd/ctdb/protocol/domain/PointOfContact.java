package gov.nih.nichd.ctdb.protocol.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.util.domain.Address;
import gov.nih.tbi.commons.util.BRICSStringUtils;

public class PointOfContact extends CtdbDomainObject {
	private static final long serialVersionUID = -5219912918863278446L;
	
	private String firstName;
	private String middleName;
	private String lastName;
	private String email;
	private String phone;
	private Address address;
	private String position;
	private String status = "";
	
	public PointOfContact(){
		super();
		this.firstName = "";
		this.middleName = "";
		this.lastName = "";
		this.email = "";
		this.phone = "";
		this.address = new Address();
		this.position = "";
		this.status = "";				
	}
	
	public PointOfContact(PointOfContact poc){
		super();
		this.firstName = poc.firstName;
		this.middleName = poc.middleName;
		this.lastName = poc.lastName;
		this.email = poc.email;
		this.phone = poc.phone;
		this.setAddress(poc.address);;
		this.position = poc.position;
		this.status = poc.status;				
	}
	
	
	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName){
		this.firstName = firstName;
	}
	
	public String getMiddleName() {
		return this.middleName;
	}

	public void setMiddleName(String middleName){
		this.middleName = middleName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getEmail(){
		return this.email;
	}
	
	public void setEmail(String email){
		this.email = email;
	}
	
	public String getPhone() {
		return this.phone;
	}
	
	public void setPhone(String phone){
		this.phone = phone;
	}
	
	public Address getAddress() {
		return this.address;
	}
	
	public void setAddress(Address address) {
		this.address = address;
	}
	
	public String getPosition(){
		return this.position;
	}
	
	public void setPosition(String position){
		this.position = position;
	}
	
	public String getFullName() {
		String firstName = this.getFirstName();
		String middleName = this.getMiddleName();
		String lastName = this.getLastName();

		// capitalize the first characters in the first and last names
		String first = BRICSStringUtils.capitalizeFirstCharacter(this.getFirstName().trim());
		String middle = BRICSStringUtils.capitalizeFirstCharacter(this.getMiddleName().trim());
		String last = BRICSStringUtils.capitalizeFirstCharacter(this.getLastName().trim());

		return first + " " + middleName + " " + last;
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

		if (obj instanceof PointOfContact) {
			PointOfContact other = (PointOfContact) obj;
			if(middleName == null && other.middleName == null) {
				if(position == null && other.position == null) {
					return firstName.equals(other.firstName) && lastName.equals(other.lastName)
							 && email.equals(other.email) && phone.equals(other.phone) && address.equals(other.address);
				} else {
					return firstName.equals(other.firstName) && lastName.equals(other.lastName)
							 && email.equals(other.email) && phone.equals(other.phone) && address.equals(other.address) && position.equals(other.position);
				}				
			}
			if (middleName != null && other.middleName != null) {
				if(position == null && other.position == null) {
					return firstName.equals(other.firstName) && middleName.equals(other.middleName) && lastName.equals(other.lastName)
							 && email.equals(other.email) && phone.equals(other.phone) && address.equals(other.address);
				} else {
					return firstName.equals(other.firstName) && middleName.equals(other.middleName) && lastName.equals(other.lastName)
							 && email.equals(other.email) && phone.equals(other.phone) && address.equals(other.address) && position.equals(other.position);
				}
			}
		}

		return false;
	}
	
	@Override
	public Document toXML() throws TransformationException {
		throw new TransformationException("Unable to transform object " + this.getClass().getName() + " with id = " + this.getId());
	}

}
