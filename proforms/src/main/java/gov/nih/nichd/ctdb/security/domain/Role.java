package gov.nih.nichd.ctdb.security.domain;

import java.util.List;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Role DomainObject for the NICHD CTDB Application
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class Role extends CtdbDomainObject {
	private static final long serialVersionUID = 6724363244038716504L;

	private String name;
	private String description;
	private List<Privilege> privList;

	/**
	 * Default Constructor for the Role Domain Object
	 */
	public Role() {}

	/**
	 * Gets the role's name
	 *
	 * @return String name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the role name
	 *
	 * @param name The role name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the role's description
	 *
	 * @return String description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the role's description
	 *
	 * @param description The role's description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the role's privileges in the system
	 *
	 * @return List Role's privileges
	 */
	public List<Privilege> getPrivList() {
		return privList;
	}

	/**
	 * Sets the privilege list for the role
	 *
	 * @param privList Privilege list for the role
	 */
	public void setPrivList(List<Privilege> privList) {
		this.privList = privList;
	}

	/**
	 * This method allows the transformation of a Role into an XML Document. If no implementation is available at this
	 * time, an UnsupportedOperationException will be thrown.
	 *
	 * @return XML Document
	 * @throws TransformationException is thrown if there is an error during the XML tranformation
	 * @throws UnsupportedOperationException is thrown if this method is currently unsupported and not implemented.
	 */
	public Document toXML() throws TransformationException, UnsupportedOperationException {
		throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in Role.");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((privList == null) ? 0 : privList.hashCode());
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
		Role other = (Role) obj;
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
		if (privList == null) {
			if (other.privList != null)
				return false;
		} else if (!privList.equals(other.privList))
			return false;
		return true;
	}
}
