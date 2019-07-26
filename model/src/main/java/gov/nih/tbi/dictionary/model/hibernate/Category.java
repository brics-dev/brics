
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
 * Model for category table
 * 
 * @author Michael Valeiras
 * 
 */
@Entity
@Table(name = "Category")
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class Category implements Serializable
{

    private static final long serialVersionUID = -8702931165175336L;

    @Id
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "SHORT_NAME")
    private String shortName;

    public Category()
    {

    }

    public Category(Category category)
    {

        this.id = category.getId();
        this.name = category.getName();
        this.shortName = category.getShortName();
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

    public String getShortName()
    {

        return shortName;
    }

    public void setShortName(String shortName)
    {

        this.shortName = shortName;
    }

    public String toString()
    {

        return "Category[ID:" + id + ", Name: " + name + ", Short Name: " + shortName + "]";
    }
    /**********************************************************************/

}
