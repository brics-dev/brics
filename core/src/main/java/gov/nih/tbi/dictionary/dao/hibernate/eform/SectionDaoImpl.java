package gov.nih.tbi.dictionary.dao.hibernate.eform;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.eform.SectionDao;
import gov.nih.tbi.dictionary.model.hibernate.eform.Section;

@Transactional("dictionaryTransactionManager")
@Repository
public class SectionDaoImpl extends GenericDictDaoImpl<Section, Long> implements SectionDao {
	
	@Autowired
	public SectionDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {
		super(Section.class, sessionFactory);
	}
	
}
