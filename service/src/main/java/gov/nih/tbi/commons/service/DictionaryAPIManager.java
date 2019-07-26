package gov.nih.tbi.commons.service;

import gov.nih.tbi.commons.model.exceptions.SchemaGenerationException;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;

public interface DictionaryAPIManager extends BaseManager{
	
	
	/**
	 * 
	 * @param shortName - shortname of the form structure
	 * @param version - version of the form structure
	 * @return
	 */
	public StructuralFormStructure getStructuralFormStructure(String shortName, String version);
	
	/**
	 * 
	 * @param variableName - variablename of the data element
	 * @param version - version fo the data element
	 * @return
	 */
	public StructuralDataElement getStructuralDataElement(String variableName, String version);
	
	/**
	 * 
	 * @param formStructure - form structure to turn into schema
	 * @return - byte array of the schema
	 */
	public byte[] createFormStructureSchema(StructuralFormStructure formStructure) throws SchemaGenerationException;
	
	/**
	 * 
	 * @param dataElement - data element to turn into schema
	 * @return - byte array of the schema
	 */
	public byte[] createDataElementSchema(StructuralDataElement dataElement) throws SchemaGenerationException;
	
	/**
	 * 
	 * @param variableName - variablename of the data element
	 * @param version - version fo the data element
	 * @return
	 */
	public SemanticDataElement getSemanticDataElement(String variableName, String version);
	
	public void deleteDataElementSemanticPart(SemanticDataElement sementicDE);
	public void deleteDataElementStructuralPart(StructuralDataElement structuralDE);
	public SemanticFormStructure getSemanticFormStructure(String variableName, String version);
	public void deleteFormStructureSemanticPart(String variableName, String version);
	public void deleteFormStructureStructuralPart(StructuralFormStructure structuralDE);
}