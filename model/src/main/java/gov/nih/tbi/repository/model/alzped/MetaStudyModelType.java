package gov.nih.tbi.repository.model.alzped;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "M")
public class MetaStudyModelType extends ModelTypeData {

	private static final long serialVersionUID = 1L;

	
	public MetaStudyModelType() {

	}
	public MetaStudyModelType(ModelType modelType) {

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

	public void setModelType(ModelType modelType) {
		this.modelType = modelType;
	}

	public Long getModelTypeId() {
		return super.modelType.getId();
	}
	
	@Override
	public int hashCode() {
		return super.modelType.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {

		if (!(o instanceof MetaStudyModelType)) {
			return false;
		}

		MetaStudyModelType s = (MetaStudyModelType) o;

		return modelType.equals(s.modelType);

	}
}
