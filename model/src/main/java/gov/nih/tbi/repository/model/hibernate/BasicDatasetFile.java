package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.commons.model.DatasetFileStatus;
import gov.nih.tbi.repository.model.SubmissionType;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Data model for dataset files
 * 
 * @author Francis Chen
 * 
 */
@Entity
@Table(name = "DATASET_FILE")
@XmlRootElement(name = "datasetFile")
@XmlAccessorType(XmlAccessType.FIELD)
public class BasicDatasetFile implements Serializable, Comparable<DatasetFile> {

	private static final long serialVersionUID = 6829996196792215808L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATASET_FILE_SEQ")
	@SequenceGenerator(name = "DATASET_FILE_SEQ", sequenceName = "DATASET_FILE_SEQ", allocationSize = 1)
	private Long id;

	@OneToOne
	@JoinColumn(name = "USER_FILE_ID")
	private UserFile userFile;

	@Column(name = "DATASET_ID")
	@XmlTransient
	private Long dataset;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "FILE_TYPE_ID")
	private SubmissionType fileType;

	@Column(name = "IS_QUERYABLE")
	private Boolean isQueryable;

	@Column(name = "LOCAL_LOCATION")
	private String localLocation;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "DATASET_FILE_STATUS")
	private DatasetFileStatus datasetFileStatus;

	public BasicDatasetFile() {

	}

	public BasicDatasetFile(BasicDatasetFile clone) {
		this.id = clone.id;
		if (clone.userFile != null) {
			this.userFile = new UserFile(clone.userFile);
		}

		this.dataset = clone.dataset;
		this.fileType = clone.fileType;
		this.isQueryable = clone.isQueryable;
		this.localLocation = clone.localLocation;
		this.datasetFileStatus = clone.datasetFileStatus;
	}

	public String getLocalLocation() {

		return localLocation;
	}

	public void setLocalLocation(String localLocation) {

		this.localLocation = localLocation;
	}

	public DatasetFileStatus getDatasetFileStatus() {

		return datasetFileStatus;
	}

	public void setDatasetFileStatus(DatasetFileStatus datasetFileStatus) {

		this.datasetFileStatus = datasetFileStatus;
	}

	public Long getId() {

		return id;
	}

	public UserFile getUserFile() {

		return userFile;
	}

	public Long getDataset() {

		return dataset;
	}

	public SubmissionType getFileType() {

		return fileType;
	}

	public void setId(Long id) {

		this.id = id;
	}

	public void setUserFile(UserFile userFile) {

		this.userFile = userFile;
	}

	public void setDataset(Long dataset) {

		this.dataset = dataset;
	}

	public void setFileType(SubmissionType fileType) {

		this.fileType = fileType;
	}

	public Boolean getIsQueryable() {

		return isQueryable;
	}

	public void setIsQueryable(Boolean isQueryable) {

		this.isQueryable = isQueryable;
	}

	@Override
	public int compareTo(DatasetFile o) {
		return userFile.compareTo(o.getUserFile());
	}
}
