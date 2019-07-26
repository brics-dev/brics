package gov.nih.tbi.ordermanager.model;

import gov.nih.tbi.commons.util.StringHashMapAdapter;
import gov.nih.tbi.query.model.DiagnosisConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "biosampleRepository")
@XmlAccessorType(XmlAccessType.FIELD)
public class DerivedBiosampleRepository {
	@XmlAttribute
	private String name;

	@XmlJavaTypeAdapter(StringHashMapAdapter.class)
	private HashMap<String, String> dataElementColumnMapping = new HashMap<String, String>();

	@XmlElement
	private List<DerivedBiosampleFormConfiguration> formConfiguration =
			new ArrayList<DerivedBiosampleFormConfiguration>();

	@XmlElement
	private DiagnosisConfiguration diagnosisConfiguration;

	public DiagnosisConfiguration getDiagnosisConfiguration() {
		return diagnosisConfiguration;
	}

	public void setDiagnosisConfiguration(DiagnosisConfiguration diagnosisConfiguration) {
		this.diagnosisConfiguration = diagnosisConfiguration;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<DerivedBiosampleFormConfiguration> getFormConfiguration() {
		return formConfiguration;
	}

	public void setFormConfiguration(List<DerivedBiosampleFormConfiguration> derivedBiosampleConfiguration) {
		this.formConfiguration = derivedBiosampleConfiguration;
	}

	public HashMap<String, String> getDataElementColumnMapping() {
		return dataElementColumnMapping;
	}

	public void setDataElementColumnMapping(HashMap<String, String> dataElementColumnMapping) {
		this.dataElementColumnMapping = dataElementColumnMapping;
	}
}
