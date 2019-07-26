package gov.nih.tbi.repository.model.alzped;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "S")
public class StudyModelName extends ModelNameData {

	private static final long serialVersionUID = 1L;

	public StudyModelName() {


	}

	public StudyModelName(ModelName modelName) {

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


	public void setModelNameId(ModelName modelName) {
		this.modelName = modelName;
	}

	public Long getModelNameId() {
		return super.modelName.getId();
	}
	
	public String getText() {
		return super.modelName.getText();
	}

	@Override
	public int hashCode() {
		return modelName.hashCode();
	}

	@Override
	public boolean equals(Object o) {

		if (!(o instanceof StudyModelName)) {
			return false;
		}

		StudyModelName s = (StudyModelName) o;

		return modelName.equals(s.modelName);
	}

}
