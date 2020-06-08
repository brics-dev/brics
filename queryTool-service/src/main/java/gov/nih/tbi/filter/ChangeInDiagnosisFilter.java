package gov.nih.tbi.filter;

import java.io.Serializable;

import com.google.gson.JsonObject;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.exceptions.FilterEvaluationException;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.repository.model.InstancedRepeatableGroupRow;
import gov.nih.tbi.repository.model.InstancedRow;

public class ChangeInDiagnosisFilter implements Filter, Serializable {
	private static final long serialVersionUID = 3008102004642766597L;

	private static String YES = "Yes";
	private static String NO = "No";
	private String name;
	private String value;
	private String logicBefore;
	public Integer groupingBefore;
	public Integer groupingAfter;

	public String getLogicBefore() {
		return logicBefore;
	}

	public void setLogicBefore(String logicBefore) {
		this.logicBefore = logicBefore;
	}

	public Integer getGroupingBefore() {
		return groupingBefore;
	}

	public void setGroupingBefore(Integer groupingBefore) {
		this.groupingBefore = groupingBefore;
	}

	public Integer getGroupingAfter() {
		return groupingAfter;
	}

	public void setGroupingAfter(Integer groupingAfter) {
		this.groupingAfter = groupingAfter;
	}

	public ChangeInDiagnosisFilter(String value, String name, String logicBefore, Integer groupingBefore,
			Integer groupingAfter) {
		this.value = value;
		this.name = name;
		this.logicBefore = logicBefore;
		this.groupingBefore = groupingBefore;
		this.groupingAfter = groupingAfter;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public FilterType getFilterType() {
		return FilterType.CHANGE_IN_DIAGNOSIS;
	}

	@Override
	public JsonObject toJson() {
		JsonObject filterJson = new JsonObject();

		filterJson.addProperty("name", getName());
		filterJson.addProperty("freeFormValue", value);
		filterJson.addProperty("filterType", getFilterType().name());

		return filterJson;
	}

	@Override
	public boolean isEmpty() {
		return value == null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupingAfter == null) ? 0 : groupingAfter.hashCode());
		result = prime * result + ((groupingBefore == null) ? 0 : groupingBefore.hashCode());
		result = prime * result + ((logicBefore == null) ? 0 : logicBefore.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		ChangeInDiagnosisFilter other = (ChangeInDiagnosisFilter) obj;
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
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws FilterEvaluationException
	 */
	public boolean evaluate(InstancedRow row) throws FilterEvaluationException {
		if (row == null) {
			return false;
		}

		if (value == null || value.isEmpty()) {
			return true;
		}

		if (YES.equalsIgnoreCase(value)) {
			return row.isDoHighlight();
		} else if (NO.equalsIgnoreCase(value)) {
			return !row.isDoHighlight();
		} else {
			throw new FilterEvaluationException(
					"Filter value for change in diagnosis filter must be either Yes or No! (case insensitive)");
		}
	}

	public String toString() {
		if (isEmpty()) {
			return QueryToolConstants.EMPTY_STRING;
		}

		final String stringQueryFormat = "(%s = '%s')";

		return String.format(stringQueryFormat, name, value);
	}

	@Override
	public boolean evaluate(InstancedRepeatableGroupRow row) {
		throw new UnsupportedOperationException("Cannot add non-data element filters to repeating groups!");
	}
}
