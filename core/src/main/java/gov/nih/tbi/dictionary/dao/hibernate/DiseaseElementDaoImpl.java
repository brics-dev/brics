
package gov.nih.tbi.dictionary.dao.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.DiseaseElementDao;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseElement;

@Transactional("dictionaryTransactionManager")
@Repository
public class DiseaseElementDaoImpl extends GenericDictDaoImpl<DiseaseElement, Long> implements DiseaseElementDao
{

    @Autowired
    public DiseaseElementDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory)
    {

        super(DiseaseElement.class, sessionFactory);
    }
}
