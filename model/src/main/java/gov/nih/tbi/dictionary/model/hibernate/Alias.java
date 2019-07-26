
package gov.nih.tbi.dictionary.model.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@Entity
@Table(name = "ALIAS")
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class Alias implements Serializable
{

    private static final long serialVersionUID = -2719452596260320910L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ALIAS_SEQ")
    @SequenceGenerator(name = "ALIAS_SEQ", sequenceName = "ALIAS_SEQ", allocationSize = 1)
    private Long id;

    @OneToOne
    @JoinColumn(name = "DATA_ELEMENT_ID")
    @XmlTransient
    private StructuralDataElement dataElement;

    @Column(name = "name")
    private String name;

    public Alias()
    {

    }
    
    public Alias(Alias alias)
    {
        this.id = alias.getId();
        this.name = alias.getName();
    }

    public Alias(String aliasName)
    {

        this.setName(aliasName);
    }

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public StructuralDataElement getDataElement()
    {

        return dataElement;
    }

    public void setDataElement(StructuralDataElement dataElement)
    {

        this.dataElement = dataElement;
    }

    public String getName()
    {

        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }
}