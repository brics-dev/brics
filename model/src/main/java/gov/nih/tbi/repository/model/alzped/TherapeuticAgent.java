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
@Table(name = "THERAPEUTIC_AGENT")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)

public class TherapeuticAgent implements AlzPed {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "THERAPEUTIC_AGENT_SEQ")
	@SequenceGenerator(name = "THERAPEUTIC_AGENT_SEQ", sequenceName = "THERAPEUTIC_AGENT_SEQ", allocationSize = 1)
	private Long id;
	
	@Column(name = "DESCRIPTION")
	private String text;
	
	public TherapeuticAgent(){
		
	}
	
	public TherapeuticAgent(String text){
		
		this.text = text;
	}
	
	public Long getId(){
		return id;
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
		TherapeuticAgent other = (TherapeuticAgent) obj;
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

	@Override
	public String toString() {
		return "[id=" + id + ", text=" + text + "]";
	}

}
