package gov.nih.tbi.repository.dataimport;

public class DataImportRepeatableGroupData {
	
	private String repeatableGroupName;
	private String currentSectionid;
	private int sectionIterator;
	
	public DataImportRepeatableGroupData(String repeatableGroupName, String currentSectionid, int sectionIterator) {
		super();
		this.repeatableGroupName = repeatableGroupName;
		this.currentSectionid = currentSectionid;
		this.sectionIterator = sectionIterator;
	}
	
	public String getRepeatableGroupName() {
		return repeatableGroupName;
	}
	
	public void setRepeatableGroupName(String repeatableGroupName) {
		this.repeatableGroupName = repeatableGroupName;
	}
	
	public String getCurrentSectionid() {
		return currentSectionid;
	}
	
	public void setCurrentSectionid(String currentSectionid) {
		this.currentSectionid = currentSectionid;
	}
	
	public int getSectionIterator() {
		return sectionIterator;
	}
	
	public void setSectionIterator(int sectionIterator) {
		this.sectionIterator = sectionIterator;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currentSectionid == null) ? 0 : currentSectionid.hashCode());
		result = prime * result + ((repeatableGroupName == null) ? 0 : repeatableGroupName.hashCode());
		result = prime * result + sectionIterator;
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
		DataImportRepeatableGroupData other = (DataImportRepeatableGroupData) obj;
		if (currentSectionid == null) {
			if (other.currentSectionid != null)
				return false;
		} else if (!currentSectionid.equals(other.currentSectionid))
			return false;
		if (repeatableGroupName == null) {
			if (other.repeatableGroupName != null)
				return false;
		} else if (!repeatableGroupName.equals(other.repeatableGroupName))
			return false;
		if (sectionIterator != other.sectionIterator)
			return false;
		return true;
	}
	
	
}
