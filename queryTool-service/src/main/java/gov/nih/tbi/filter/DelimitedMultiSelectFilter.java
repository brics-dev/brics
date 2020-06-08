package gov.nih.tbi.filter;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;

import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;

public class DelimitedMultiSelectFilter extends DataElementFilter implements Filter, Serializable {

	private static final long serialVersionUID = 6388278499646983932L;

	private final static String DELIMITER = ";";
	private String delimitedValues;

	private List<String> values;

	private DelimitedMultiSelectMode mode;

	private static final String INCLUSIVE_PATTERN_FORMAT = ".*(%s).*";

	public static enum DelimitedMultiSelectMode {
		EXACT("exact"), INCLUSIVE("inclusive");

		private String name;

		private DelimitedMultiSelectMode(final String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public DelimitedMultiSelectFilter(FormResult form, RepeatableGroup group, DataElement element,
			String delimitedValues, String name, String logicBefore, Integer groupingBefore, Integer groupingAfter,
			DelimitedMultiSelectMode mode) {
		super(form, group, element, name, logicBefore, groupingBefore, groupingAfter);
		this.delimitedValues = delimitedValues;
		this.mode = mode;
		if (delimitedValues != null && !delimitedValues.isEmpty()) {
			this.values = BRICSStringUtils.delimitedStringToList(delimitedValues, DELIMITER);
		}
	}

	public DelimitedMultiSelectMode getMode() {
		return mode;
	}


	public void setMode(DelimitedMultiSelectMode mode) {
		this.mode = mode;
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
	public JsonObject toJson() {
		JsonObject filterJson = super.toJson();
		filterJson.addProperty("freeFormValue", delimitedValues);

		return filterJson;
	}

	@Override
	public FilterType getFilterType() {
		return FilterType.DELIMITED_MULTI_SELECT;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((delimitedValues == null) ? 0 : delimitedValues.hashCode());
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
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
		DelimitedMultiSelectFilter other = (DelimitedMultiSelectFilter) obj;
		if (delimitedValues == null) {
			if (other.delimitedValues != null)
				return false;
		} else if (!delimitedValues.equals(other.delimitedValues))
			return false;
		if (mode != other.mode)
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	@Override
	public boolean isEmpty() {
		return values == null || values.isEmpty();
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean evaluate(String cellValue) {
		// if no permissible value is selected, show everything.
		if (values == null || values.isEmpty()) {
			return true;
		}

		if (cellValue == null || cellValue.isEmpty()) {
			return false;
		}

		switch (mode) {
			case EXACT:
				for (String value : values) {
					if (cellValue.equalsIgnoreCase(value)) {
						return true;
					}
				}

				return false;
			case INCLUSIVE:
				Pattern p = buildInclusiveRegex();
				Matcher m = p.matcher(cellValue);
				return m.matches();
			default:
				throw new UnsupportedOperationException(
						mode.getName() + " in an unimplemented for delimited multi-select filters.");
		}
	}

	protected Pattern buildInclusiveRegex() {
		String token = String.join("|", (String[]) values.toArray());
		String regexString = String.format(INCLUSIVE_PATTERN_FORMAT, token);
		Pattern p = Pattern.compile(regexString);
		return p;
	}

	@Override
	public String toString() {
		String output = buildQueryInString(values);
		return output;
	}
}
