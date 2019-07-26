package gov.nih.tbi.filter;

import java.io.Serializable;
import java.util.List;

import com.google.gson.JsonObject;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;

import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.util.InstancedDataUtil;

public class DelimitedMultiSelectFilter extends DataElementFilter implements Filter, Serializable {

	private static final long serialVersionUID = 6388278499646983932L;

	private final static String DELIMITER = ";";
	private String delimitedValues;

	private List<String> values;

	public DelimitedMultiSelectFilter(FormResult form, RepeatableGroup group, DataElement element, boolean blank,
			String delimitedValues) {
		super(form, group, element, blank);
		this.delimitedValues = delimitedValues;

		if (delimitedValues != null && !delimitedValues.isEmpty()) {
			this.values = BRICSStringUtils.delimitedStringToList(delimitedValues, DELIMITER);
		}
	}

	@Override
	public ElementFilter toElementFilter(String variable) {
		if(isEmpty()) {
			return null;
		}
		
		ExprVar var = new ExprVar(variable);
		Expr filterExpression = InstancedDataUtil.multiRegexFilter(var, values);

		return applyIsBlank(var, new ElementFilter(filterExpression));
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
		JsonObject filterJson = new JsonObject();

		filterJson.addProperty("groupUri", getGroup().getUri());
		filterJson.addProperty("elementUri", getElement().getUri());
		filterJson.addProperty("freeFormValue", delimitedValues);
		filterJson.addProperty("blank", isBlank());
		filterJson.addProperty("filterType", getFilterType().name());

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
			if (isBlank()) {
				return true;
			} else {
				return false;
			}
		}

		for (String value : values) {
			if (cellValue.equalsIgnoreCase(value)) {
				return true;
			}
		}

		return false;
	}
}
