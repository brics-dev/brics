package gov.nih.tbi.repository.dao.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.repository.dao.TherapyTypeDao;
import gov.nih.tbi.repository.model.alzped.TherapyType;

@Transactional("metaTransactionManager")
@Repository
public class TherapyTypeDaoImpl extends GenericDaoImpl<TherapyType, Long> implements TherapyTypeDao {

	@Autowired
    public TherapyTypeDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory)
    {

        super(TherapyType.class, sessionFactory);
    }

}
