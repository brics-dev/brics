package gov.nih.tbi.repository.dao.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.repository.dao.TherapeuticAgentDao;
import gov.nih.tbi.repository.model.alzped.TherapeuticAgent;

@Transactional("metaTransactionManager")
@Repository
public class TherapeuticAgentDaoImpl extends GenericDaoImpl<TherapeuticAgent, Long> implements TherapeuticAgentDao
{

    @Autowired
    public TherapeuticAgentDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory)
    {

        super(TherapeuticAgent.class, sessionFactory);
    }
    

}
