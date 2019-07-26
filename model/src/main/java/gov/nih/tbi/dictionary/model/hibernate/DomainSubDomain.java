
package gov.nih.tbi.dictionary.model.hibernate;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Model for placing subdomains under domains
 * 
 * @author Michael Valeiras
 * 
 */
@Entity
@Table(name = "DOMAIN_SUB_DOMAIN")
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class DomainSubDomain implements Serializable
{
    private static final long serialVersionUID = 3718924284808930778L;

    @Id
    private Long id;

    @ManyToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "DOMAIN_ID")
    private Domain domain;

    @ManyToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "SUB_DOMAIN_ID")
    private SubDomain subDomain;
    
    @ManyToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "DISEASE_ID")
    private Disease disease;
    
    public DomainSubDomain()
    {

    }

    public DomainSubDomain(Long id, Domain domain, SubDomain subDomain, Disease disease)
    {

        this.id = id;
        this.domain = domain;
        this.subDomain = subDomain;
        this.disease = disease;
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

    public Domain getDomain()
    {

        return domain;
    }

    public void setDomain(Domain domain)
    {

        this.domain = domain;
    }

    public SubDomain getSubDomain()
    {

        return subDomain;
    }

    public void setSubDomain(SubDomain subDomain)
    {

        this.subDomain = subDomain;
    }
       
    public Disease getDisease()
    {
    
        return disease;
    }

    
    public void setDisease(Disease disease)
    {
    
        this.disease = disease;
    }

    public String toString()
    {

        return "[ID:" + id + ", Domain: " + domain + ", SubDomain: " + subDomain + ", Disease: " + disease + "]";
    }
    /**********************************************************************/

}
