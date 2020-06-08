package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;

import com.google.gson.annotations.Expose;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.VisualizationEntityMap;
import gov.nih.tbi.commons.model.DataSource;
import gov.nih.tbi.commons.model.RecruitmentStatus;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.commons.model.StudyType;
import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.repository.model.SubmissionType;

/**
 * Model for Access Records to use in Visualization
 * 
 * @author Sofia Zaim
 */


@Table(name = "ACCESS_RECORD")
@XmlRootElement(name = "accessRecord")
@XmlAccessorType(XmlAccessType.FIELD)
public class VisualizationAccessData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8503894534132600784L;
	
	@Expose
	private Integer id;

	@Expose
	private Integer accountId;
	
	@Expose
	private String userName;
	
	@Expose
	private String firstName;
	
	@Expose
	private String lastName;
	
	@Expose
	private Integer studyId;
	
	@Expose
	private String studyTitle;
	
	@Expose
	private Integer datasetId;
	

	public VisualizationAccessData() {

	}

	public VisualizationAccessData(Integer accountId, String userName, String firstName, String lastName, Integer studyId, String studyTitle, Integer datasetId) {

		this.setAccountId(accountId);
		this.setUserName(userName);
		this.setFirstName(firstName);
		this.setLastName(lastName);
		this.setStudyId(studyId);
		this.setStudyTitle(studyTitle);
		this.setDatasetId(datasetId);
	}
	
	public Integer getId() {

		return id;
	}

	/**
	 * @return the accountId
	 */
	public Integer getAccountId() {
		return accountId;
	}

	/**
	 * @param accountId the accountId to set
	 */
	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}
	
	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}
	
	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}
	
	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param studyId the studyId to set
	 */
	public void setStudyId(Integer studyId) {
		this.studyId = studyId;
	}
	
	/**
	 * @return the studyId
	 */
	public Integer getStudyId() {
		return studyId;
	}

	/**
	 * @param studyTitle the studyTitle to set
	 */
	public void setStudyTitle(String studyTitle) {
		this.studyTitle = studyTitle;
	}
	
	/**
	 * @return the studyTitle
	 */
	public String getStudyTitle() {
		return studyTitle;
	}
	
	/**
	 * @param datasetId the datasetId to set
	 */
	public void setDatasetId(Integer datasetId) {
		this.datasetId = datasetId;
	}
	
	/**
	 * @return the datasetId
	 */
	public Integer getDatasetId() {
		return datasetId;
	}
	
	/**
	 * Overrides display name for use in permission pages
	 */
	public String getDisplayName() {
		return BRICSStringUtils.capitalizeFirstCharacter(getLastName().trim()) + ", "
				+ BRICSStringUtils.capitalizeFirstCharacter(getFirstName().trim());
	}


	@Override
	public String toString() {

		return "Visualization Access Record [id=" + getId() + ", dataset id="
				+ getDatasetId() + ", study id="
				+ getStudyId() + ", account id=" + getAccountId() + "]";
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, accountId, userName, firstName, lastName, studyId, studyTitle, datasetId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof VisualizationAccessData)) {
			return false;
		}
		VisualizationAccessData other = (VisualizationAccessData) obj;
		return Objects.equals(id, other.id) && Objects.equals(accountId, other.accountId)
				&& Objects.equals(userName, other.userName) && Objects.equals(firstName, other.firstName)
				&& Objects.equals(lastName, other.lastName) 
				&& Objects.equals(studyId, other.studyId) 
				&& Objects.equals(studyTitle, other.studyTitle)
				&& Objects.equals(datasetId, other.datasetId);
	}




	
}
