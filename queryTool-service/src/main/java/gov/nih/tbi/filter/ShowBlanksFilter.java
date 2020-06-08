package gov.nih.tbi.filter;

import com.google.gson.JsonObject;

import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;

public class ShowBlanksFilter extends DataElementFilter {

	private static final long serialVersionUID = 3081175300821145320L;

	public ShowBlanksFilter(FormResult form, RepeatableGroup group, DataElement element, String name,
			String logicBefore, Integer groupingBefore, Integer groupingAfter) {
		super(form, group, element, name, logicBefore, groupingBefore, groupingAfter);
	}

	@Override
	public JsonObject toJson() {
		JsonObject filterJson = super.toJson();
		return filterJson;
	}

	@Override
	public FilterType getFilterType() {
		return FilterType.SHOW_BLANKS;
	}

	@Override
	public boolean isEmpty() {
		// a show blank filter is always full
		return false;
	}

	@Override
	protected boolean evaluate(String cellValue) {
		if (cellValue == null || cellValue.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		final String filterFormat = "%s = ''";
		return String.format(filterFormat, getReadableFilterName());
	}
}
