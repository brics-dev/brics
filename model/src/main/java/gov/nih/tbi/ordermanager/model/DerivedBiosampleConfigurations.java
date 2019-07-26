package gov.nih.tbi.ordermanager.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "derivedBiosampleConfigurations")
@XmlAccessorType(XmlAccessType.FIELD)
public class DerivedBiosampleConfigurations {
	@XmlElement
	private List<DerivedBiosampleRepository> derivedBiosampleRepositories = new ArrayList<DerivedBiosampleRepository>();

	@XmlAttribute
	private String repositoryColumnName;

	@XmlAttribute
	private String guidColumnName;

	@XmlAttribute
	private String visitTypeColumnName;
	
	@XmlAttribute
	private String sampleTypeColumnName;

	public List<DerivedBiosampleRepository> getDerivedBiosampleRepositories() {
		return derivedBiosampleRepositories;
	}

	public void setDerivedBiosampleRepositories(List<DerivedBiosampleRepository> derivedBiosampleRepositories) {
		this.derivedBiosampleRepositories = derivedBiosampleRepositories;
	}

	public String getRepositoryColumnName() {
		return repositoryColumnName;
	}

	public void setRepositoryColumnName(String repositoryColumnName) {
		this.repositoryColumnName = repositoryColumnName;
	}

	public String getGuidColumnName() {
		return guidColumnName;
	}

	public void setGuidColumnName(String guidColumnName) {
		this.guidColumnName = guidColumnName;
	}

	public String getVisitTypeColumnName() {
		return visitTypeColumnName;
	}

	public void setVisitTypeColumnName(String visitTypeColumnName) {
		this.visitTypeColumnName = visitTypeColumnName;
	}
	
	public String getSampleTypeColumnName() {
		return sampleTypeColumnName;
	}

	public void setSampleTypeColumnName(String sampleTypeColumnName) {
		this.sampleTypeColumnName = sampleTypeColumnName;
	}
}
