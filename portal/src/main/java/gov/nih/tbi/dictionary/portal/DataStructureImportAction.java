package gov.nih.tbi.dictionary.portal;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletContext;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.model.DataStructureExport;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseStructure;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.formstructure.export.DataElementExport;
import gov.nih.tbi.dictionary.model.hibernate.formstructure.export.FormStructureExport;
import gov.nih.tbi.dictionary.model.hibernate.formstructure.export.MapElementExport;
import gov.nih.tbi.dictionary.utils.FormStructureValidtor;
import gov.nih.tbi.dictionary.validators.JAXBValidationEventCollector;
import gov.nih.tbi.portal.PortalUtils;

public class DataStructureImportAction extends BaseDictionaryAction {
	
	private static final long serialVersionUID = 5858137416528195965L;
	private static final Logger logger = Logger.getLogger(DataStructureImportAction.class);
	
	private List<List<String>> importStructureErrors = new ArrayList<List<String>>();
	private List<List<String>> importStructureSuccess = new ArrayList<List<String>>();
	private String uploadContentType;
	private File upload;

		public String importFormStructure() throws MalformedURLException, UnsupportedEncodingException {

			FileInputStream inStream = null;

			if (ServiceConstants.XML_FILE.equals(uploadContentType)) {

				try {
					// obtain a list of nodes with the tag name dataStructure
					NodeList dataStructureNodes = buildFormStructureNodeList(inStream);

					// This will notify the use that only the first (limit) will be looked through.
					if (dataStructureNodes.getLength() > ServiceConstants.DATA_STRUCTURE_LIMIT) {
						addXmlFormStructureImportMaxError(dataStructureNodes.getLength());
					}
					logger.debug("Processing " + dataStructureNodes.getLength() + " form structures.");

					// loop through the dataStructures
					for (int i = 0; i < dataStructureNodes.getLength() && i < ServiceConstants.DATA_STRUCTURE_LIMIT; i++) {
						logger.debug("Processing form structure " + i);

						JAXBValidationEventCollector validationCollector = new JAXBValidationEventCollector();
						FormStructureExport xmlDataStructure = unmarshallFormStructureNode(dataStructureNodes.item(i), validationCollector);

						//if there are errors to the structure of the XML than display them to the user and move on
						if (validationCollector.hasEvents()) {
							addSchemaValidationErrorsToErrorList(xmlDataStructure,validationCollector);
							continue;
						}

						// Create new data structure from imported xml document
						FormStructure myDataStructure = dictionaryManager.retrieveImportDataStructure(xmlDataStructure, getOrgName());
						
						// This will validate the data structure
						FormStructureValidtor formValidator = new FormStructureValidtor(myDataStructure);
						// clear errors and valid form structures for the new data
						List<String> validationMessages = formValidator.validateDataStructure();
						if(myDataStructure.isCAT()) {
							validationMessages.addAll(formValidator.validateCatQuestionOid(getCatQuestionsOid(myDataStructure.getCatOid())));
						}
						//need a dao call to see if the form structure short name is unique
						if (!dictionaryManager.isImportFSNameUnique(myDataStructure, myDataStructure.getShortName())) {
							validationMessages.add("A form structure with the short name '" + myDataStructure.getShortName() + "' already exists in the system.");
						}
						
						if (!validationMessages.isEmpty()) {
							validationMessages.add(0,"Errors were found associated with form structure: " + myDataStructure.getTitle());
							setImportStructureErrors(validationMessages);
							continue;
						}

						// Set presentation tier info for this imported
						applyImportBusinessRulesForFormStructure(myDataStructure);
						myDataStructure = dictionaryManager.saveFormStructure(myDataStructure);
						createNewFormStructureOwner(myDataStructure.getId());

						// The form structure was successfully added to the system
						addSuccessImportMessage(myDataStructure);
						
					}
					
				} catch (Exception e) {
					List<String> criticalErrors = new ArrayList<String>();
					logger.error("There was a critical error processing a form structure import!",e);
					criticalErrors.add("There was a critical error with the XML document. " + e.getMessage());
					setImportStructureErrors(criticalErrors);
				} finally {
					if (inStream != null)
						try {
							inStream.close();
						} catch (IOException e) {
							logger.error("There was a critical error closing a stream for a form structure import!",e);
						}
				}
			}
			// error thrown if the file being uploaded is not an XML file
			else {
				List<String> incorrectFileType = new ArrayList<String>();
				incorrectFileType.add("Invalid file type. If the file is open in another application please close it and try again.");
				setImportStructureErrors(incorrectFileType);
			}
			return PortalConstants.ACTION_IMPORT_FORM_STRUCTURE;
		}
		
		private NodeList buildFormStructureNodeList(FileInputStream inStream) throws ParserConfigurationException, SAXException, IOException{
			// create an instance of Document to iterate through the various
			// data structure nodes
			DocumentBuilderFactory dataStructureFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dataStructureBuilder = dataStructureFactory.newDocumentBuilder();
			inStream = new FileInputStream(upload);
			InputSource is = new InputSource(inStream);
			Document dataStructureDocument = dataStructureBuilder.parse(is);

			// obtain a list of nodes with the tag name dataStructure
			return dataStructureDocument.getElementsByTagName("dataStructure");
		}
		
		private FormStructureExport unmarshallFormStructureNode(Node formStructureNode, JAXBValidationEventCollector validationCollector) throws JAXBException, SAXException{
			
			// used to validate the data in the XML file and validate against XSD contract
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			URL xsd = DataStructureAction.class.getClassLoader().getResource("/DataStructureImport.xsd");
			Schema schema = sf.newSchema(xsd);

			// used to import the data into export POJOs. allows for legacy import
			JAXBContext jaxContext = JAXBContext.newInstance(DataStructureExport.class, FormStructureExport.class,
				gov.nih.tbi.dictionary.model.hibernate.formstructure.export.RepeatableGroup.class, DiseaseStructure.class, 
				DataElementExport.class, MapElementExport.class);
			Unmarshaller um = jaxContext.createUnmarshaller();

			// Set validation handler to handle any jaxb exceptions
			um.setSchema(schema);
			um.setEventHandler(validationCollector);

			return (FormStructureExport) um.unmarshal(formStructureNode);
		}
		
		private void addXmlFormStructureImportMaxError(int formStructureLength){
			List<String> overLimit = new ArrayList<String>();
			overLimit.add("There are " + formStructureLength + " form structures in the file. The first " + ServiceConstants.DATA_STRUCTURE_LIMIT + " will be added.");
			setImportStructureErrors(overLimit);
		}
		
		private void addSchemaValidationErrorsToErrorList(FormStructureExport xmlDataStructure, JAXBValidationEventCollector validationCollector){
			List<String> schemaErrors = new ArrayList<String>();
			schemaErrors.add("Errors were found associated with form structure: " + xmlDataStructure.getTitle());
			for (ValidationEvent event : validationCollector.getEvents()) {
				String msg = event.getMessage();
				//custom error message for main group if it isn't present. This is for better communication to the user
				if (msg.equalsIgnoreCase("Undefined ID \"Main\".")) {
					schemaErrors.add("A repeatable group named Main is missing from the form structure.");
				} else {
					schemaErrors.add(msg);
				}
			}
			setImportStructureErrors(schemaErrors);
		}
		
		private void applyImportBusinessRulesForFormStructure(FormStructure myDataStructure) throws UnsupportedEncodingException{
			//form structure should be a draft, with a new user and current create date
			myDataStructure.setVersion(PortalConstants.VERSION_NEW);
			myDataStructure.setDateCreated(new Date());
			myDataStructure.setModifiedUserId(getAccount().getUserId());
			myDataStructure.setCreatedBy(getDisplayNameByUsername(getAccount().getUserName()));
			myDataStructure.setPublicationDate(null);
		}
		
		//create owner in entity map
		private void createNewFormStructureOwner(Long formSturctureId) throws HttpException, IOException{
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
			restProvider.registerEntity(getAccount().getId(), EntityType.DATA_STRUCTURE, formSturctureId, PermissionType.OWNER, null);
		}
		
		private void addSuccessImportMessage(FormStructure myDataStructure){
			List<String> successMessages = new ArrayList<String>();
			successMessages.add("The form structure " + myDataStructure.getTitle() + " has been successfully added to the system.");
			successMessages.add(getModulesDDTURL() + "publicData/dataStructureAction!view.action?dataStructureName=" + myDataStructure.getShortName());
			setImportStructureSuccess(successMessages);
		}
		
		public List<List<String>> getImportStructureErrors() {
			return importStructureErrors;
		}

		public void setImportStructureErrors(List<String> list) {
			this.importStructureErrors.add(list);
		}

		public List<List<String>> getImportStructureSuccess() {
			return importStructureSuccess;
		}

		public void setImportStructureSuccess(List<String> list) {
			this.importStructureSuccess.add(list);
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
		
		// added by Ching-heng
		private ArrayList<String> getCatQuestionsOid(String formOID){
			ArrayList<String> questionsOIDList = new ArrayList<String>();
			Properties hmProperties = new Properties();
			ServletContext aContext = getSession().getServletContext();
			InputStream hmS = aContext.getResourceAsStream("healthMeasurement.properties");					
			byte[] postData = "".getBytes();
			String output;
			DataOutputStream writer = null;
			HttpURLConnection connection = null;
			JSONObject formObj = null;
			URL url;
			ByteArrayOutputStream baos = null;
			try {
				hmProperties.load(hmS);
				String apiUrl = hmProperties.getProperty("healthMeasurement.api.url");
				String token = hmProperties.getProperty("healthMeasurement.api.token");
				byte[] encodedBytes = Base64.getEncoder().encode(token.getBytes());
				Charset ascii = Charset.forName("US-ASCII");
				String asciiEncoded = new String(encodedBytes, ascii);			
				url = new URL(apiUrl + formOID + ".json");
				connection = (HttpsURLConnection) url.openConnection();
				connection.setDoOutput(true);
				connection.setRequestMethod("POST");
				connection.addRequestProperty("Authorization", "Basic "+asciiEncoded);
				connection.addRequestProperty("Content-Length", "0");
				writer = new DataOutputStream(connection.getOutputStream());
				writer.write(postData);
				BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
				while ((output = br.readLine()) != null) {
					formObj = new JSONObject(output);
				}
				if(!formObj.has("Error")) {
					// Questions
					JSONArray items = formObj.getJSONArray("Items");
					for(int i = 0; i<items.length(); i++) {
						JSONObject item = items.getJSONObject(i);	
						questionsOIDList.add(item.getString("FormItemOID"));
					}
				} else { //get info from batter form
					String batteryAapiUrl = hmProperties.getProperty("healthMeasurement.api.batteries.url");
					String formApiUrl = hmProperties.getProperty("healthMeasurement.api.url");		
					
					questionsOIDList.addAll(dictionaryManager.getQuestionOIDListForBattery(batteryAapiUrl, formApiUrl, formOID, asciiEncoded));
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return questionsOIDList;
		}
}
