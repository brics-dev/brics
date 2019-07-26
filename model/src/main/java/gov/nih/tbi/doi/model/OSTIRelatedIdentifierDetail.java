package gov.nih.tbi.doi.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "relatedidentifers_detail")
@XmlAccessorType(XmlAccessType.FIELD)
public class OSTIRelatedIdentifierDetail implements Serializable {
	private static final long serialVersionUID = 1855288948283649462L;
	
	@XmlElement(name = "related_identifier")
	private String relatedIdentifier;
	
	/**
	 * OSTI's documentation does not identify and other identifier type besides "DOI." If other types are needed, then
	 * this property may need to be converted into an enum object.
	 */
	@XmlElement(name = "related_identifier_type")
	private String identifierType;

	@XmlElement(name = "relation_type")
	private OSTIRelationType relationType;

	public OSTIRelatedIdentifierDetail() {
		identifierType = "DOI";
	}

	public String getRelatedIdentifier() {
		return relatedIdentifier;
	}

	public void setRelatedIdentifier(String relatedIdentifier) {
		this.relatedIdentifier = relatedIdentifier;
	}

	public String getIdentifierType() {
		return identifierType;
	}

	public void setIdentifierType(String identifierType) {
		this.identifierType = identifierType;
	}

	public OSTIRelationType getRelationType() {
		return relationType;
	}

	public void setRelationType(OSTIRelationType relationType) {
		this.relationType = relationType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifierType == null) ? 0 : identifierType.hashCode());
		result = prime * result + ((relatedIdentifier == null) ? 0 : relatedIdentifier.hashCode());
		result = prime * result + ((relationType == null) ? 0 : relationType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof OSTIRelatedIdentifierDetail)) {
			return false;
		}
		OSTIRelatedIdentifierDetail other = (OSTIRelatedIdentifierDetail) obj;
		if (identifierType == null) {
			if (other.identifierType != null) {
				return false;
			}
		} else if (!identifierType.equals(other.identifierType)) {
			return false;
		}
		if (relatedIdentifier == null) {
			if (other.relatedIdentifier != null) {
				return false;
			}
		} else if (!relatedIdentifier.equals(other.relatedIdentifier)) {
			return false;
		}
		if (relationType != other.relationType) {
			return false;
		}
		return true;
	}

}
