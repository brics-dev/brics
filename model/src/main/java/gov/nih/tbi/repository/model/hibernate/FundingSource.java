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
@Table(name = "FUNDING_SOURCE")
@XmlRootElement(name = "fundingSource")
@XmlAccessorType(XmlAccessType.FIELD)
public class FundingSource implements Serializable {

	private static final long serialVersionUID = -5902035915001640972L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FUNDING_SOURCE_SEQ")
	@SequenceGenerator(name = "FUNDING_SOURCE_SEQ", sequenceName = "FUNDING_SOURCE_SEQ", allocationSize = 1)
	private Long id;
	
	@Expose
    @Column(name = "NAME")
	private String name;

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
	
}
