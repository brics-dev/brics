
package gov.nih.tbi.commons.model.hibernate;

import java.io.Serializable;
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

import gov.nih.tbi.commons.model.FileClassification;

@Entity
@Table(name = "FILE_TYPE")
public class FileType implements Serializable {

	private static final long serialVersionUID = -6031773610785558635L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FILE_TYPE_SEQ")
	@SequenceGenerator(name = "FILE_TYPE_SEQ", sequenceName = "FILE_TYPE_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "NAME")
	private String name;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "CLASSIFICATION_ID")
	private FileClassification fileClassification;

	@Column(name = "IS_ACTIVE")
	private Boolean isActive;
	
	public FileType(){}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public FileClassification getFileClassification() {
		return fileClassification;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setId(Integer id) {
		this.id = Long.getLong(id.toString());
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFileClassification(FileClassification fileClassification) {
		this.fileClassification = fileClassification;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fileClassification, id, isActive, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof FileType)) {
			return false;
		}
		FileType other = (FileType) obj;
		return fileClassification == other.fileClassification && Objects.equals(id, other.id)
				&& Objects.equals(isActive, other.isActive) && Objects.equals(name, other.name);
	}
	
}
