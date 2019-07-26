package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.repository.model.DownloadPackageOrigin;
import gov.nih.tbi.repository.model.DownloadableAdapter;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * This object represents a set of download files that are added as part of a SINGLE add to download list action
 * 
 * @author Francis Chen
 *
 */
@Entity
@Table(name = "DOWNLOAD_PACKAGE")
@XmlRootElement(name = "download_package")
@XmlAccessorType(XmlAccessType.FIELD)
public class DownloadPackage implements Serializable {

	private static final long serialVersionUID = -7610979289265733927L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DOWNLOAD_PACKAGE_SEQ")
	@SequenceGenerator(name = "DOWNLOAD_PACKAGE_SEQ", sequenceName = "DOWNLOAD_PACKAGE_SEQ", allocationSize = 1)
	@XmlAttribute
	private Long id;

	@Column(name = "DESCRIPTION")
	private String name;

	@Column(name = "DATE_ADDED")
	@XmlAttribute
	private Date dateAdded;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "downloadPackage", targetEntity = Downloadable.class, orphanRemoval = true)
	@XmlJavaTypeAdapter(DownloadableAdapter.class)
	private Set<Downloadable> downloadables;

	@XmlElement
	@ManyToOne(targetEntity = User.class)
	@JoinColumn(name = "USER_ID")
	private User user;

	@XmlAttribute
	@Enumerated(EnumType.STRING)
	@Column(name = "ORIGIN")
	private DownloadPackageOrigin origin;

	public DownloadPackage() {
		this.downloadables = new HashSet<Downloadable>();
		this.dateAdded = new Date();
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

	public Date getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(Date dateAdded) {
		this.dateAdded = dateAdded;
	}

	public Set<Downloadable> getDownloadables() {
		return downloadables;
	}

	public void setDownloadables(Set<Downloadable> downloadables) {
		this.downloadables = downloadables;
	}

	public void addDownloadable(Downloadable downloadable) {
		this.downloadables.add(downloadable);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public DownloadPackageOrigin getOrigin() {
		return origin;
	}

	public void setOrigin(DownloadPackageOrigin origin) {
		this.origin = origin;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dateAdded == null) ? 0 : dateAdded.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((downloadables == null) ? 0 : downloadables.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
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
		DownloadPackage other = (DownloadPackage) obj;
		if (dateAdded == null) {
			if (other.dateAdded != null)
				return false;
		} else if (!dateAdded.equals(other.dateAdded))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (downloadables == null) {
			if (other.downloadables != null)
				return false;
		} else if (!downloadables.equals(other.downloadables))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (origin != other.origin)
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DownloadPackage [id=" + id + ", description=" + name + ", dateAdded=" + dateAdded + ", user="
				+ user + ", origin=" + origin + "]";
	}

}
