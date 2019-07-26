
package gov.nih.tbi.dictionary.model.hibernate;

import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * This object stores Data Element External IDs
 * 
 * @author Francis Chen
 */
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExternalId implements Serializable
{

    private static final long serialVersionUID = 1736785038166589234L;

    private String uri;

    private Long id;

    @XmlTransient
    private SemanticDataElement dataElement;

    private Schema schema;

    private String value;

    public ExternalId()
    {

    }

    public ExternalId(String uri, Schema schema, String value)
    {

        super();
        this.uri = uri;
        this.schema = schema;
        this.value = value;
    }

    public ExternalId(Schema schema, String value)
    {

        super();
        this.schema = schema;
        this.value = value;
    }

    @Deprecated
    public ExternalId(SemanticDataElement dataElement, Schema schema, String value)
    {

        this.dataElement = dataElement;
        this.schema = schema;
        this.value = value;
    }

    public ExternalId(ExternalId externalId)
    {

        this.dataElement = externalId.getSemanticDataElement();
        this.schema = externalId.getSchema();
        this.value = externalId.getValue();
    }

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public Schema getSchema()
    {

        return schema;
    }

    public void setSchema(Schema schema)
    {

        this.schema = schema;
    }

    public String getValue()
    {

        return value;
    }

    public void setValue(String value)
    {

        this.value = value;
    }

    public SemanticDataElement getSemanticDataElement()
    {

        return dataElement;
    }

    public void setSemanticDataElement(SemanticDataElement dataElement)
    {

        this.dataElement = dataElement;
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
