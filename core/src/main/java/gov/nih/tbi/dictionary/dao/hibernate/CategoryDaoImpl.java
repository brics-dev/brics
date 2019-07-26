
package gov.nih.tbi.dictionary.dao.hibernate;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.CategoryDao;
import gov.nih.tbi.dictionary.model.hibernate.Category;

@Transactional("dictionaryTransactionManager")
@Repository
public class CategoryDaoImpl extends GenericDictDaoImpl<Category, Long> implements CategoryDao
{

    @Autowired
    public CategoryDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory)
    {

        super(Category.class, sessionFactory);
    }
    
}
