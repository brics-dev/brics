package gov.nih.tbi.dictionary.dao.hibernate;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.SchemaPvDao;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.SchemaPv;

@Transactional("dictionaryTransactionManager")
@Repository
public class SchemaPvDaoImpl extends GenericDictDaoImpl<SchemaPv, Long> implements SchemaPvDao {

	@Autowired
	public SchemaPvDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(SchemaPv.class, sessionFactory);
	}

	@Override
	public SchemaPv getSchemaMapping(DataElement dataElement, String schemaName, String pvValue) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SchemaPv> query = cb.createQuery(SchemaPv.class);
		Root<SchemaPv> root = query.from(SchemaPv.class);

		query.where(cb.and(cb.equal(root.join("schema", JoinType.LEFT).get("name"), schemaName),
				cb.equal(root.join("valueRange", JoinType.LEFT).get("valueRange"), pvValue),
				cb.equal(root.get("dataElement"), dataElement.getStructuralObject())));

		return getUniqueResult(query);
	}

	@Override
	public List<SchemaPv> getDeSchemaSystemId(DataElement dataElement, String schemaName) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SchemaPv> query = cb.createQuery(SchemaPv.class);
		Root<SchemaPv> root = query.from(SchemaPv.class);

		query.where(cb.and(cb.equal(root.join("schema", JoinType.LEFT).get("name"), schemaName),
				cb.equal(root.get("dataElement"), dataElement.getStructuralObject())));

		return createQuery(query).getResultList();
	}


	@Override
	public List<SchemaPv> getAllByDataElement(DataElement dataElement) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SchemaPv> query = cb.createQuery(SchemaPv.class);
		Root<SchemaPv> root = query.from(SchemaPv.class);

		Join<SchemaPv, Schema> schemaJoin = root.join("schema", JoinType.LEFT);
		query.where(cb.equal(root.get("dataElement"), dataElement.getStructuralObject()));
		
		query.orderBy(cb.asc(schemaJoin.get("name")));
	//	query.distinct(true);

		return createQuery(query).getResultList();
	}


	@Override
	public List<SchemaPv> getAllByDataElementId(Long dataElementId) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SchemaPv> query = cb.createQuery(SchemaPv.class);
		Root<SchemaPv> root = query.from(SchemaPv.class);

		query.where(cb.equal(root.join("dataElement", JoinType.LEFT).get("id"), dataElementId));
		query.orderBy(cb.asc(root.join("schema", JoinType.LEFT).get("name")));

		return createQuery(query).getResultList();
	}
}
