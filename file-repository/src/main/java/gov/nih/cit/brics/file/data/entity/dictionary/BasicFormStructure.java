package gov.nih.cit.brics.file.data.entity.dictionary;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.repository.model.SubmissionType;

@Entity
@Table(name = "DATA_STRUCTURE")
public class BasicFormStructure {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATA_STRUCTURE_SEQ")
	@SequenceGenerator(name = "DATA_STRUCTURE_SEQ", sequenceName = "DATA_STRUCTURE_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "SHORT_NAME")
	private String shortName;

	@Column(name = "VERSION")
	private String version;

	@Column(name = "TITLE")
	private String title;

	@Column(name = "DESCRIPTION")
	private String description;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "STATUS_ID")
	private StatusType status;

	@Column(name = "PUBLICATION_DATE")
	private LocalDateTime publicationDate;

	@Column(name = "VALIDATABLE")
	private Boolean validatable;

	@Column(name = "ORGANIZATION")
	private String organization;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "FILE_TYPE_ID")
	private SubmissionType fileType;

	@Column(name = "IS_COPYRIGHTED")
	private Boolean isCopyrighted;

	@Column(name = "MODIFIED_DATE")
	private LocalDateTime modifiedDate;

	@Column(name = "MODIFIED_USER_ID")
	private Long modifiedUserId;

	@Column(name = "IS_CAT")
	private boolean isCAT;

	@Column(name = "CAT_OID")
	private String catOid;

	@Column(name = "MEASUREMENT_TYPE")
	private String measurementType;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public StatusType getStatus() {
		return status;
	}

	public void setStatus(StatusType status) {
		this.status = status;
	}

	public LocalDateTime getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(LocalDateTime publicationDate) {
		this.publicationDate = publicationDate;
	}

	public Boolean getValidatable() {
		return validatable;
	}

	public void setValidatable(Boolean validatable) {
		this.validatable = validatable;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public SubmissionType getFileType() {
		return fileType;
	}

	public void setFileType(SubmissionType fileType) {
		this.fileType = fileType;
	}

	public Boolean getIsCopyrighted() {
		return isCopyrighted;
	}

	public void setIsCopyrighted(Boolean isCopyrighted) {
		this.isCopyrighted = isCopyrighted;
	}

	public LocalDateTime getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(LocalDateTime modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public Long getModifiedUserId() {
		return modifiedUserId;
	}

	public void setModifiedUserId(Long modifiedUserId) {
		this.modifiedUserId = modifiedUserId;
	}

	public boolean isCAT() {
		return isCAT;
	}

	public void setCAT(boolean isCAT) {
		this.isCAT = isCAT;
	}

	public String getCatOid() {
		return catOid;
	}

	public void setCatOid(String catOid) {
		this.catOid = catOid;
	}

	public String getMeasurementType() {
		return measurementType;
	}

	public void setMeasurementType(String measurementType) {
		this.measurementType = measurementType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(catOid, description, fileType, id, isCAT, isCopyrighted, measurementType, modifiedDate,
				modifiedUserId, organization, publicationDate, shortName, status, title, validatable, version);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof BasicFormStructure)) {
			return false;
		}
		BasicFormStructure other = (BasicFormStructure) obj;
		return Objects.equals(catOid, other.catOid) && Objects.equals(description, other.description)
				&& fileType == other.fileType && Objects.equals(id, other.id) && isCAT == other.isCAT
				&& Objects.equals(isCopyrighted, other.isCopyrighted)
				&& Objects.equals(measurementType, other.measurementType)
				&& Objects.equals(modifiedDate, other.modifiedDate)
				&& Objects.equals(modifiedUserId, other.modifiedUserId)
				&& Objects.equals(organization, other.organization)
				&& Objects.equals(publicationDate, other.publicationDate) && Objects.equals(shortName, other.shortName)
				&& status == other.status && Objects.equals(title, other.title)
				&& Objects.equals(validatable, other.validatable) && Objects.equals(version, other.version);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BasicFormStructure [id=");
		builder.append(id);
		builder.append(", shortName=");
		builder.append(shortName);
		builder.append(", version=");
		builder.append(version);
		builder.append(", title=");
		builder.append(title);
		builder.append(", status=");
		builder.append(status);
		builder.append("]");
		return builder.toString();
	}
}
