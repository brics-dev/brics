package gov.nih.tbi.filter;

import java.util.List;

import gov.nih.tbi.filter.PermissibleValueFilter;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;

public class SingleSelectFilter extends PermissibleValueFilter {
	private static final long serialVersionUID = 6641309665860408351L;

	public SingleSelectFilter(FormResult form, RepeatableGroup group, DataElement element, List<String> values,
			String freeFormValue, String name, String logicBefore, Integer groupingBefore, Integer groupingAfter) {
		super(form, group, element, values, freeFormValue, name, logicBefore, groupingBefore, groupingAfter);
	}


	@Override
	public FilterType getFilterType() {
		return FilterType.SINGLE_SELECT;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean evaluate(String cellValue) {
		List<String> combinedValues = getCombinedValues();

		// if no permissible value is selected, show everything.
		if (combinedValues == null || combinedValues.isEmpty()) {
			return true;
		}

		if (cellValue == null || cellValue.isEmpty()) {
			return false;
		}
		
		if (cellValue.equalsIgnoreCase(this.getFreeFormValue())) {
			return true;
		}


		for (String value : combinedValues) {
			value = value.toLowerCase();
			if(this.getDescriptionToValue().containsKey(value)) {
				if (cellValue.equalsIgnoreCase(getDescriptionToValue().get(value))) {
					return true;
				}
			}else if(this.getValueToDescription().containsValue(value)) {
				if (cellValue.equalsIgnoreCase(value)) {
					return true;
				}
			}else if (cellValue.equalsIgnoreCase(value)) {
				return true;
			}
		}

		return false;
	}
	
	@Override
	public String toString() {
		String output = buildQueryInString(getCombinedValues());
		return output;
	}
}
