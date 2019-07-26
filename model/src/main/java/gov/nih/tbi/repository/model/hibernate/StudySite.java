package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.commons.model.hibernate.Address;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@Entity
@Table(name = "STUDY_SITE")
@XmlAccessorType(XmlAccessType.FIELD)
public class StudySite implements Serializable {

	private static final long serialVersionUID = -4308404269722845208L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDY_SITE_SEQ")
    @SequenceGenerator(name = "STUDY_SITE_SEQ", sequenceName = "STUDY_SITE_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "SITE_NAME")
    private String siteName;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = Address.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "Address_ID")
    private Address address;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;
    
    @Column(name = "IS_PRIMARY")
    private boolean isPrimary;

    public StudySite() {
    }
    
    public StudySite(StudySite studySite) {
    	this.id = studySite.id;
    	this.siteName = studySite.siteName;
    	this.address = studySite.address;
    	this.phoneNumber = studySite.phoneNumber;
    	this.isPrimary = studySite.isPrimary;
    }
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	public void setIsPrimary(String isPrimary) {
		this.isPrimary = Boolean.parseBoolean(isPrimary);
	}
	
	public String getAddressLine() {
		
		if (address == null) {
			return null;
		}
		String address1 = address.getAddress1();
		String address2 = address.getAddress2();
		
		return address1 + (address2 != null ? ", " + address2 : "");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((siteName == null) ? 0 : siteName.hashCode());
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
		result = prime * result + (isPrimary ? 1 : 0);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StudySite other = (StudySite) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		
		if (siteName == null) {
			if (other.siteName != null)
				return false;
		} else if (!siteName.equals(other.siteName))
			return false;
		
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		
		if (phoneNumber == null) {
			if (other.phoneNumber != null)
				return false;
		} else if (!phoneNumber.equals(other.phoneNumber))
			return false;
		
		if (isPrimary != other.isPrimary) {
			return false;
		}
			
		return true;
	}
	
}
