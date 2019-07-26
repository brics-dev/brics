package gov.nih.tbi.repository.model.alzped;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name = "THERAPY_TYPE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)

public class TherapyType implements AlzPed {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "THERAPY_TYPE_SEQ")
	@SequenceGenerator(name = "THERAPY_TYPE_SEQ", sequenceName = "THERAPY_TYPE_SEQ", allocationSize = 1)
	private Long id;
	
	@Column(name = "DESCRIPTION")
	private String text;
	
	public TherapyType(){
		
	}
	
	public TherapyType(String description){
		
		this.text = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		TherapyType other = (TherapyType) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
}
