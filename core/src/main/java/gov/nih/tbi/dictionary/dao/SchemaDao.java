package gov.nih.tbi.dictionary.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.dictionary.model.hibernate.Schema;

public interface SchemaDao extends GenericDao<Schema, Long> {
	public Schema getByName(String name);
	public Schema getBySystemid(String systemId);
	public List<Schema> getByFormStructureNames(List<String> names);
}
