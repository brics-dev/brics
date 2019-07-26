
package gov.nih.tbi.pojo;

public class BeanField
{

    private String name;
    private String propertyUri;
    private Class<?> type;
    private boolean collection;

    public BeanField(String name, String propertyUri, Class<?> type, boolean collection)
    {

        this.name = name;
        this.propertyUri = propertyUri;
        this.type = type;
        this.collection = collection;
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

    public boolean isCollection()
    {

        return collection;
    }

    public void setCollection(boolean collection)
    {

        this.collection = collection;
    }

}
