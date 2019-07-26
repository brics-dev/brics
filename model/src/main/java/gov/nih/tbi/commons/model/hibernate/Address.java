package gov.nih.tbi.commons.model.hibernate;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * 
 * @author vpacha
 * 
 */
@Entity
@Table(name = "Address")
@XmlAccessorType(XmlAccessType.FIELD)
public class Address implements Serializable {

	private static final long serialVersionUID = 166970162687840050L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ADDRESS_SEQ")
	@SequenceGenerator(name = "ADDRESS_SEQ", sequenceName = "ADDRESS_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "Address1")
	private String address1;

	@Column(name = "Address2")
	private String address2;

	@Column(name = "City")
	private String city;

	@Column(name = "State")
	private String oldState;

	@Column(name = "Zip_Code")
	private String zipCode;

	@OneToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST})
	@JoinColumn(name = "COUNTRY_ID")
	private Country country;

	@OneToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST})
	@JoinColumn(name = "STATE_ID")
	private State state;
	
	public Address() {
		
	}
	
	public Address(Address address) {
		this.id = address.id;
		this.address1 = address.address1;
		this.address2 = address.address2;
		this.city = address.city;
		this.oldState = address.oldState;
		this.zipCode = address.zipCode;
		this.country = address.country;
	}
	
	public Long getId() {

		return id;
	}

	public void setId(Long id) {

		this.id = id;
	}

	public String getAddress1() {

		return address1;
	}

	public void setAddress1(String address1) {

		this.address1 = address1;
	}

	public String getAddress2() {

		return address2;
	}

	public void setAddress2(String address2) {

		this.address2 = address2;
	}

	public String getZipCode() {

		return zipCode;
	}

	public void setZipCode(String zipCode) {

		this.zipCode = zipCode;
	}

	public String getCity() {

		return city;
	}

	public void setCity(String city) {

		this.city = city;
	}

	public String getOldState() {

		return oldState;
	}

	public void setOldState(String oldState) {

		this.oldState = oldState;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}
	
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((address1 == null) ? 0 : address1.hashCode());
		result = prime * result + ((address2 == null) ? 0 : address2.hashCode());
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((oldState == null) ? 0 : oldState.hashCode());
		result = prime * result + ((zipCode == null) ? 0 : zipCode.hashCode());
		result = prime * result + ((country == null) ? 0 : country.hashCode());
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
		Address other = (Address) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		
		if (address1 == null) {
			if (other.address1 != null)
				return false;
		} else if (!address1.equals(other.address1))
			return false;
		
		if (address2 == null) {
			if (other.address2 != null)
				return false;
		} else if (!address2.equals(other.address2))
			return false;
		
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		
		if (oldState == null) {
			if (other.oldState != null)
				return false;
		} else if (!oldState.equals(other.oldState))
			return false;
		
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		
		if (zipCode == null) {
			if (other.zipCode != null)
				return false;
		} else if (!zipCode.equals(other.zipCode))
			return false;
		
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		
		return true;
	}

}
