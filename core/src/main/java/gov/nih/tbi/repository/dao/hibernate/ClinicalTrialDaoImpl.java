
package gov.nih.tbi.repository.dao.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.repository.dao.ClinicalTrialDao;
import gov.nih.tbi.repository.model.hibernate.ClinicalTrial;

@Transactional("metaTransactionManager")
@Repository
public class ClinicalTrialDaoImpl extends GenericDaoImpl<ClinicalTrial, Long> implements ClinicalTrialDao
{

    @Autowired
    public ClinicalTrialDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory)
    {

        super(ClinicalTrial.class, sessionFactory);
    }
}
