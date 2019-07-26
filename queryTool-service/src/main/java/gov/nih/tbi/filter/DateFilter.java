package gov.nih.tbi.filter;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.JsonObject;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDT;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;

public class DateFilter extends DataElementFilter implements Filter, Serializable {
	private static final long serialVersionUID = 8610505275286107469L;

	private Date dateMin;
	private Date dateMax;

	public DateFilter(FormResult form, RepeatableGroup group, DataElement element, boolean blank, Date dateMax,
			Date dateMin) {
		super(form, group, element, blank);
		this.dateMax = dateMax;
		this.dateMin = dateMin;
	}

	public Date getDateMin() {
		return dateMin;
	}

	public void setDateMin(Date dateMin) {
		this.dateMin = dateMin;
	}

	public Date getDateMax() {
		return dateMax;
	}

	public void setDateMax(Date dateMax) {
		this.dateMax = dateMax;
	}

	@Override
	public JsonObject toJson() {
		JsonObject filterJson = new JsonObject();

		filterJson.addProperty("groupUri", getGroup().getUri());
		filterJson.addProperty("elementUri", getElement().getUri());
		filterJson.addProperty("blank", isBlank());

		String maxString = BRICSTimeDateUtil.dateToStringUtc(dateMax);
		String minString = BRICSTimeDateUtil.dateToStringUtc(dateMin);
		filterJson.addProperty("dateMax", maxString);
		filterJson.addProperty("dateMin", minString);
		filterJson.addProperty("filterType", getFilterType().name());

		return filterJson;
	}

	@Override
	public ElementFilter toElementFilter(String variable) {
		if(isEmpty()) {
			return null;
		}
		
		ExprVar var = new ExprVar(variable);
		String minString = BRICSTimeDateUtil.dateToDateString(dateMin);
		String maxString = BRICSTimeDateUtil.dateToDateString(dateMax);

		Expr filterExpression;
		Expr min = new E_GreaterThan(var,
				new NodeValueDT(minString, NodeFactory.createLiteral(minString, XSDDatatype.XSDdateTime)));
		Expr max = new E_LessThanOrEqual(var,
				new NodeValueDT(maxString, NodeFactory.createLiteral(maxString, XSDDatatype.XSDdateTime)));
		filterExpression = new E_LogicalAnd(min, max);

		return applyIsBlank(var, new ElementFilter(filterExpression));
	}

	@Override
	public FilterType getFilterType() {
		return FilterType.DATE;
	}

	@Override
	public boolean isEmpty() {
		return dateMin == null || dateMax == null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((dateMax == null) ? 0 : dateMax.hashCode());
		result = prime * result + ((dateMin == null) ? 0 : dateMin.hashCode());
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
		DateFilter other = (DateFilter) obj;
		if (dateMax == null) {
			if (other.dateMax != null)
				return false;
		} else if (!dateMax.equals(other.dateMax))
			return false;
		if (dateMin == null) {
			if (other.dateMin != null)
				return false;
		} else if (!dateMin.equals(other.dateMin))
			return false;
		return true;
	}

	@Override
	public boolean evaluate(String cellValue) {
		if (cellValue == null || cellValue.isEmpty()) {
			if (isBlank()) {
				return true;
			} else {
				return false;
			}
		}

		Date cellDateValue = BRICSTimeDateUtil.zuluStringToDate(cellValue);

		if (dateMax == null && dateMin == null) {
			return true;
		}

		return cellDateValue.compareTo(dateMin) >= 0 && cellDateValue.compareTo(dateMax) <= 0;
	}
}
