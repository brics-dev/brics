
package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.portal.AbstractEntityPermissionManagementAction;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.service.WebServiceManager;
import gov.nih.tbi.dictionary.model.SessionDataElement;
import gov.nih.tbi.portal.PortalUtils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Data Element implementation of the Permission Management Action
 */
public class DataElementPermissionManagementAction extends AbstractEntityPermissionManagementAction
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 504835554495110404L;

    /******************************************************************************/

    @Autowired
    SessionDataElement sessionDataElement;

    @Autowired
    WebServiceManager webServiceManager;

    /******************************************************************************/

    @Override
    public Long getEntityId()
    {

        return sessionDataElement.getDataElement().getId();
    }

    @Override
    public EntityType getEntityType()
    {

        return EntityType.DATA_ELEMENT;
    }

    @Override
    public List<EntityMap> getEntityMapList()
    {

        return sessionDataElement.getEntityMapList();
    }

    @Override
    public List<EntityMap> getRemovedMapList()
    {

        return sessionDataElement.getRemovedMapList();
    }

    @Override
    public String getActionName()
    {

        return "dataElementPermissionAction";
    }
    
    @Override
    public List<String> getEntityMapAuthNameList()
    {
    	return sessionDataElement.getEntityMapAuthNameList();
    }

    /******************************************************************************/

    /**
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     ****************************************************************************/

    @Override
    public String load() throws MalformedURLException, UnsupportedEncodingException
    {
        if (sessionDataElement.getEntityMapList() == null)
        {
            List<EntityMap> entityMapList = null;
            List<String> entityMapAuthNameList = new ArrayList<String>();

            if (getEntityId() != null)
            {
                // entityMapList = getAccountManager().listEntityAccess(getEntityId(), EntityType.DATA_ELEMENT);
                // Call webservice

                String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
                RestAccountProvider restProvider = new RestAccountProvider(accountUrl,
                        PortalUtils.getProxyTicket(accountUrl));
                entityMapList = restProvider.listEntityAccess(getEntityId(), EntityType.DATA_ELEMENT);
                for(EntityMap em: entityMapList) {
                	entityMapAuthNameList.add(em.getAuthority().getDisplayName());
                }
            }
            else
            {
                entityMapList = new ArrayList<EntityMap>();
            }

            sessionDataElement.setEntityMapList(entityMapList);
            sessionDataElement.setEntityMapAuthNameList(entityMapAuthNameList);
        }

        return list();
    }

}
