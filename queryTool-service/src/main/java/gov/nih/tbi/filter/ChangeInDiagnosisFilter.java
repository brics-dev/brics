package gov.nih.tbi.filter;

import java.io.Serializable;

import com.google.gson.JsonObject;
import com.hp.hpl.jena.sparql.expr.E_Bound;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.exceptions.FilterException;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.repository.model.InstancedRow;

public class ChangeInDiagnosisFilter implements Filter, Serializable {
	private static final long serialVersionUID = 3008102004642766597L;

	private static String YES = "Yes";
	private static String NO = "No";

	private String value;

	public ChangeInDiagnosisFilter(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public FilterType getFilterType() {
		return FilterType.CHANGE_IN_DIAGNOSIS;
	}

	@Override
	public JsonObject toJson() {
		JsonObject filterJson = new JsonObject();

		filterJson.addProperty("freeFormValue", value);
		filterJson.addProperty("blank", false);
		filterJson.addProperty("filterType", getFilterType().name());

		return filterJson;
	}

	@Override
	public ElementFilter toElementFilter(String variable) {

		Expr expression = null;

		if (YES.equalsIgnoreCase(value)) {
			expression = new E_Equals(new ExprVar(QueryToolConstants.DO_HIGHLIGHT_VAR), NodeValue.makeString("true"));
		} else if (NO.equalsIgnoreCase(value)) {
			expression = new E_LogicalNot(new E_Bound(new ExprVar(QueryToolConstants.DO_HIGHLIGHT_VAR)));
		} else {
			return null;
		}

		return new ElementFilter(expression);
	}

	@Override
	public boolean isEmpty() {
		return value == null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChangeInDiagnosisFilter other = (ChangeInDiagnosisFilter) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean evaluate(InstancedRow row) {
		if (row == null) {
			return false;
		}

		if (value == null || value.isEmpty()) {
			return true;
		}

		if (YES.equalsIgnoreCase(value)) {
			return row.isDoHighlight();
		} else if (NO.equalsIgnoreCase(value)) {
			return !row.isDoHighlight();
		} else {
			throw new FilterException(
					"Filter value for change in diagnosis filter must be either Yes or No! (case insensitive)");
		}
	}
}
