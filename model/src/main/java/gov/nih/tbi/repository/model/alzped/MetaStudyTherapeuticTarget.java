package gov.nih.tbi.repository.model.alzped;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value="M")
public class MetaStudyTherapeuticTarget extends TherapeuticTargetData {

	private static final long serialVersionUID = 1L;
	
	public MetaStudyTherapeuticTarget(){
		
	}
	
	public MetaStudyTherapeuticTarget(TherapeuticTarget t) {
		therapeuticTarget = t;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public TherapeuticTarget getTherapeuticTarget() {
		return therapeuticTarget;
	}


	public void setTherapeuticTarget(TherapeuticTarget therapeuticTarget) {
		this.therapeuticTarget = therapeuticTarget;
	}
	
	public Long getTherapeuticTargetId() {
		return super.therapeuticTarget.getId();
	}
	
	@Override
	public int hashCode() {
		return super.therapeuticTarget.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
	
		if(!(o instanceof MetaStudyTherapeuticTarget)){
			return false;
		}
		
		MetaStudyTherapeuticTarget s = (MetaStudyTherapeuticTarget) o;
		
		return therapeuticTarget.equals(s.therapeuticTarget);
		
	}


}
