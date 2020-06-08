package gov.nih.tbi.filter;

import java.io.Serializable;
import java.util.List;

import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.util.InstancedDataUtil;

public class MultiRangeFilter extends PermissibleValueFilter implements Filter, Serializable {

	private static final long serialVersionUID = 8564245394626443726L;
	
	public MultiRangeFilter(FormResult form, RepeatableGroup group, DataElement element, List<String> values,
			String name, String logicBefore, Integer groupingBefore, Integer groupingAfter) {
		super(form, group, element, values, null, name, logicBefore, groupingBefore, groupingAfter);
	}
	
	@Override
	public FilterType getFilterType() {
		return FilterType.MULTI_RANGE;
	}

	@Override
	public boolean isEmpty() {
		return (getValues() == null || getValues().isEmpty());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean evaluate(String cellValue) {
		if (cellValue == null || cellValue.isEmpty()) {
			return false;
		}

		List<String> filterValues = getValues();
		if (filterValues == null || filterValues.isEmpty()) {
			return true;
		}

		Double doubleCellValue = Double.valueOf(cellValue);
		
		for (String filterValue : filterValues) {
			String[] filterArr = filterValue.split(InstancedDataUtil.AGE_RANGE_SEPARATOR);
			if (filterArr == null || filterArr.length != 2) {
				continue;
			}
			
			Double minimum = Double.valueOf(filterArr[0]);
			Double maximum = Double.valueOf(filterArr[1]);
			
			if (minimum != null && doubleCellValue >= minimum && maximum != null && doubleCellValue <= maximum) {
				return true;
			}
		}
		
		return false;
	}

}
