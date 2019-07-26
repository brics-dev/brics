package gov.nih.tbi.repository.model.alzped;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value="S")
public class StudyTherapeuticTarget extends TherapeuticTargetData {

	private static final long serialVersionUID = 1L;
	
	public StudyTherapeuticTarget(){
		
		
	}
	
	public StudyTherapeuticTarget(TherapeuticTarget therapeuticTarget){
		
		this.therapeuticTarget = therapeuticTarget;
	}
	
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}

	public Long getTherapeuticTargetId() {
		return super.therapeuticTarget.getId();
	}

	public TherapeuticTarget getTherapeuticTarget() {
		return therapeuticTarget;
	}

	public String getText() {
		return super.therapeuticTarget.getText();
	}
	
	@Override
	public int hashCode() {
		return therapeuticTarget.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
	
		if(!(o instanceof StudyTherapeuticTarget)){
			return false;
		}
		
		StudyTherapeuticTarget s = (StudyTherapeuticTarget) o;
		
		return therapeuticTarget.equals(s.therapeuticTarget);
	}

}
