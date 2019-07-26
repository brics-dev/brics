package gov.nih.tbi.repository.model.alzped;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "S")
public class StudyTherapyType extends TherapyTypeData {

	private static final long serialVersionUID = 1L;

	public StudyTherapyType() {

	}

	public StudyTherapyType(TherapyType therapyType) {

		this.therapyType = therapyType;
	}

	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public TherapyType getTherapyType() {
		return therapyType;
	}
	
	public Long getTherapyTypeId() {
		return super.therapyType.getId();
	}

	public String getText() {
		return super.therapyType.getText();
	}

	@Override
	public int hashCode() {
		return therapyType.hashCode();
	}

	@Override
	public boolean equals(Object o) {

		if (!(o instanceof StudyTherapyType)) {
			return false;
		}

		StudyTherapyType s = (StudyTherapyType) o;

		return therapyType.equals(s.therapyType);
	}
}
