package gov.nih.tbi.repository.dataimport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DataImportDataElementData implements Comparable<DataImportDataElementData> {
	
	private String groupDataElement;
	private String sectionId;
	private List<String> value;
	private List<Integer> id;
	private boolean isRepeatable;
	
	public DataImportDataElementData(String groupDataElement, String sectionId, String repeatableGroupName, List<String> value, List<Integer> id, boolean isRepeatable) {
		super();
		this.groupDataElement = groupDataElement;
		this.sectionId = sectionId;
		this.value = value;
		this.id = id;
		this.isRepeatable = isRepeatable;
	}
	
	public DataImportDataElementData(Map<String, Object> map) {
		super();
		this.groupDataElement = map.get("groupdataelement").toString();
		this.sectionId = map.get("sectionid").toString();
		this.value = Arrays.asList(map.get("submitanswer").toString());
		this.id = new ArrayList<Integer>();
		this.isRepeatable = Boolean.valueOf(map.get("isrepeatable").toString());
	}

	public String getGroupDataElement() {
		return groupDataElement;
	}

	public void setGroupDataElement(String groupDataElement) {
		this.groupDataElement = groupDataElement;
	}

	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}

	public List<Integer> getId() {
		return id;
	}

	public void setId(List<Integer> id) {
		this.id = id;
	}
	
	public String getSectionId() {
		return sectionId;
	}

	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}

	public boolean isRepeatable() {
		return isRepeatable;
	}

	public void setRepeatable(boolean isRepeatable) {
		this.isRepeatable = isRepeatable;
	}
	
	public String getRepeatableGroupName() {
		if(groupDataElement != null && !groupDataElement.isEmpty()) {
			return groupDataElement.substring(0,groupDataElement.indexOf('.'));
		}
		return "";
	}
	
	public void addStringToValues(int position, String s) {
		String val = this.value.get(position);
		val = val.concat(";"+s);
		this.value.set(position, val);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupDataElement == null) ? 0 : groupDataElement.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		DataImportDataElementData other = (DataImportDataElementData) obj;
		if (groupDataElement == null) {
			if (other.groupDataElement != null)
				return false;
		} else if (!groupDataElement.equals(other.groupDataElement))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public int compareTo(DataImportDataElementData de) {
		if(this.groupDataElement.equals(de.getGroupDataElement()) &&
		   this.id.equals(de.getId()) && 
		   this.value.equals(de.getValue())) {
			return 0;
		}
		return 1;
	}

	@Override
	public String toString() {
		return "DataImportDataElementData [groupDataElement=" + groupDataElement + ", value=" + value + ", id=" + id + "]";
	}
	
	
}
