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
 * be removed and all its corresponding references should be changed to use Grant class.  
 *   
 * @author jim3
 *
 */
@Entity
@Table(name = "GRANT_TABLE")
public class MetaStudyGrant implements Serializable {

	private static final long serialVersionUID = -2142127874566747177L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GRANT_TABLE_SEQ")
	@SequenceGenerator(name = "GRANT_TABLE_SEQ", sequenceName = "GRANT_TABLE_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "GRANT_ID")
	private String grantId;

	@Column(name = "GRANT_NAME")
	private String grantName;

	@Column(name = "GRANT_FUNDERS")
	private String grantFunders;

	public MetaStudyGrant() {
		
	}
	
	public MetaStudyGrant(MetaStudyGrant grant) {
		this.id = grant.id;
		this.grantId = grant.grantId;
		this.grantName = grant.grantName;
		this.grantFunders = grant.grantFunders;		
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getGrantId() {
		return grantId;
	}

	public void setGrantId(String grantId) {
		this.grantId = grantId;
	}

	public String getGrantName() {
		return grantName;
	}

	public void setGrantName(String grantName) {
		this.grantName = grantName;
	}

	public String getGrantFunders() {
		return grantFunders;
	}

	public void setGrantFunders(String grantFunders) {
		this.grantFunders = grantFunders;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((grantId == null) ? 0 : grantId.hashCode());
		result = prime * result + ((grantName == null) ? 0 : grantName.hashCode());
		result = prime * result + ((grantFunders == null) ? 0 : grantFunders.hashCode());
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
		MetaStudyGrant other = (MetaStudyGrant) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		
		if (grantId == null) {
			if (other.grantId != null)
				return false;
		} else if (!grantId.equals(other.grantId))
			return false;
		
		if (grantName == null) {
			if (other.grantName != null)
				return false;
		} else if (!grantName.equals(other.grantName))
			return false;
		
		if (grantFunders == null) {
			if (other.grantFunders != null)
				return false;
		} else if (!grantFunders.equals(other.grantFunders))
			return false;
		
		return true;
	}

}
