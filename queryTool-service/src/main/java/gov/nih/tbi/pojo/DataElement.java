package gov.nih.tbi.pojo;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.constants.QueryToolConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement()
public class DataElement extends BaseResult implements Serializable {
	private static final long serialVersionUID = 3816360835171179856L;

	@XmlElement(required = true)
	private String uri;

	@XmlAttribute()
	private Long id;

	private String name;
	private Integer position;
	private String title;
	private String description;
	private DataType type;

	@XmlElementWrapper(name = "permissibleValues")
	@XmlElement(name = "permissibleValue", type = PermissibleValue.class)
	private List<PermissibleValue> permissibleValues;

	private Double minimumValue;
	private Double maximumValue;
	private InputRestrictions inputRestrictions;
	private boolean selected;
	private RequiredType requiredType;

	public DataElement() {
		this.uri = "";
		this.name = "";
		this.permissibleValues = new ArrayList<PermissibleValue>();
		this.selected = true;
	}

	/**
	 * Make a copy of the object passed into the argument.
	 * 
	 * @param clone - The object to copy.
	 * @throws NullPointerException If the argument is null.
	 */
	public DataElement(DataElement clone) throws NullPointerException {
		if (clone == null) {
			throw new NullPointerException("The clone argument cannot be null.");
		}

		this.id = clone.id;
		this.uri = clone.uri;
		this.name = clone.name;
		this.title = clone.title;
		this.description = clone.description;
		this.inputRestrictions = clone.inputRestrictions;
		this.type = clone.type;
		this.permissibleValues = new ArrayList<PermissibleValue>(clone.permissibleValues);
		this.maximumValue = clone.maximumValue;
		this.minimumValue = clone.minimumValue;
		this.selected = clone.selected;
		this.requiredType = clone.requiredType;
	}

	public JsonObject toJsonBasic() {
		JsonObject deJson = new JsonObject();

		deJson.addProperty("uri", uri);
		deJson.addProperty("name", name);
		deJson.addProperty("title", title);
		deJson.addProperty("selected", selected);
		return deJson;
	}

	/*
	 * This is called when we retrieve all details to create a filter for this data element
	 */
	public JsonObject toJsonDetails() {
		JsonObject deJson = new JsonObject();

		deJson.addProperty("uri", uri);
		deJson.addProperty("name", name);
		deJson.addProperty("title", title);
		deJson.addProperty("inputRestrictions", inputRestrictions.getValue());
		deJson.addProperty("type", type.getValue());
		deJson.addProperty("maximumValue", maximumValue);
		deJson.addProperty("minimumValue", minimumValue);

		JsonArray pvListJson = new JsonArray();
		for (PermissibleValue pv : getPermissibleValues()) {
			JsonObject pvJson = pv.toJson();
			pvListJson.add(pvJson);
		}
		deJson.add("permissibleValues", pvListJson);

		deJson.addProperty("selected", selected);
		return deJson;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<PermissibleValue> getPermissibleValues() {
		return permissibleValues;
	}

	public void setPermissibleValues(List<PermissibleValue> permissibleValues) {

		if (this.permissibleValues == null) {
			this.permissibleValues = new ArrayList<PermissibleValue>();
		} else {
			this.permissibleValues.clear();
		}

		if (permissibleValues != null) {
			this.permissibleValues.addAll(permissibleValues);
		}
	}

	public Double getMinimumValue() {
		return minimumValue;
	}

	public void setMinimumValue(Double minimumValue) {
		this.minimumValue = minimumValue;
	}

	public Double getMaximumValue() {
		return maximumValue;
	}

	public void setMaximumValue(Double maximumValue) {
		this.maximumValue = maximumValue;
	}

	@Override
	public String getClassUri() {
		return QueryToolConstants.DATAELEMENT_URI;
	}

	public DataType getType() {
		return type;
	}

	public void setType(DataType type) {
		this.type = type;
	}

	public void setType(String type) {
		this.type = DataType.getByValue(type);
	}


	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {

		this.selected = selected;
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public void setUri(String uri) {
		this.uri = uri;
	}

	public RequiredType getRequiredType() {
		return requiredType;
	}

	public void setRequiredType(RequiredType requiredType) {
		this.requiredType = requiredType;
	}

	/**
	 * Overrides the original equals method by only comparing the URI and name. If the URIs and names are the same, then
	 * we can say that the two objects are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		if (obj instanceof DataElement) {
			DataElement de = (DataElement) obj;

			return this.uri.equals(de.uri) && this.name.equals(de.name);
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

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public void setPosition(String position) {
		this.position = Integer.valueOf(position);
	}

	public InputRestrictions getInputRestrictions() {
		return inputRestrictions;
	}

	public void setInputRestrictions(InputRestrictions ir) {
		this.inputRestrictions = ir;
	}

	public void setInputRestrictions(String inputRestrictions) {
		this.inputRestrictions = InputRestrictions.getByValue(inputRestrictions);
	}

	public boolean hasPermissibleValues() {
		return (permissibleValues != null && !permissibleValues.isEmpty());
	}
}
