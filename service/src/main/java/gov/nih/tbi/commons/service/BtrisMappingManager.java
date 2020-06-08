package gov.nih.tbi.commons.service;

import java.util.List;

import gov.nih.tbi.dictionary.model.hibernate.BtrisMapping;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

public interface BtrisMappingManager extends BaseManager {

	public BtrisMapping getBtrisMappingByName(DataElement latestElement, String btrisObservationName,
			String btrisSpecimenType);

	public BtrisMapping getBtrisMappingByCode(DataElement latestElement, String btrisRedCode);

	public BtrisMapping getBtrisMappingSubjectByName(DataElement latestElement, String btrisObservationName,
			String btrisTable);

	public BtrisMapping getBtrisMappingByDeNameAndPv(DataElement latestElement, String pvValue,
			String btrisObservationName, String btrisTable);

	public ValueRange getValueRangeByDeNameAndPv(String elementName, String pvValue);

	public ValueRange getValueRangeByDeAndPv(DataElement dataElement, String pvValue);

	public BtrisMapping saveBtrisMapping(BtrisMapping btrisMapping);

	public void saveBtrisMappingList(List<BtrisMapping> btrisMappingList);

	public DataElement getLatestDeByName(String elementName);

	public BtrisMapping getBtrisMappingByDE(DataElement latestElement);

	public BtrisMapping getBtrisMappingByDEName(String elementName);
}

