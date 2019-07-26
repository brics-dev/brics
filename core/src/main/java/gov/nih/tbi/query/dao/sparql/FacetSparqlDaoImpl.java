
package gov.nih.tbi.query.dao.sparql;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.MetadataStore;
import gov.nih.tbi.commons.dao.sparql.GenericSparqlDaoImpl;
import gov.nih.tbi.commons.util.RDFConstants;
import gov.nih.tbi.query.dao.FacetSparqlDao;
import gov.nih.tbi.query.model.Facet;
import gov.nih.tbi.query.model.FacetItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * 
 * @author Francis Chen
 * 
 */
@Transactional
@Repository
public class FacetSparqlDaoImpl extends GenericSparqlDaoImpl<Facet> implements FacetSparqlDao
{

    // TODO: get rid of this later
    private final String getPropertyValuesForClassQuery = "SELECT ?o (COUNT(?o) AS ?n) ?label " + "WHERE { " + "?r1 rdfs:subClassOf <CLASSURI_REPLACE> . " + "?r1 <PROPERTYURI_REPLACE> ?o . ";

    private final String groupByQuery = "FILTER(?o!=\"\" && (!isBlank(?o) || bound(?label)) ) . " + "OPTIONAL{ ?o rdfs:label ?label . FILTER(LANG(?label)='en' || LANG(?label)='')} " + "} GROUP BY ?o ?label ORDER BY DESC(?n) ";

    public FacetSparqlDaoImpl()
    {

    }

    /**
     * Returns all the facets used in query tool, sans facet items. I can't think of a reason why you would call this
     * instead of getEager; So, if you're not sure which one you need, use getEager instead.
     */
    public List<Facet> getAllBasic()
    {

        // Only part of the facets that arn't hardcoded are the facet items, so just use the constant.
        // This will need to be changed if we ever decide to make the facets configurable.
        return RDFConstants.QT_FACETS;
    }

    /**
     * Returns all the facets used in query tool, along with all the facet items.
     */
    @Override
    public List<Facet> getAll()
    {

        List<Facet> facets = getAllBasic();

        // load the facet items for each facet
        for (Facet facet : facets)
        {
            facet.setItems(getFacetItems(facet));
        }

        return facets;
    }

    /**
     * Returns a list of all the facet items for a particular facet
     * 
     * @param facet
     * @return
     */
    private List<FacetItem> getFacetItems(Facet facet)
    {

        String query = getFacetItemQuery(facet);
        ResultSet results = virtuosoStore.querySelect(query, MetadataStore.REASONING);
        List<FacetItem> facetItems = new ArrayList<FacetItem>();

        String strVar = results.getResultVars().get(0);
        String countVar = results.getResultVars().get(1);

        while (results.hasNext())
        {
            QuerySolution row = results.next();

            if (!row.contains(strVar))
            {
                continue;
            }

            String label = CoreConstants.EMPTY_STRING;
            if (row.contains("label"))
            {
                label = row.get("label").toString();
            }
            FacetItem item = new FacetItem();
            item.setCount(row.getLiteral(countVar).getInt());
            item.setLabel(label);
            item.setRdfURI(row.get(strVar).toString());
            facetItems.add(item);
        }

        return facetItems;
    }

    /**
     * Returns the query to get all the facet items for a particular facet
     * 
     * @param facet
     * @return
     */
    private String getFacetItemQuery(Facet facet)
    {

        String classURI = facet.getClassURI();
        String propertyURI = facet.getPropertyURI();

        String query = getPropertyValuesForClassQuery + groupByQuery;

        query = query.replaceAll("CLASSURI_REPLACE", classURI);
        query = query.replaceAll("PROPERTYURI_REPLACE", propertyURI);

        return query;
    }

    /**
     * Returns the query to get all the facet items for a particular facet. Filter by facets already selected
     * 
     * @param facet
     * @param selectedFacets
     * @return
     */
    private String getFacetItemQuery(Facet facet, List<String> selectedFacets)
    {

        // TODO: Implement this
        return null;
    }

    /**
     * Converts a list of facets to a hashmap of classUri's to it's facet
     * 
     * @param facets
     * @return
     */
    private Map<String, Facet> facetsToUriObjectMap(List<Facet> facets)
    {

        Map<String, Facet> keyFacetMap = new HashMap<String, Facet>();

        for (Facet facet : facets)
        {
            if (facet.getClassURI() != null)
            {
                keyFacetMap.put(facet.getClassURI(), facet);
            }
        }

        return keyFacetMap;
    }

    @Override
    public Facet get(String uri)
    {

        // TODO: implement this
        return null;
    }

    @Override
    public Facet save(Facet object)
    {

        // TODO Auto-generated method stub
        return null;
    }
}
