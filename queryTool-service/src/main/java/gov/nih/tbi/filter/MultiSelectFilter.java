package gov.nih.tbi.filter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;

import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;

public class MultiSelectFilter extends PermissibleValueFilter implements Serializable {
	private static final long serialVersionUID = -4377632370853143430L;
	private static final String MULTI_SELECT_DELIMITER = ";";

	private FilterMode mode;

	// TRUE => only show data that have more than one value
	private boolean multiDataOption;

	public MultiSelectFilter(FormResult form, RepeatableGroup group, DataElement element, List<String> values,
			String freeFormValue, String name, FilterMode mode, String logicBefore, Integer groupingBefore,
			Integer groupingAfter) {
		super(form, group, element, values, freeFormValue, name, logicBefore, groupingBefore, groupingAfter);
		this.mode = mode;
		this.multiDataOption = false;
	}

	public MultiSelectFilter(FormResult form, RepeatableGroup group, DataElement element, List<String> values,
			String freeFormValue, String name, FilterMode mode, boolean multiData, String logicBefore,
			Integer groupingBefore, Integer groupingAfter) {
		super(form, group, element, values, freeFormValue, name, logicBefore, groupingBefore, groupingAfter);
		this.mode = mode;
		this.multiDataOption = multiData;
	}

	public FilterMode getMode() {
		return mode;
	}

	public void setMode(FilterMode mode) {
		this.mode = mode;
	}

	public boolean isMultiData() {
		return multiDataOption;
	}

	public void setMultiData(boolean multiData) {
		this.multiDataOption = multiData;
	}

	@Override
	public JsonObject toJson() {
		JsonObject filterJson = super.toJson();

		filterJson.addProperty("mode", mode.getName());
		filterJson.addProperty("multiData", multiDataOption);

		if (getValues() != null) {
			JsonArray pvJson = new JsonArray();
			for (String pv : getValues()) {
				pvJson.add(new JsonPrimitive(pv));
			}
			filterJson.add("permissibleValues", pvJson);
		}

		return filterJson;
	}

	@Deprecated
	public ElementFilter toElementFilter(String variable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilterType getFilterType() {
		return FilterType.MULTI_SELECT;
	}

	protected static Set<String> convertToLowercasedSet(List<String> stringList) {
		ListIterator<String> iterator = stringList.listIterator();
		while (iterator.hasNext()) {
			iterator.set(iterator.next().toLowerCase());
		}

		return new HashSet<>(stringList);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean evaluate(String cellValue) {
		List<String> filterValues = getCombinedValues();

		// if no permissible value is selected, show everything.
		// except when multi-data is selected, then we'll want to show anything with more than one data point
		if ((filterValues == null || filterValues.isEmpty()) && !multiDataOption) {
			return true;
		}

		if (cellValue == null || cellValue.isEmpty()) {
			return false;
		}
		
		// this is a set of the filter values
		Set<String> filterValueSet = convertToLowercasedSet(filterValues);

		Set<String> descriptionValueSet = new HashSet<>();

		for (String filterValue : filterValueSet) {
			String currentDescriptionValue = getDescriptionToValue().get(filterValue);

			if (currentDescriptionValue != null) {
				descriptionValueSet.add(currentDescriptionValue.toLowerCase());
			}
		}

		Set<String> valueDescriptionSet = new HashSet<>();

		for (String filterValue : filterValueSet) {
			String currentValueDescription = getValueToDescription().get(filterValue);

			if (currentValueDescription != null) {
				valueDescriptionSet.add(currentValueDescription.toLowerCase());
			}
		}

		List<String> splitCellValues = BRICSStringUtils.delimitedStringToList(cellValue, MULTI_SELECT_DELIMITER);

		// this is a set of the data we are filtering
		Set<String> cellValueSet = convertToLowercasedSet(splitCellValues);

		// TRUE => there is more than one value in the cell
		boolean hasMultiData = cellValueSet.size() > 1;

		boolean evalResult = false;

		// if the user didn't select any filter value, we automatically evaluate to true
		if (filterValues.isEmpty()) {
			evalResult = true;
		} else {
			switch (mode) {
				case INCLUSIVE:
					for (String currentCellValue : cellValueSet) {
						if (filterValueSet.contains(currentCellValue) || descriptionValueSet.contains(currentCellValue)
								|| valueDescriptionSet.contains(currentCellValue)) {
							evalResult = true;
							break;
						}
					}
					break;
				case EXACT:
					evalResult = cellValueSet.equals(filterValueSet) || cellValueSet.equals(descriptionValueSet)
							|| cellValueSet.equals(valueDescriptionSet);
					break;
				default:
					throw new UnsupportedOperationException(mode.getName()
							+ " is unsupported.  When a new mode gets added, the evaluation routine must be implmented in MultiSelectFilter.evaluate");
			}
		}

		if (!multiDataOption) {
			return evalResult;
		} else {
			// if multi-data checkbox was selected, make sure the data we are filtering actually has more than one
			// element.
			return evalResult && hasMultiData;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
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
		MultiSelectFilter other = (MultiSelectFilter) obj;
		if (mode != other.mode)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		if (!getCombinedValues().isEmpty()) {
			switch (mode) {
				case EXACT:
					final String stringFormat = "%s = '%s' AND ";
					sb.append("(");
					for (String value : getCombinedValues()) {
						sb.append(String.format(stringFormat, getReadableFilterName(), value));
					}

					sb.replace(sb.length() - 5, sb.length(), QueryToolConstants.EMPTY_STRING);
					sb.append(")");
					break;
				case INCLUSIVE:
					sb.append(buildQueryInString(getCombinedValues()));
					break;
				default:
					throw new UnsupportedOperationException(mode.getName()
							+ " is unsupported.  When a new mode gets added, the evaluation routine must be implmented in MultiSelectFilter.toString");
			}
		}

		String currentString = sb.toString();

		if (multiDataOption) {
			final String multiDataFormat = "(%s.size > 1)";
			String multiDataString = String.format(multiDataFormat, getReadableFilterName());

			if (currentString.isEmpty()) {
				return multiDataString;
			} else {
				return "(" + currentString + " AND " + multiDataString + ")";
			}
		}

		return currentString;
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty() && !multiDataOption;
	}
}
