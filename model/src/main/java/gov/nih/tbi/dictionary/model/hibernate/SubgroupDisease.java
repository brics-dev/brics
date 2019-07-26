package gov.nih.tbi.dictionary.model.hibernate;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Model object for Subgroup_Disease join table
 * @author Francis Chen
 */
@Entity
@Table(name = "SUBGROUP_DISEASE")
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubgroupDisease implements Serializable
{
    private static final long serialVersionUID = 1189891080008435724L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SUBGROUP_DISEASE_SEQ")
    @SequenceGenerator(name = "SUBGROUP_DISEASE_SEQ", sequenceName = "SUBGROUP_DISEASE_SEQ", allocationSize = 1)
    private Long id;
    
    @OneToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "SUBGROUP_ID")
    private Subgroup subgroup;
    
    @OneToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "DISEASE_ID")
    private Disease disease;

    
    public Subgroup getSubgroup()
    {
    
        return subgroup;
    }

    
    public void setSubgroup(Subgroup subgroup)
    {
    
        this.subgroup = subgroup;
    }

    
    public Disease getDisease()
    {
    
        return disease;
    }

    
    public void setDisease(Disease disease)
    {
    
        this.disease = disease;
    }

    
    public Long getId()
    {
    
        return id;
    }

    
    public void setId(Long id)
    {
    
        this.id = id;
    }
}
