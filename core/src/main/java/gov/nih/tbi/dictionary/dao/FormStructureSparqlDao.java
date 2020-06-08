
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.commons.dao.GenericSparqlDao;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.model.FormStructureFacet;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.dictionary.model.hibernate.FormLabel;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FormStructureSparqlDao extends GenericSparqlDao<SemanticFormStructure>
{

    public SemanticFormStructure getLatest(String shortName);

    /**
     * Gets the Semantic Form Structure data from the RDF store based on the short name and the version of the Form
     * Structure. This has been adapted to the new RDF model.
     * 
     * @param shortName
     * @param version
     * @return
     */
    public SemanticFormStructure get(String shortName, String version);

    public Map<String, SemanticFormStructure> getAllIntoShortNameVersionMap();

    public Map<String, SemanticFormStructure> getShortNameAndVersionsMap(List<NameAndVersion> nameAndVersions);

    public void remove(String shortName, String version);

    public List<SemanticFormStructure> getByShortNameAndVersions(List<NameAndVersion> nameAndVersions);

    /**
     * Performs a search on the RDF graph given a wide variety of search criteria defined in FormStructureFacet.
     * 
     * @param selectedFacets
     * @param searchKeywords
     * @param modifiedDate
     * @param pageData
     * @return
     */
    public List<SemanticFormStructure> search(Map<FormStructureFacet, Set<String>> selectedFacets,
            Set<String> searchTerms, boolean exactMatch, PaginationData pageData, boolean onlyOwned);

    /**
     * Returns only the count of the total search results. This is intended to be used by webservice.
     * 
     * @param selectedFacets
     * @param searchTerms
     * @param onlyOwned
     * @return
     */
    public int searchCount(Map<FormStructureFacet, Set<String>> selectedFacets, Set<String> searchTerms,
            boolean exactMatch, boolean onlyOwned);

    public SemanticFormStructure saveOverwrite(SemanticFormStructure form, String oldName, String oldVersion);
    
    /**
     * Return a list of semantic form structures that have one the given statuses
     * @param statuses
     * @return
     */
    public List<SemanticFormStructure> getByStatuses(Set<StatusType> statuses);

    public List<SemanticFormStructure> getLatestByNames(List<String> names);
    
    public void removeFormLabel(Long formLabelId);
    
    public void updateFormLabel(FormLabel formLabel, String newLabel);
    
}
