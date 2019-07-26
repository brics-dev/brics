
package gov.nih.tbi.dictionary.ws.cxf;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@Path("/dataElement/")
public class DataElementRestService
{

    private static Logger logger = Logger.getLogger(DataElementRestService.class);

    @Autowired
    private DictionaryToolManager dictionaryToolManager;

    @Autowired
    private AccountManager accountManager;

    @Autowired
    protected ModulesConstants modulesConstants;

    @SuppressWarnings("unchecked")
    @GET
    @Path("/list")
    public DataDictionaryDataElement list() throws MalformedURLException, UnsupportedEncodingException
    {

        Account account = accountManager.getAccountByUserName("anonymous");
        RestAccountProvider accountProvider = new RestAccountProvider(modulesConstants.getModulesAccountURL(),
                modulesConstants.getModulesDDTURL());

        List<DataElement> dataElement = (List<DataElement>) dictionaryToolManager
                .getDataElementsListByIds(new ArrayList<Long>(accountProvider.listUserAccess(account.getId(),
                        EntityType.DATA_ELEMENT, PermissionType.READ, false)));

        return new DataDictionaryDataElement(dataElement);
    }

    @GET
    @Path("/view")
    public DataElement getByVariableName(@QueryParam("variableName") String variableName)
    {

        Account account = accountManager.getAccountByUserName("anonymous");

        try
        {

            if (variableName == null || variableName.isEmpty())
            {
                return null;
            }

            DataElement de = dictionaryToolManager.getLatestDataElementByName(variableName);

            logger.debug("dataElementId: " + de.getId());

            return de;

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @GET
    @Path("/{id}")
    public DataElement getById(@PathParam("id") String id)
    {

        try
        {
            if (id == null || id.isEmpty())
            {
                return null;
            }

            Long longId = Long.valueOf(id);
            logger.debug("id: " + longId);

            DataElement de = (DataElement) dictionaryToolManager.getDataElement(longId);

            if (de != null && de.getId() != null)
            {
                logger.debug("dataElementId: " + de.getId());
            }
            return de;

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

}
