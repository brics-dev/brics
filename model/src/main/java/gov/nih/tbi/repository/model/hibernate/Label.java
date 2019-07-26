package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "LABEL")
public class Label implements Serializable {

	private static final long serialVersionUID = 4754554510385375697L;
	
    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "META_STUDY_LABEL_SEQ")
	@SequenceGenerator(name = "META_STUDY_LABEL_SEQ", sequenceName = "META_STUDY_LABEL_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "LABEL")
    private String label;

    @Transient
    private Long count;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

}
