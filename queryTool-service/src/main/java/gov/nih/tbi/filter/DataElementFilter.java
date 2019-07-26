package gov.nih.tbi.filter;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.sparql.expr.E_Bound;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueString;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.repository.model.InstancedRepeatableGroupRow;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;
import gov.nih.tbi.repository.model.RepeatingCellColumn;
import gov.nih.tbi.repository.model.RepeatingCellValue;

public abstract class DataElementFilter implements Filter, Serializable {

	private static final long serialVersionUID = 4037723955085134849L;

	private FormResult form;
	private DataElement element;
	private RepeatableGroup group;
	private boolean blank;

	public DataElementFilter(FormResult form, RepeatableGroup group, DataElement element, boolean blank) {
		this.group = group;
		this.element = element;
		this.blank = blank;
		this.form = form;
	}

	public FormResult getForm() {
		return form;
	}


	public void setForm(FormResult form) {
		this.form = form;
	}

	public boolean isBlank() {
		return blank;
	}

	public void setBlank(boolean blank) {
		this.blank = blank;
	}


	public DataElement getElement() {
		return element;
	}

	public void setElement(DataElement element) {
		this.element = element;
	}

	public RepeatableGroup getGroup() {
		return group;
	}

	public void setGroup(RepeatableGroup group) {
		this.group = group;
	}

	protected ElementFilter applyIsBlank(ExprVar variable, ElementFilter filter) {
		Expr filterExpression = filter.getExpr();
		if (isBlank()) {
			filterExpression = new E_LogicalOr(
					new E_Equals(new E_Str(variable), new NodeValueString(QueryToolConstants.EMPTY_STRING)),
					filterExpression);
			filterExpression = new E_LogicalOr(new E_LogicalNot(new E_Bound(variable)), filterExpression);
			return new ElementFilter(filterExpression);
		} else {
			return filter;
		}
	}

	public boolean evaluate(InstancedRow row) {
		if (!getGroup().doesRepeat()) {
			return evaluateNonRepeatingData(row);
		} else {
			return evaluateRepeatingData(row);
		}
	}

	protected boolean evaluateNonRepeatingData(InstancedRow row) {
		String cellValue = getCellValue(row);
		return evaluate(cellValue);
	}

	/**
	 * This method evaluates the filter against a repeating group. The boolean returned by this method tell us if the
	 * parent instanced row should be filtered out; therefore, it will return true if any row remain in this group after
	 * filter is applied, and false if all the rows are filtered out. If the group has been expanded, this method will
	 * also remove any rows that are filterd out.
	 * 
	 * @param row
	 * @return
	 */
	protected boolean evaluateRepeatingData(InstancedRow row) {

		// if the given instanced row is null, then we want to return true if isBlank is check, false otherwise.
		if (isEmpty()) {
			return true;
		}

		if (row == null) {
			return isBlank();
		}

		RepeatingCellValue rcv = getRepeatingData(row);

		List<InstancedRepeatableGroupRow> rgRows = rcv.getRows();
		Iterator<InstancedRepeatableGroupRow> rgIterator = rgRows.iterator();

		RepeatingCellColumn rgColumn = new RepeatingCellColumn(getForm().getShortNameAndVersion(), getGroup().getName(),
				getElement().getName(), getElement().getType());

		boolean evalOutput = false;

		while (rgIterator.hasNext()) {
			InstancedRepeatableGroupRow currentRow = rgIterator.next();
			String value = currentRow.getCellValue(rgColumn);

			boolean evalResult = evaluate(value);

			if (!evalResult && rcv.isExpanded()) {
				rgIterator.remove();

				if (rgRows.isEmpty()) {
					rcv.setExpanded(false);
				}
			}

			// if cell is expanded, we will need to filter out the row. If cell is not expanded, we can just short
			// circuit and return the result.
			if (evalResult) {
				if (!rcv.isExpanded()) {
					return true;
				} else {
					evalOutput = true;
				}
			}
		}

		return evalOutput;
	}

	/**
	 * Returns the repeating cell value that the filter is applied to.
	 * 
	 * @param row
	 * @return
	 */
	protected RepeatingCellValue getRepeatingData(InstancedRow row) {
		if (!group.doesRepeat()) {
			throw new UnsupportedOperationException("Call getCellValue() instead");
		}

		RepeatingCellValue rcv = (RepeatingCellValue) row.getCellValue(this.getForm().getShortNameAndVersion(),
				this.getGroup().getName(), null);

		if (rcv != null) {
			return rcv;
		} else {
			return null;
		}
	}

	protected String getCellValue(InstancedRow row) {
		if (row == null) {
			return null;
		}

		if (group.doesRepeat()) {
			throw new UnsupportedOperationException("Call getRepeatingData() instead");
		}


		NonRepeatingCellValue cv = (NonRepeatingCellValue) row.getCellValue(this.getForm().getShortNameAndVersion(),
				this.getGroup().getName(), this.getElement().getName());

		if (cv != null) {
			return cv.getValue();
		}

		return null;
	}

	protected abstract boolean evaluate(String cellValue);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (blank ? 1231 : 1237);
		result = prime * result + ((element == null) ? 0 : element.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
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
		DataElementFilter other = (DataElementFilter) obj;
		if (blank != other.blank)
			return false;
		if (element == null) {
			if (other.element != null)
				return false;
		} else if (!element.equals(other.element))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		return true;
	}
}
