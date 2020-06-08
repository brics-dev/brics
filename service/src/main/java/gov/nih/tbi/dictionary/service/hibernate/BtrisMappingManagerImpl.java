package gov.nih.tbi.dictionary.service.hibernate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import gov.nih.tbi.account.service.complex.BaseManagerImpl;
import gov.nih.tbi.commons.service.BtrisMappingManager;
import gov.nih.tbi.dictionary.dao.BtrisMappingDao;
import gov.nih.tbi.dictionary.dao.DataElementDao;
import gov.nih.tbi.dictionary.dao.ValueRangeDao;
import gov.nih.tbi.dictionary.model.hibernate.BtrisMapping;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

@Service
@Scope("singleton")
public class BtrisMappingManagerImpl extends BaseManagerImpl implements BtrisMappingManager {
	private static final long serialVersionUID = -9024820137899531543L;

	@Autowired
	BtrisMappingDao btrisMappingDao;

	@Autowired
	ValueRangeDao valueRangeDao;

	@Autowired
	DataElementDao dataElementDao;


	@Override
	public BtrisMapping getBtrisMappingByName(DataElement latestElement, String btrisObservationName,
			String btrisSpecimenType) {
		if (latestElement != null) {
			return btrisMappingDao.getBtrisMappingByName(latestElement, btrisObservationName, btrisSpecimenType);
		}
		return null;
	}

	@Override
	public BtrisMapping getBtrisMappingByCode(DataElement latestElement, String btrisRedCode) {
		if (latestElement != null) {
			return btrisMappingDao.getBtrisMappingByCode(latestElement, btrisRedCode);
		}
		return null;
	}

	@Override
	public BtrisMapping getBtrisMappingSubjectByName(DataElement latestElement, String btrisObservationName,
			String btrisTable) {
		if (latestElement != null) {
			return btrisMappingDao.getBtrisMappingSubjectByName(latestElement, btrisObservationName, btrisTable);
		}
		return null;
	}

	@Override
	public BtrisMapping getBtrisMappingByDeNameAndPv(DataElement latestElement, String pvValue,
			String btrisObservationName, String btrisTable) {
		ValueRange vr = this.getValueRangeByDeAndPv(latestElement, pvValue);
		if (vr != null) {
			return btrisMappingDao.getBtrisMappingByDeNameAndPv(latestElement, vr, btrisObservationName, btrisTable);
		}
		return null;
	}

	@Override
	public ValueRange getValueRangeByDeNameAndPv(String elementName, String pvValue) {
		DataElement latestDe = getLatestDeByName(elementName);
		if (latestDe != null) {
			return valueRangeDao.getByDeNameAndPv(latestDe, pvValue);
		}
		return null;
	}

	@Override
	public ValueRange getValueRangeByDeAndPv(DataElement dataElement, String pvValue) {
		for (ValueRange vr : dataElement.getValueRangeList()) {
			if (pvValue.equalsIgnoreCase(vr.getValueRange())) {
				return vr;
			}
		}
		return null;
	}

	@Override
	public BtrisMapping saveBtrisMapping(BtrisMapping btrisMapping) {
		return btrisMappingDao.save(btrisMapping);
	}

	@Override
	public void saveBtrisMappingList(List<BtrisMapping> btrisMappingList) {
		for (BtrisMapping bm : btrisMappingList) {
			btrisMappingDao.save(bm);
		}
	}

	@Override
	public DataElement getLatestDeByName(String elementName) {
		return dataElementDao.getLatestByNameCaseInsensitive(elementName);
	}

	@Override
	public BtrisMapping getBtrisMappingByDE(DataElement latestElement) {
		return btrisMappingDao.getBtrisMappingByDE(latestElement);
	}

	@Override
	public BtrisMapping getBtrisMappingByDEName(String elementName) {
		DataElement latestElement = this.getLatestDeByName(elementName);
		if(latestElement != null) {
			return btrisMappingDao.getBtrisMappingByDE(latestElement);
		} else {
			return null;
		}
	}

}
