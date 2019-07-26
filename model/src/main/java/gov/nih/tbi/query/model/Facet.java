
package gov.nih.tbi.query.model;

import java.util.LinkedList;
import java.util.List;

public class Facet
{

    private String headingLabel;
    private String classURI;
    private String propertyURI;

    // Use doubly linked list to improve performance? - Francis
    private List<FacetItem> items = new LinkedList<FacetItem>();

    public Facet(String headingLabel, String classURI, String propertyURI)
    {

        this.headingLabel = headingLabel;
        this.classURI = classURI;
        this.propertyURI = propertyURI;
    }

    public String getHeadingLabel()
    {

        return headingLabel;
    }

    public void setHeadingLabel(String headingLabel)
    {

        this.headingLabel = headingLabel;
    }

    public String getClassURI()
    {

        return classURI;
    }

    public void setClassURI(String classURI)
    {

        this.classURI = classURI;
    }

    public String getPropertyURI()
    {

        return propertyURI;
    }

    public void setPropertyURI(String propertyURI)
    {

        this.propertyURI = propertyURI;
    }

    public List<FacetItem> getItems()
    {

        return items;
    }

    public void setItems(List<FacetItem> items)
    {

        this.items = items;
    }
}
