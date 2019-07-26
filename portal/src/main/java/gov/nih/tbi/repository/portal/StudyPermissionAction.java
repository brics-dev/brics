
package gov.nih.tbi.repository.portal;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.portal.AbstractEntityPermissionManagementAction;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.repository.model.SessionStudy;

public class StudyPermissionAction extends AbstractEntityPermissionManagementAction
{

    private static final long serialVersionUID = 6413332401293273460L;

    @Autowired
    SessionStudy sessionStudy;

    @Autowired
    AccountManager accountManager;

    @Override
    public Long getEntityId()
    {

        return sessionStudy.getStudy().getId();
    }

    @Override
    public EntityType getEntityType()
    {

        return EntityType.STUDY;
    }

    @Override
    public List<EntityMap> getEntityMapList()
    {

        return sessionStudy.getEntityMapList();
    }

    @Override
    public List<EntityMap> getRemovedMapList()
    {

        return sessionStudy.getRemovedMapList();
    }

    @Override
    public String getActionName()
    {

        return "studyPermissionAction";
    }
    
    @Override
    public List<String> getEntityMapAuthNameList()
    {
    	return sessionStudy.getEntityMapAuthNameList();
    }
    
    @Override
    public String load() throws MalformedURLException, UnsupportedEncodingException
    {
    	System.out.println("StudyPermissionManagementAction.load.getPermissionAuthorities().size(): "+getPermissionAuthorities().size());
        if (sessionStudy.getEntityMapList() == null)
        {
            List<EntityMap> entityMapList = null;
            List<String> entityMapAuthNameList = new ArrayList<String>();

            if (getEntityId() != null)
            {
                entityMapList = accountManager.listEntityAccess(getEntityId(), EntityType.STUDY);
                for(EntityMap em: entityMapList) {
                	System.out.println("StudyPermissionAction.load.entityMap.getAuthority().getDisplayName(): "+em.getAuthority().getDisplayName());
                	entityMapAuthNameList.add(em.getAuthority().getDisplayName());
                }
            }
            else
            {
                entityMapList = new ArrayList<EntityMap>();
            }

            sessionStudy.setEntityMapList(entityMapList);
            sessionStudy.setEntityMapAuthNameList(entityMapAuthNameList);

        }

        return list();
    }

}
