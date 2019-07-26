package gov.nih.tbi.query.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "repeatableGroupDataElement")
@XmlAccessorType(XmlAccessType.FIELD)
public class RepeatableGroupDataElement {
	@XmlAttribute(name = "repeatableGroup")
	private String repeatableGroupName;

	@XmlAttribute(name = "dataElement")
	private String dataElementName;

	public RepeatableGroupDataElement() {}

	public RepeatableGroupDataElement(String repeatableGroupName, String dataElementName) {
		super();
		this.repeatableGroupName = repeatableGroupName;
		this.dataElementName = dataElementName;
	}

	public String getRepeatableGroupName() {
		return repeatableGroupName;
	}

	public void setRepeatableGroupName(String repeatableGroupName) {
		this.repeatableGroupName = repeatableGroupName;
	}

	public String getDataElementName() {
		return dataElementName;
	}

	public void setDataElementName(String dataElementName) {
		this.dataElementName = dataElementName;
	}


}
