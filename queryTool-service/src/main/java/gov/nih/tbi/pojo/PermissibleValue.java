package gov.nih.tbi.pojo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.JsonObject;

import gov.nih.tbi.constants.QueryToolConstants;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement()
public class PermissibleValue extends BaseResult implements Serializable, Comparable<PermissibleValue> {
	private static final long serialVersionUID = -5689028178650188230L;

	@XmlElement(required = true)
	private String uri;
	
	@XmlAttribute()
	private Long id;
	
	private String valueLiteral;
	private String valueDescription;
	private String title;
	
	public PermissibleValue() {
		uri = "";
	}
	
	public PermissibleValue(PermissibleValue base) throws NullPointerException {
		if (base == null) {
			throw new NullPointerException("The base object cannot be null.");
		}

		this.uri = base.uri;
		this.id = base.id;
		this.title = base.title;
		this.valueLiteral = base.valueLiteral;
		this.valueDescription = base.valueDescription;
	}
	
	public JsonObject toJson() {
		JsonObject rgJson = new JsonObject();

		rgJson.addProperty("uri", uri);
		rgJson.addProperty("title", title);
		rgJson.addProperty("pvalueLiteral", valueLiteral);
		rgJson.addProperty("pvalueDescription", valueDescription);
		return rgJson;
	}
	
	@Override
	public String getUri() {
		return uri;
	}
	
	public String getValueLiteral() {
		return valueLiteral;
	}

	public void setValueLiteral(String valueLiteral) {
		this.valueLiteral = valueLiteral;
	}

	public String getValueDescription() {
		return valueDescription;
	}

	public void setValueDescription(String valueDescription) {
		this.valueDescription = valueDescription;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getClassUri() {
		return QueryToolConstants.STUDY_URI;
	}

	@Override
	public void setUri(String uri) {
		if (uri != null) {
			this.uri = uri;
		} else {
			this.uri = "";
		}
	}
	
	@Override
	public int compareTo(PermissibleValue o) {
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