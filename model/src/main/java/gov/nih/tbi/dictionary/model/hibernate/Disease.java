package gov.nih.tbi.dictionary.model.hibernate;

import gov.nih.tbi.ModelConstants;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@Entity
@Table(name = "DISEASE")
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class Disease implements Serializable {

	private static final long serialVersionUID = -412275640270465798L;

	/**********************************************************************/

	@Id
	private Long id;

	@Column(name = "DISEASE_NAME")
	private String name;

	@Column(name = "IS_ACTIVE")
	private Boolean isActive;

	@Column(name = "IS_MAJOR")
	private Boolean isMajor;

	public Disease() {

	}

	public Disease(Disease disease) {

		this.id = disease.getId();
		this.name = disease.getName();
		this.isActive = disease.getIsActive();
		this.isMajor = disease.getIsMajor();
	}

	public Disease(String name, Boolean isActive) {
		this.name = name;
		this.isActive = isActive;
	}

	public Disease(Long id, String name, Boolean isActive, Boolean isMajor) {

		super();
		this.id = id;
		this.name = name;
		this.isActive = isActive;
		this.isMajor = isMajor;
	}

	/**********************************************************************/

	public Long getId() {

		return id;
	}

	public void setId(Long id) {

		this.id = id;
	}

	public String getName() {

		return name == null ? ModelConstants.EMPTY_STRING : name;
	}

	public void setName(String name) {

		this.name = name;
	}

	public Boolean getIsActive() {

		return isActive;
	}

	public void setIsActive(Boolean isActive) {

		this.isActive = isActive;
	}

	public Boolean getIsMajor() {

		return isMajor;
	}

	public void setIsMajor(Boolean isMajor) {

		this.isMajor = isMajor;
	}

	public String toString() {

		return "[ID:" + id + ", Name: " + name + ", isActive: " + isActive + ", isMajor: " + isMajor + "]";
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((isActive == null) ? 0 : isActive.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((isMajor == null) ? 0 : isMajor.hashCode());
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
		Disease other = (Disease) obj;
		if (isActive == null) {
			if (other.isActive != null)
				return false;
		} else if (!isActive.equals(other.isActive))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (isMajor == null) {
			if (other.isMajor != null)
				return false;
		} else if (!isMajor.equals(other.isMajor))
			return false;
		return true;
	}
}
