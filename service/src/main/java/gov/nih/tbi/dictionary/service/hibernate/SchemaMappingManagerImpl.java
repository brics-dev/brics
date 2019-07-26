package gov.nih.tbi.dictionary.service.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import gov.nih.tbi.account.service.complex.BaseManagerImpl;
import gov.nih.tbi.commons.service.SchemaMappingManager;
import gov.nih.tbi.dictionary.dao.DataElementDao;
import gov.nih.tbi.dictionary.dao.SchemaDao;
import gov.nih.tbi.dictionary.dao.SchemaPvDao;
import gov.nih.tbi.dictionary.dao.ValueRangeDao;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.SchemaPv;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

@Service
@Scope("singleton")
public class SchemaMappingManagerImpl extends BaseManagerImpl implements SchemaMappingManager {
	private static final long serialVersionUID = -9024820137899531543L;

	@Autowired
	SchemaDao schemaDao;

	@Autowired
	SchemaPvDao schemaPvDao;

	@Autowired
	ValueRangeDao valueRangeDao;

	@Autowired
	DataElementDao dataElementDao;


	@Override
	public SchemaPv getSchemaMapping(String elementName, String schemaName, String pvValue) {
		DataElement latestDe = getLatestDeByName(elementName);
		if (latestDe != null) {
			return schemaPvDao.getSchemaMapping(latestDe, schemaName, pvValue);
		}
		return null;
	}

	@Override
	public SchemaPv getSchemaMapping(DataElement element, String schemaName, String pvValue) {
		List<SchemaPv> schemaPvs = schemaPvDao.getAllByDataElementId(element.getId());

		for (SchemaPv schemaPv : schemaPvs) {
			if (schemaName.equals(schemaPv.getSchema().getName())) {
				// if value ranges are equal
				if (schemaPv.getValueRange() != null
						&& pvValue.equalsIgnoreCase(schemaPv.getValueRange().getValueRange())) {
					return schemaPv;

					// if value ranges are both blank
				} else if (schemaPv.getValueRange() == null && StringUtils.isBlank(pvValue)) {
					return schemaPv;
				}
			}
		}

		return null;
	}

	@Override
	public List<Schema> getAllSchemas() {
		return schemaDao.getAll();
	}

	@Override
	public Schema getSchemaByName(String schemaName) {
		return schemaDao.getByName(schemaName);
	}

	@Override
	public Schema getSchemaBySystemId(String schemaSystemId) {
		return schemaDao.getBySystemid(schemaSystemId);
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
	public ValueRange saveValueRange(ValueRange vr) {
		return valueRangeDao.save(vr);
	}

	@Override
	public SchemaPv saveSchemaPv(SchemaPv schemaPv) {
		return schemaPvDao.save(schemaPv);
	}

	@Override
	public DataElement getLatestDeByName(String elementName) {
		// we are calling the case insensitive version of this call just in case the user mixes up the casing of the
		// variable name in the mapping csv
		return dataElementDao.getLatestByNameCaseInsensitive(elementName);
	}

	@Override
	public String getDeSchemaSystemId(String elementName, String schemaName) {
		DataElement latestDe = getLatestDeByName(elementName);
		return getDeSchemaSystemId(latestDe, schemaName);
	}

	@Override
	public String getDeSchemaSystemId(DataElement latestDe, String schemaName) {

		List<SchemaPv> listOfPvs = new ArrayList<SchemaPv>();
		if (latestDe != null) {
			listOfPvs = schemaPvDao.getDeSchemaSystemId(latestDe, schemaName);
		}

		if (listOfPvs.size() > 0) {
			SchemaPv pv = listOfPvs.get(0);
			return pv.getSchemaDeId();
		} else {
			return null;
		}
	}

	@Override
	public List<SchemaPv> getAllMappings(DataElement latestDe) {
		if (latestDe != null) {
			return schemaPvDao.getAllByDataElement(latestDe);
		} else {
			return null;
		}
	}

	@Override
	public List<SchemaPv> getAllMappings(String elementName) {
		DataElement latestDe = getLatestDeByName(elementName);
		if (latestDe != null) {
			return schemaPvDao.getAllByDataElement(latestDe);
		} else {
			return null;
		}
	}

	@Override
	public List<ValueRange> getValueRangesByDe(DataElement de) {
		if (de != null) {
			return valueRangeDao.getByDeName(de);
		} else {
			return null;
		}
	}

	public List<Schema> getSchemasByFormStructureNames(List<String> names) {
		return schemaDao.getByFormStructureNames(names);
	}
}
