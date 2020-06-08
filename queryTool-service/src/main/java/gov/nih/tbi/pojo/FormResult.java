package gov.nih.tbi.pojo;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.filter.DataElementFilter;
import gov.nih.tbi.filter.Filter;
import gov.nih.tbi.filter.FilterFactory;
import gov.nih.tbi.filter.JaxbFilter;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.RepeatingCellColumn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement()
public class FormResult extends BaseResult implements Serializable, Comparable<FormResult> {
	private static final long serialVersionUID = 928899490084040875L;

	@XmlElement(required = true)
	private String uri;

	@XmlAttribute()
	private Long id;

	private String shortName;
	private String title;
	private String version;

	@XmlElementWrapper(name = "studies")
	@XmlElement(name = "studyResult", type = StudyResult.class)
	private List<StudyResult> studies;

	@XmlElementWrapper(name = "repeatableGroups")
	@XmlElement(name = "repeatableGroup", type = RepeatableGroup.class)
	private List<RepeatableGroup> repeatableGroups;

	@XmlElementWrapper(name = "filters")
	@XmlElement(name = "filter", type = JaxbFilter.class)
	private List<Filter> filters;

	// this map is used to cache the column objects, so we don't initialize a new column object everytime and take up
	// memory.
	@XmlTransient
	private Map<String, DataTableColumn> columns;

	private boolean hasGuidData;

	public FormResult() {
		uri = "";
		studies = new ArrayList<StudyResult>();
		repeatableGroups = new ArrayList<RepeatableGroup>();
		filters = new LinkedList<Filter>();
		hasGuidData = true;
	}

	/**
	 * Creates a new FormResult instance that is a clone if the given base object. When cloning the lists, new lists
	 * will be created before cloning the base list's elements. The clones of the elements in the repeatable group and
	 * studies lists will be new instances of those elements.
	 * 
	 * @param base - The FormResult object whose fields will be copied to this object.
	 * @throws NullPointerException If the given base object is null.
	 */
	public FormResult(FormResult base) throws NullPointerException {
		if (base == null) {
			throw new NullPointerException("The given base object cannot be null.");
		}

		this.uri = base.uri;
		if (base.id != null) {
			this.id = new Long(base.id);
		}
		this.shortName = base.shortName;
		this.title = base.title;
		this.version = base.version;

		// Clone the repeatable groups list.
		int listSize = base.repeatableGroups.size() > 0 ? base.repeatableGroups.size() : 10;
		this.repeatableGroups = new ArrayList<RepeatableGroup>(listSize);

		for (RepeatableGroup group : base.repeatableGroups) {
			this.repeatableGroups.add(new RepeatableGroup(group));
		}

		// Clone the study list.
		this.studies = new ArrayList<StudyResult>();

		for (StudyResult study : base.studies) {
			this.studies.add(new StudyResult(study));
		}

		// Clone the filter list.
		this.filters = new LinkedList<Filter>();

		for (Filter filter : base.filters) {
			this.filters.add(filter);
		}

		this.hasGuidData = base.hasGuidData;
	}

	public boolean isHasGuidData() {
		return hasGuidData;
	}

	public void setHasGuidData(boolean hasGuidData) {
		this.hasGuidData = hasGuidData;
	}

	public DataTableColumn getColumnFromString(String form, String repeatableGroup, String dataElement, DataType type) {
		if (columns == null) {
			columns = new HashMap<>();
		}

		String key = form + "." + repeatableGroup + "." + dataElement + "." + type.getValue();
		DataTableColumn column = columns.get(key);

		if (column == null) {
			column = new RepeatingCellColumn(form, repeatableGroup, dataElement, type);
			columns.put(key, column);
		}

		return column;
	}

	public DataTableColumn getColumnFromString(String form, String repeatableGroup, String dataElement) {
		if (columns == null) {
			columns = new HashMap<>();
		}

		String key = form + "." + repeatableGroup + "." + dataElement;
		DataTableColumn column = columns.get(key);

		if (column == null) {
			column = new DataTableColumn(form, repeatableGroup, dataElement);
			columns.put(key, column);
		}

		return column;
	}

	public DataTableColumn getColumnFromString(String form, String hardcoded) {
		if (columns == null) {
			columns = new HashMap<>();
		}

		String key = form + "." + hardcoded;
		DataTableColumn column = columns.get(key);

		if (column == null) {
			column = new DataTableColumn(form, hardcoded);
			columns.put(key, column);
		}

		return column;
	}

	public Map<String, DataTableColumn> getColumns() {
		return columns;
	}

	public void setColumnCache(Map<String, DataTableColumn> columns) {
		this.columns = columns;
	}

	/*
	 * This method is called when loading all data element filters on cart page.
	 */
	public JsonObject toJsonDetails() {

		JsonObject formJson = new JsonObject();
		formJson.addProperty("uri", uri);

		JsonArray rgListJson = new JsonArray();
		for (RepeatableGroup rg : repeatableGroups) {
			JsonObject rgJson = rg.toJson();
			rgListJson.add(rgJson);
		}
		formJson.add("repeatableGroups", rgListJson);

		JsonArray filterListJson = new JsonArray();
		for (Filter filter : filters) {
			JsonObject filterJson = filter.toJson();
			filterListJson.add(filterJson);
		}
		formJson.add("filters", filterListJson);

		return formJson;
	}


	/**
	 * Implements the tests of equality for a FormResult object. Once the method determines that two separate FormResult
	 * objects are being compared, the method will only compare the URIs of each object to decide if the objects are
	 * equal.
	 * 
	 * @param obj - The object to test for equality.
	 * @return True if and only if the given object is determined to be equal to the current object.
	 */
	@Override
	public boolean equals(Object obj) {
		// Check if the object is null
		if (obj == null) {
			return false;
		}

		// Check if the object is actually this instance of FormResult.
		if (this == obj) {
			return true;
		}

		// Check if the object is an instance of FormResult
		if (obj instanceof FormResult) {
			FormResult other = (FormResult) obj;

			return this.uri.equals(other.uri);
		}

		return false;
	}

	/**
	 * Overriding the base implementation of this method for any FormResult instances. The returned hash code will be
	 * based only the hashing of the URI's hash code.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 1;

		hash = hash * prime + (uri != null ? uri.hashCode() : 0);

		return hash;
	}

	public Long getId() {

		return id;
	}

	public void setId(Long id) {

		this.id = id;
	}

	public String getShortName() {

		return shortName;
	}

	public void setShortName(String shortName) {

		this.shortName = shortName;
	}

	public String getTitle() {

		return title;
	}

	public void setTitle(String title) {

		this.title = title;
	}

	public String getVersion() {

		return version;
	}

	public void setVersion(String version) {

		this.version = version;
	}

	public List<StudyResult> getStudies() {

		return studies;
	}

	/**
	 * Does a deep copy to studies property
	 * 
	 * @param studies
	 */
	public void setStudies(List<StudyResult> studiesToAdd) {
		this.studies.clear();

		if (studiesToAdd != null) {
			this.studies.addAll(studiesToAdd);
		}
	}

	@Override
	public String getClassUri() {

		return QueryToolConstants.FORM_STRUCTURE_URI;
	}

	public int getNumberOfStudies() {
		return (studies == null) ? 0 : studies.size();
	}

	public List<RepeatableGroup> getSelectedRepeatableGroups() {

		List<RepeatableGroup> repeatableGroups = new ArrayList<RepeatableGroup>();

		for (RepeatableGroup rg : this.repeatableGroups) {
			if (rg.hasSelectedElements()) {
				repeatableGroups.add(rg);
			}
		}

		return repeatableGroups;
	}


	public List<RepeatableGroup> getRepeatableGroups() {

		return repeatableGroups;
	}

	public RepeatableGroup getRepeatableGroupByUri(String uri) {
		for (RepeatableGroup rg : repeatableGroups) {
			if (uri.equals(rg.getUri())) {
				return rg;
			}
		}

		return null;
	}

	public RepeatableGroup getRepeatableGroupByName(String name) {
		for (RepeatableGroup rg : repeatableGroups) {
			if (name.equals(rg.getName())) {
				return rg;
			}
		}

		return null;
	}

	public void setRepeatableGroups(List<RepeatableGroup> repeatableGroups) {
		this.repeatableGroups.clear();

		if (repeatableGroups != null) {
			this.repeatableGroups.addAll(repeatableGroups);
		}
	}

	public boolean containsGroup(String rgUri) {
		for (RepeatableGroup group : repeatableGroups) {
			if (rgUri.equals(group.getUri())) {
				return true;
			}
		}

		return false;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters.clear();

		if (filters != null) {
			this.filters.addAll(filters);
		}
	}

	public void addFilter(RepeatableGroup group, DataElement element, String name) {
		if (!isFiltered(group, element)) {
			filters.add(FilterFactory.createFilterByInference(this, group, element, name));
		}
	}
	
	public void addFilter(Filter filter) {
		if(this.filters == null) {
			this.filters = new ArrayList<> ();
		}
		
		filters.add(filter);
	}

	public DataElement getElement(String deShortName) {

		for (RepeatableGroup group : repeatableGroups) {
			for (DataElement element : group.getDataElements()) {
				if (element.getName().equals((deShortName))) {
					return element;
				}
			}
		}
		return null;
	}

	public void deleteFilter(Filter filter) {
		filters.remove(filter);
	}

	public boolean isGroupElementSelected(RepeatableGroup group, DataElement element) {
		for (RepeatableGroup groupCheck : repeatableGroups) {
			if (groupCheck.equals(group)) {
				return groupCheck.isElementSelected(element);
			}
		}

		return false;
	}

	/**
	 * Returns true if the group-element combination has already been added as a filter, false otherwise.
	 * 
	 * @param group
	 * @param element
	 * @return
	 */
	public boolean isFiltered(RepeatableGroup group, DataElement element) {

		if (filters.isEmpty()) {
			return false;
		}

		for (Filter filter : filters) {
			if (filter.getClass().isAssignableFrom(DataElementFilter.class)) {
				DataElementFilter abstractFilter = (DataElementFilter) filter;

				if (group.equals(abstractFilter.getGroup()) && element.equals(abstractFilter.getElement())) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public void setUri(String uri) {
		this.uri = uri != null ? uri : "";
	}

	public void selectAllElements() {

		for (RepeatableGroup rg : repeatableGroups) {
			rg.selectAllElements();
		}

	}

	public void unselectAllElements() {

		for (RepeatableGroup rg : repeatableGroups) {
			rg.unselectAllElements();
		}
	}

	public int getDataElementCount() {

		int count = 0;

		for (RepeatableGroup rg : repeatableGroups) {
			count += rg.getSelectedElements().size();
		}

		return count;
	}

	public boolean hasFilter() {
		return !filters.isEmpty();
	}

	public boolean hasFilterOnDataElement(RepeatableGroup rg, DataElement de) {

		for (Filter filter : filters) {
			if (filter.getClass().isAssignableFrom(DataElementFilter.class)) {
				DataElementFilter abstractFilter = (DataElementFilter) filter;
				if (abstractFilter.getGroup().equals(rg) && abstractFilter.getElement().equals(de)) {
					return true;
				}
			}
		}

		return false;
	}

	public RepeatableGroup getRepeatableGroupByDeIndex(int index) {

		int currentIndex = -1;
		for (RepeatableGroup rg : repeatableGroups) {
			currentIndex += rg.getSelectedElements().size();

			if (currentIndex >= index) {
				return rg;
			}
		}

		return null;
	}

	public RepeatableGroup getRepeatingGroupByDeIndex(int index) {

		int currentIndex = -1;
		for (RepeatableGroup rg : repeatableGroups) {
			if (rg.doesRepeat()) {
				currentIndex += rg.getSelectedElements().size();

				if (currentIndex >= index) {
					return rg;
				}
			}
		}

		return null;
	}

	/**
	 * Returns the data element in the form by index. This index represents the position the data element would be in if
	 * all data elements in the form are laid out flat in an array without repeatable groups.
	 * 
	 * @param index
	 * @return
	 */
	public DataElement getDataElementByIndex(int index) {

		int currentIndex = 0;

		if (getRepeatableGroups().size() == 1) {
			RepeatableGroup rg = getRepeatableGroups().get(0);
			return rg.getSelectedElements().get(index);
		}

		for (RepeatableGroup rg : repeatableGroups) {
			currentIndex += rg.getSelectedElements().size();

			if (currentIndex > index) {
				if (rg.getSelectedElements().size() == currentIndex) {
					return rg.getSelectedElements().get(index);
				} else {
					return rg.getSelectedElements().get(rg.getSelectedElements().size() - (currentIndex - index));
				}
			}
		}

		return null;
	}

	public List<RepeatableGroup> getRepeatingRepeatableGroups() {

		List<RepeatableGroup> rgList = new ArrayList<RepeatableGroup>();

		for (RepeatableGroup rg : repeatableGroups) {
			if (rg.doesRepeat()) {
				rgList.add(rg);
			}
		}

		return rgList;
	}

	/**
	 * Returns the data element in the form by index. This index represents the position the data element would be in if
	 * all data elements in the form are laid out flat in an array without repeatable groups.
	 * 
	 * @param index
	 * @return
	 */
	public DataElement getRepeatingDataElementByIndex(int index) {

		int currentIndex = 0;

		if (getRepeatingRepeatableGroups().size() == 1) {
			RepeatableGroup rg = getRepeatableGroups().get(0);
			return rg.getSelectedElements().get(index);
		}

		for (RepeatableGroup rg : getRepeatingRepeatableGroups()) {
			currentIndex += rg.getSelectedElements().size();

			if (currentIndex > index) {
				if (rg.getSelectedElements().size() == currentIndex) {
					return rg.getSelectedElements().get(index);
				} else {
					return rg.getSelectedElements().get(rg.getSelectedElements().size() - (currentIndex - index));
				}
			}
		}

		return null;
	}

	public String getShortNameAndVersion() {

		return shortName + "V" + version;
	}

	public RepeatableGroup getGroupByName(String name) {

		for (RepeatableGroup rg : repeatableGroups) {
			if (name.equals(rg.getName())) {
				return rg;
			}
		}

		return null;
	}

	public String getCssClassName() {

		return shortName.replaceAll("[#\\*,\\>\\<\\'\\(\\)\\s:\\.\\[\\]]+", "_");
	}

	@Override
	public int compareTo(FormResult o) {
		if (o == null) {
			return 1;
		} else if (this == o) {
			return 0;
		} else if (this.title == null) {
			return -1;
		} else if (o.title == null) {
			return 1;
		} else {
			return this.title.compareToIgnoreCase(o.title);
		}
	}
}
