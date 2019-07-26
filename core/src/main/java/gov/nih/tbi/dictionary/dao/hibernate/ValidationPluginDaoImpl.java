
package gov.nih.tbi.dictionary.dao.hibernate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.ValidationPluginDao;
import gov.nih.tbi.dictionary.model.hibernate.ValidationPlugin;

@Transactional("dictionaryTransactionManager")
@Repository
public class ValidationPluginDaoImpl extends GenericDictDaoImpl<ValidationPlugin, Long> implements ValidationPluginDao {

	@Autowired
	public ValidationPluginDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(ValidationPlugin.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public ValidationPlugin getValidatorByName(String name) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<ValidationPlugin> query = cb.createQuery(ValidationPlugin.class);
		Root<ValidationPlugin> root = query.from(ValidationPlugin.class);

		query.where(cb.equal(root.get("name"), name)).distinct(true);
		return getUniqueResult(query);
	}
}
