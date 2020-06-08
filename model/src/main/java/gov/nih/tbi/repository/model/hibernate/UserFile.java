package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.gson.annotations.Expose;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.file.model.hibernate.BricsFile;

@Entity
@Table(name = "USER_FILE")
@XmlRootElement(name = "UserFile")
public class UserFile implements Serializable, Comparable<UserFile> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -136903741147861148L;
	
	@Expose
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATAFILE_ENDPOINT_INFO_SEQ")
	@SequenceGenerator(name = "DATAFILE_ENDPOINT_INFO_SEQ", sequenceName = "DATAFILE_ENDPOINT_INFO_SEQ", allocationSize = 1)
	private Long id;

	@Expose
	@Column(name = "FILE_NAME")
	private String name;

	@Column(name = "FILE_DESCRIPTION")
	private String description;

	@Column(name = "FILE_PATH")
	private String path;

	@OneToOne
	@JoinColumn(name = "DATAFILE_ENDPOINT_INFO_ID")
	@XmlTransient
	private DatafileEndpointInfo dataFileEndpointInfo;

	@Column(name = "USER_ID")
	private Long userId;

	@ManyToOne
	@JoinColumn(name = "DATASTORE_BINARY_INFO_ID")
	private DataStoreBinaryInfo dataStoreBinaryInfo;

	@OneToOne
	@JoinColumn(name = "FILE_TYPE_ID")
	private FileType fileType;

	@Column(name = "USER_FILE_SIZE")
	private Long size;

	@Expose
	@Column(name = "UPLOAD_DATE")
	private Date uploadedDate;

	@OneToOne
	@JoinColumn(name = "BRICS_FILE_ID")
	private BricsFile bricsFile;

	public UserFile() {}

	public UserFile(UserFile clone) {
		this.id = clone.id;
		this.name = clone.name;
		this.description = clone.description;
		this.path = clone.path;
		this.dataFileEndpointInfo = clone.dataFileEndpointInfo;
		this.fileType = clone.fileType;
		this.size = clone.size;
		this.userId = clone.userId;
		if (clone.uploadedDate != null) {
			this.uploadedDate = new Date(clone.uploadedDate.getTime());
		}
		
	}

	public Date getUploadedDate() {

		return uploadedDate;
	}

	public void setUploadedDate(Date uploadedDate) {

		this.uploadedDate = uploadedDate;
	}

	public Long getSize() {

		return size;
	}

	public void setSize(Long size) {

		this.size = size;
	}

	public Long getId() {

		return id;
	}

	public void setId(Long id) {

		this.id = id;
	}

	public String getName() {

		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

	public String getDescription() {

		return description;
	}

	public void setDescription(String description) {

		this.description = description;
	}

	public String getPath() {

		return path;
	}

	public void setPath(String path) {

		this.path = path;
	}

	public DatafileEndpointInfo getDatafileEndpointInfo() {

		return dataFileEndpointInfo;
	}

	public void setDatafileEndpointInfo(DatafileEndpointInfo datafileEndpointInfo) {

		this.dataFileEndpointInfo = datafileEndpointInfo;
	}

	public DataStoreBinaryInfo getDataStoreBinaryInfo() {

		return dataStoreBinaryInfo;
	}

	public void setDataStoreBinaryInfo(DataStoreBinaryInfo dataStoreBinaryInfo) {

		this.dataStoreBinaryInfo = dataStoreBinaryInfo;
	}

	public FileType getFileType() {

		return fileType;
	}

	public void setFileType(FileType fileType) {

		this.fileType = fileType;
	}

	public Long getUserId() {

		return userId;
	}

	public void setUserId(Long userId) {

		this.userId = userId;
	}

	/**
	 * Returns the expirationDate as a string formatted yyyy-MM-dd
	 * 
	 * @return
	 */
	public String getUploadDateString() {

		if (uploadedDate == null) {
			return null;
		}
		SimpleDateFormat df = new SimpleDateFormat(ModelConstants.ISO_DATE_FORMAT);

		return df.format(uploadedDate);
	}

	public String getIsoUploadDateString() {

		if (uploadedDate == null) {
			return null;
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		return df.format(uploadedDate);
	}

	public BricsFile getBricsFile() {
		return bricsFile;
	}

	public void setBricsFile(BricsFile bricsFile) {
		this.bricsFile = bricsFile;
	}

	@Override
	public int compareTo(UserFile o) {
		return name.compareTo(o.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(bricsFile, description, fileType, id, name, path, size, uploadedDate, userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof UserFile)) {
			return false;
		}
		UserFile other = (UserFile) obj;
		return Objects.equals(bricsFile, other.bricsFile) && Objects.equals(description, other.description)
				&& Objects.equals(fileType, other.fileType) && Objects.equals(id, other.id)
				&& Objects.equals(name, other.name) && Objects.equals(path, other.path)
				&& Objects.equals(size, other.size) && Objects.equals(uploadedDate, other.uploadedDate)
				&& Objects.equals(userId, other.userId);
	}
}
