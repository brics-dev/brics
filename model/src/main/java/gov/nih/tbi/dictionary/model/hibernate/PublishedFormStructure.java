package gov.nih.tbi.dictionary.model.hibernate;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "PUBLISHED_FORM_STRUCTURE")
@XmlRootElement(name = "CreateTableFromFormStructure")
@XmlAccessorType(XmlAccessType.FIELD)
public class PublishedFormStructure implements Serializable {
	
	private static final long serialVersionUID = -6888473986725865807L;
	
	@Id 
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PUBLISHED_FORM_STRUCTURE_SEQ")
	@SequenceGenerator(name = "PUBLISHED_FORM_STRUCTURE_SEQ", sequenceName = "PUBLISHED_FORM_STRUCTURE_SEQ", allocationSize = 1)
	private Long id;
	
	@Column(name="FORM_STRUCTURE_ID")
	private Long formStructureId;
	
	@Column(name="DISEASE_ID")
	private Long diseaseId;
	
	@Column(name="IS_PUBLISHED")
	private boolean isPublished;
	
	@Column(name="ERROR_MESSAGE")
	private String errorMessage;
	
	@Column(name="PUBLICATION_DATE")
	private Date publicationDate;

	
	public PublishedFormStructure() {
		super();
	}

	public PublishedFormStructure(Long formStructureId, Long diseaseId) {
		this.formStructureId = formStructureId;
		this.diseaseId = diseaseId;
	}

	public Long getId() {
		return id;
	}

	public Long getFormStructureId() {
		return formStructureId;
	}

	public void setFormStructureId(Long formStructureId) {
		this.formStructureId = formStructureId;
	}

	public Long getDiseaseId() {
		return diseaseId;
	}

	public void setDiseaseId(Long diseaseId) {
		this.diseaseId = diseaseId;
	}

	public boolean isPublished() {
		return isPublished;
	}

	public void setPublished(boolean isPublished) {
		this.isPublished = isPublished;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Date getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(Date publicationDate) {
		this.publicationDate = publicationDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((diseaseId == null) ? 0 : diseaseId.hashCode());
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + ((formStructureId == null) ? 0 : formStructureId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (isPublished ? 1231 : 1237);
		result = prime * result + ((publicationDate == null) ? 0 : publicationDate.hashCode());
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
		PublishedFormStructure other = (PublishedFormStructure) obj;
		if (diseaseId == null) {
			if (other.diseaseId != null)
				return false;
		} else if (!diseaseId.equals(other.diseaseId))
			return false;
		if (errorMessage == null) {
			if (other.errorMessage != null)
				return false;
		} else if (!errorMessage.equals(other.errorMessage))
			return false;
		if (formStructureId == null) {
			if (other.formStructureId != null)
				return false;
		} else if (!formStructureId.equals(other.formStructureId))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (isPublished != other.isPublished)
			return false;
		if (publicationDate == null) {
			if (other.publicationDate != null)
				return false;
		} else if (!publicationDate.equals(other.publicationDate))
			return false;
		return true;
	}
	
	
	
}
