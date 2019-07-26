package gov.nih.nichd.ctdb.security.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Privilege DomainObject for the NICHD CTDB Application
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class Privilege extends CtdbDomainObject {
	private static final long serialVersionUID = 8583867815910464369L;

	private String name;
	private String code;
	private String description;

	/**
	 * Default Constructor for the Privilege Domain Object
	 */
	public Privilege() {
		// default constructor
	}


	public Privilege(String code) {
		this.code = code;
	}

	/**
	 * Gets the privilege name
	 *
	 * @return String name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the privilege name
	 *
	 * @param name The privilege name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the privilege code
	 *
	 * @return String code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the privilege code
	 *
	 * @param code The privilege code
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Gets the privilege's description
	 *
	 * @return String description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the privilege's description
	 *
	 * @param description The privilege's description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Privilege other = (Privilege) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}


	/**
	 * This method allows the transformation of a Privilege into an XML Document. If no implementation is available at
	 * this time, an UnsupportedOperationException will be thrown.
	 *
	 * @return XML Document
	 * @throws TransformationException is thrown if there is an error during the XML tranformation
	 * @throws UnsupportedOperationException is thrown if this method is currently unsupported and not implemented.
	 */
	public Document toXML() throws TransformationException, UnsupportedOperationException {
		throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in Privilege.");
	}
}
