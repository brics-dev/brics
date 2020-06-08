package gov.nih.tbi.filter;

import com.google.gson.JsonObject;

import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.repository.model.InstancedRepeatableGroupRow;
import gov.nih.tbi.repository.model.InstancedRow;

public interface Filter {
	public JsonObject toJson();

	public FilterType getFilterType();

	/**
	 * Returns true if there has been a filter value selected or entered, false otherwise
	 * 
	 * @return
	 */
	public boolean isEmpty();

	/**
	 * Evaluate the filter using the data from row and return the resulting boolean.
	 * 
	 * @param row - data we are evaluating against.
	 * @return the exact result of the evaluation.
	 */
	public boolean evaluate(InstancedRow row);
	
	public String getName();
	public void setName(String name);
	
	//the following fields are needed for the frontend for saved query
	public String getLogicBefore();
	public void setLogicBefore(String logicBefore);
	public Integer getGroupingBefore();
	public void setGroupingBefore(Integer groupingBefore);
	public Integer getGroupingAfter();
	public void setGroupingAfter(Integer groupingAfter);
	public boolean evaluate(InstancedRepeatableGroupRow row);
}
