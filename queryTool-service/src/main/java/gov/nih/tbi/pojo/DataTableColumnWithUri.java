package gov.nih.tbi.pojo;

import gov.nih.tbi.repository.model.DataTableColumn;

public class DataTableColumnWithUri extends DataTableColumn {

	private static final long serialVersionUID = -2169315548879306348L;
	
	private String formUri;
	private String repeatableGroupUri;
	private String dataElementUri;
	
	public DataTableColumnWithUri() {
		
	}
	
	public DataTableColumnWithUri(FormResult formResult, RepeatableGroup repeatableGroup, DataElement dataElement) {
		super.setForm(formResult.getShortName());
		super.setRepeatableGroup(repeatableGroup.getName());
		super.setDataElement(dataElement.getName());
		this.formUri = formResult.getUri();
		this.repeatableGroupUri = repeatableGroup.getUri();
		this.dataElementUri = dataElement.getUri();
	}
	
	public String getFormUri() {
		return formUri;
	}
	public void setFormUri(String formUri) {
		this.formUri = formUri;
	}
	public String getRepeatableGroupUri() {
		return repeatableGroupUri;
	}
	public void setRepeatableGroupUri(String repeatableGroupUri) {
		this.repeatableGroupUri = repeatableGroupUri;
	}
	public String getDataElementUri() {
		return dataElementUri;
	}
	public void setDataElementUri(String dataElementUri) {
		this.dataElementUri = dataElementUri;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((dataElementUri == null) ? 0 : dataElementUri.hashCode());
		result = prime * result + ((formUri == null) ? 0 : formUri.hashCode());
		result = prime * result + ((repeatableGroupUri == null) ? 0 : repeatableGroupUri.hashCode());
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
		DataTableColumnWithUri other = (DataTableColumnWithUri) obj;
		if (dataElementUri == null) {
			if (other.dataElementUri != null)
				return false;
		} else if (!dataElementUri.equals(other.dataElementUri))
			return false;
		if (formUri == null) {
			if (other.formUri != null)
				return false;
		} else if (!formUri.equals(other.formUri))
			return false;
		if (repeatableGroupUri == null) {
			if (other.repeatableGroupUri != null)
				return false;
		} else if (!repeatableGroupUri.equals(other.repeatableGroupUri))
			return false;
		return true;
	}
}
