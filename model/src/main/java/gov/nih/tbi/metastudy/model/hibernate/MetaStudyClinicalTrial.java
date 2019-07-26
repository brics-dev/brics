package gov.nih.tbi.metastudy.model.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * This model class was created as a temporary fix to CRIT-6494. We believe it is a hibernate mapping bug and would 
 * be resolved when we upgrade our hibernate library to the newer version. In that case this new model class should
 * be removed and all its corresponding references should be changed to use ClinicalTrial class.  
 *   
 * @author jim3
 *
 */
@Entity
@Table(name = "CLINICAL_TRIAL")
public class MetaStudyClinicalTrial implements Serializable {


	private static final long serialVersionUID = 2178515723584353628L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CLINICAL_TRIAL_SEQ")
	@SequenceGenerator(name = "CLINICAL_TRIAL_SEQ", sequenceName = "CLINICAL_TRIAL_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "CLINICAL_TRIAL_ID")
	private String clinicalTrialId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getClinicalTrialId() {
		return clinicalTrialId;
	}

	public void setClinicalTrialId(String clinicalTrialId) {
		this.clinicalTrialId = clinicalTrialId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((clinicalTrialId == null) ? 0 : clinicalTrialId.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		MetaStudyClinicalTrial other = (MetaStudyClinicalTrial) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		
		if (clinicalTrialId == null) {
			if (other.clinicalTrialId != null)
				return false;
		} else if (!clinicalTrialId.equals(other.clinicalTrialId))
			return false;
		
		return true;
	}
}

