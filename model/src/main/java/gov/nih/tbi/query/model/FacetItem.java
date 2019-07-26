
package gov.nih.tbi.query.model;

public class FacetItem
{

    private String label;
    private String rdfURI;
    private Integer count;

    public String getLabel()
    {

        return label;
    }

    public void setLabel(String label)
    {

        this.label = label;
    }

    public String getRdfURI()
    {

        return rdfURI;
    }

    public void setRdfURI(String rdfURI)
    {

        this.rdfURI = rdfURI;
    }

    public Integer getCount()
    {

        return count;
    }

    public void setCount(Integer count)
    {

        this.count = count;
    }

}
