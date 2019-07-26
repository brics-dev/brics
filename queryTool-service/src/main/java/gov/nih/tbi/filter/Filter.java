package gov.nih.tbi.filter;

import com.google.gson.JsonObject;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;

import gov.nih.tbi.pojo.FilterType;
import gov.nih.tbi.repository.model.InstancedRow;

public interface Filter {
	public JsonObject toJson();

	/**
	 * Returns JENA representation of the current filter for use in generation of SPARQL queries.
	 * 
	 * @param variable
	 * @return
	 */
	public ElementFilter toElementFilter(String variable);

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
}
