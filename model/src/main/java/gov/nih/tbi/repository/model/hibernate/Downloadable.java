package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.repository.model.DownloadableOrigin;
import gov.nih.tbi.repository.model.SubmissionType;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


/**
 * This object represents a download file that can be part of a download package.
 * 
 * @author Francis Chen
 *
 */
@Entity
@Table(name = "DOWNLOAD_FILE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "origin", discriminatorType = DiscriminatorType.STRING)
@XmlRootElement(name = "download_file")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Downloadable implements Serializable {

	private static final long serialVersionUID = 6198729900311174288L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DOWNLOAD_FILE_SEQ")
	@SequenceGenerator(name = "DOWNLOAD_FILE_SEQ", sequenceName = "DOWNLOAD_FILE_SEQ", allocationSize = 1)
	@XmlAttribute
	private Long id;

	@ManyToOne
	@JoinColumn(name = "USER_FILE_ID")
	private UserFile userFile;

	@ManyToOne
	@JoinColumn(name = "DOWNLOAD_PACKAGE_ID", nullable = false)
	@XmlTransient
	private DownloadPackage downloadPackage;

	@XmlAttribute
	@Enumerated(EnumType.STRING)
	@Column(name = "ORIGIN")
	public abstract DownloadableOrigin getOrigin();


	@XmlAttribute
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "TYPE")
	private SubmissionType type;
	
	
	public abstract String getDownloadSubdirectory();

	public Downloadable() {}

	public Downloadable(UserFile userFile) {
		super();
		this.userFile = userFile;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserFile getUserFile() {
		return userFile;
	}

	public void setUserFile(UserFile userFile) {
		this.userFile = userFile;
	}

	public DownloadPackage getDownloadPackage() {
		return downloadPackage;
	}

	public void setDownloadPackage(DownloadPackage downloadPackage) {
		this.downloadPackage = downloadPackage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((userFile == null) ? 0 : userFile.hashCode());
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
		Downloadable other = (Downloadable) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (userFile == null) {
			if (other.userFile != null)
				return false;
		} else if (!userFile.equals(other.userFile))
			return false;
		return true;
	}
	
	public SubmissionType getType() {
		return type;
	}

	public void setType(SubmissionType type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "Downloadable [id=" + id + ", userFile=" + userFile + ", downloadPackage=" + downloadPackage + "]";
	}
}
