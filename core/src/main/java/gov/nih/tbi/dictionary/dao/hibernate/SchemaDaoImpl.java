package gov.nih.tbi.dictionary.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.SchemaDao;
import gov.nih.tbi.dictionary.model.hibernate.Schema;

@Transactional("dictionaryTransactionManager")
@Repository
public class SchemaDaoImpl extends GenericDictDaoImpl<Schema, Long> implements SchemaDao {

	@Autowired
	public SchemaDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(Schema.class, sessionFactory);
	}

	@Override
	public Schema getByName(String name) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Schema> query = cb.createQuery(Schema.class);
		Root<Schema> root = query.from(Schema.class);

		query.where(cb.like(cb.upper(root.get(CoreConstants.NAME)), name.toUpperCase()));
		return getUniqueResult(query);
	}

	@Override
	public Schema getBySystemid(String systemId) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Schema> query = cb.createQuery(Schema.class);
		Root<Schema> root = query.from(Schema.class);

		query.where(cb.like(cb.upper(root.get("schemaSystemId")), systemId.toUpperCase()));
		return getUniqueResult(query);
	}

	public List<Schema> getByFormStructureNames(List<String> names) {
		if (names.isEmpty()) {
			return new ArrayList<Schema>();
		}

		// note: we need to make sure that our query only includes the latest version of any data element. This is
		// because schema PV changes will version a data element and the data element id remains static for a form
		// structure once it's attached.
		String queryTemplate = "select distinct s.* from schema_pv spv join (select distinct max(de2.id) as de_id from "
				+ "data_structure ds join repeatable_group rg on ds.id = rg.data_structure_id join "
				+ "map_element me on me.repeatable_group_id = rg.id join data_element de on de.id = me.data_element_id "
				+ "join data_element de2 on de2.element_name = de.element_name where ds.short_name IN (%s) group by "
				+ "de2.element_name) sub on spv.data_element_id = sub.de_id join schema s on spv.schema_id = s.id;";
		
		StringBuilder namesBuilder = new StringBuilder();
		for (String name : names) {
			namesBuilder.append(CoreConstants.SINGLE_QUOTE).append(name).append(CoreConstants.SINGLE_QUOTE)
					.append(CoreConstants.COMMA);
		}

		namesBuilder.replace(namesBuilder.length() - 1, namesBuilder.length(), CoreConstants.EMPTY_STRING);

		String queryString = String.format(queryTemplate, namesBuilder.toString());
		NativeQuery<Schema> query = getSession().createNativeQuery(queryString, Schema.class);
		
		// TODO Test this::
		List<Schema> schemas = query.getResultList();
		return schemas;
	}
	
}
