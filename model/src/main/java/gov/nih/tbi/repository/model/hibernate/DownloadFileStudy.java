package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "STUDY")
public class DownloadFileStudy implements Serializable {

	private static final long serialVersionUID = -4050761516391623326L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDY_SEQ")
	@SequenceGenerator(name = "STUDY_SEQ", sequenceName = "STUDY_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "TITLE")
	private String title;

	public DownloadFileStudy() {

	}

	public DownloadFileStudy(Study study) {
		this.id = study.getId();
		this.title = study.getTitle();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		DownloadFileStudy other = (DownloadFileStudy) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DownloadFileStudy [id=" + id + ", title=" + title + "]";
	}
}
