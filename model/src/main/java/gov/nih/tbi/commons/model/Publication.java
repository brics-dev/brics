package gov.nih.tbi.commons.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;

import com.google.gson.annotations.Expose;

/**
 * The persistent class for the publication database table.
 * 
 */
@Entity
@Table(name = "Publication")
public class Publication implements Serializable {

	private static final long serialVersionUID = -1201468353912079077L;

	private static final SimpleDateFormat isoFormatting = new SimpleDateFormat("yyyy-MM-dd");
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PUBLICATION_SEQ")
	@SequenceGenerator(name = "PUBLICATION_SEQ", sequenceName = "PUBLICATION_SEQ", allocationSize = 1)
	private Long id;
	
	@Expose
	@Column(name = "title")
	private String title;

	@Expose
	@Column(name = "description")
	private String description;
	
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "first_author_id", nullable = false)
	private Author firstAuthor;
	
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "last_author_id")
	private Author lastAuthor;

	@Temporal(TemporalType.DATE)
	@Column(name = "publication_date")
	private Date publicationDate;

	@Column(name = "pubmed_id")
	private String pubmedId;


	public Publication() {}
	
	public Publication(Publication publication) {
		this.id = publication.id;
		this.title = publication.title;
		this.description = publication.description;
		this.firstAuthor = publication.firstAuthor;
		this.lastAuthor = publication.lastAuthor;
		this.publicationDate = publication.publicationDate;
		this.pubmedId = publication.pubmedId;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getPublicationDate() {
		return this.publicationDate;
	}

	public void setPublicationDate(String publicationDate) {
		
		if (!StringUtils.isEmpty(publicationDate)) {
			try {
				this.publicationDate = isoFormatting.parse(publicationDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			this.publicationDate = null;
		}
	}
	
	public String getPubmedId() {
		return this.pubmedId;
	}

	public void setPubmedId(String pubmedId) {
		this.pubmedId = pubmedId;
	}


	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((publicationDate == null) ? 0 : publicationDate.hashCode());
		result = prime * result + ((pubmedId == null) ? 0 : pubmedId.hashCode());
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
		Publication other = (Publication) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
	
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		
	
		if (publicationDate == null) {
			if (other.publicationDate != null)
				return false;
		} else if (!publicationDate.equals(other.publicationDate))
			return false;
		if (pubmedId == null) {
			if (other.pubmedId != null)
				return false;
		} else if (!pubmedId.equals(other.pubmedId))
			return false;
	
	
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}



	public Author getFirstAuthor() {
		return firstAuthor;
	}

	public void setFirstAuthor(Author firstAuthor) {
		this.firstAuthor = firstAuthor;
	}

	public Author getLastAuthor() {
		return lastAuthor;
	}

	public void setLastAuthor(Author lastAuthor) {
		this.lastAuthor = lastAuthor;
	}

	@Override
	public String toString() {
		return "Publication [id=" + id + ", title=" + title + ", description=" + description + ", publicationDate="
				+ publicationDate + ", pubmedId=" + pubmedId + "]";
	}
	
	public String getDateString() {
		return isoFormatting.format(getPublicationDate());
	}

}