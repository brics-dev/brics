package gov.nih.tbi.repository.model.alzped;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "S")
public class StudyTherapeuticAgent extends TherapeuticAgentData {

	private static final long serialVersionUID = 1L;

	public StudyTherapeuticAgent() {


	}

	public StudyTherapeuticAgent(TherapeuticAgent therapeuticAgent) {

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
	
	public Long getTherapeuticAgentId() {
		return super.therapeuticAgent.getId();
	}

	public String getText() {
		return super.therapeuticAgent.getText();
	}

	@Override
	public int hashCode() {
		return therapeuticAgent.hashCode();
	}

	@Override
	public boolean equals(Object o) {

		if (!(o instanceof StudyTherapeuticAgent)) {
			return false;
		}

		StudyTherapeuticAgent s = (StudyTherapeuticAgent) o;

		return therapeuticAgent.equals(s.therapeuticAgent);
	}
}
