package gov.nih.tbi.ordermanager.model;

import gov.nih.tbi.commons.util.StringHashMapAdapter;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.query.model.RepeatableGroupDataElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "formConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
public class DerivedBiosampleFormConfiguration {

	@XmlElement(name = "form")
	private NameAndVersion formNameAndVersion;

	@XmlElement(name = "repeatableGroupDataElement")
	private List<RepeatableGroupDataElement> repeatableGroupDataElements = new ArrayList<RepeatableGroupDataElement>();

	@XmlJavaTypeAdapter(StringHashMapAdapter.class)
	private HashMap<String, String> visitTypeMapping = new HashMap<String, String>();

	public NameAndVersion getFormNameAndVersion() {
		return formNameAndVersion;
	}

	public void setFormNameAndVersion(NameAndVersion formNameAndVersion) {
		this.formNameAndVersion = formNameAndVersion;
	}

	public List<RepeatableGroupDataElement> getRepeatableGroupDataElements() {
		return repeatableGroupDataElements;
	}

	public void setRepeatableGroupDataElements(List<RepeatableGroupDataElement> repeatableGroupDataElementRequests) {
		this.repeatableGroupDataElements = repeatableGroupDataElementRequests;
	}

	public HashMap<String, String> getVisitTypeMapping() {
		return visitTypeMapping;
	}

	public void setVisitTypeMapping(HashMap<String, String> visitTypeMapping) {
		this.visitTypeMapping = visitTypeMapping;
	}
}
