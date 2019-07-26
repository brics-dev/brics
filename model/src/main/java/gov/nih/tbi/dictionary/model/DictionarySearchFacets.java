
package gov.nih.tbi.dictionary.model;

import java.util.HashMap;
import java.util.Map;

/**
 * This is object stores all the dictionary facets to be passed from the action to the dao
 * @author Francis Chen
 *
 */
public class DictionarySearchFacets
{
    //hash map facets with it's respectible facetType as the key
    private Map<FacetType, BaseDictionaryFacet> facetMap = new HashMap<FacetType, BaseDictionaryFacet>();

    public DictionarySearchFacets(Map<FacetType, BaseDictionaryFacet> facetMap)
    {

        super();
        this.facetMap = facetMap;
    }

    public DictionarySearchFacets()
    {

    }

    public Map<FacetType, BaseDictionaryFacet> getFacetMap()
    {

        return facetMap;
    }

    public void setFacetMap(Map<FacetType, BaseDictionaryFacet> facetMap)
    {

        this.facetMap = facetMap;
    }
    
    public void addFacet(BaseDictionaryFacet values)
    {
        this.facetMap.put(values.getType(), values);
    }
    
    public BaseDictionaryFacet getByType(FacetType type)
    {
        return facetMap.get(type);
    }
    
    public boolean isEmpty()
    {
        return facetMap == null || facetMap.isEmpty();
    }
    
    @Override
    public int hashCode()
    {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((facetMap == null) ? 0 : facetMap.hashCode());
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
        DictionarySearchFacets other = (DictionarySearchFacets) obj;
        if (facetMap == null)
        {
            if (other.facetMap != null)
                return false;
        }
        else if (!facetMap.equals(other.facetMap))
            return false;
        return true;
    }

}
