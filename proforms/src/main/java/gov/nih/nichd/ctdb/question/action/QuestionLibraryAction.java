package gov.nih.nichd.ctdb.question.action;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.domain.VisualScale;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionLibraryAction extends BaseAction
{
	private static final long serialVersionUID = 745740987402021854L;
	private static final Logger logger = Logger.getLogger(QuestionLibraryAction.class);
	
	private String jsonString = "[]";
	
	/**
	 * Gets a JSON string representation of the questions to be displayed in the
	 * library table. This method will now return a traditional Struts 2 result indicator. 
	 * Instead it will write the JSON string directly to the response.
	 * 
	 * @return 
	 * @throws IOException When there are errors with the response object.
	 */
	public String getQuestionLibrary() throws IOException
	{
		String strutsResult = BaseAction.SUCCESS;
		
		logger.info("Getting question library...");
		
		try
		{
			List<Long> excludeIds = new ArrayList<Long>();
			
			// Get the IDs to exclude from the query parameter
			if ( (request.getParameter("qIds") != null) && !request.getParameter("qIds").isEmpty() )
			{
				for ( String id : Arrays.asList(request.getParameter("qIds").split(",")) )
				{
					excludeIds.add(Long.valueOf(id.trim()));
				}
			}
			
			QuestionManager qm = new QuestionManager();
			jsonString = qm.getQuestionLibraryAsJSON(excludeIds);
			logger.info("Sending question library...");
		}
		catch ( NumberFormatException nfe )
		{
			logger.error("One of the excluded IDs is not a number.", nfe);
			strutsResult = "badRequest";
		}
		catch ( CtdbException ce )
		{
			logger.error("Couldn't get a connection to the database.", ce);
			strutsResult = BaseAction.ERROR;
		}
		catch ( SQLException sqle )
		{
			logger.error("Couldn't get a list of questions from the database.", sqle);
			strutsResult = BaseAction.ERROR;
		}
		
		return strutsResult;
	}
	
	/**
	 * This method gets the questionid based on question name
	 * 
	 * question name is in syntax:   formid_dataElementName
	 * 
	 * 
	 */
	public String getQuestionIdFromQuestionName() throws IOException
	{
		String strutsResult = BaseAction.SUCCESS;
		
		// Check if the query parameter was set
		if (request.getParameter("qName") == null)
		{
			logger.error("The question name was not sent in the request.");
			return BaseAction.ERROR;
		}
		
		logger.info("Getting question details from database...");
		
		try
		{
			QuestionManager qm = new QuestionManager();
			String questionName = request.getParameter("qName");
			int questionId = qm.getQuestionId(questionName);
			


			
			// Build the JSON question object
			JSONObject qj = new JSONObject();
			qj.put("questionId", questionId);

			// Set JSON string
			jsonString = qj.toString();
			logger.info("Sending question details as JSON => " + jsonString);
			
			
			
		}catch ( NumberFormatException nfe )
		{
			logger.error("The question ID or version sent with the request is invalid.", nfe);
			strutsResult = BaseAction.ERROR;
		}
		catch ( CtdbException e )
		{
			logger.error("A database error occurred while retrieving the question (" + request.getParameter("qId") + ").", e);
			strutsResult = BaseAction.ERROR;
		}
		catch ( JSONException je )
		{
			logger.error("Could not convert the question to a JSON object.", je);
			strutsResult = BaseAction.ERROR;
		}
		
		return strutsResult;
		
		
	}
	
	public String getQuestionFromLibrary() throws IOException
	{
		String strutsResult = BaseAction.SUCCESS;
		
		// Check if the query parameter was set
		if ( (request.getParameter("qId") == null) || (request.getParameter("qVersion") == null) )
		{
			logger.error("The question ID or version was not sent in the request.");
			return BaseAction.ERROR;
		}
		
		logger.info("Getting question details from library...");
		
		try
		{
			QuestionManager qm = new QuestionManager();
			int questionId = Integer.parseInt(request.getParameter("qId"));
			int questionVersion = Integer.parseInt(request.getParameter("qVersion"));
			Question q = qm.getQuestion(questionId, questionVersion);
			
			//filter   out Other please specify if include Other is true
			if(q.isIncludeOtherOption()) {
				List<Answer> answers = q.getAnswers();
				Iterator iter = answers.iterator();
				while(iter.hasNext()) {
					Answer a = (Answer)iter.next();
					if(a.getDisplay().equals(CtdbConstants.OTHER_OPTION_DISPLAY)) {
						iter.remove();
					}
				}
			}
			
			
			
			// Build the JSON question object
			JSONObject qj = new JSONObject();
			qj.put("questionId", q.getId());
			qj.put("questionVersionLetter", q.getVersion().toString());
			qj.put("questionVersionNumber", q.getVersion().getVersionNumber());
			qj.put("questionName", q.getName());
			qj.put("questionText", q.getText());
			qj.put("descriptionUp", q.getDescriptionUp());
			qj.put("descriptionDown", q.getDescriptionDown());
			qj.put("questionType", Integer.toString(q.getType().getValue())); 
			qj.put("questionOptions", convertQuestionOptionsToString(qm, questionId, questionVersion));
			qj.put("imageOption", convertImageOptionsToString(qm, q));
			
			//// Check if the image file name is needed
			if ( q.getType().getValue() == 9 )
			{
				qj.put("imageFileName", qm.getImageMapFileNameByquestionId(questionId, questionVersion));
			}
			
			qj.put("visualScaleInfo", qm.getVisualScaleInfo(questionId, questionVersion));
			qj.put("graphicNames", qm.getQuestionImages(questionId));
			qj.put("defaultValue", q.getDefaultValue());
			qj.put("unansweredValue", q.getUnansweredValue());
			qj.put("associatedGroupIds", qm.getAssociatedGroupIds(questionId));
			qj.put("includeOther", q.isIncludeOtherOption());
			qj.put("hasDecimalPrecision", q.getHasDecimalPrecision());
			qj.put("hasCalDependent", q.getHasCalDependent());
			qj.put("prepopulation", q.getPrepopulation());
			
			Set<Integer> attachedFormIds = qm.getAttachedFormIds(questionId, questionVersion);
        	Integer formId = (Integer) session.get(FormConstants.FORMID);
        	
        	if ( formId != null )
        	{
        		attachedFormIds.remove(formId); // don't include the current form Id.
        	}
			
        	qj.put("attachedFormIds", attachedFormIds);
			qj.put("attachedFormNames", qm.getCalDependentFormNames(questionId));
			
			qj.put("hasSavedInDatabase", true);
			// Set JSON string
			jsonString = qj.toString();
			logger.info("Sending question details as JSON => " + jsonString);
		}
		catch ( NumberFormatException nfe )
		{
			logger.error("The question ID or version sent with the request is invalid.", nfe);
			strutsResult = BaseAction.ERROR;
		}
		catch ( ObjectNotFoundException onfe )
		{
			logger.error("The question with ID => " + request.getParameter("qId") + " could not be found in the database", onfe);
			strutsResult = BaseAction.ERROR;
		}
		catch ( CtdbException e )
		{
			logger.error("A database error occurred while retrieving the question (" + request.getParameter("qId") + ").", e);
			strutsResult = BaseAction.ERROR;
		}
		catch (SQLException e)
		{
			logger.error("A database error occurred while retrieving the question (" + request.getParameter("qId") + ").", e);
			strutsResult = BaseAction.ERROR;
		}
		catch ( JSONException je )
		{
			logger.error("Could not convert the question to a JSON object.", je);
			strutsResult = BaseAction.ERROR;
		}
		
		return strutsResult;
	}
	
	/**
	 * Converts the question options to a JSON array.
	 * 
	 * @param qm - The question manager object used to query the database.
	 * @param questionId - The ID of the current question.
	 * @param questionVersion - The version of the current question.
	 * @return	A JSON array containing any options associated with the current question.
	 * @throws CtdbException	If any database errors occurs.
	 * @throws JSONException	If there was an error with creating the JSON array.
	 */
	@SuppressWarnings("unused")
	private JSONArray convertQuestionOptionsToJSON(QuestionManager qm, int questionId, int questionVersion) throws CtdbException, JSONException
	{
		JSONArray answerOptions = new JSONArray();
		
		for ( Answer a : qm.getAnswers(questionId, questionVersion) )
		{
			if ( (a.getDisplay() != null) && !a.getDisplay().equals(CtdbConstants.OTHER_OPTION_DISPLAY) )
			{
				JSONObject opt = new JSONObject();
				opt.put("option", a.getDisplay());
				opt.put("score", a.getScore());
				
				if ( a.getSubmittedValue() != null )
				{
					opt.put("submittedValue", a.getSubmittedValue());
				}
				else
				{
					opt.put("submittedValue", "");
				}
				
				answerOptions.put(opt);
			}
		}
		
		return answerOptions;
	}
	
	// TODO - Remove this abominable method when the alien symbol is no longer used.
	private String convertQuestionOptionsToString(QuestionManager qm, int questionId, int questionVersion) throws CtdbException
	{
		StringBuffer options = new StringBuffer();
		
		for ( Iterator<Answer> it = qm.getAnswers(questionId, questionVersion).iterator(); it.hasNext(); )
		{
				Answer a = it.next();
				String score = "";
				String submittedValue = "";
				
				if (a.getScore() != Integer.MIN_VALUE)
				{
					score = String.valueOf(a.getScore());
				}
				
				if ( a.getSubmittedValue() != null )
				{
					submittedValue = a.getSubmittedValue();
					logger.info("Adding the following question option: (" + a.getDisplay() + ", " + submittedValue + ")");
				}
				
				if ( it.hasNext() )
				{
					options.append(a.getDisplay() + "|" + score + "|" + submittedValue + StrutsConstants.alienSymbol);
				}
				else
				{
					options.append(a.getDisplay() + "|" + score + "|" + submittedValue);
				}
		}
		
		logger.info("Sending the following question options: \"" + options.toString() + "\"");
		//logger.info(StrutsConstants.alienSymbol + " has " + StrutsConstants.alienSymbol.getBytes().length + " characters.");
		
		return options.toString();
	}
	
	/**
	 * Converts the image options to a JSON array.
	 * 
	 * @param qm - The question manager object used to query the database. 
	 * @param q - The current question.
	 * @return	A JSON array containing the image options for the current question, or null if the 
	 * current question is not an image map type question.
	 * @throws CtdbException	If a database error occurs.
	 * @throws SQLException		If a database error occurs.
	 */
	@SuppressWarnings("unused")
	private JSONArray convertImageOptionsToJSON(QuestionManager qm, Question q) throws CtdbException, SQLException
	{
		JSONArray imageOptions = null;
		
		if ( q.getType().getValue() == 9 )
		{
			imageOptions = new JSONArray();
			
			for ( String imageOpt : qm.getImageMapOptions(q.getId(), q.getVersion().getVersionNumber()) )
			{
				imageOptions.put(imageOpt);
			}
		}
		
		return imageOptions;
	}
	
	// TODO - Remove this abominable method when the alien symbol is no longer used.
	private String convertImageOptionsToString(QuestionManager qm, Question q) throws CtdbException, SQLException
	{
		String imageOptions = "";
		
		if ( q.getType().getValue() == 9 )
		{
			List<String> imageOpts = qm.getImageMapOptions(q.getId(), q.getVersion().getVersionNumber());
			
			for ( Iterator<String> it = imageOpts.iterator(); it.hasNext(); )
			{
				String imageOpt = it.next();
				
				imageOptions += imageOpt.trim();
				
				if ( it.hasNext() )
				{
					imageOptions += StrutsConstants.alienSymbol;
				}
			}
		}
		
		return imageOptions;
	}
	
	/**
	 * Converts the visual scale object that may be associated with the current question 
	 * to a JSON object.
	 * 
	 * @param qm - The question manager object used to query the database.
	 * @param q - The current question.
	 * @return	The JSON version of the associated visual scale, or null if the current 
	 * question is not a visual scale type question.
	 * @throws CtdbException	If a database error occurs.
	 * @throws SQLException		If a database error occurs.
	 * @throws JSONException	When there is an issue with creating the JSON object.
	 */
	@SuppressWarnings("unused")
	private JSONObject convertVisualScaleToJSON(QuestionManager qm, Question q) throws CtdbException, SQLException, JSONException
	{
		JSONObject json = null;
		
		if ( q.getType().getValue() == 10 )
		{
			VisualScale vs = qm.getVisualScale(q.getId(), q.getVersion().getVersionNumber());
			json = new JSONObject();
			json.put("rangeStart", vs.getRangeStart());
			json.put("rangeEnd", vs.getRangeEnd());
			json.put("width", vs.getWidth());
			json.put("rightText", vs.getRightText());
			json.put("leftText", vs.getLeftText());
			json.put("centerText", vs.getCenterText());
			json.put("showHandle", vs.isShowHandle());
		}
		
		return json;
	}
	
	/**
	 * Converts the string version of the question types to the ones used in the form builder.
	 * 
	 * @param qt - The type object of the current question.
	 * @return	The string representation of the question type used in the form builder.
	 * @throws NoSuchFieldException	If an invalid question type ID is encountered.
	 */
	private String convertQuestionType(QuestionType qt) throws NoSuchFieldException
	{
		String questionType = "";
		
		switch (qt.getValue())
		{
			case 1 : questionType = "textbox";
					 break;
			case 2 : questionType = "textarea";
					 break;
			case 3 : questionType = "select";
					 break;
			case 4 : questionType = "radio";
					 break;
			case 5 : questionType = "multiSelect";
					 break;
			case 6 : questionType = "checkbox";
					 break;
			case 7 : questionType = "calculated";
					 break;
			case 8 : questionType = "patientCalendar";
					 break;
			case 9 : questionType = "imageMap";
					 break;
			case 10 : questionType = "visualscale";
					  break;
			case 11 : questionType = "fileUpload";
					  break;
			case 12 : questionType = "textblock";
					  break;
			default : throw new NoSuchFieldException("The question type " + qt.getDispValue() + " is not valid.");
		}
		
		return questionType;
	}

	/**
	 * @return the jsonString
	 */
	public String getJsonString() {
		return jsonString;
	}

	/**
	 * @param jsonString the jsonString to set
	 */
	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}
}
