package gov.nih.tbi.repository.model.alzped;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "M")
public class MetaStudyModelName extends ModelNameData {

	private static final long serialVersionUID = 1L;

	public MetaStudyModelName() {

	}

	public MetaStudyModelName(ModelName modelName) {
		this.modelName = modelName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ModelName getModelName() {
		return modelName;
	}

	public void setModelName(ModelName modelName) {
		this.modelName = modelName;
	}

	public Long getModelNameId() {
		return super.modelName.getId();
	}
	
	@Override
	public int hashCode() {
		return super.modelName.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {

		if (!(o instanceof MetaStudyModelName)) {
			return false;
		}

		MetaStudyModelName s = (MetaStudyModelName) o;

		return modelName.equals(s.modelName);
	}

}
