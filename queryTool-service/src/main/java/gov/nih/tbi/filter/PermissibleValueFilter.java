package gov.nih.tbi.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;

import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.exceptions.FilterException;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.util.InstancedDataUtil;

public class PermissibleValueFilter extends DataElementFilter implements Filter, Serializable {
	private static final long serialVersionUID = -3671349123391539540L;

	private static final String REGEX_FORMAT = ".*%s.*|";
	private List<String> values;
	private String freeFormValue;

	public PermissibleValueFilter(FormResult form, RepeatableGroup group, DataElement element, boolean blank,
			List<String> values, String freeFormValue) {
		super(form, group, element, blank);
		this.values = values;
		this.freeFormValue = freeFormValue;
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

	@Override
	public JsonObject toJson() {
		JsonObject filterJson = new JsonObject();

		filterJson.addProperty("groupUri", getGroup().getUri());
		filterJson.addProperty("elementUri", getElement().getUri());
		filterJson.addProperty("blank", isBlank());

		filterJson.addProperty("filterType", getFilterType().name());

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ElementFilter toElementFilter(String variable) {
		if (isEmpty()) {
			return null;
		}

		ExprVar var = new ExprVar(variable);

		InputRestrictions inputRestriction = getElement().getInputRestrictions();
		String elementType = getElement().getType();


		List<String> finalValues = getCombinedValues();


		Expr filterExpression = null;

		switch (inputRestriction) {
			case SINGLE:
			case FREE_FORM:
				if (QueryToolConstants.NUMERIC_DE_TYPE.equals(elementType)) {
					filterExpression = InstancedDataUtil.isOneOfNumeric(var, finalValues);
				} else {
					filterExpression = InstancedDataUtil.isOneOfString(var, finalValues);
				}

				break;
			case MULTIPLE:
				filterExpression = InstancedDataUtil.multiRegexFilter(var, finalValues);
				break;
			default:
				throw new FilterException(
						"Permissible value filter must be single-select, multi-select, or free-form (other, specify).");
		}

		ElementFilter elementFilter = new ElementFilter(filterExpression);

		return applyIsBlank(var, elementFilter);
	}

	@Override
	public FilterType getFilterType() {
		return FilterType.PERMISSIBLE_VALUE;
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
			if (isBlank()) {
				return true;
			} else {
				return false;
			}
		}

		InputRestrictions inputRestriction = getElement().getInputRestrictions();

		switch (inputRestriction) {
			case SINGLE:
			case FREE_FORM:

				for (String value : combinedValues) {
					if (cellValue.equalsIgnoreCase(value)) {
						return true;
					}
				}

				return false;
			case MULTIPLE:
				Pattern p = compileRegexPattern();
				Matcher m = p.matcher(cellValue);
				return m.matches();
			default:
				throw new FilterException(
						"Permissible value filter must be single-select, multi-select, or free-form (other, specify).");
		}
	}

	/**
	 * Compiles the regex pattern based on the values. End result should be something like '.*VALUE1.*|.*VALUE2.*|...'
	 * 
	 * @return
	 */
	protected Pattern compileRegexPattern() {
		List<String> combinedValues = getCombinedValues();

		StringBuffer sb = new StringBuffer();
		String regexFormatter = REGEX_FORMAT;

		for (String value : combinedValues) {
			sb = sb.append(String.format(regexFormatter, value));
		}

		sb = sb.replace(sb.length() - 1, sb.length(), "");

		Pattern p = Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);

		return p;
	}
}
