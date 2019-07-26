package gov.nih.tbi.dictionary.portal.eform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jcraft.jsch.JSchException;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;
import gov.nih.tbi.dictionary.portal.BaseEformAction;
import gov.nih.tbi.dictionary.validation.eform.EformPartialValidation;
import gov.nih.tbi.dictionary.validator.eform.EformFieldValidator;
import gov.nih.tbi.dictionary.validators.JAXBValidationEventCollector;
import gov.nih.tbi.portal.PortalUtils;

public class EformImportAction extends BaseEformAction {
	
	private static final long serialVersionUID = 5858137416528195965L;
	private static final Logger logger = Logger.getLogger(EformImportAction.class);
	
	private List<List<String>> importEformErrors;
	private List<List<String>> importEformSuccess;
	private String uploadContentType;
	private File upload;
	
	private boolean reenterShortName;
	
	private String eformShortName;
	
	public String importEformView() throws UserPermissionException {
		if (!getIsDictionaryAdmin()) {
			sessionEform.clear();
			throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
		}
		getSessionEform().clear();
		return PortalConstants.ACTION_IMPORT_EFORM;
	}
	
	public String updateEformShortName() throws HttpException, JSchException, IOException, UserPermissionException {
		String existingEformShortName = null;
		if (!getIsDictionaryAdmin()) {
			sessionEform.clear();
			throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
		}
		Eform eform = getSessionEform().getEform();
		existingEformShortName = eform.getShortName();
		
		
		List<String> shortNameValidatoinErrors = eformManager.validateEformShortName(getEformShortName());
		
		if(shortNameValidatoinErrors.isEmpty()){
			eform.setShortName(getEformShortName());
			saveEform(eform);
		} else {
			eform.setShortName(existingEformShortName);
		shortNameValidatoinErrors.add(0,"There are errors associated with the eform " + eform.getTitle());
		//if the short name is still not unique. add error messaging and do it check again
		setImportEformErrors(shortNameValidatoinErrors);
		getSessionEform().setEform(eform);
		setReenterShortName(true);
		}
		
		return PortalConstants.ACTION_IMPORT_EFORM;
	}
	
	public String importEform() throws MalformedURLException, UnsupportedEncodingException, UserPermissionException {
		if (!getIsDictionaryAdmin()) {
			sessionEform.clear();
			throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
		}

		getSessionEform().clear();
		long startTimeImport = System.currentTimeMillis();
		List<String> importErrors = new ArrayList<String>();

		if (MediaType.TEXT_XML.equals(uploadContentType) || MediaType.APPLICATION_XML.equals(uploadContentType)) {
			try {
				FileInputStream inStream = new FileInputStream(upload);

				try {
					// get node list of eforms
					NodeList eformNodes = buildEformNodeList(inStream);

					// This will notify the use that only the first (limit) will be looked through.
					if (eformNodes.getLength() > ServiceConstants.EFORM_IMPORT_LIMIT) {
						addXmlEformImportMaxError(eformNodes.getLength());
					}

					logger.debug("Processing " + eformNodes.getLength() + " eform(s).");

					// you can only upload one eform at this time. this will allow for more uploads in the future
					for (int i = 0; i < eformNodes.getLength() && i < ServiceConstants.EFORM_IMPORT_LIMIT; i++) {
						logger.debug("Processing eform " + i);

						JAXBValidationEventCollector validationCollector = new JAXBValidationEventCollector();
						Eform unmarshalledEform = unmarshallFormStructureNode(eformNodes.item(i), validationCollector);

						// if there are errors to the structure of the XML than display them to the user and move on
						if (validationCollector.hasEvents()) {
							addSchemaValidationErrorsToErrorList(unmarshalledEform, validationCollector);
							continue;
						}

						EformFieldValidator eformFieldValidation = new EformFieldValidator(unmarshalledEform);

						importErrors.addAll(eformFieldValidation.validateEform());

						FormStructure assocaitedFormStructure =
								dictionaryManager.getLatestDataStructure(unmarshalledEform.getFormStructureShortName());
						EformPartialValidation businessRuleValidation =
								new EformPartialValidation(unmarshalledEform, assocaitedFormStructure);

						importErrors.addAll(businessRuleValidation.eformMinimumValidation());

						boolean isShortNameUnique =
								eformManager.isEformShortNameUnique(unmarshalledEform.getShortName());

						if (importErrors.isEmpty()) {
							if (!isShortNameUnique) {
								importErrors.add(
										"There are errors associated with the eform " + unmarshalledEform.getTitle());
								importErrors
										.add("The short name for the imported eForm already exists in the system.");
								getSessionEform().setEform(unmarshalledEform);
								setReenterShortName(true);
							} else {
								saveEform(unmarshalledEform);
							}
						} else {
							importErrors.add(0,
									"There are errors associated with the eform " + unmarshalledEform.getTitle());
						}
					}
				} finally {
					inStream.close();
				}
			} catch (IllegalArgumentException | JAXBException e) {
				logger.error("The uploaded XML file is malformed or doesn't represent an eForm object.", e);
				importErrors.clear();
				importErrors.add("Invalid XML file. Please inspect the uploaded file and try again.");
			} catch (Exception e) {
				logger.error("There was a critical error processing the eform!", e);
				importErrors.clear();
				importErrors.add("There was a critical error with the XML document. " + e.getMessage());
			}
		} else {
			importErrors.add(
					"Invalid file type. If the file is open in another application please close it and try again.");
		}

		// Check for any critical errors.
		if (!importErrors.isEmpty()) {
			setImportEformErrors(importErrors);
		}

		long endTimeImport = System.currentTimeMillis();
		logger.debug("That took to import an eform " + (endTimeImport - startTimeImport) + " milliseconds");

		return PortalConstants.ACTION_IMPORT_EFORM;
	}
	
	private NodeList buildEformNodeList(FileInputStream inStream)
			throws ParserConfigurationException, SAXException, IllegalArgumentException, IOException {
		// create an instance of Document to iterate through the various
		// data structure nodes
		DocumentBuilderFactory eformDocumentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder eformBuilder = eformDocumentFactory.newDocumentBuilder();
		InputSource is = new InputSource(inStream);
		Document eformDocument = eformBuilder.parse(is);

		// obtain a list of nodes with the tag name dataStructure
		NodeList eFormNodes = eformDocument.getElementsByTagName("Eform");

		if (eFormNodes.getLength() == 0) {
			throw new IllegalArgumentException("The upload XML file does not reference an eForm object.");
		}

		return eFormNodes;
	}
	
	private Eform unmarshallFormStructureNode(Node eformNode, JAXBValidationEventCollector validationCollector) throws JAXBException, SAXException{
		
		// used to validate the data in the XML file and validate against XSD contract
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		URL xsd = EformImportAction.class.getClassLoader().getResource("/EformImport.xsd");
		Schema schema = sf.newSchema(xsd);

		// used to import the data into export POJOs. allows for legacy import
		JAXBContext jaxContext = JAXBContext.newInstance(Eform.class);
		Unmarshaller um = jaxContext.createUnmarshaller();

		// Set validation handler to handle any jaxb exceptions
		um.setSchema(schema);
		um.setEventHandler(validationCollector);

		return (Eform) um.unmarshal(eformNode);
	}
	
	private void addSchemaValidationErrorsToErrorList(Eform importedEform, JAXBValidationEventCollector validationCollector){
		List<String> schemaErrors = new ArrayList<String>();
		schemaErrors.add("Errors were found associated with eform: " + importedEform.getTitle());
		for (ValidationEvent event : validationCollector.getEvents()) {
			schemaErrors.add(event.getMessage());
		}
		setImportEformErrors(schemaErrors);
	}
	
	private void addXmlEformImportMaxError(int eformLength){
		List<String> overLimit = new ArrayList<String>();
		overLimit.add("There are " + eformLength + " eforms in the file. The first eform will be added.");
		setImportEformErrors(overLimit);
	}
	
	private void saveEform(Eform Eform) throws JSchException, HttpException, IOException{
		long startTimeCopy = System.currentTimeMillis();
		Eform eformWithoutSectionAndQuestionIds = eformManager.eformImportCopy(Eform, getAccount());
		long endTimeCopy = System.currentTimeMillis();
		logger.error("That took to copy an eform " + (endTimeCopy - startTimeCopy) + " milliseconds");
		
		// Set presentation tier info for this imported
		long startTimeSave = System.currentTimeMillis();
		eformManager.saveOrUpdateEform(eformWithoutSectionAndQuestionIds);
		long endTimeSave = System.currentTimeMillis();
		logger.error("That took to save an eform " + (endTimeSave - startTimeSave) + " milliseconds");
		
		createNewEformOwner(eformWithoutSectionAndQuestionIds.getId());

		// The form structure was successfully added to the system
		addSuccessImportMessage(eformWithoutSectionAndQuestionIds);
	}
	
	//create owner in entity map
	private void createNewEformOwner(Long eformId) throws HttpException, IOException{
		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
		restProvider.registerEntity(getAccount().getId(), EntityType.EFORM, eformId, PermissionType.OWNER, null);
	}	
	
	private void addSuccessImportMessage(Eform successfulEformImport){
		List<String> successMessages = new ArrayList<String>();
		successMessages.add("The eForm " + successfulEformImport.getTitle() + " has been successfully added to the system.");
		setImportEformSuccess(successMessages);
	}
	
	public List<List<String>> getImportEformErrors() {
		return importEformErrors;
	}

	public void setImportEformErrors(List<String> errorList) {
		if(importEformErrors == null){
			importEformErrors = new ArrayList<List<String>>();
		}
		this.importEformErrors.add(errorList);
	}

	public List<List<String>> getImportEformSuccess() {
		return importEformSuccess;
	}

	public void setImportEformSuccess(List<String> successlist) {
		if(importEformSuccess == null){
			importEformSuccess = new ArrayList<List<String>>();
		}
		this.importEformSuccess.add(successlist);
	}
	
	public void setReenterShortName(boolean reenterShortName){
		this.reenterShortName = reenterShortName;
	}
	
	public boolean getReenterShortName(){
		return this.reenterShortName;
	}
	public File getUpload() {
		return upload;
	}

	public void setUpload(File upload) {
		this.upload = upload;
	}
	
	public String getUploadContentType() {
		return uploadContentType;
	}

	public void setUploadContentType(String uploadContentType) {
		this.uploadContentType = uploadContentType;
	}
	
	public void setEformShortName(String eformShortName){
		this.eformShortName = eformShortName;
	}
	
	public String getEformShortName(){
	    
		return this.eformShortName;
	}
}