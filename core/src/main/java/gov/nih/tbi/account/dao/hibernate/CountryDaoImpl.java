
package gov.nih.tbi.account.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.dao.CountryDao;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.hibernate.Country;

@Transactional("metaTransactionManager")
@Repository
public class CountryDaoImpl extends GenericDaoImpl<Country, Long> implements CountryDao {

	@Autowired
	public CountryDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {

		super(Country.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public Country getByName(String name) {

		CriteriaBuilder builder = getCriteriaBuilder();
		CriteriaQuery<Country> query = builder.createQuery(Country.class);

		Root<Country> root = query.from(Country.class);
		query.where(builder.equal(root.get(CoreConstants.NAME), name)).distinct(true);

		return getUniqueResult(query);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public List<Country> getAll() {

		// Add United States to the output first
		List<Country> outList = new ArrayList<Country>();
		outList.add(getByName(CoreConstants.UNITED_STATES));

		// Add the rest of the countries
		CriteriaBuilder builder = getCriteriaBuilder();
		CriteriaQuery<Country> query = builder.createQuery(Country.class);
		Root<Country> root = query.from(Country.class);
		
		query.where(builder.notEqual(root.get(CoreConstants.NAME), CoreConstants.UNITED_STATES));
		query.orderBy(builder.asc(root.get(CoreConstants.NAME))).distinct(true);

		TypedQuery<Country> q = createQuery(query);
		outList.addAll(q.getResultList());
		return outList;
	}
}
