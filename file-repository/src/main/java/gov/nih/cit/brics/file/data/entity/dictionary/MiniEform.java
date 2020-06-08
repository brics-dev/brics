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

@Entity
@Table(name = "EFORM")
public class MiniEform {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EFORM_SEQ")
	@SequenceGenerator(name = "EFORM_SEQ", sequenceName = "EFORM_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "TITLE")
	private String title;

	@Column(name = "SHORT_NAME")
	private String shortName;

	@Column(name = "FORM_STRUCTURE_NAME")
	private String formStructureShortName;

	@Column(name = "DESCRIPTION")
	private String description;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "STATUS_ID")
	public StatusType status;

	@Column(name = "CREATED_BY")
	private String createBy;

	@Column(name = "CREATE_DATE")
	private LocalDateTime createDate;

	@Column(name = "PUBLISHED_DATE")
	private LocalDateTime publicationDate;

	@Column(name = "UPDATED_DATE")
	private LocalDateTime updatedDate;

	@Column(name = "IS_SHARED")
	private Boolean isShared;

	@Column(name = "IS_LEGACY")
	private Boolean isLegacy;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getFormStructureShortName() {
		return formStructureShortName;
	}

	public void setFormStructureShortName(String formStructureShortName) {
		this.formStructureShortName = formStructureShortName;
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

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public LocalDateTime getCreateDate() {
		return createDate;
	}

	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}

	public LocalDateTime getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(LocalDateTime publicationDate) {
		this.publicationDate = publicationDate;
	}

	public LocalDateTime getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(LocalDateTime updatedDate) {
		this.updatedDate = updatedDate;
	}

	public Boolean getIsShared() {
		return isShared;
	}

	public void setIsShared(Boolean isShared) {
		this.isShared = isShared;
	}

	public Boolean getIsLegacy() {
		return isLegacy;
	}

	public void setIsLegacy(Boolean isLegacy) {
		this.isLegacy = isLegacy;
	}

	@Override
	public int hashCode() {
		return Objects.hash(createBy, createDate, description, formStructureShortName, id, isLegacy, isShared,
				publicationDate, shortName, status, title, updatedDate);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MiniEform)) {
			return false;
		}
		MiniEform other = (MiniEform) obj;
		return Objects.equals(createBy, other.createBy) && Objects.equals(createDate, other.createDate)
				&& Objects.equals(description, other.description)
				&& Objects.equals(formStructureShortName, other.formStructureShortName) && Objects.equals(id, other.id)
				&& Objects.equals(isLegacy, other.isLegacy) && Objects.equals(isShared, other.isShared)
				&& Objects.equals(publicationDate, other.publicationDate) && Objects.equals(shortName, other.shortName)
				&& status == other.status && Objects.equals(title, other.title)
				&& Objects.equals(updatedDate, other.updatedDate);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MiniEform [id=");
		builder.append(id);
		builder.append(", title=");
		builder.append(title);
		builder.append(", shortName=");
		builder.append(shortName);
		builder.append(", formStructureShortName=");
		builder.append(formStructureShortName);
		builder.append(", status=");
		builder.append(status);
		builder.append("]");
		return builder.toString();
	}
}
