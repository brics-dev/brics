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
@Table(name = "FORM_LABEL")
@XmlRootElement(name = "formLabel")
@XmlAccessorType(XmlAccessType.FIELD)
public class FormLabel implements Serializable, Comparable<FormLabel> {

	private static final long serialVersionUID = 6085217323019623662L;
	
    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FORM_LABEL_SEQ")
	@SequenceGenerator(name = "FORM_LABEL_SEQ", sequenceName = "FORM_LABEL_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "LABEL")
    private String label;

	@Column(name = "CREATED_BY")
    private String createdBy;
    
    @Column(name = "CREATED_DATE")
    private Date createdDate;

    public FormLabel() {
    }
    
    public FormLabel(Long id, String label, String createdBy, Date createdDate) {
		this.id = id;
		this.label = label;
		this.createdBy = createdBy;
		this.createdDate = createdDate;
	}

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

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

    public int compareTo(FormLabel formLabel) {
        return this.getLabel().compareTo(formLabel.getLabel());
    }
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((createdBy == null) ? 0 : createdBy.hashCode());
		result = prime * result + ((createdDate == null) ? 0 : createdDate.hashCode());
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
		FormLabel other = (FormLabel) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (createdBy == null) {
			if (other.createdBy != null)
				return false;
		} else if (!createdBy.equals(other.createdBy))
			return false;
		if (createdDate == null) {
			if (other.createdDate != null)
				return false;
		} else if (!createdDate.equals(other.createdDate))
			return false;
		return true;
	}
    
}
