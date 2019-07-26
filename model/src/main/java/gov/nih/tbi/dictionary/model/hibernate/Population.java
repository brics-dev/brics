
package gov.nih.tbi.dictionary.model.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Model for population table
 * 
 * @author Michael Valeiras
 * 
 */
@Entity
@Table(name = "POPULATION")
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class Population implements Serializable
{

    private static final long serialVersionUID = -2333716368295670974L;

    /**********************************************************************/

    @Id
    private Long id;

    @Column(name = "NAME")
    private String name;

    public Population()
    {

    }

    public Population(String name)
    {
        this.name = name;
    }
    
    /**
     * Eventually we should remove the ID field
     * @param id
     * @param name
     */
    @Deprecated
    public Population(Long id, String name)
    {

        this.id = id;
        this.name = name;
    }

    /**********************************************************************/

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public String getName()
    {

        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }

    public String toString()
    {

        return "Population[ID:" + id + ", Name: " + name + "]";
    }
    /**********************************************************************/

}
