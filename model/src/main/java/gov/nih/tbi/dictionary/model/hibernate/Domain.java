
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
 * Model for domain
 * 
 * @author Francis Chen
 * 
 */
@Entity
@Table(name = "DOMAIN_DE")
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class Domain implements Serializable, Comparable<Domain>
{

    private static final long serialVersionUID = 3512111550858480544L;

    /**********************************************************************/

    @Id
    private Long id;

    @Column(name = "DOMAIN_NAME")
    private String name;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    public Domain()
    {

    }

    public Domain(String name)
    {

        this.name = name;
        this.isActive = Boolean.TRUE; //not sure if this is correct
    }

    public Domain(Domain domain)
    {

        this.id = domain.getId();
        this.name = domain.getName();
        this.isActive = domain.getIsActive();
    }

    public Domain(Long id, String name)
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

    public Boolean getIsActive()
    {

        return isActive;
    }

    public void setIsActive(Boolean isActive)
    {

        this.isActive = isActive;
    }

    public String toString()
    {

        return name;
    }

    /**********************************************************************/

    @Override
    public int compareTo(Domain d1)
    {

        return this.getName().compareTo(d1.getName());
    }

    @Override
    public int hashCode()
    {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((isActive == null) ? 0 : isActive.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Domain other = (Domain) obj;
        if (isActive == null)
        {
            if (other.isActive != null)
                return false;
        }
        else
            if (!isActive.equals(other.isActive))
                return false;
        if (name == null)
        {
            if (other.name != null)
                return false;
        }
        else
            if (!name.equals(other.name))
                return false;
        return true;
    }
}
