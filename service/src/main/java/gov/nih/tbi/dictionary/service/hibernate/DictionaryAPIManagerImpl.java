package gov.nih.tbi.dictionary.service.hibernate;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.service.complex.BaseManagerImpl;
import gov.nih.tbi.commons.model.exceptions.SchemaGenerationException;
import gov.nih.tbi.commons.service.DictionaryAPIManager;
import gov.nih.tbi.dictionary.dao.DataElementSparqlDao;
import gov.nih.tbi.dictionary.dao.FormStructureSparqlDao;
import gov.nih.tbi.dictionary.dao.FormStructureSqlDao;
import gov.nih.tbi.dictionary.dao.StructuralDataElementDao;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;
import gov.nih.tbi.dictionary.service.schema.SchemaGenerationService;

@Service
@Scope("singleton")
public class DictionaryAPIManagerImpl extends BaseManagerImpl implements DictionaryAPIManager {

	private static final long serialVersionUID = -8831567925710278369L;
	private static Logger logger = Logger.getLogger(DictionaryAPIManagerImpl.class);

	@Autowired
	StructuralDataElementDao structuralDataElementDao;
	
	@Autowired
	DataElementSparqlDao dataElementSparqlDao;

	@Autowired
	FormStructureSqlDao formStructureSqlDao;
	
	@Autowired
	FormStructureSparqlDao formStructureSparqlDao;

	@Autowired
	protected ModulesConstants modulesConstants;

	/**
	 * This method will retrieve just the structural form structure
	 * Get the latest if version is null. Get the exact match if version is not null
	 */
	public StructuralFormStructure getStructuralFormStructure(String shortName, String version) {

		// if the version is null get the latest
		if (version == null || version.equals("")) {
			return formStructureSqlDao.getLatestVersionByShortName(shortName);
		} else {
			return formStructureSqlDao.get(shortName, version);
		}
	}

	/**
	 * This method will retrieve just the structural data element
	 * Get the latest if version is null. Get the exact match if version is not null
	 */
	public StructuralDataElement getStructuralDataElement(String variableName, String version) {

		// if the version is null get the latest
		if (version == null || version.equals("")) {
			return structuralDataElementDao.getLatestByName(variableName);
		} else {
			return structuralDataElementDao.getByNameAndVersion(variableName, version);
		}
	}

	/**
	 * takes a structural form structure and creates XSD
	 * @throws SchemaGenerationException 
	 */
	public byte[] createFormStructureSchema(StructuralFormStructure formStructure) throws SchemaGenerationException {

		try {
			//form structure should not be null at this point
			if(formStructure == null){
				throw new SchemaGenerationException("The data element for schema generation is null.");
			}
			// Try to write form structure into schema
			// This should not fail. If it does throw an internal server error for
			// the web service request.
			return SchemaGenerationService.writeStructureSchema(formStructure);
			
			// print the error and display information, but present it to the
			// rest service as something generic
		} catch (ParserConfigurationException | TransformerException | SAXException | IOException e) {
			logger.error("There was an error parsing shortName: " + formStructure.getShortName() + " version: "
					+ formStructure.getVersion() + " into XSD. The stack will follow.", e);
			throw new SchemaGenerationException("There was an internal error. Please contanct the help desk.");
		}
			catch(NullPointerException nullFormSturcture){
				logger.error("There was an error creating structural form structure rules into schema.", nullFormSturcture);
				throw new SchemaGenerationException("There was an internal error. Please contanct the help desk.");
			}
	}

	/**
	 * takes a structural data element and creates XSD
	 * @throws SchemaGenerationException 
	 */
	public byte[] createDataElementSchema(StructuralDataElement dataElement) throws SchemaGenerationException {

		try {
			//data element should not be null at this point
			if(dataElement == null){
				throw new SchemaGenerationException("The data element for schema generation is null.");
			}
		
			// Try to write form structure into schema
			// This should not fail. If it does throw an internal server error for
			// the web service request.
			return SchemaGenerationService.writeElementSchema(dataElement);
			
			// print the error and display information, but present it to the
			// rest service as something generic
		} catch (ParserConfigurationException | TransformerException | SAXException | IOException e) {
			logger.error("There was an error parsing shortName: " + dataElement.getName() + " version: "
					+ dataElement.getVersion() + " into XSD. The stack will follow.", e);
			throw new SchemaGenerationException("There was an internal error. Please contanct the help desk.");
		}catch (NullPointerException nullDataElement){
			logger.error("There was an error creating a structural data element into schema.", nullDataElement);
			throw new SchemaGenerationException("There was an internal error. Please contanct the help desk.");
	}
	}
	
	public SemanticDataElement getSemanticDataElement(String variableName, String version) {
		// if the version is null get the latest
		if (version == null || version.equals("")) {
			return dataElementSparqlDao.getByName(variableName);
		} else {
			return dataElementSparqlDao.getByNameAndVersion(variableName, version);
		}
	}
		
	public void deleteDataElementSemanticPart(SemanticDataElement sementicDE){
		dataElementSparqlDao.remove(sementicDE);
	}

	public void deleteDataElementStructuralPart(StructuralDataElement structuralDE) {
		structuralDataElementDao.remove(structuralDE.getId());
	}
	
	public SemanticFormStructure getSemanticFormStructure(String variableName, String version) {
		// if the version is null get the latest
		if (version == null || version.equals("")) {
			return formStructureSparqlDao.getLatest(variableName);
		} else {
			return formStructureSparqlDao.get(variableName, version);
		}
	}
	
	public void deleteFormStructureSemanticPart(String variableName, String version){
		formStructureSparqlDao.remove(variableName, version);
	}

	public void deleteFormStructureStructuralPart(StructuralFormStructure structuralFS) {
		formStructureSqlDao.remove(structuralFS.getId());
	}
}