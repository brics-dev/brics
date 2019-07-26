
package gov.nih.tbi.dictionary.model.hibernate;

import java.io.Serializable;

/**
 * DomainPair: Represents a legal pairing of a domain/subdomain that is placed in a given subgroup. A data_element
 * maintains a list of these pairings and must have at least one for every subgroup for every disease that data element
 * is a part of.
 * 
 * @author mvalei
 * 
 */
public class DomainPair implements Serializable
{

    private static final long serialVersionUID = 5956377029164425372L;

    /**********************************************************************/

    private Long id;

    private DiseaseElement diseaseElement;

    private Domain domain;

    private SubDomain subDomain;

    /**********************************************************************/

    public DomainPair()
    {

    }

    public DomainPair(DomainPair domainPair)
    {

        this.domain = domainPair.getDomain();
        this.subDomain = domainPair.getSubdomain();
    }

    @Deprecated
    public DomainPair(DiseaseElement diseaseElement, Domain domain, SubDomain subDomain)
    {

        this.diseaseElement = diseaseElement;
        this.domain = domain;
        this.subDomain = subDomain;
    }

    public DomainPair(Domain domain, SubDomain subdomain)
    {

        this.domain = domain;
        this.subDomain = subdomain;
    }

    public DomainPair(String domain, String subDomain)
    {

        this.domain = new Domain(domain);
        this.subDomain = new SubDomain(subDomain);
    }

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

    public SubDomain getSubdomain()
    {

        return subDomain;
    }

    public void setSubdomain(SubDomain subDomain)
    {

        this.subDomain = subDomain;
    }

    public DiseaseElement getDiseaseElement()
    {

        return diseaseElement;
    }

    public void setDiseaseElement(DiseaseElement diseaseElement)
    {

        this.diseaseElement = diseaseElement;
    }

    /**********************************************************************/

    public String toString()
    {

        return domain + "." +subDomain;
    }
}
