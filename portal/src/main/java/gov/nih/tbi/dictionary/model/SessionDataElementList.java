
package gov.nih.tbi.dictionary.model;

import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class stores a Collection of elements that extend the AbstractDataElement class. It allows for a list of
 * elements to be maintained at the session level so that they may be used across requests.
 * 
 * @author dhollo
 * 
 */
public class SessionDataElementList implements Serializable
{

    private static final long serialVersionUID = 1L;

    /******************************************************************************************************/

    private List<DataElement> dataElements;
    private Set<MapElement> mapElements;

    /******************************************************************************************************/

    public List<DataElement> getDataElements()
    {

        if (dataElements == null)
        {
            dataElements = new ArrayList<DataElement>();
        }

        return dataElements;
    }

    public Set<MapElement> getMapElements()
    {

        if (mapElements == null)
        {
            mapElements = new HashSet<MapElement>();
        }

        return mapElements;
    }

    public void setDataElements(List<DataElement> dataElements)
    {

        this.dataElements = dataElements;
    }

    public void setMapElements(Set<MapElement> sessionList)
    {

        this.mapElements = sessionList;
    }
}
