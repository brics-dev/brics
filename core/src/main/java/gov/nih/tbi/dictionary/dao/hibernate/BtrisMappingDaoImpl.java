package gov.nih.tbi.dictionary.dao.hibernate;

import java.util.List;

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
import gov.nih.tbi.dictionary.dao.BtrisMappingDao;
import gov.nih.tbi.dictionary.model.hibernate.BtrisMapping;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

@Transactional("dictionaryTransactionManager")
@Repository
public class BtrisMappingDaoImpl extends GenericDictDaoImpl<BtrisMapping, Long> implements BtrisMappingDao {

	@Autowired
	public BtrisMappingDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(BtrisMapping.class, sessionFactory);
	}

	@Override
	public BtrisMapping getBtrisMappingByName(DataElement latestElement, String btrisObservationName,
			String btrisSpecimenType) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BtrisMapping> query = cb.createQuery(BtrisMapping.class);
		Root<BtrisMapping> root = query.from(BtrisMapping.class);

		query.where(cb.and(cb.equal(root.get("btrisObservationName"), btrisObservationName),
				cb.equal(root.get("btrisSpecimenType"), btrisSpecimenType),
				cb.equal(root.get("bricsDataElement"), latestElement.getStructuralObject())
				
				));

		return getUniqueResult(query);
	}

	@Override
	public BtrisMapping getBtrisMappingByCode(DataElement latestElement, String btrisRedCode) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BtrisMapping> query = cb.createQuery(BtrisMapping.class);
		Root<BtrisMapping> root = query.from(BtrisMapping.class);

		query.where(cb.and(cb.equal(root.get("btrisRedCode"), btrisRedCode),
				cb.equal(root.get("bricsDataElement"), latestElement.getStructuralObject())));

		return getUniqueResult(query);
	}

	@Override
	public BtrisMapping getBtrisMappingSubjectByName(DataElement latestElement, String btrisObservationName,
			String btrisTable) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BtrisMapping> query = cb.createQuery(BtrisMapping.class);
		Root<BtrisMapping> root = query.from(BtrisMapping.class);

		query.where(cb.and(cb.equal(root.get("btrisObservationName"), btrisObservationName),
				cb.equal(root.get("btrisTable"), btrisTable),
				cb.equal(root.get("bricsDataElement"), latestElement.getStructuralObject())));

		return getUniqueResult(query);
	}

	@Override
	public BtrisMapping getBtrisMappingByDeNameAndPv(DataElement latestElement, ValueRange vr,
			String btrisObservationName, String btrisTable) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BtrisMapping> query = cb.createQuery(BtrisMapping.class);
		Root<BtrisMapping> root = query.from(BtrisMapping.class);

		query.where(cb.and(cb.equal(root.get("btrisObservationName"), btrisObservationName),
				cb.equal(root.get("btrisTable"), btrisTable), cb.equal(root.get("bricsValueRange"), vr),
				cb.equal(root.get("bricsDataElement"), latestElement.getStructuralObject())));

		return getUniqueResult(query);
	}

	@Override
	public BtrisMapping getBtrisMappingByDE(DataElement latestElement) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BtrisMapping> query = cb.createQuery(BtrisMapping.class);
		Root<BtrisMapping> root = query.from(BtrisMapping.class);

		query.where(cb.equal(root.get("bricsDataElement"), latestElement.getStructuralObject()));
		List<BtrisMapping> btrisMappingList = createQuery(query).getResultList();
		if (btrisMappingList != null && btrisMappingList.size() > 0) {
			return btrisMappingList.get(0);
		} else {
			return null;
		}
	}
}
