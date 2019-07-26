package gov.nih.tbi.query.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "diagnosisConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
public class DiagnosisConfiguration {

	// this is the permissible value that the diagnosis will become in the catalog when the derived value does not map
	// to catalog's diagnosis data element
	@XmlAttribute(name = "catalogOther")
	private String catalogOtherPermissibleValue;

	// this is the name of the catalog data element representing clinical diagnosis
	@XmlAttribute(name = "catalogDe")
	private String catalogDataElementName;

	// this is the name of the data element where we are deriving diagnosis from
	@XmlAttribute(name = "deriveDe")
	private String deriveDataElementName;

	// this is the permissible value the diagnosis will become in the catalog when there are no diagnosis derived
	@XmlAttribute(name = "noDiagnosis")
	private String noDiagnosisPermissibleValue;

	@XmlAttribute(name = "caseOrControl")
	private String caseOrControl;

	public String getCaseOrControl() {
		return caseOrControl;
	}

	public void setCaseOrControl(String caseOrControl) {
		this.caseOrControl = caseOrControl;
	}

	public String getCatalogOtherPermissibleValue() {
		return catalogOtherPermissibleValue;
	}

	public void setCatalogOtherPermissibleValue(String catalogOtherPermissibleValue) {
		this.catalogOtherPermissibleValue = catalogOtherPermissibleValue;
	}

	public String getCatalogDataElementName() {
		return catalogDataElementName;
	}

	public void setCatalogDataElementName(String catalogDataElementName) {
		this.catalogDataElementName = catalogDataElementName;
	}


	public String getNoDiagnosisPermissibleValue() {
		return noDiagnosisPermissibleValue;
	}

	public void setNoDiagnosisPermissibleValue(String noDiagnosisPermissibleValue) {
		this.noDiagnosisPermissibleValue = noDiagnosisPermissibleValue;
	}

	public String getDeriveDataElementName() {
		return deriveDataElementName;
	}

	public void setDeriveDataElementName(String deriveDataElementName) {
		this.deriveDataElementName = deriveDataElementName;
	}
}
