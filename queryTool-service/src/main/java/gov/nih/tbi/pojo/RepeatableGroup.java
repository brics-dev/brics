package gov.nih.tbi.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.service.model.MetaDataCache;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement()
public class RepeatableGroup implements Serializable, Comparable<RepeatableGroup> {
	private static final long serialVersionUID = -6192047321024317549L;

	@XmlElement(required = true)
	private String uri;

	private String name;
	private Integer position;
	private String type;
	private Integer threshold;

	@XmlElementWrapper(name = "dataElements")
	@XmlElement(name = "dataElement", type = DataElement.class)
	private LinkedList<DataElement> dataElements;

	private boolean isExpanded;

	public RepeatableGroup() {
		this.uri = "";
		this.name = "";

		this.isExpanded = false;
		this.dataElements = new LinkedList<DataElement>();
	}

	/**
	 * Creates a new RepeatableGroup instance that is a clone of the given base object. When cloning the list of data
	 * elements, a new list will be created and be populated with new cloned instances of the base list's elements.
	 * 
	 * @param base - The object to copy.
	 * @throws NullPointerException If the argument is null.
	 */
	public RepeatableGroup(RepeatableGroup base) throws NullPointerException {
		if (base == null) {
			throw new NullPointerException("The clone argument cannot be null.");
		}

		this.isExpanded = base.isExpanded;
		this.uri = base.uri;
		this.name = base.name;
		this.position = base.position;
		this.type = base.type;
		this.threshold = base.threshold;

		// Clone the data element list.
		this.dataElements = new LinkedList<DataElement>();

		for (DataElement de : base.dataElements) {
			this.dataElements.add(new DataElement(de));
		}
	}

	public JsonObject toJson() {
		JsonObject rgJson = new JsonObject();

		rgJson.addProperty("uri", uri);
		rgJson.addProperty("name", name);
		rgJson.addProperty("position", position);

		JsonArray deListJson = new JsonArray();
		for (DataElement de : dataElements) {
			JsonObject deJson = de.toJsonBasic();
			deListJson.add(deJson);
		}
		rgJson.add("dataElements", deListJson);

		return rgJson;
	}

	public String getUri() {

		return uri;
	}

	public void setUri(String uri) {

		this.uri = uri;
	}

	public String getName() {

		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

	public List<DataElement> getDataElements() {

		return dataElements;
	}

	public void setDataElements(LinkedList<DataElement> dataElements) {

		this.dataElements = dataElements;
	}

	public boolean containsDataElement(String uri) {

		if (dataElements == null) {
			return false;
		}

		for (DataElement de : dataElements) {
			if (uri.equals(de.getUri())) {
				return true;
			}
		}

		return false;
	}

	public List<DataElement> getSelectedElements() {

		List<DataElement> out = new ArrayList<DataElement>();

		for (DataElement element : dataElements) {
			if (element.isSelected()) {
				out.add(element);
			}
		}

		return out;
	}


	public boolean hasSelectedElements() {

		if (dataElements == null) {
			return false;
		}

		for (DataElement de : dataElements) {
			if (de.isSelected()) {
				return true;
			}
		}

		return false;
	}


	public void addDataElement(MetaDataCache metaDataCache, DataElement dataElement) {
		if (dataElements == null) {
			dataElements = new LinkedList<DataElement>();
		}
		if (dataElements.isEmpty() || getPositionOfElement(metaDataCache, dataElement) == null) {
			dataElements.add(new DataElement(dataElement));
		}

		else {
			boolean added = false;
			for (int i = 0; i < dataElements.size(); i++) {
				if (dataElement != null && dataElements.get(i) != null
						&& getPositionOfElement(metaDataCache, dataElement) != null
						&& getPositionOfElement(metaDataCache, dataElements.get(i)) != null
						&& getPositionOfElement(metaDataCache, dataElement) < getPositionOfElement(metaDataCache,
								dataElements.get(i))) {
					added = true;
					dataElements.add(i, new DataElement(dataElement));
					break;
				}

			}

			if (!added) {
				dataElements.add(new DataElement(dataElement));
			}
		}
	}

	public Integer getPositionOfElement(MetaDataCache metaDataCache, DataElement element) {
		return metaDataCache.getRgDePosition(this, element);
	}

	public boolean isElementSelected(DataElement element) {

		for (DataElement elementCheck : dataElements) {
			if (element.equals(elementCheck)) {
				if (elementCheck.isSelected()) {
					return true;
				} else {
					return false;
				}
			}
		}

		return false;
	}

	public void selectAllElements() {

		for (DataElement de : dataElements) {
			de.setSelected(true);
		}
	}

	public void unselectAllElements() {

		for (DataElement de : dataElements) {
			de.setSelected(false);
		}
	}

	public Integer getPosition() {

		return position;
	}

	public void setPosition(Integer position) {

		this.position = position;
	}

	public void setPosition(String position) {

		this.position = Integer.valueOf(position);
	}

	public int getIndexOfDataElement(DataElement dataElement) {
		return this.getSelectedElements().indexOf(dataElement);
	}

	@Override
	public int compareTo(RepeatableGroup o) {

		return this.getPosition().compareTo(o.getPosition());
	}

	/**
	 * Overrides the original equals method by only comparing the URI and name. If the URIs and names are the same, then
	 * we can say that the two objects are equals.
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}

		if (this == other) {
			return true;
		}

		if (other instanceof RepeatableGroup) {
			RepeatableGroup rg = (RepeatableGroup) other;

			return uri.equals(rg.uri) && name.equals(rg.name);
		}

		return false;
	}

	@Override
	public int hashCode() {

		int hashCode = 1;
		hashCode = 31 * hashCode + (uri == null ? 0 : uri.hashCode());
		hashCode = 31 * hashCode + (name == null ? 0 : name.hashCode());
		return hashCode;
	}

	public String getType() {

		return type;
	}

	public void setType(String type) {

		this.type = type;
	}

	public Integer getThreshold() {

		return threshold;
	}

	public void setThreshold(Integer threshold) {

		this.threshold = threshold;
	}

	public boolean doesRepeat() {

		if (type == null || threshold == null) {
			return false;
		} else if (threshold == 0) // 0 means repeat infinite times
		{
			return true;
		} else if (QueryToolConstants.RG_EXACTLY.equals(type) && threshold == 1) {
			return false;
		} else if (QueryToolConstants.RG_LESSTHAN.equals(type) && threshold <= 1) {
			return false;
		} else {
			return true;
		}
	}

	public boolean getExpanded() {

		return isExpanded;
	}

	public boolean isExpanded() {

		return isExpanded;
	}

	public void setExpanded(boolean isExpanded) {

		this.isExpanded = isExpanded;
	}

	public DataElement getDataElement(String dataElement) {

		if (dataElements == null) {
			return null;
		}

		for (DataElement de : dataElements) {
			if (dataElement.equals(de.getName())) {
				return de;
			}
		}

		return null;
	}

	public String getCssClassName() {
		String cssClassName = name.replaceAll("[#\\*,\\>\\<\\'\\(\\)\\s:\\.\\[\\]]+", "_");
		return cssClassName;
	}
}
