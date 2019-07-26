package gov.nih.tbi.query.model;

/**
 * This represents the key used to index derived data.  GUID and visitType are used together to index.  Hashcode() must be updated if new fields are added
 * @author fchen
 *
 */
public class DerivedDataKey {
	private String guid;

	private String visitType;
	
	public DerivedDataKey() {
		super();
	}

	public DerivedDataKey(String guid, String visitType) {
		super();
		this.guid = guid;
		this.visitType = visitType;
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((guid == null) ? 0 : guid.hashCode());
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
		DerivedDataKey other = (DerivedDataKey) obj;
		if (guid == null) {
			if (other.guid != null)
				return false;
		} else if (!guid.equals(other.guid))
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
		return "DerivedDataKey [guid=" + guid + ", visitType=" + visitType + "]";
	}
}
