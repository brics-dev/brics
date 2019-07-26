
package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.service.WebServiceManager;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.portal.PortalUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class contains the action of exporting Form Structures into CSV
 * 
 * @author Francis Chen
 * 
 */
public class DataStructureCsvExportAction extends BaseDictionaryAction
{

    private static final long serialVersionUID = -1823233898572586487L;

    @Autowired
    WebServiceManager webServiceManager;

    private InputStream inputStream;

    private String fileName;

    public InputStream getInputStream()
    {

        return inputStream;
    }

    public void setInputStream(InputStream inputStream)
    {

        this.inputStream = inputStream;
    }

    public String getFileName()
    {

        return fileName;
    }

    public void setFileName(String fileName)
    {

        this.fileName = fileName;
    }

    /**
     * This methods exports the Form Structure to CSV
     * 
     * @return
     * @throws Exception
     */
    public String export() throws Exception
    {

        setupStream(false);

        return PortalConstants.ACTION_EXPORT;
    }

    /**
     * This methods exports the Form Structure to CSV with the sample data
     * 
     * @return
     * @throws Exception
     */
    public String exportWithData() throws Exception
    {

        setupStream(true);

        return PortalConstants.ACTION_EXPORT;
    }

    private void setupStream(boolean includeData) throws Exception
    {

        String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
        String dsName = getRequest().getParameter(PortalConstants.DATASTRUCTURE_NAME);
        FormStructure dataStructure = null;
        try
        {
            dataStructure = dictionaryManager.getDataStructureLatestVersion(dsName);
            // Set filename for use later
            fileName = dataStructure.getShortName().toString() + ".csv";
        }
        catch (Exception e)
        {
            throw new Exception("String dsName cannot be empty");
        }

        RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
        PermissionType permission = restProvider.getAccess(getAccount().getId(), EntityType.DATA_STRUCTURE,
                dataStructure.getId()).getPermission();

        ByteArrayOutputStream baos = dictionaryManager.exportDataStructure(accountUrl,
                PortalUtils.getProxyTicket(accountUrl), permission, dataStructure, includeData, getDiseaseId());
        inputStream = new ByteArrayInputStream(baos.toByteArray());
    }
}
