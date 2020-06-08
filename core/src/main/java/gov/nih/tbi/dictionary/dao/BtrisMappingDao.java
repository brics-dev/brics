package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.BtrisMapping;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

public interface BtrisMappingDao extends GenericDao<BtrisMapping, Long> {
	public BtrisMapping getBtrisMappingByName(DataElement latestElement, String btrisObservationName,
			String btrisSpecimenType);

	public BtrisMapping getBtrisMappingByCode(DataElement latestElement, String btrisRedCode);

	public BtrisMapping getBtrisMappingSubjectByName(DataElement latestElement, String btrisObservationName,
			String btrisTable);

	public BtrisMapping getBtrisMappingByDeNameAndPv(DataElement latestElement, ValueRange vr,
			String btrisObservationName,
			String btrisTable);

	public BtrisMapping getBtrisMappingByDE(DataElement latestElement);
}

