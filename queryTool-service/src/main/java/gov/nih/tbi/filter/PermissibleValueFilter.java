package gov.nih.tbi.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.PermissibleValue;
import gov.nih.tbi.pojo.RepeatableGroup;

public abstract class PermissibleValueFilter extends DataElementFilter implements Filter, Serializable {
	private static final long serialVersionUID = -3671349123391539540L;

	private List<String> values;
	private String freeFormValue;
	private HashMap<String, String> valueToDescription = new HashMap<String, String>();
	private HashMap<String, String> descriptionToValue = new HashMap<String, String>();

	public PermissibleValueFilter(FormResult form, RepeatableGroup group, DataElement element, List<String> values,
			String freeFormValue, String name, String logicBefore, Integer groupingBefore, Integer groupingAfter) {
		super(form, group, element, name, logicBefore, groupingBefore, groupingAfter);
		this.values = values;
		this.freeFormValue = freeFormValue;

		if (element.getPermissibleValues() != null) {
			for (PermissibleValue pv : element.getPermissibleValues()) {
				valueToDescription.put(pv.getValueLiteral().toLowerCase(), pv.getValueDescription().toLowerCase());
				descriptionToValue.put(pv.getValueDescription().toLowerCase(), pv.getValueLiteral().toLowerCase());
			}
		}
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public String getFreeFormValue() {
		return freeFormValue;
	}

	public void setFreeFormValue(String freeFormValue) {
		this.freeFormValue = freeFormValue;
	}

	public HashMap<String, String> getValueToDescription() {
		return valueToDescription;
	}

	public void setValueToDescription(HashMap<String, String> valueToDescription) {
		this.valueToDescription = valueToDescription;
	}

	public HashMap<String, String> getDescriptionToValue() {
		return descriptionToValue;
	}

	public void setDescriptionToValue(HashMap<String, String> descriptionToValue) {
		this.descriptionToValue = descriptionToValue;
	}

	@Override
	public JsonObject toJson() {
		JsonObject filterJson = super.toJson();

		if (values != null) {
			JsonArray pvJson = new JsonArray();
			for (String pv : values) {
				pvJson.add(new JsonPrimitive(pv));
			}
			filterJson.add("permissibleValues", pvJson);
		}

		if (freeFormValue != null) {
			filterJson.addProperty("freeFormValue", freeFormValue);
		}

		return filterJson;
	}

	@Override
	public boolean isEmpty() {
		return (values == null || values.isEmpty()) && (freeFormValue == null || freeFormValue.isEmpty());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((freeFormValue == null) ? 0 : freeFormValue.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
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
		PermissibleValueFilter other = (PermissibleValueFilter) obj;
		if (freeFormValue == null) {
			if (other.freeFormValue != null)
				return false;
		} else if (!freeFormValue.equals(other.freeFormValue))
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	/**
	 * Combine the permissible value selection with the free form (other, specify) value into one single arraylist and
	 * return.
	 * 
	 * @return
	 */
	protected List<String> getCombinedValues() {
		List<String> combinedValues = new ArrayList<>();

		if (values != null) {
			combinedValues.addAll(values);
		}

		if (freeFormValue != null && !freeFormValue.isEmpty()) {
			combinedValues.add(freeFormValue);
		}

		return combinedValues;
	}
}
