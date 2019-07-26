package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "DATASET")
public class DownloadFileDataset implements Serializable {
	private static final long serialVersionUID = 4777677093673855610L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATASET_SEQ")
	@SequenceGenerator(name = "DATASET_SEQ", sequenceName = "DATASET_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "NAME")
	private String name;

	@ManyToOne
	@JoinColumn(name = "STUDY_ID")
	private DownloadFileStudy study;

	public DownloadFileDataset() {

	}

	public DownloadFileDataset(BasicDataset dataset) {
		this.id = dataset.getId();
		this.name = dataset.getName();
		this.study = new DownloadFileStudy(dataset.getStudy());
	}

	public DownloadFileDataset(Dataset dataset) {
		this.id = dataset.getId();
		this.name = dataset.getName();
		this.study = new DownloadFileStudy(dataset.getStudy());
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

	public DownloadFileStudy getStudy() {
		return study;
	}

	public void setStudy(DownloadFileStudy study) {
		this.study = study;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((study == null) ? 0 : study.hashCode());
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
		DownloadFileDataset other = (DownloadFileDataset) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (study == null) {
			if (other.study != null)
				return false;
		} else if (!study.equals(other.study))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DownloadFileDataset [id=" + id + ", name=" + name + ", study=" + study + "]";
	}
}
