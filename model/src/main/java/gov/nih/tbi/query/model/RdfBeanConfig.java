
package gov.nih.tbi.query.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains all the information needed by the GenericSparqlDao to generate queries properly.
 * 
 * @author Francis Chen
 * 
 */
public class RdfBeanConfig
{

    private String classUri;
    private List<RdfBeanField> beanFields;

    public RdfBeanConfig(String classUri, List<RdfBeanField> beanFields)
    {

        this.classUri = classUri;
        this.beanFields = beanFields;
    }

    public String getClassUri()
    {

        return classUri;
    }

    public void setClassUri(String classUri)
    {

        this.classUri = classUri;
    }

    public List<RdfBeanField> getBeanFields()
    {

        return beanFields;
    }

    public void setBeanFields(List<RdfBeanField> beanFields)
    {

        this.beanFields = beanFields;
    }

    public List<String> getFieldNames()
    {

        List<String> output = new ArrayList<String>();

        if (beanFields != null)
        {
            for (RdfBeanField field : beanFields)
            {
                output.add(field.getName());
            }
        }
        
        return output;
    }
}
