package gov.nih.tbi.dictionary.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.SchemaPv;

public interface SchemaPvDao extends GenericDao<SchemaPv, Long> {
	public SchemaPv getSchemaMapping(DataElement latestElement, String schemaName, String pvValue);
	public List<SchemaPv> getDeSchemaSystemId(DataElement latestElement, String schemaName);
	public List<SchemaPv> getAllByDataElement(DataElement dataElement);
	public List<SchemaPv> getAllByDataElementId(Long dataElementId);
}
