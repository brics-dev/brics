
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.DomainSubDomain;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;

import java.util.List;

public interface DomainSubDomainDao extends GenericDao<DomainSubDomain, Long>
{

    /**
     * Get the list of Domains that contains a subDomain. If subDomain is null then return all the Domains.
     * 
     * @param subDomain
     *            : the subDomain we wish to know the domains for
     * @return
     */
    public List<Domain> getDomains(SubDomain subDomain);

    /**
     * Get the list of SubDomains that contains a domain. If domain is null then return all the SubDomains.
     * 
     * @param domain
     *            : the domain we wish to know the subDomains for
     * @return
     */
    public List<SubDomain> getSubDomains(Domain domain, Disease disease);

    /**
     * Returns true if the domain and subdomain are a legal pairing, false otherwise
     * 
     * @param domain
     * @param subDomain
     * @return
     */
    public boolean legalPair(Disease disease, Domain domain, SubDomain subDomain);

    /**
     * Get a list of legal domain for a single disease
     * 
     * @param disease
     * @return
     */
    public List<Domain> getByDisease(Disease disease);
}
