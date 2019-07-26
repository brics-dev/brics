
package gov.nih.tbi.dictionary.dao.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.MeasuringTypeDao;
import gov.nih.tbi.dictionary.model.hibernate.MeasuringType;

@Transactional("dictionaryTransactionManager")
@Repository
public class MeasuringTypeDaoImpl extends GenericDictDaoImpl<MeasuringType, Long> implements MeasuringTypeDao
{

    @Autowired
    public MeasuringTypeDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory)
    {

        super(MeasuringType.class, sessionFactory);
    }

}
