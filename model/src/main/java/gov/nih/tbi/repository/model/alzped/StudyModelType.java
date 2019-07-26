package gov.nih.tbi.repository.model.alzped;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value="S")
public class StudyModelType extends ModelTypeData {

	private static final long serialVersionUID = 1L;
	
	public StudyModelType(){
		
		
	}
	
	public StudyModelType(ModelType modelType){
		
		this.modelType = modelType;
	}
	
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public ModelType getModelType() {
		return modelType;
	}
	
	public Long getModelTypeId() {
		return super.modelType.getId();
	}
	
	public String getText() {
		return super.modelType.getText();
	}
	
	@Override
	public int hashCode() {
		return super.modelType.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
	
		if(!(o instanceof StudyModelType)){
			return false;
		}
		
		StudyModelType s = (StudyModelType) o;
		
		return modelType.equals(s.modelType);
		
	}

}
