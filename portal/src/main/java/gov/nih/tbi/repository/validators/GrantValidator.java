
package gov.nih.tbi.repository.validators;

import gov.nih.tbi.commons.service.RepositoryManager;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class GrantValidator extends FieldValidatorSupport
{

    //
    // private static final String DCB_REST_LOCATION = "http://localhost:8085/rest.service/service";
    // private static final String DCB_REST_GRANT_LOCATION = "/impacii/projectinfo/%s.xml";

    @Autowired
    RepositoryManager repositoryManager;

    /**
     * Method called by struts2 validation process
     */
    @SuppressWarnings("unchecked")
    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        try
        {
            String defaultPath = this.getValidatorContext().getText("dcb.rest.url");

            String path = String.format(defaultPath, fieldValue);
            HttpClient client = new HttpClient();
            GetMethod method = new GetMethod(path);
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK)
            {
                addFieldError(fieldName, "Invalid Clinical Trial ID");
            }
            else
            {
                InputStream rstream = null;
                rstream = method.getResponseBodyAsStream();

                Document queryresponse = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(rstream);
                queryresponse.normalize();

                if (queryresponse.getElementsByTagName("error").getLength() > 0)
                {
                    addFieldError(fieldName, "Invalid Clinical Trial ID");
                }
            }
        }
        catch (Exception e)
        {
            addFieldError(fieldName, "Invalid Clinical Trial ID");
            e.printStackTrace();
        }

    }
}
