package gov.nih.tbi.dictionary.model.hibernate;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import gov.nih.tbi.commons.model.Publication;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.repository.model.SupportingDocumentationInterface;
import gov.nih.tbi.repository.model.hibernate.UserFile;

@Entity
@Table(name = "SUPPORTING_DOCUMENTATION")
@XmlRootElement(name = "supportingDocumentation")
@XmlAccessorType(XmlAccessType.FIELD)
public class DictionarySupportingDocumentation implements Serializable, SupportingDocumentationInterface {

	private static final long serialVersionUID = 4112456985723642133L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SUPPORTING_DOCUMENTATION_SEQ")
	@SequenceGenerator(name = "SUPPORTING_DOCUMENTATION_SEQ", sequenceName = "SUPPORTING_DOCUMENTATION_SEQ", allocationSize = 1)
	private Long id;

	@OneToOne
	@JoinColumn(name = "USER_FILE_ID")
	private UserFile userFile;

	@OneToOne
	@JoinColumn(name = "FILE_TYPE_ID")
	private FileType fileType;

	@Column(name = "URL")
	private String url;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "TITLE")
	private String title;
	
	@Column(name = "DATE_CREATED")
	private Date dateCreated;
	
	@ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "event_log_id")
    private DictionaryEventLog dictionaryEventLog;
	
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

	public String getName() {

		return url == null ? userFile.getName() : url;
	}

	public DictionaryEventLog getDictionaryEventLog() {
		return dictionaryEventLog;
	}

	public void setDictionaryEventLog(DictionaryEventLog dictionaryEventLog) {
		this.dictionaryEventLog = dictionaryEventLog;
	}
	
	/**
	 * dummy method to allow this implementation to fit the interface.  Because we're bad
	 * and don't follow our structures correctly
	 */
	public Publication getPublication() {
		return null;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	
}
