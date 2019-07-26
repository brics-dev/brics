package gov.nih.tbi.repository.model.alzped;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.DiscriminatorFormula;

@Entity
@Table(name = "THERAPEUTIC_AGENT_DATA")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula(
        "(CASE WHEN study_id IS NOT NULL THEN 'S' " +
        " WHEN meta_study_id IS NOT NULL THEN 'M' END)")

public abstract class TherapeuticAgentData  implements Serializable {
	
	private static final long serialVersionUID = -6766207945079509925L;


	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "THERAPEUTIC_AGENT_DATA_SEQ")
	@SequenceGenerator(name = "THERAPEUTIC_AGENT_DATA_SEQ", sequenceName = "THERAPEUTIC_AGENT_DATA_SEQ", allocationSize = 1)
	protected Long id;

	@JoinColumn(name="THERAPEUTIC_AGENT_ID")
	@ManyToOne(targetEntity = TherapeuticAgent.class)
	protected TherapeuticAgent therapeuticAgent;
	
	@Column(name="STUDY_ID" , insertable=false, updatable=false)
	protected Long studyId;
	
	@Column(name="META_STUDY_ID", insertable=false, updatable=false)
	protected Long metaStudyId;
	
	public Long getStudyId() {
		return studyId;
	}

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}
	
	public Long getMetaStudyId() {
		return metaStudyId;
	}

	public void setMetaStudyId(Long metaStudyId) {
		this.metaStudyId = metaStudyId;
	}
	
}
