package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;

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

import com.google.gson.annotations.Expose;


@Entity
@Table(name = "STUDY_TYPE")
@XmlRootElement(name = "studyType")
@XmlAccessorType(XmlAccessType.FIELD)
public class StudyType implements Serializable {

	private static final long serialVersionUID = -5018328839361182532L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDY_TYPE_SEQ")
	@SequenceGenerator(name = "STUDY_TYPE_SEQ", sequenceName = "STUDY_TYPE_SEQ", allocationSize = 1)
	private Long id;

	@Expose
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "IS_ACTIVE")
	private Boolean isActive;

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

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
	
	

}
