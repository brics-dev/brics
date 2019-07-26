package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.commons.model.Publication;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.repository.model.SupportingDocumentationInterface;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.DiscriminatorFormula;

import com.google.gson.annotations.Expose;

@Entity
@Table(name = "SUPPORTING_DOCUMENTATION")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula(
        "CASE WHEN study_id IS NOT NULL THEN 'S' " +
        " WHEN meta_study_id IS NOT NULL THEN 'M' " +
        " WHEN event_log_id IS NOT NULL THEN 'E' END")
@XmlRootElement(name = "supportingDocumentation")
@XmlAccessorType(XmlAccessType.FIELD)
public class SupportingDocumentation implements Serializable, SupportingDocumentationInterface {

	private static final long serialVersionUID = 5622704520312389253L;
	
	@Expose
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SUPPORTING_DOCUMENTATION_SEQ")
	@SequenceGenerator(name = "SUPPORTING_DOCUMENTATION_SEQ", sequenceName = "SUPPORTING_DOCUMENTATION_SEQ", allocationSize = 1)
	private Long id;

	@Expose
	@OneToOne
	@JoinColumn(name = "USER_FILE_ID")
	private UserFile userFile;

	@OneToOne
	@JoinColumn(name = "FILE_TYPE_ID")
	private FileType fileType;
	
	@Expose
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "PUBLICATION_ID")
	private Publication publication;

	@Expose
	@Column(name = "URL")
	private String url;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "DATE_CREATED")
	private Date dateCreated;

	@Column(name = "SOFTWARE_NAME")
	private String softwareName;

	@Column(name = "VERSION")
	private String version;

	@Column(name = "TITLE")
	private String title;
	
	public Long getId() {
		return id;
	}

	public UserFile getUserFile() {
		return userFile;
	}

	public FileType getFileType() {
		return fileType;
	}

	public String getUrl() {
		return url;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setUserFile(UserFile userFile) {
		this.userFile = userFile;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public Publication getPublication() {
		return publication;
	}

	public void setPublication(Publication publication) {
		this.publication = publication;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Boolean getIsUrl() {
		return url != null;
	}

	// returns description if object is a url, otherwise return description of the userfile
	public String getDescription() {

		if (url != null) {
			return description;
		} else {
			return (description != null ? description : userFile.getDescription());
		}
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getSoftwareName() {
		return softwareName;
	}

	public void setSoftwareName(String softwareName) {
		this.softwareName = softwareName;
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

	public String getName() {
		return url == null ? userFile.getName() : url;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((fileType == null) ? 0 : fileType.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((userFile == null) ? 0 : userFile.getName().hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		
		SupportingDocumentation other = (SupportingDocumentation) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (fileType == null) {
			if (other.fileType != null)
				return false;
		} else if (!fileType.equals(other.fileType))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (userFile == null) {
			if (other.userFile != null)
				return false;
		} else if (!userFile.getName().equals(other.getName()))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
}
