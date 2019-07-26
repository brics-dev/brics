
package gov.nih.tbi.pojo;

public enum ResultType
{
	STUDY("Study", gov.nih.tbi.constants.QueryToolConstants.STUDY_URI, "study_"),
	FORM_STRUCTURE("Form", gov.nih.tbi.constants.QueryToolConstants.FORM_STRUCTURE_URI, "form_"),
	DATA_ELEMENT("DataElement", gov.nih.tbi.constants.QueryToolConstants.DATAELEMENT_URI, "de_");

    private String name;
    private String uri;
    private String prefix;

    private ResultType(String name, String uri, String prefix)
    {

        this.name = name;
        this.uri = uri;
        this.prefix = prefix;
    }

    public String getPrefix()
    {

        return prefix;
    }

    public void setPrefix(String prefix)
    {

        this.prefix = prefix;
    }

    public String getName()
    {

        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }

    public String getUri()
    {

        return uri;
    }

    public void setUri(String uri)
    {

        this.uri = uri;
    }
}
