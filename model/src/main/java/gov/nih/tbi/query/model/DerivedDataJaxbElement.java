package gov.nih.tbi.query.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "derivedDataElement")
@XmlAccessorType(XmlAccessType.FIELD)
public class DerivedDataJaxbElement {

	@XmlAttribute
	private String guid;

	@XmlAttribute
	private String visitType;

	@XmlElement(name = "keys")
	private List<String> keys = new ArrayList<String>();

	@XmlElement(name = "values")
	private List<String> values = new ArrayList<String>();

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getVisitType() {
		return visitType;
	}

	public void setVisitType(String visitType) {
		this.visitType = visitType;
	}

	public List<String> getKeys() {
		return keys;
	}

	public void setKeys(List<String> keys) {
		this.keys = keys;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((guid == null) ? 0 : guid.hashCode());
		result = prime * result + ((keys == null) ? 0 : keys.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		result = prime * result + ((visitType == null) ? 0 : visitType.hashCode());
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
		DerivedDataJaxbElement other = (DerivedDataJaxbElement) obj;
		if (guid == null) {
			if (other.guid != null)
				return false;
		} else if (!guid.equals(other.guid))
			return false;
		if (keys == null) {
			if (other.keys != null)
				return false;
		} else if (!keys.equals(other.keys))
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		if (visitType == null) {
			if (other.visitType != null)
				return false;
		} else if (!visitType.equals(other.visitType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DerivedDataJaxbElement [guid=" + guid + ", visitType=" + visitType + ", keys=" + keys + ", values="
				+ values + "]";
	}
}
