package gov.nih.tbi.filter;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;

import gov.nih.tbi.commons.util.SparqlConstructionUtil;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.util.InstancedDataUtil;

public class FreeFormFilter extends DataElementFilter implements Filter, Serializable {
	private static final long serialVersionUID = 7742512880562860623L;

	public String value;

	public FreeFormFilter(FormResult form, RepeatableGroup group, DataElement element, boolean blank, String value) {
		super(form, group, element, blank);
		this.value = value;
	}

	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public JsonObject toJson() {
		JsonObject filterJson = new JsonObject();
		filterJson.addProperty("groupUri", getGroup().getUri());
		filterJson.addProperty("elementUri", getElement().getUri());
		filterJson.addProperty("freeFormValue", getValue());
		filterJson.addProperty("blank", isBlank());
		filterJson.addProperty("filterType", getFilterType().name());

		return filterJson;
	}

	@Override
	public ElementFilter toElementFilter(String variable) {
		if(isEmpty()) {
			return null;
		}
		
		ExprVar exprVar = new ExprVar(variable);
		ElementFilter filter = new ElementFilter(InstancedDataUtil.buildRegexFilterInsensitive(exprVar, getValue()));
		return applyIsBlank(exprVar, filter);
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
			if (isBlank()) {
				return true;
			} else {
				return false;
			}
		}

		Pattern p = Pattern.compile(".*" + SparqlConstructionUtil.regexEscape(value) + ".*", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(cellValue);
		return m.matches();
	}
}
