package gov.nih.tbi.filter;

import java.io.Serializable;

import com.google.gson.JsonObject;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDecimal;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;

import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;

public class RangedNumericFilter extends DataElementFilter implements Filter, Serializable {
	private static final long serialVersionUID = 2509973278753739091L;

	private Double maximum;
	private Double minimum;

	public RangedNumericFilter(FormResult form, RepeatableGroup group, DataElement element, boolean blank,
			Double maximum, Double minimum) {
		super(form, group, element, blank);
		this.maximum = maximum;
		this.minimum = minimum;
	}

	public Double getMaximum() {
		return maximum;
	}

	public void setMaximum(Double maximum) {
		this.maximum = maximum;
	}

	public Double getMinimum() {
		return minimum;
	}

	public void setMinimum(Double minimum) {
		this.minimum = minimum;
	}

	@Override
	public JsonObject toJson() {
		JsonObject filterJson = new JsonObject();

		filterJson.addProperty("groupUri", getGroup().getUri());
		filterJson.addProperty("elementUri", getElement().getUri());
		filterJson.addProperty("maximum", maximum);
		filterJson.addProperty("minimum", minimum);
		filterJson.addProperty("blank", isBlank());
		filterJson.addProperty("filterType", getFilterType().name());

		return filterJson;
	}

	@Override
	public FilterType getFilterType() {
		return FilterType.RANGED_NUMERIC;
	}

	@Override
	public ElementFilter toElementFilter(String variable) {
		if(isEmpty()) {
			return null;
		}
		
		ExprVar exprVar = new ExprVar(variable);
		Expr minExpr = new E_GreaterThanOrEqual(exprVar, NodeValueDecimal.makeDecimal(minimum.doubleValue()));
		Expr maxExpr = new E_LessThanOrEqual(exprVar, NodeValueDecimal.makeDecimal(maximum.doubleValue()));
		Expr filterExpression = new E_LogicalAnd(minExpr, maxExpr);
		return new ElementFilter(filterExpression);
	}

	@Override
	public boolean isEmpty() {
		return maximum == null || minimum == null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((maximum == null) ? 0 : maximum.hashCode());
		result = prime * result + ((minimum == null) ? 0 : minimum.hashCode());
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
		RangedNumericFilter other = (RangedNumericFilter) obj;
		if (maximum == null) {
			if (other.maximum != null)
				return false;
		} else if (!maximum.equals(other.maximum))
			return false;
		if (minimum == null) {
			if (other.minimum != null)
				return false;
		} else if (!minimum.equals(other.minimum))
			return false;
		return true;
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean evaluate(String cellValue) {
		if (cellValue == null || cellValue.isEmpty()) {
			if (isBlank()) {
				return true;
			} else {
				return false;
			}
		}

		Double doubleCellValue = Double.valueOf(cellValue);

		boolean output = true;

		if (maximum != null) {
			output = output && doubleCellValue <= maximum;
		}

		if (minimum != null) {
			output = output && doubleCellValue >= minimum;
		}

		return output;
	}
}
