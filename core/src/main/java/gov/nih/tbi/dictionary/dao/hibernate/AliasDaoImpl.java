
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
import gov.nih.tbi.dictionary.dao.AliasDao;
import gov.nih.tbi.dictionary.model.hibernate.Alias;

@Transactional("dictionaryTransactionManager")
@Repository
public class AliasDaoImpl extends GenericDictDaoImpl<Alias, Long> implements AliasDao {

	@Autowired
	public AliasDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(Alias.class, sessionFactory);
	}

	public Alias getAliasByName(String aliasName) {

		if (aliasName == null) {
			aliasName = CoreConstants.EMPTY_STRING;
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Alias> query = cb.createQuery(Alias.class);

		Root<Alias> root = query.from(persistentClass);
		query.where(cb.like(cb.upper(root.get("name")), aliasName.toUpperCase() + "%"));

		Alias alias = getUniqueResult(query.distinct(true));
		return alias;
	}

}
