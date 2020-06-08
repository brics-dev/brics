package gov.nih.tbi.filter;

import java.io.Serializable;
import java.util.List;

import com.google.gson.JsonObject;

import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.repository.model.InstancedRepeatableGroupRow;
import gov.nih.tbi.repository.model.InstancedRow;

public class DatasetFilter implements Filter, Serializable {
	private static final long serialVersionUID = 6826542684224912605L;
	
	private final static String DELIMITER = ";";
	private String delimitedValues;
	private List<String> values;
	private String name;
	private String logicBefore;
	public Integer groupingBefore;
	public Integer groupingAfter;
	
	
	public DatasetFilter(String name, String delimitedValues, String logicBefore, Integer groupingBefore, Integer groupingAfter) {
		super();
		this.name = name;
		this.logicBefore = logicBefore;
		this.groupingBefore = groupingBefore;
		this.groupingAfter = groupingAfter;
		
		this.delimitedValues = delimitedValues;
		if (delimitedValues != null && !delimitedValues.isEmpty()) {
			this.values = BRICSStringUtils.delimitedStringToList(delimitedValues, DELIMITER);
		}
	}

	@Override
	public JsonObject toJson() {
		
		JsonObject filterJson = new JsonObject();

		filterJson.addProperty("name", getName());
		filterJson.addProperty("freeFormValue", getDelimitedValues());
		filterJson.addProperty("filterType", getFilterType().name());

		return filterJson;
	}

	@Override
	public FilterType getFilterType() {
		return FilterType.DATASET;
	}

	@Override
	public boolean isEmpty() {
		return values == null || values.isEmpty();
	}

	@Override
	public boolean evaluate(InstancedRow row) {
		
		if (row == null) {
			return false;
		}

		if (values == null || values.isEmpty()) {
			return true;
		}
		
		for (String value: values) {
			if(value.equalsIgnoreCase(row.getReadableDatasetId())) {
				return true;
			}
		}
		
		return false;
	}
	
	public String getDelimitedValues() {
		return delimitedValues;
	}

	public void setDelimitedValues(String delimitedValues) {
		this.delimitedValues = delimitedValues;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		
	}

	@Override
	public String getLogicBefore() {
		return logicBefore;
	}

	@Override
	public void setLogicBefore(String logicBefore) {
		this.logicBefore = logicBefore;
		
	}

	@Override
	public Integer getGroupingBefore() {
		return groupingBefore;
	}

	@Override
	public void setGroupingBefore(Integer groupingBefore) {
		this.groupingBefore = groupingBefore;
		
	}

	@Override
	public Integer getGroupingAfter() {
		return groupingAfter;
	}

	@Override
	public void setGroupingAfter(Integer groupingAfter) {
		this.groupingAfter = groupingAfter;
		
	}

	@Override
	public boolean evaluate(InstancedRepeatableGroupRow row) {
		throw new UnsupportedOperationException("Cannot add non-data element filters to repeating groups!");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delimitedValues == null) ? 0 : delimitedValues.hashCode());
		result = prime * result + ((groupingAfter == null) ? 0 : groupingAfter.hashCode());
		result = prime * result + ((groupingBefore == null) ? 0 : groupingBefore.hashCode());
		result = prime * result + ((logicBefore == null) ? 0 : logicBefore.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
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
		DatasetFilter other = (DatasetFilter) obj;
		if (delimitedValues == null) {
			if (other.delimitedValues != null)
				return false;
		} else if (!delimitedValues.equals(other.delimitedValues))
			return false;
		if (groupingAfter == null) {
			if (other.groupingAfter != null)
				return false;
		} else if (!groupingAfter.equals(other.groupingAfter))
			return false;
		if (groupingBefore == null) {
			if (other.groupingBefore != null)
				return false;
		} else if (!groupingBefore.equals(other.groupingBefore))
			return false;
		if (logicBefore == null) {
			if (other.logicBefore != null)
				return false;
		} else if (!logicBefore.equals(other.logicBefore))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	
}
