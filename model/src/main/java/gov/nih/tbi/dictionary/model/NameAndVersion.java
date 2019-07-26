package gov.nih.tbi.dictionary.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "NameAndVersion")
@XmlAccessorType(XmlAccessType.FIELD)
public class NameAndVersion {

	@XmlAttribute(name = "name")
	private String name;

	@XmlAttribute(name = "version")
	private String version;

	public NameAndVersion() {

	}

	public NameAndVersion(String name, String version) {

		this.name = name;

		if (name == null) {
			throw new NullPointerException("Name cannot be null for NameAndVersion");
		}

		this.version = version;

		if (version == null) {
			throw new NullPointerException("Version cannot be null for NameAndVersion");
		}
	}

	public String getName() {

		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

	public String getVersion() {

		return version;
	}

	public void setVersion(String version) {

		this.version = version;
	}

	public String toString() {

		return name + "V" + version;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		NameAndVersion other = (NameAndVersion) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

}
