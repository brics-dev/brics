package gov.nih.tbi.filter;

import java.io.File;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;

public class FreeFormFilter extends DataElementFilter implements Filter, Serializable {
	private static final long serialVersionUID = 7742512880562860623L;

	public String value;
	protected Pattern regexPattern;
	private FilterMode mode;
	private static final String EXACT_REGEX_FORMAT = "(%s)";
	private static final String INCLUSIVE_REGEX_FORMAT = ".*(%s).*";

	public FreeFormFilter(FormResult form, RepeatableGroup group, DataElement element, String value, String name,
			String logicBefore, Integer groupingBefore, Integer groupingAfter, FilterMode mode) {
		super(form, group, element, name, logicBefore, groupingBefore, groupingAfter);
		this.value = value;
		this.mode = mode;
		this.regexPattern = buildRegexPattern(this.value);
	}

	public FilterMode getMode() {
		return mode;
	}

	public void setMode(FilterMode mode) {
		this.mode = mode;
		this.regexPattern = buildRegexPattern(this.value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
		this.regexPattern = buildRegexPattern(value);
	}

	/**
	 * Given a string, returns a <b>case insensitive</b> regex pattern that will be used to do free text search.
	 * 
	 * (e.g. "I love pies" => .*\b(I|love|pies)\b.*
	 * 
	 * @param value
	 * @return
	 */
	protected Pattern buildRegexPattern(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}

		String regexFormat = null;

		switch (mode) {
			case INCLUSIVE:
				regexFormat = INCLUSIVE_REGEX_FORMAT;
				break;
			case EXACT:
				regexFormat = EXACT_REGEX_FORMAT;
				break;
			default:
				throw new UnsupportedOperationException("Unimplemented mode for free form filters: " + mode.getName());
		}

		String regexText = String.format(regexFormat, value);
		return Pattern.compile(regexText, Pattern.CASE_INSENSITIVE);
	}

	@Override
	public JsonObject toJson() {
		JsonObject filterJson = super.toJson();
		filterJson.addProperty("freeFormValue", getValue());
		filterJson.addProperty("mode", mode.getName());
		return filterJson;
	}

	@Override
	public FilterType getFilterType() {
		return FilterType.FREE_FORM;
	}

	@Override
	public boolean isEmpty() {
		return value == null || value.isEmpty();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		FreeFormFilter other = (FreeFormFilter) obj;
		if (mode != other.mode)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	protected boolean evaluate(String cellValue) {
		// no value enter means nothing is being filtered
		if (value == null) {
			return true;
		}

		if (cellValue == null || cellValue.isEmpty()) {
			return false;
		}

		// in the case of file or thumbnail data elements, the value we receive here is actually the full file path, we
		// only want to filter by the file name
		switch (getElement().getType()) {
			case THUMBNAIL:
			case TRIPLANAR:
			case FILE:
				File f = new File(cellValue);
				cellValue = f.getName();
				break;
			default:
				break;
		}

		Matcher m = regexPattern.matcher(cellValue);
		return m.matches();
	}

	public String toString() {
		final String stringFormat = "(%s = '%s')";

		String output = QueryToolConstants.EMPTY_STRING;

		if (!isEmpty()) {
			output += String.format(stringFormat, getReadableFilterName(), value);
		}

		return output;
	}
}
