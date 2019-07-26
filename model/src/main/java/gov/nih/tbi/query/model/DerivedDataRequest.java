package gov.nih.tbi.query.model;

import gov.nih.tbi.dictionary.model.NameAndVersion;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "derivedDataRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class DerivedDataRequest {

	@XmlElement(name = "form")
	private NameAndVersion formNameAndVersion;

	@XmlElement(name = "repeatableGroupDataElementRequest")
	private List<RepeatableGroupDataElement> repeatableGroupDataElementRequests;

	@XmlElement(name = "guids")
	private Set<String> guids;

	public NameAndVersion getFormNameAndVersion() {
		return formNameAndVersion;
	}

	public void setFormNameAndVersion(NameAndVersion formNameAndVersion) {
		this.formNameAndVersion = formNameAndVersion;
	}

	public List<RepeatableGroupDataElement> getRepeatableGroupDataElementRequests() {
		return repeatableGroupDataElementRequests;
	}

	public void setRepeatableGroupDataElementRequests(
			List<RepeatableGroupDataElement> repeatableGroupDataElementRequests) {
		this.repeatableGroupDataElementRequests = repeatableGroupDataElementRequests;
	}

	public Set<String> getGuids() {
		return guids;
	}

	public void setGuids(Set<String> guids) {
		this.guids = guids;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((formNameAndVersion == null) ? 0 : formNameAndVersion.hashCode());
		result = prime * result + ((guids == null) ? 0 : guids.hashCode());
		result =
				prime
						* result
						+ ((repeatableGroupDataElementRequests == null) ? 0 : repeatableGroupDataElementRequests
								.hashCode());
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
		DerivedDataRequest other = (DerivedDataRequest) obj;
		if (formNameAndVersion == null) {
			if (other.formNameAndVersion != null)
				return false;
		} else if (!formNameAndVersion.equals(other.formNameAndVersion))
			return false;
		if (guids == null) {
			if (other.guids != null)
				return false;
		} else if (!guids.equals(other.guids))
			return false;
		if (repeatableGroupDataElementRequests == null) {
			if (other.repeatableGroupDataElementRequests != null)
				return false;
		} else if (!repeatableGroupDataElementRequests.equals(other.repeatableGroupDataElementRequests))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DerivedDataRequest [formNameAndVersion=" + formNameAndVersion + ", repeatableGroupDataElementRequests="
				+ repeatableGroupDataElementRequests + ", guids=" + guids + "]";
	}
}
