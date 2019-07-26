package gov.nih.nichd.ctdb.util.domain;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.CtdbLookup;

public class Address extends CtdbDomainObject {
	private static final long serialVersionUID = 7001032039942622385L;

	private String addressOne;
	private String addressTwo;
	private String city;
	private CtdbLookup state;
	private CtdbLookup country;
	private String zipCode;
	private String ctdbLookupStringForStateDisplay;
	private String ctdbLookupStringForCountryDisplay;


	/**
	 * Default Constructor for the Patient Domain Object
	 */
	public Address() {
		super();
		addressOne = "";
		addressTwo = "";
		city = "";
		state = null;
		country = null;
		zipCode = "";
		ctdbLookupStringForStateDisplay = "";
		ctdbLookupStringForCountryDisplay = "";
	}

	public Address(int ID) {
		super();
		setId(ID);
		addressOne = "";
		addressTwo = "";
		city = "";
		state = null;
		country = null;
		zipCode = "";
		ctdbLookupStringForStateDisplay = "";
		ctdbLookupStringForCountryDisplay = "";
	}

	public Address(Address address) {
		super();
		this.addressOne = address.addressOne;
		this.addressTwo = address.addressTwo;
		this.city = address.city;
		this.state = address.state;
		this.country = address.country;
		this.zipCode = address.zipCode;
		this.ctdbLookupStringForStateDisplay = address.ctdbLookupStringForStateDisplay;
		this.ctdbLookupStringForCountryDisplay = address.ctdbLookupStringForCountryDisplay;
	}

	/**
	 * Gets the first line of the address
	 *
	 * @return The first line of the address
	 */
	public String getAddressOne() {
		return addressOne;
	}

	/**
	 * Sets the first line of the address
	 *
	 * @param addressOne The first line of the address
	 */
	public void setAddressOne(String addressOne) {
		this.addressOne = StringUtils.trim(addressOne);
	}

	/**
	 * Gets the second line of the address
	 *
	 * @return The second line of the address
	 */
	public String getAddressTwo() {
		return addressTwo;
	}

	/**
	 * Sets the second line of the address
	 *
	 * @param addressTwo The second line of the address
	 */
	public void setAddressTwo(String addressTwo) {
		this.addressTwo = StringUtils.trim(addressTwo);
	}

	/**
	 * Gets the address city
	 *
	 * @return The address city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * Sets the address city
	 *
	 * @param city The address city
	 */
	public void setCity(String city) {
		this.city = StringUtils.trim(city);
	}

	/**
	 * Gets the address state
	 *
	 * @return The address state
	 */
	public CtdbLookup getState() {
		return state;
	}

	/**
	 * Sets the address state
	 *
	 * @param state The address state
	 */
	public void setState(CtdbLookup state) {
		this.state = state;
	}

	/**
	 * Gets the address zip code
	 *
	 * @return The address zip code
	 */
	public String getZipCode() {
		return zipCode;
	}

	/**
	 * Sets the address zip code
	 *
	 * @param zipCode The address zip code
	 */
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	/**
	 * Getter for property country.
	 * 
	 * @return Value of property country.
	 */
	public CtdbLookup getCountry() {
		return country;
	}

	/**
	 * Setter for property country.
	 * 
	 * @param country New value of property country.
	 */
	public void setCountry(CtdbLookup country) {
		this.country = country;
	}

	/**
	 * This method allows the transformation of a Address into an XML Document. If no implementation is available at
	 * this time, an UnsupportedOperationException will be thrown.
	 *
	 * @return XML Document
	 * @throws UnsupportedOperationException is thrown if this method is currently unsupported and not implemented.
	 */
	public Document toXML() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in Address.");
	}

	public String getCtdbLookupStringForStateDisplay() {
		return ctdbLookupStringForStateDisplay;
	}

	public void setCtdbLookupStringForStateDisplay(String ctdbLookupStringForStateDisplay) {
		this.ctdbLookupStringForStateDisplay = ctdbLookupStringForStateDisplay;
	}

	public String getCtdbLookupStringForCountryDisplay() {
		return ctdbLookupStringForCountryDisplay;
	}

	public void setCtdbLookupStringForCountryDisplay(String ctdbLookupStringForCountryDisplay) {
		this.ctdbLookupStringForCountryDisplay = ctdbLookupStringForCountryDisplay;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((addressOne == null) ? 0 : addressOne.hashCode());
		result = prime * result + ((addressTwo == null) ? 0 : addressTwo.hashCode());
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((zipCode == null) ? 0 : zipCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Address))
			return false;
		Address other = (Address) obj;
		if (addressOne == null) {
			if (other.addressOne != null)
				return false;
		} else if (!addressOne.equals(other.addressOne))
			return false;
		if (addressTwo == null) {
			if (other.addressTwo != null)
				return false;
		} else if (!addressTwo.equals(other.addressTwo))
			return false;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
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
		return true;
	}

	@Override
	public String toString() {
		String stateName = "";
		if (this.state.getLongName() != null) {
			stateName = this.state.getLongName();
		} else if (this.state.getShortName() != null) {
			stateName = this.state.getShortName();
		}
		String state = this.state == null ? this.ctdbLookupStringForStateDisplay : stateName;

		String countryName = "";
		if (this.country.getLongName() != null) {
			countryName = this.country.getLongName();
		} else if (this.country.getShortName() != null) {
			countryName = this.country.getShortName();
		}
		String country = this.country == null ? this.ctdbLookupStringForCountryDisplay : countryName;

		String rtnStr = this.addressOne;
		if (!this.addressTwo.equals("")) {
			rtnStr += ", " + this.addressTwo;
		}
		if (!this.city.equals("")) {
			rtnStr += ", " + this.city;
		}
		if (!state.equals("")) {
			rtnStr += ", " + state;
		}
		if (!country.equals("")) {
			rtnStr += ", " + country;
		}
		if (!this.zipCode.equals("")) {
			rtnStr += ", " + this.zipCode;
		}
		return rtnStr;
	}
}
