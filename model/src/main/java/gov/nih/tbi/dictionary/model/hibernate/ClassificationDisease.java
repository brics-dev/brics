
package gov.nih.tbi.dictionary.model.hibernate;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Model object for the classification_disease join table
 * 
 * @author Francis Chen
 */
@Entity
@Table(name = "CLASSIFICATION_DISEASE")
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassificationDisease implements Serializable
{

    private static final long serialVersionUID = -8415661130846419983L;

    @Id
    private Long id;

    @OneToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "CLASSIFICATION_ID")
    private Classification classification;

    @OneToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "DISEASE_ID")
    private Disease disease;

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public Classification getClassification()
    {

        return classification;
    }

    public void setClassification(Classification classification)
    {

        this.classification = classification;
    }

    public Disease getDisease()
    {

        return disease;
    }

    public void setDisease(Disease disease)
    {

        this.disease = disease;
    }
}
