/**
 * Title: CtdbDomainObject Description: Base DomainObject for the NICHD CTDB Application Company: Booz Allen Hamilton
 * 
 * @version 1.0
 */

package gov.nih.nichd.ctdb.common;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.security.domain.User;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class CtdbDomainObject implements DomainObject {
	@XmlTransient
	private static final long serialVersionUID = -272259079338522621L;

	private int id = Integer.MIN_VALUE;

	@XmlTransient
	protected int createdBy = Integer.MIN_VALUE;

	@XmlTransient
	protected Date createdDate;

	@XmlTransient
	private String createdByUsername;

	@XmlTransient
	protected int updatedBy = Integer.MIN_VALUE;

	@XmlTransient
	protected Date updatedDate;

	@XmlTransient
	private String updatedByUsername;

	@XmlTransient
	protected boolean active;

	private Version version;

	@XmlTransient
	private CtdbLookup objectClass;

	private String label;

	/**
	 * Default Constructor for the CTDB Domain Object
	 */
	public CtdbDomainObject() {
		// default constructor
	}

	/**
	 * Gets the Domain Object's ID
	 *
	 * @return Domain Object's ID
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the Domain Object's ID
	 *
	 * @param id The domain object ID
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets creator's ID of the Domain Object
	 *
	 * @return The domain object creator's ID
	 */
	public int getCreatedBy() {
		return createdBy;
	}

	/**
	 * Sets the Domain Object's Created By ID
	 *
	 * @param createdBy The domain object's creator ID
	 */
	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * Gets creation date of the Domain Object
	 *
	 * @return Creation date
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * Sets the Domain Object's Creation Date
	 *
	 * @param createdDate The domain object's creation date
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * Gets the creator's username of the DomainObject
	 *
	 * @return The creator's username
	 */
	public String getCreatedByUsername() {
		return createdByUsername;
	}

	/**
	 * Sets the creator's username of the DomainObject
	 *
	 * @param createdByUsername The creator's username
	 */
	public void setCreatedByUsername(String createdByUsername) {
		this.createdByUsername = createdByUsername;
	}

	/**
	 * Gets updated by user's ID of the Domain Object
	 *
	 * @return The domain object updated by user's ID
	 */
	public int getUpdatedBy() {
		return updatedBy;
	}

	/**
	 * Sets the Domain Object's updated by user's ID
	 *
	 * @param updatedBy The domain object updated by user's ID
	 */
	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}

	/**
	 * Gets updated date of the Domain Object
	 *
	 * @return Updated date
	 */
	public Date getUpdatedDate() {
		return updatedDate;
	}

	/**
	 * Sets the Domain Object's Updated Date
	 *
	 * @param updatedDate The domain object's updated date
	 */
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	/**
	 * Gets the updated by user's username of the DomainObject
	 *
	 * @return The updated by user's username
	 */
	public String getUpdatedByUsername() {
		return updatedByUsername;
	}

	/**
	 * Sets the updated by user's username of the DomainObject
	 *
	 * @param updatedByUsername The updated by user's username
	 */
	public void setUpdatedByUsername(String updatedByUsername) {
		this.updatedByUsername = updatedByUsername;
	}

	/**
	 * Gets the active status of the Domain Object
	 *
	 * @return True if object is active, false otherwise
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Sets the Domain Object's Active Status
	 *
	 * @param active The domain object's active status
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Gets the version for the object
	 *
	 * @return The object version
	 */
	public Version getVersion() {
		return version;
	}

	/**
	 * Sets the version for the object
	 *
	 * @param version The object version
	 */
	public void setVersion(Version version) {
		this.version = version;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Abstract method that sub-classes must implement. This method allows the transformation of a DomainObject into an
	 * XML Document. If no implementation is available at this time, sub-classes should throw an
	 * UnsupportedOperationException.
	 *
	 * @return XML Document
	 * @throws TransformationException is thrown if there is an error during the XML tranformation
	 * @throws UnsupportedOperationException is thrown if this method is currently unsupported and not implemented.
	 */
	public abstract Document toXML() throws TransformationException;

	/**
	 * Creates a new Document object to be used by sub-classes to implement the toXML() methods and create and XML
	 * representation of the domain object.
	 *
	 * @return A new Document object
	 * @throws TransformationException Thrown if any error occurs while creating the new document.
	 */
	protected Document newDocument() throws TransformationException {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (Exception e) {
			throw new TransformationException("Unable to create new document: " + e.getMessage(), e);
		}
	}

	/**
	 * Initializes the Document object for the Domain Object and sets the CtdbDomainObject specific attriutes as
	 * attributes of the root tag. This method will return the root element of the Document in order to allow
	 * sub-classes to append onto the current document Object.
	 *
	 * @param document Document object created in sub-class
	 * @param className Domain object class name that this Document will represent
	 * @return The root element of the Document object.
	 * @throws TransformationException Thrown if any errors occur while creating the root element of the Document object
	 */
	protected Element initXML(Document document, String className) throws TransformationException {
		try {
			// create root element
			Element root = document.createElement(className);
			root.setAttribute("id", Integer.toString(this.id));
			document.appendChild(root);

			return root;
		} catch (Exception e) {
			throw new TransformationException(
					"Unable to transform object " + className + " with id = " + this.getId() + ": " + e.getMessage(),
					e);
		}
	}

	public void updateCreatedByInfo(User user) {
		this.createdBy = user.getId();
		this.createdByUsername = user.getUsername();
	}

	public void updateUpdatedByInfo(User user) {
		this.updatedBy = user.getId();
		this.updatedByUsername = user.getUsername();
	}

	public CtdbLookup getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(CtdbLookup objectClass) {
		this.objectClass = objectClass;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + createdBy;
		result = prime * result + ((createdByUsername == null) ? 0 : createdByUsername.hashCode());
		result = prime * result + ((createdDate == null) ? 0 : createdDate.hashCode());
		result = prime * result + id;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + updatedBy;
		result = prime * result + ((updatedByUsername == null) ? 0 : updatedByUsername.hashCode());
		result = prime * result + ((updatedDate == null) ? 0 : updatedDate.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		CtdbDomainObject other = (CtdbDomainObject) obj;
		if (active != other.active)
			return false;
		if (createdBy != other.createdBy)
			return false;
		if (createdByUsername == null) {
			if (other.createdByUsername != null)
				return false;
		} else if (!createdByUsername.equals(other.createdByUsername))
			return false;
		if (createdDate == null) {
			if (other.createdDate != null)
				return false;
		} else if (!createdDate.equals(other.createdDate))
			return false;
		if (id != other.id)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (updatedBy != other.updatedBy)
			return false;
		if (updatedByUsername == null) {
			if (other.updatedByUsername != null)
				return false;
		} else if (!updatedByUsername.equals(other.updatedByUsername))
			return false;
		if (updatedDate == null) {
			if (other.updatedDate != null)
				return false;
		} else if (!updatedDate.equals(other.updatedDate))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
}
