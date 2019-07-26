
package gov.nih.tbi.dictionary.model.hibernate;

import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

public class DiseaseElement implements Serializable
{

    private static final long serialVersionUID = -7115713871006818272L;

    /**********************************************************************/

    private Long id;

    private String uri;

    public String getUri()
    {

        return uri;
    }

    public void setUri(String uri)
    {

        this.uri = uri;
    }

    private Disease disease;

    private Set<DomainPair> domainList;

    private SemanticDataElement dataElement;

    /**********************************************************************/

    public DiseaseElement()
    {

    }

    public DiseaseElement(DiseaseElement de)
    {

        this.uri = de.getUri();
        this.id = de.getId();
        this.disease = de.getDisease();
        this.domainList = new LinkedHashSet<DomainPair>(de.getDomainList());
        this.dataElement = de.getSemanticDataElement(); // remove this later
    }

    public DiseaseElement(Disease disease, Set<DomainPair> domainList)
    {

        this.disease = disease;
        this.domainList = domainList;
    }

    /**
     * We don't to support circular depenencies anymore
     * @param disease
     * @param dataElement
     * @param domainList
     */
    @Deprecated
    public DiseaseElement(Disease disease, SemanticDataElement dataElement, Set<DomainPair> domainList)
    {

        this.disease = disease;
        this.dataElement = dataElement;
        this.domainList = domainList;
    }

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public Disease getDisease()
    {

        return disease;
    }

    public void setDisease(Disease disease)
    {

        this.disease = disease;
    }

    public Set<DomainPair> getDomainList()
    {

        return domainList;
    }

    public void setDomainList(Set<DomainPair> domainList)
    {

        this.domainList = domainList;
    }

    /**
     * Add a new domain and subdomain to the given disease
     * 
     * @param pair
     */
    public void addDomainPair(Domain domain, SubDomain subdomain)
    {

        if (this.domainList == null)
        {
            this.domainList = new LinkedHashSet<DomainPair>();
        }
        
        domainList.add(new DomainPair(domain, subdomain));
    }

    public void addDomainPair(DomainPair domainPair)
    {

        if (this.domainList == null)
        {
            this.domainList = new LinkedHashSet<DomainPair>();
        }

        domainList.add(domainPair);
    }

    @Deprecated
    public SemanticDataElement getSemanticDataElement()
    {

        return dataElement;
    }

    @Deprecated
    public void setSemanticDataElement(SemanticDataElement dataElement)
    {

        this.dataElement = dataElement;
    }

    /**********************************************************************/

    @Override
    public String toString()
    {

        return "DiseaseElement [uri=" + uri + ", disease=" + disease + "domainPair= " + domainList + "]";
    }
}
