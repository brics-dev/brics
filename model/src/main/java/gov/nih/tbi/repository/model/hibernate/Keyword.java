package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DiscriminatorFormula;

@Entity
@Table(name = "KEYWORD")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula(
        "(CASE WHEN study_id IS NOT NULL THEN 'S' " +
        " WHEN meta_study_id IS NOT NULL THEN 'M' END)")
public abstract class Keyword implements Serializable {

	private static final long serialVersionUID = 2318295425303641550L;
	
    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "META_STUDY_KEYWORD_SEQ")
	@SequenceGenerator(name = "META_STUDY_KEYWORD_SEQ", sequenceName = "META_STUDY_KEYWORD_SEQ", allocationSize = 1)
    protected Long id;

    @Column(name = "KEYWORD")
    protected String keyword;
    
    @Transient
    protected Long count;

}
