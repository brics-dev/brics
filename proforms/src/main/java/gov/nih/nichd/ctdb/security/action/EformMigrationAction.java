package gov.nih.nichd.ctdb.security.action;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.form.util.EformJsonUtility;
import gov.nih.nichd.ctdb.form.util.EformWsProvider;
import gov.nih.nichd.ctdb.security.common.UserNotFoundException;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.tbi.dictionary.model.hibernate.eform.adapters.EformMigrationAdapter;

public class EformMigrationAction extends BaseAction {
	private static final long serialVersionUID = -2331226895577929320L;
	private static final Logger logger = Logger.getLogger(EformMigrationAction.class);
	
	private String formMigrateJsonArray = "[]";
	private String eFormJson = "{}";
	private int formId = -1;

	/**
	 * Sets up the form migration page. Retrieves basic information for the forms that need
	 * to be migrated. Returns a JSON array of form information to the user's web browser.
	 * 
	 *  @return Struts result value.
	 */
	public String execute() {
		User user = getUser();
		
		// Ensure that the current user is system admin.
		if ( (user == null) || !user.isSysAdmin() ) {
			return CtdbConstants.SECURITY_ACCESS_DENIED;
		}
		
		buildLeftNav(-1, new int[]{3, 8, 11, 15});
		
		FormManager fman = new FormManager();
		
		try {
			JSONArray formArray = fman.getFormsToMigrate();
			
			formMigrateJsonArray = formArray.toString();
			
			// Check for malformed JSON.
			if ( formMigrateJsonArray == null ) {
				String msg = "The form JSON array returned from the DB is malformed.";
				
				logger.error(msg);
				addActionError(msg);
				formMigrateJsonArray = "[]";
				
				return ERROR;
			}
		}
		catch ( CtdbException ce ) {
			String msg = "Database error occurred while getting list of forms to migrate.";
			logger.error(msg, ce);
			addActionError(msg);
			
			return ERROR;
		}
		catch ( JSONException je ) {
			String msg = "An error occurred while translating the DB results to JSON.";
			logger.error(msg, je);
			addActionError(msg);
			
			return ERROR;
		}
		
		return SUCCESS;
	}
	
	/**
	 * First stage of the migration process. With the given ProFoRMS Form ID, the full form
	 * object (with sections and questions) will be taken from the database and translated
	 * into a JSON object suitable for the creation of the associated eForm.
	 * 
	 * @return Struts result status. Success or one of the error statuses.
	 */
	public String formToEformConversion() {
		User user = getUser();
		
		// Ensure that the current user is system admin.
		if ( (user == null) || !user.isSysAdmin() ) {
			addActionError("The user (" + user.getUsername() + ") is not a system admin.");
			return StrutsConstants.FORBIDDEN;
		}
		
		// Validate form ID and version data.
		if ( formId <= 0 ) {
			addActionError("Invalid form ID given.");
			return StrutsConstants.BAD_REQUEST;
		}
		
		FormManager fm = new FormManager();
		
		logger.info("eForm conversion requested for form ID: " + formId + "...");
		
		try {
			EformJsonUtility eFormJsonUtil = new EformJsonUtility();
			Form form = fm.getFormAndSetofQuestions(formId);
			
			logger.info("Converting the \"" + form.getName() + "\" form to an eForm JSON object...");
			
			JSONObject eformJsonObj = eFormJsonUtil.formToEformJson(form);
			
			// Convert JSON object to a string for the response back to the client.
			eFormJson = eformJsonObj.toString();
		}
		catch ( UserNotFoundException unfe ) {
			String msg = "Could not find the form owner in the database";
			logger.error(msg, unfe);
			addActionError(msg);
			
			return ERROR;
		}
		catch ( ObjectNotFoundException onfe ) {
			String msg = "Could not find a form matching the formId: " + formId;
			logger.error(msg, onfe);
			addActionError(msg);
			
			return ERROR;
		}
		catch ( CtdbException ce ) {
			String msg = "Database error occured during the form to eForm JSON conversion.";
			logger.error(msg, ce);
			addActionError(msg);
			
			return ERROR;
		}
		catch ( JSONException je ) {
			String msg = "An error occurred while adding data to the eForm JSON object.";
			logger.error(msg, je);
			addActionError(msg);
			
			return ERROR;
		}
		catch ( Exception e ) {
			String msg = "An error occured while converting a form to an eForm JSON object.";
			logger.error(msg, e);
			addActionError(msg + " " + e.getMessage());
			
			return ERROR;
		}
		
		return SUCCESS;
	}
	
	public String createEForm() {
		User user = getUser();
		
		// Ensure that the current user is system admin.
		if ( (user == null) || !user.isSysAdmin() ) {
			addActionError("The user (" + user.getUsername() + ") is not a system admin.");
			return StrutsConstants.FORBIDDEN;
		}
		
		// Get JSON string from the request body.
		String eformJsonStr = "";
		
		logger.info("Reading the eForm JSON string from the request...");
		
		try {
			BufferedReader br = request.getReader();
			eformJsonStr = IOUtils.toString(br);
			br.close();
			logger.debug("JSON string:\n" + eformJsonStr);
		}
		catch ( IOException ie ) {
			String msg = "Couldn't read the request body.";
			logger.error(msg, ie);
			addActionError(msg);
			
			return ERROR;
		}
		
		// Create the eForm in the Dictionary.
		InputStream respIn = null;
		
		try {
			EformWsProvider provider = new EformWsProvider();
			
			logger.info("Saving the eForm to the Dictionary...");
			
			respIn = provider.saveEFormToDictForMigration(request, eformJsonStr);
		}
		catch ( WebApplicationException wae ) {
			String msg = "Error occured while creating the eForm with the Dictionary web service call.";
			logger.error(msg, wae);
			addActionError(msg);
			
			return ERROR;
		}
		catch ( IOException | RuntimeException e ) {
			String msg = "Couldn't create a proxy ticket for the Dictionary web service call.";
			logger.error(msg, e);
			addActionError(msg);
			
			return ERROR;
		}
		
		// Return the XML file back to the client.
		try {
			HttpServletResponse response = ServletActionContext.getResponse();
			
			logger.info("Sending the new eForm XML data back to be browser...");
			
			// Set Header info.
			response.setHeader("Content-Type", MediaType.APPLICATION_XML + "; charset=UTF-8");
			
			OutputStream out = response.getOutputStream();
			byte[] buffer = new byte[1024];
			
			try {
				int numRead = respIn.read(buffer);
				
				while ( numRead > 0 ) {
					out.write(buffer, 0, numRead);
					out.flush();
					numRead = respIn.read(buffer);
				}
			}
			finally {
				respIn.close();
				out.close();
			}
		}
		catch ( IOException ie ) {
			String msg = "Couldn't create a response with the new eForm XML data.";
			logger.error(msg, ie);
			addActionError(msg);
			
			return ERROR;
		}
		
		return null;
	}
	
	/**
	 * Links the eForm referenced in the request body to the ProFoRMS Form indicated by the
	 * ID stored in the "formId" query parameter.
	 * 
	 * @return Struts result status. Success or one of the error statuses.
	 */
	public String linkEFormToForm() {
		User user = getUser();
		
		// Ensure that the current user is system admin.
		if ( (user == null) || !user.isSysAdmin() ) {
			addActionError("The user (" + user.getUsername() + ") is not a system admin.");
			return StrutsConstants.FORBIDDEN;
		}
		
		try {
			JAXBContext context = JAXBContext.newInstance(EformMigrationAdapter.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			BufferedReader br = request.getReader();
			EformMigrationAdapter ema = (EformMigrationAdapter) unmarshaller.unmarshal(br);
			
			logger.info("Linking the eform (" + ema.getEformShortName() + ") to ProFoRMS form (" + formId + ").");
			
			FormManager fm = new FormManager();
			fm.updateProformsTablesForMigration(formId, ema);
		}
		catch ( JAXBException je ) {
			String msg = "Couldn't unmarshall XML to the adapter class.";
			logger.error(msg, je);
			addActionError(msg);
			
			return ERROR;
		}
		catch ( CtdbException ce ) {
			String msg = "A database error occured while linking an eForm to a ProFoRMS form.";
			logger.error(msg, ce);
			addActionError(msg);
			
			return ERROR;
		}
		catch ( IOException ie ) {
			String msg = "Couldn't read the request body.";
			logger.error(msg, ie);
			addActionError(msg);
			
			return ERROR;
		}
		catch ( Exception e ) {
			String msg = "An error occured while linking an eForm to a ProFoRMS form.";
			logger.error(msg, e);
			addActionError(msg);
			
			return ERROR;
		}
		
		return SUCCESS;
	}
	
	public String testXmlDownload() {
		HttpServletResponse response = ServletActionContext.getResponse();
		
		logger.info("Testing the eForm JSON string from the request...");
		
		// Test for valid JSON object.
		try {
			BufferedReader br = request.getReader();
			String eformJsonStr = IOUtils.toString(br);
			br.close();
			
			JSONObject json = new JSONObject(eformJsonStr);
			logger.info("JSON object transformation test was succuessful for the \"" + json.getString("title") + "\" form.");
		}
		catch ( IOException ie ) {
			String msg = "Couldn't read the request body.";
			logger.error(msg, ie);
			addActionError(msg);
			
			return ERROR;
		}
		catch ( JSONException je ) {
			String msg = "Malformed JSON string.";
			logger.error(msg, je);
			addActionError(msg);
			
			return ERROR;
		}
		
		try {
			// Set Header info.
			response.setHeader("Content-Type", MediaType.APPLICATION_XML + "; charset=UTF-8");
			
			FileInputStream fileIn = new FileInputStream("C:\\brics\\eform.xml");
			OutputStream out = response.getOutputStream();
			byte[] buffer = new byte[1024];
			
			try {
				int numRead = fileIn.read(buffer);
				
				while ( numRead > 0 ) {
					out.write(buffer, 0, numRead);
					out.flush();
					numRead = fileIn.read(buffer);
				}
			}
			finally {
				fileIn.close();
				out.close();
			}
		}
		catch( FileNotFoundException fnfe ) {
			String msg = "Couldn't find the XML file.";
			logger.error(msg, fnfe);
			addActionError(msg);
			
			return ERROR;
		}
		catch ( IOException ie ) {
			String msg = "Error while sending the XML file.";
			logger.error(msg, ie);
			addActionError(msg);
			
			return ERROR;
		}
		
		return null;
	}

	public String getFormMigrateJsonArray() {
		return formMigrateJsonArray;
	}

	public void setFormMigrateJsonArray(String formMigrateJsonArray) {
		this.formMigrateJsonArray = formMigrateJsonArray;
	}

	public String getEFormJson() {
		return eFormJson;
	}

	public void setEFormJson(String eFormJson) {
		this.eFormJson = eFormJson;
	}

	public int getFormId() {
		return formId;
	}

	public void setFormId(int formId) {
		this.formId = formId;
	}
}
