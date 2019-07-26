package gov.nih.tbi.metastudy.model.hibernate;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.annotations.Expose;

import gov.nih.tbi.commons.model.ResearchManagementRole;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;
import gov.nih.tbi.repository.model.hibernate.UserFile;

/**
 * This model class was created as a temporary fix to CRIT-6494. We believe it is a hibernate mapping bug and would 
 * be resolved when we upgrade our hibernate library to the newer version. In that case, this class should be removed  
 * and the corresponding the Dao classes should be merged with ResearchManageMentDaoImpl class.  
 *   
 * @author jim3
 *
 */
@Entity
@Table(name = "RESEARCH_MANAGEMENT")
public class ResearchManagementMeta implements Serializable {

	private static final long serialVersionUID = -2743494384900916059L;

	@Expose
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RESEARCH_MANAGEMENT_SEQ")
    @SequenceGenerator(name = "RESEARCH_MANAGEMENT_SEQ", sequenceName = "RESEARCH_MANAGEMENT_SEQ", allocationSize = 1)
    private Long id;

    @Expose
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "ROLE_ID")
	private ResearchManagementRole role;
	
	@Expose
    @Column(name = "FIRST_NAME")
    private String firstName;

    @Expose
    @Column(name = "MI")
    private String mi;

    @Expose
    @Column(name = "LAST_NAME")
    private String lastName;

    @Expose
    @Column(name = "SUFFIX")
    private String suffix;

    @Expose
    @Column(name = "EMAIL")
    private String email;

    @Expose
    @Column(name = "ORG_NAME")
    private String orgName;

	@XmlTransient
    @OneToOne(fetch=FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "PICTURE_FILE_ID")
    private UserFile pictureFile;

	public ResearchManagementMeta() {
	}
	
	public ResearchManagementMeta(ResearchManagementMeta researchMgmt) {
		this.id = researchMgmt.getId();
		this.role = researchMgmt.role;
		this.firstName = researchMgmt.firstName;
		this.mi = researchMgmt.mi;
		this.lastName = researchMgmt.lastName;
		this.suffix = researchMgmt.suffix;
		this.email = researchMgmt.email;
		this.orgName = researchMgmt.orgName;
	}
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ResearchManagementRole getRole() {
		return role;
	}

	public void setResearchMgmtRole(ResearchManagementRole role) {
		this.role = role;
	}

	public void setRole(Long roleId) {
		this.role = ResearchManagementRole.roleOf(roleId);
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMi() {
		return mi;
	}

	public void setMi(String mi) {
		this.mi = mi;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getFullName() {
		String fullName = "";
		
		if (!StringUtils.isBlank(firstName)) {
			fullName += firstName + " ";
		}
		if (!StringUtils.isBlank(mi)) {
			fullName += mi + " ";
		}
		if (!StringUtils.isBlank(lastName)) {
			fullName += lastName;
		}
		if (!StringUtils.isBlank(suffix)) {
			fullName += ", " + suffix;
		}
		
		return fullName.trim();
	}
	
	public String getRowId() {
		String rowId = "";
		
		if (!StringUtils.isBlank(firstName)) {
			rowId += firstName;
		}
		if (!StringUtils.isBlank(mi)) {
			rowId += mi;
		}
		if (!StringUtils.isBlank(lastName)) {
			rowId += lastName;
		}
		if (!StringUtils.isBlank(suffix)) {
			rowId += suffix;
		}
		
		rowId += role.getId();
		
		return rowId.trim();
	}	
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public UserFile getPictureFile() {
		return pictureFile;
	}

	public void setPictureFile(UserFile pictureFile) {
		this.pictureFile = pictureFile;
	}

	public boolean isPrimaryPI() {
		return role == ResearchManagementRole.PRIMARY_PRINCIPAL_INVESTIGATOR;
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((role == null) ? 0 : ((Long)role.getId()).intValue());
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((mi == null) ? 0 : mi.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((orgName == null) ? 0 : orgName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ResearchManagementMeta))
			return false;
		ResearchManagementMeta other = (ResearchManagementMeta) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		
		if (role == null) {
			if (other.role != null)
				return false;
		} else if (role != other.role)
			return false;
		
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		
		if (mi == null) {
			if (other.mi != null)
				return false;
		} else if (!mi.equals(other.mi))
			return false;
		
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		
		if (orgName == null) {
			if (other.orgName != null)
				return false;
		} else if (!orgName.equals(other.orgName))
			return false;
		
		return true;
	}
	
	@Override
	public String toString() {
		return firstName + " " + lastName;
	}
	
	public boolean stringEquals(ResearchManagementMeta other) {

		if (this == other)
			return true;
		if (other == null)
			return false;
		
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;

		if (role == null) {
			if (other.role != null)
				return false;
		} else if (role != other.role)
			return false;

		if (StringUtils.isEmpty(firstName)) {
			if (StringUtils.isNotEmpty(other.firstName))
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;

		if (StringUtils.isEmpty(mi)) {
			if (StringUtils.isNotEmpty(other.mi))
				return false;
		} else if (!mi.equals(other.mi))
			return false;

		if (StringUtils.isEmpty(lastName)) {
			if (StringUtils.isNotEmpty(other.lastName))
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;

		if (StringUtils.isEmpty(email)) {
			if (StringUtils.isNotEmpty(other.email))
				return false;
		} else if (!email.equals(other.email))
			return false;

		if (StringUtils.isEmpty(orgName)) {
			if (StringUtils.isNotEmpty(other.orgName))
				return false;
		} else if (!orgName.equals(other.orgName))
			return false;

		return true;

	}	
	
}
