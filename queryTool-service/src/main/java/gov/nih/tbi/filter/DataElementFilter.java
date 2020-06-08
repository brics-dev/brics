package gov.nih.tbi.filter;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonObject;

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
	private String name;
	private String logicBefore;
	public Integer groupingBefore;
	public Integer groupingAfter;

	public DataElementFilter(FormResult form, RepeatableGroup group, DataElement element, String name,
			String logicBefore, Integer groupingBefore, Integer groupingAfter) {
		this.group = group;
		this.element = element;
		this.form = form;
		this.name = name;
		this.logicBefore = logicBefore;
		this.groupingBefore = groupingBefore;
		this.groupingAfter = groupingAfter;
	}

	public String getLogicBefore() {
		return logicBefore;
	}

	public void setLogicBefore(String logicBefore) {
		this.logicBefore = logicBefore;
	}

	public Integer getGroupingBefore() {
		return groupingBefore;
	}

	public void setGroupingBefore(Integer groupingBefore) {
		this.groupingBefore = groupingBefore;
	}

	public Integer getGroupingAfter() {
		return groupingAfter;
	}

	public void setGroupingAfter(Integer groupingAfter) {
		this.groupingAfter = groupingAfter;
	}

	protected String getReadableFilterName() {
		String readableName = form.getShortName() + "." + group.getName() + "." + element.getName();
		readableName = readableName.replaceAll(QueryToolConstants.WS, QueryToolConstants.EMPTY_STRING);
		return readableName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCombinationName() {
		return form.getShortName() + group.getName() + element.getName();
	}

	public FormResult getForm() {
		return form;
	}


	public void setForm(FormResult form) {
		this.form = form;
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

	/**
	 * {@inheritDoc}
	 */
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
			return false;
		}

		RepeatingCellValue rcv = getRepeatingData(row);

		List<InstancedRepeatableGroupRow> rgRows = rcv.getRows();
		Iterator<InstancedRepeatableGroupRow> rgIterator = rgRows.iterator();

		FormResult currentForm = getForm();
		RepeatingCellColumn rgColumn =
				(RepeatingCellColumn) currentForm.getColumnFromString(getForm().getShortNameAndVersion(),
						getGroup().getName(), getElement().getName(), getElement().getType());

		boolean evalOutput = false;

		while (rgIterator.hasNext()) {
			InstancedRepeatableGroupRow currentRow = rgIterator.next();
			String value = currentRow.getCellValue(rgColumn);

			boolean evalResult = evaluate(value);

			// if cell is expanded, we will need to filter out the row. If cell is not expanded, we can just short
			// circuit and return the result.
			if (evalResult) {
				evalOutput = true;
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

	public boolean evaluate(InstancedRepeatableGroupRow row) {
		RepeatingCellColumn column =
				new RepeatingCellColumn(form.getShortNameAndVersion(), group.getName(), element.getName());
		String cellValue = row.getCellValue(column);
		return evaluate(cellValue);
	}

	/**
	 * Given N number of values, this method will return a string "%filter name% IN (value1, value2, ..., valueN)" This
	 * method will also truncate the list after listing five elements in the clause.
	 * 
	 * @param values
	 * @return
	 */
	protected String buildQueryInString(List<String> values) {
		final int TO_STRING_LIMIT = 5;

		if (isEmpty()) {
			return QueryToolConstants.EMPTY_STRING;
		}

		StringBuffer sb = new StringBuffer();
		sb.append("(");
		sb.append(getReadableFilterName());
		sb.append(" IN (");

		int toIndex = Math.min(values.size(), TO_STRING_LIMIT);

		for (int i = 0; i < toIndex; i++) {
			String currentValue = values.get(i);
			sb.append("'");
			sb.append(currentValue);
			sb.append("', ");
		}

		// remove the last comma
		sb.replace(sb.length() - 2, sb.length(), "");

		// if the values are being truncated, add the ellipsis.
		if (values.size() > TO_STRING_LIMIT) {
			sb.append("...");
		}

		sb.append("))");

		return sb.toString();
	}

	@Override
	public JsonObject toJson() {
		JsonObject filterJson = new JsonObject();

		filterJson.addProperty("name", getName());
		filterJson.addProperty("groupUri", getGroup().getUri());
		filterJson.addProperty("elementUri", getElement().getUri());
		filterJson.addProperty("filterType", getFilterType().name());

		if (logicBefore != null) {
			filterJson.addProperty("logicBefore", logicBefore);
		}

		if (groupingBefore != null) {
			filterJson.addProperty("groupingBefore", groupingBefore);
		}

		if (groupingAfter != null) {
			filterJson.addProperty("groupingAfter", groupingAfter);
		}

		return filterJson;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((element == null) ? 0 : element.hashCode());
		result = prime * result + ((form == null) ? 0 : form.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((groupingAfter == null) ? 0 : groupingAfter.hashCode());
		result = prime * result + ((groupingBefore == null) ? 0 : groupingBefore.hashCode());
		result = prime * result + ((logicBefore == null) ? 0 : logicBefore.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (element == null) {
			if (other.element != null)
				return false;
		} else if (!element.equals(other.element))
			return false;
		if (form == null) {
			if (other.form != null)
				return false;
		} else if (!form.equals(other.form))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (groupingAfter == null) {
			if (other.groupingAfter != null)
				return false;
		} else if (!groupingAfter.equals(other.groupingAfter))
			return false;
		if (groupingBefore == null) {
			if (other.groupingBefore != null)
				return false;
		} else if (!groupingBefore.equals(other.groupingBefore))
			return false;
		if (logicBefore == null) {
			if (other.logicBefore != null)
				return false;
		} else if (!logicBefore.equals(other.logicBefore))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
