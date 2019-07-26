package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.commons.model.Data;
import gov.nih.tbi.commons.model.GuidJoinedData;

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
@Table(name = "DATASET_SUBJECT")
public class DatasetSubject implements Serializable, GuidJoinedData {

	private static final long serialVersionUID = -1438853510474210357L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATASET_SUBJECT_SEQ")
	@SequenceGenerator(name = "DATASET_SUBJECT_SEQ", sequenceName = "DATASET_SUBJECT_SEQ", allocationSize = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "DATASET_ID")
	private BasicDataset dataset;

	@Column(name = "SUBJECT_GUID")
	private String subjectGuid;

	public Long getId() {

		return id;
	}

	public void setId(Long id) {

		this.id = id;
	}

	public BasicDataset getDataset() {

		return dataset;
	}

	public void setDataset(BasicDataset dataset) {

		this.dataset = dataset;
	}

	public void setSubjectGuid(String subjectGuid) {

		this.subjectGuid = subjectGuid;
	}

	public String getSubjectGuid() {

		return subjectGuid;
	}

	public String getGuid() {

		return getSubjectGuid();
	}

	public Data getData() {

		return getDataset();
	}
}
