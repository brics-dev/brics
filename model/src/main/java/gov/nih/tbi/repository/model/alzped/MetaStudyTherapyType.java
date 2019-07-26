package gov.nih.tbi.repository.model.alzped;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "M")
public class MetaStudyTherapyType extends TherapyTypeData {

	private static final long serialVersionUID = 1L;

	public MetaStudyTherapyType() {

	}

	public MetaStudyTherapyType(TherapyType t) {
		therapyType = t;
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

	public void setMetaStudyTherapyType(TherapyType therapyType) {
		this.therapyType = therapyType;
	}
	
	public Long getTherapyTypeId() {
		return super.therapyType.getId();
	}
	
	@Override
	public int hashCode() {
		return super.therapyType.hashCode();
	}

	@Override
	public boolean equals(Object o) {

		if (!(o instanceof MetaStudyTherapyType)) {
			return false;
		}

		MetaStudyTherapyType s = (MetaStudyTherapyType) o;

		return therapyType.equals(s.therapyType);

	}

}
