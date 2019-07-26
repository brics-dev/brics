package gov.nih.tbi.metastudy.model.hibernate;

import gov.nih.tbi.repository.model.hibernate.SupportingDocumentation;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;


@Entity
@DiscriminatorValue(value="M")
@XmlAccessorType(XmlAccessType.FIELD)
public class MetaStudyDocumentation extends SupportingDocumentation implements Serializable {

	private static final long serialVersionUID = 5622704520312389253L;
	

	//This needs to be eager in order to remove the access record when the meta study data is removed
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "supportingDocumentation", orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<MetaStudyAccessRecord> metaStudyAccessRecords = new HashSet<MetaStudyAccessRecord>();
	
	@Transient
    private String doi = "";

	public void setMetaStudyAccessRecords(Set<MetaStudyAccessRecord> metaStudyAccessRecords) {
		this.metaStudyAccessRecords = metaStudyAccessRecords;
	}

	public Set<MetaStudyAccessRecord> getMetaStudyAccessRecords() {
		return metaStudyAccessRecords;
	}
	
	public void setDoi(String doi){
		this.doi = doi;
	}
	public String getDoi(){
		return this.doi;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((doi == null) ? 0 : doi.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MetaStudyDocumentation other = (MetaStudyDocumentation) obj;
		if (doi == null) {
			if (other.doi != null)
				return false;
		} else if (!doi.equals(other.doi))
			return false;
		return true;
	}

}
