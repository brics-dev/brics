package gov.nih.tbi.file.model.hibernate;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.nih.tbi.file.model.SystemFileCategory;

@Entity
@Table(name = "BRICS_FILE")
public class BricsFile {
	@Id
	@Column(name = "ID")
	private String id;

	@Column(name = "FILE_NAME")
	private String fileName;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "FILE_CATEGORY_ID")
	private SystemFileCategory fileCategory;

	@Column(name = "LINKED_OBJECT_ID")
	private Long linkedObjectID;

	@Column(name = "LEGACY_USER_FILE_ID")
	private Long legacyUserFileId;

	@Column(name = "UPLOADED_BY")
	private Long uploadedBy;

	@Column(name = "UPLOADED_DATE")
	private LocalDateTime uploadedDate;

	public BricsFile() {}

	public BricsFile(String id, String fileName, SystemFileCategory fileCategory, Long assoicatedObjectID) {
		this.id = id;
		this.fileName = fileName;
		this.fileCategory = fileCategory;
		this.linkedObjectID = assoicatedObjectID;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public SystemFileCategory getFileCategory() {
		return fileCategory;
	}

	public void setFileCategory(SystemFileCategory fileCategory) {
		this.fileCategory = fileCategory;
	}

	public Long getLinkedObjectID() {
		return linkedObjectID;
	}

	public void setLinkedObjectID(Long linkedObjectID) {
		this.linkedObjectID = linkedObjectID;
	}

	public Long getLegacyUserFileId() {
		return legacyUserFileId;
	}

	public void setLegacyUserFileId(Long legacyUserFileId) {
		this.legacyUserFileId = legacyUserFileId;
	}

	public Long getUploadedBy() {
		return uploadedBy;
	}

	public void setUploadedBy(Long uploadedBy) {
		this.uploadedBy = uploadedBy;
	}

	public LocalDateTime getUploadedDate() {
		return uploadedDate;
	}

	public void setUploadedDate(LocalDateTime uploadedDate) {
		this.uploadedDate = uploadedDate;
	}

	@Override
	public int hashCode() {
		return Objects.hash(linkedObjectID, fileCategory, fileName, id, legacyUserFileId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof BricsFile)) {
			return false;
		}

		BricsFile other = (BricsFile) obj;
		return Objects.equals(linkedObjectID, other.linkedObjectID) && fileCategory == other.fileCategory
				&& Objects.equals(fileName, other.fileName) && Objects.equals(id, other.id)
				&& Objects.equals(legacyUserFileId, other.legacyUserFileId);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BricsFile [id=");
		builder.append(id);
		builder.append(", fileName=");
		builder.append(fileName);
		builder.append(", fileCategory=");
		builder.append(fileCategory);
		builder.append(", linkedObjectID=");
		builder.append(linkedObjectID);
		builder.append(", legacyUserFileId=");
		builder.append(legacyUserFileId);
		builder.append(", uploadedBy=");
		builder.append(uploadedBy);
		builder.append(", uploadedDate=");
		builder.append(uploadedDate);
		builder.append("]");
		return builder.toString();
	}
}
