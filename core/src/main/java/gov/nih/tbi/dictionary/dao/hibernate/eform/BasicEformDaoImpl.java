package gov.nih.tbi.dictionary.dao.hibernate.eform;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.NonUniqueResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.dictionary.dao.eform.BasicEformDao;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;

@Transactional("dictionaryTransactionManager")
@Repository
public class BasicEformDaoImpl extends GenericDictDaoImpl<BasicEform, Long> implements BasicEformDao {

	@Autowired
	public BasicEformDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {
		super(BasicEform.class, sessionFactory);
	}

	public List<BasicEform> list(Set<Long> eformIds) {

		if (eformIds != null && !eformIds.isEmpty()) {
			CriteriaBuilder cb = getCriteriaBuilder();
			CriteriaQuery<BasicEform> query = cb.createQuery(BasicEform.class);
			Root<BasicEform> root = query.from(BasicEform.class);

			query.where(root.get("id").in(eformIds)).distinct(true);
			List<BasicEform> returnObject = createQuery(query).getResultList();
			return returnObject;

		} else {
			return new ArrayList<BasicEform>();
		}
	}

	public List<BasicEform> searchEformWithFormStructureTitle(List<Long> eformIds) {

		StringBuffer queryBuffer = new StringBuffer(200);

		// Build the initial query.
		queryBuffer
				.append("select ef.id as id, ef.title as title, ef.short_name as shortName, ef.status_id as status, ")
				.append("ef.updated_date as updatedDate, ef.form_structure_name as formStructureShortName, ")
				.append("ef.description as description, ef.create_date as createDate, ef.created_by as createBy, ")
				.append("ef.published_date as publicationDate, ds.title as formStructureTitle from eform ef, data_structure ds ")
				.append("where ef.form_structure_name = ds.short_name and ds.id in (SELECT max(ds2.id) FROM data_structure ds2 ")
				.append("WHERE ds.short_name = ds2.short_name)");

		if (eformIds != null && !eformIds.isEmpty()) {
			queryBuffer.append(" and ef.id in (");

			for (Iterator<Long> it = eformIds.iterator(); it.hasNext();) {
				queryBuffer.append(it.next().toString());
				if (it.hasNext()) {
					queryBuffer.append(", ");
				}
			}
			queryBuffer.append(")");
		}

		queryBuffer.append(" order by title;");
		NativeQuery query = getSession().createNativeQuery(queryBuffer.toString());

		List<Object[]> results = query.getResultList();
		List<BasicEform> eforms = new ArrayList<BasicEform>();

		for (Object[] obj : results) {
			BasicEform eform = new BasicEform();
			eform.setId(((BigInteger) obj[0]).longValue());
			eform.setTitle(String.valueOf(obj[1]));
			eform.setShortName(String.valueOf(obj[2]));
			eform.setStatus(StatusType.statusOf(((BigInteger) obj[3]).longValue()));
			eform.setUpdatedDate((Date) obj[4]);
			eform.setFormStructureShortName(String.valueOf(obj[5]));
			eform.setDescription(String.valueOf(obj[6]));
			eform.setCreateDate((Date) obj[7]);
			eform.setCreateBy(String.valueOf(obj[8]));
			eform.setPublicationDate((Date) obj[9]);
			eform.setFormStructureTitle(String.valueOf(obj[10]));
			eforms.add(eform);
		}

		return eforms;
	}

	public boolean isEformShortNameUnique(String shortName) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BasicEform> query = cb.createQuery(BasicEform.class);
		Root<BasicEform> root = query.from(BasicEform.class);

		query.where(cb.like(cb.upper(root.get("shortName")), shortName.toUpperCase())).distinct(true);

		BasicEform emptyCheckEform = null;
		try {
			emptyCheckEform = getUniqueResult(query);
		} catch (NonUniqueResultException e) {
			return false;
		}

		if (emptyCheckEform == null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<BasicEform> basicEformSearch(Set<Long> eformIds, List<StatusType> eformStatus, String formStructureName,
			String createdBy, Boolean isShared) {
		
		
		

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BasicEform> query = cb.createQuery(BasicEform.class);
		Root<BasicEform> root = query.from(BasicEform.class);
		Predicate predicate = cb.conjunction();

		if (eformIds != null && !eformIds.isEmpty()) {
			predicate = cb.and(predicate, root.get("id").in(eformIds));
		}
		if (eformStatus != null) {
			predicate = cb.and(predicate, root.get("status").in(eformStatus));
			//predicate = cb.and(predicate, cb.equal(root.get("status"),StatusType.PUBLISHED));
		}
		if (!StringUtils.isBlank(formStructureName)) {
			predicate = cb.and(predicate,
					cb.like(cb.upper(root.get("formStructureShortName")), formStructureName.toUpperCase()));
		}
		if (!StringUtils.isBlank(createdBy)) {
			predicate = cb.and(predicate, cb.equal(root.get("createBy"), createdBy));
		}
		if (isShared != null) {
			predicate = cb.and(predicate, cb.equal(root.get("isShared"), isShared));
		}

		//return createQuery(query.distinct(true)).getResultList();
		return createQuery(query.where(predicate).distinct(true)).getResultList();
	}

}
