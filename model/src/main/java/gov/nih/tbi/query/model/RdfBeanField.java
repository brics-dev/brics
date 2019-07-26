
package gov.nih.tbi.query.model;

/**
 * This class contains the information for a bean field
 * @author Francis Chen
 */
public class RdfBeanField
{
    private String name; //name of the field
    private String propertyUri; //URI of the field in RDF
    private Class<?> type; //Type of the field 

    public RdfBeanField(String name, String propertyUri, Class<?> type)
    {

        this.name = name;
        this.propertyUri = propertyUri;
        this.type = type;
    }

    public String getName()
    {

        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }

    public String getPropertyUri()
    {

        return propertyUri;
    }

    public void setPropertyUri(String propertyUri)
    {

        this.propertyUri = propertyUri;
    }

    public Class<?> getType()
    {

        return type;
    }

    public void setType(Class<?> type)
    {

        this.type = type;
    }
}
