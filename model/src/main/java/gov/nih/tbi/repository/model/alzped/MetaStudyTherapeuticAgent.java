package gov.nih.tbi.repository.model.alzped;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "M")
public class MetaStudyTherapeuticAgent extends TherapeuticAgentData {

	private static final long serialVersionUID = 1L;

	public MetaStudyTherapeuticAgent() {

	}

	public MetaStudyTherapeuticAgent(TherapeuticAgent therapeuticAgent) {
		this.therapeuticAgent = therapeuticAgent;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TherapeuticAgent getTherapeuticAgent() {
		return therapeuticAgent;
	}

	public void setTherapeuticAgent(TherapeuticAgent therapeuticAgent) {
		this.therapeuticAgent = therapeuticAgent;
	}
	
	public Long getTherapeuticAgentId() {
		return super.therapeuticAgent.getId();
	}

	@Override
	public int hashCode() {
		return super.therapeuticAgent.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {

		if (!(o instanceof MetaStudyTherapeuticAgent)) {
			return false;
		}

		MetaStudyTherapeuticAgent s = (MetaStudyTherapeuticAgent) o;

		return therapeuticAgent.equals(s.therapeuticAgent);

	}

}
