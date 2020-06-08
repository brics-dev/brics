package gov.nih.tbi.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;

import gov.nih.tbi.constants.QueryToolConstants;

public class Facet implements Serializable {

	private static final long serialVersionUID = 2478780549727880430L;

	private String headingLabel;
	private String classURI;
	private String propertyURI;

	// Use doubly linked list to improve performance? - Francis
	private LinkedList<FacetItem> items = new LinkedList<FacetItem>();
	private List<String> selectedItems = new ArrayList<String>();

	// A whitelist exists to downselect facet items without actually
	// selecting (or filtering by) them.
	private List<String> whiteListItems = new ArrayList<String>();

	private String filterQuery = QueryToolConstants.EMPTY_STRING;

	public void populateItemsFromJena(QueryResult results) {

		String strVar = results.getResultVars().get(0);
		String countVar = results.getResultVars().get(1);

		for (QuerySolution row : results.getQueryData()) {
			if (!row.contains(strVar)) {
				continue;
			}

			String label = QueryToolConstants.EMPTY_STRING;
			if (row.contains("label")) {
				label = row.get("label").toString();
			}
			if (row.contains("title")) {
				label = row.get("title").toString();
			}
			FacetItem item = new FacetItem();
			item.setCount(row.getLiteral(countVar).getInt());
			item.setLabel(label);
			item.setRdfURI(row.get(strVar).toString());
			getItems().add(item);
		}

		reorderFacetItems();
	}

	/**
	 * Moves selected facet items to the beginning of the list
	 */
	public void reorderFacetItems() {
		int insertIndex = 0;

		if (selectedItems != null && !selectedItems.isEmpty()) {
			for (int i = 0; i < items.size(); i++) {
				FacetItem item = items.get(i);
				if (selectedItems.contains(item.getRdfURI()) && insertIndex != i) {
					items.remove(i);
					items.add(insertIndex, item);
					insertIndex++;
				}
			}
		}
	}

	public String getHeadingLabel() {

		return headingLabel;
	}

	public void setHeadingLabel(String headingLabel) {

		this.headingLabel = headingLabel;
	}

	public String getClassURI() {

		return classURI;
	}

	public void setClassURI(String classURI) {

		this.classURI = classURI;
	}

	public String getPropertyURI() {

		return propertyURI;
	}

	public void setPropertyURI(String propertyURI) {

		this.propertyURI = propertyURI;
	}

	public List<FacetItem> getItems() {

		return items;
	}

	public void setItems(LinkedList<FacetItem> items) {

		this.items = items;
	}

	public List<String> getSelectedItems() {

		return selectedItems;
	}

	public void setSelectedItems(List<String> selectedItems) {

		this.selectedItems = selectedItems;
	}

	public List<String> getWhiteListItems() {
		return whiteListItems;
	}

	public void setWhiteListItems(List<String> whiteListItems) {
		this.whiteListItems = whiteListItems;
	}

	public String getFilterQuery() {

		return filterQuery;
	}

	public void setFilterQuery(String filterQuery) {

		this.filterQuery = filterQuery;
	}

}
