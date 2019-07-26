package gov.nih.tbi.commons.service;

import java.util.List;

import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.SchemaPv;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

public interface SchemaMappingManager extends BaseManager {
	
	/**
	 * Obtains the SchemaPv object matching the intersection of element shortname,
	 * schema name, and permissible value. 
	 * 
	 * @param elementName data element shortname
	 * @param schemaName schema name
	 * @param pvValue permissible value (valueRange in ValueRange object)
	 * @return Schema PV mapping object matching the criteria or null if not found
	 */
	public SchemaPv getSchemaMapping(String elementName, String schemaName, String pvValue);
	
	/**
	 * Obtains the SchemaPv object matching the intersection of element, schema name,
	 * and permissible value.
	 * 
	 * @param element the data element
	 * @param schemaName schema name
	 * @param pvValue permissible value (valueRange in ValueRange object)
	 * @return Schema PV mapping object matching the criteria or null if not found
	 */
	public SchemaPv getSchemaMapping(DataElement element, String schemaName, String pvValue);
	
	public List<Schema> getAllSchemas();
	
	public Schema getSchemaByName(String schemaName);
	
	public Schema getSchemaBySystemId(String schemaSystemId);
	
	public ValueRange getValueRangeByDeNameAndPv(String elementName, String pvValue);
	
	public ValueRange getValueRangeByDeAndPv(DataElement dataElement, String pvValue);
	
	public ValueRange saveValueRange(ValueRange vr);
	
	public SchemaPv saveSchemaPv(SchemaPv schemaPv);
	
	public DataElement getLatestDeByName(String elementName);
	
	public String getDeSchemaSystemId(String elementName, String schemaName);
	
	public String getDeSchemaSystemId(DataElement latestDe, String schemaName);
	
	public List<SchemaPv> getAllMappings(DataElement latestDe);
	
	public List<SchemaPv> getAllMappings(String elementName);
	
	public List<ValueRange> getValueRangesByDe(DataElement de);
	
	public List<Schema> getSchemasByFormStructureNames(List<String> names);
}
