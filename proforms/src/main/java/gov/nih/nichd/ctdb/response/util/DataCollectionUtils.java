package gov.nih.nichd.ctdb.response.util;

import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.EditAssignment;
import gov.nih.nichd.ctdb.response.domain.FormInterval;
import gov.nih.nichd.ctdb.response.domain.Response;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.security.common.UserNotFoundException;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.ws.HashMethods;
/**
 * This is the Utilities class created to simplify data collection code and have several utils method to make code clean and readable
 * @author khanaly
 *
 */
public class DataCollectionUtils {
	private static final Logger logger = Logger.getLogger(DataCollectionUtils.class);
	
	public static boolean isWebServiceUp(HttpServletRequest request, User usr){
		boolean webServiceRunning = true;
		String restfulDomain = SysPropUtil.getProperty("webservice.usermgmt.url");
		SecuritySessionUtil ssu = null;
		String proxyTicket = null;
	
		try{
			 ssu = new SecuritySessionUtil(request);
			 proxyTicket = ssu.getProxyTicket(restfulDomain);
		}catch(UnknownHostException uhe){
			logger.error("Brics account web sercive not reachable UnknownHostException.",uhe);
			webServiceRunning = false;
		}catch(NoRouteToHostException nrthe){
			logger.error("Brics account web sercive not reachable NoRouteToHostException.",nrthe);
			webServiceRunning = false;
		}catch(RuntimeException re){
			webServiceRunning = false;
			logger.error("Brics account web sercive not reachable RuntimeException",re);
		}catch(Exception e){
			webServiceRunning = false;
			logger.error("Brics account web sercive not reachable Exception",e);
		}
		
		return webServiceRunning;
	}
	
	
	
	
	
	/**
	 * 
	 * used for edit form and Audit Comment
	 * 
	 * @param aform
	 */
	public static void completeAform(AdministeredForm aform) {
		Form form = aform.getForm();
		HashMap<String,Question> questionMap = form.getQuestionMap();
	    List<Response> responses = aform.getResponses();

		
	   Set<String> keySet =  questionMap.keySet();
	   Iterator iter = keySet.iterator();	
	   while(iter.hasNext()) {
		   String key = (String)iter.next();
		   Question question =  questionMap.get(key);
		   int questionId = question.getId();
		   int sectionId = question.getSectionId();
		   boolean found = false;
		   for(int i=0;i<responses.size();i++) {
			   Response response = responses.get(i);
			   Question responseQuestion = response.getResponse1().getQuestion();
				int responseQuestionId = responseQuestion.getId();
				int responseSectionId = responseQuestion.getSectionId();
				if(responseQuestionId==questionId && responseSectionId == sectionId) {
					found = true;
					response.setQuestion(question);
					response.getResponse1().setQuestion(question); 
					break;
				}  
		   }
		   if(!found) {
			   //create blank response
			   List answers = new ArrayList<String>();
			   List submitAnswers = new ArrayList<String>();
			   Response response = new Response();
			   response.setId(Integer.MIN_VALUE);
			   response.setQuestion(question);
			   response.setAnswers(answers);
			   response.setSubmitAnswers(submitAnswers);
			   Response response1 = new Response();
			   response1.setId(Integer.MIN_VALUE);
			   response1.setQuestion(question);
			   response1.setAnswers(answers);
			   response1.setSubmitAnswers(submitAnswers);
			   response.setResponse1(response1);
			   responses.add(response);
		   }   
	   }
	   aform.setResponses(responses);
		
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Method to call BRICS account ws to get the Account object for password verification with Proforms User password
	 * @param request
	 * @return either brics and proforms password matches or not(true/false)
	 * @throws CtdbException 
	 */
	public static boolean getBricsAccountInformation(HttpServletRequest request, User usr) throws CtdbException{
		boolean matchPasswordFlag = false;
		String restfulDomain = SysPropUtil.getProperty("webservice.usermgmt.url");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.account.ws.url")+"/" + usr.getBricsUserId();
		SecuritySessionUtil ssu = null;
		String proxyTicket=null;
		try {
			ssu = new SecuritySessionUtil(request);
			proxyTicket = ssu.getProxyTicket(restfulDomain);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoRouteToHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Client client = ClientBuilder.newClient();
		restfulUrl= ssu.compileProxiedWebserviceUrl(restfulUrl, proxyTicket);
		WebTarget wt = client.target(restfulUrl);
		Account bricsAccount = wt.request(MediaType.TEXT_XML).get(Account.class);
		if(HashMethods.convertFromByte(bricsAccount.getPassword()).equals(usr.getPassword())){
			logger.info("Match");
			matchPasswordFlag = true;
		}else{
			logger.info("nonMatch");
			matchPasswordFlag = false;
		}
	
		return matchPasswordFlag;
	}
	
	/**
	 * This is reusable static methods to send email 
	 * @param suject of the email to be sent
	 * @param bodyText of the email to be send
	 * @throws UnknownHostException
	 */
	public static void sendEmailOnDuplicateDataEntry(String suject,String bodyText) throws UnknownHostException{
		logger.info("DataCollectionUtils->sendEmailOnDuplicateDataEntry");
		Properties props = System.getProperties();
        String smtpServer = SysPropUtil.getProperty("mail.smtp.host");
        props.put("mail.smtp.host", smtpServer);
        String port = SysPropUtil.getProperty("mail.smtp.port");
        props.put("mail.smtp.port", port);
        Session session = Session.getDefaultInstance(props, null);
        
        suject += getEnvironmentFlag();
        try { 
            Message emailMessage = new MimeMessage(session);
            emailMessage.setSubject(suject);
            emailMessage.setFrom(new InternetAddress("proforms@datacollection.com"));
            emailMessage.setRecipients(Message.RecipientType.TO,InternetAddress.parse("Yogaraj.Khanal@nih.gov"));
            emailMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse("tgebremichael@sapient.com,rchoudhury@sapient.com,gpopkhadze@sapient.com,abdul.basit@nih.gov,yrkhanal@gmail.com,pandyan@mail.nih.gov,jeng@sapient.com,jenna.linde@nih.gov"));
            emailMessage.setText(bodyText);
            emailMessage.setSentDate(new Date());
			Transport.send(emailMessage);
 
		} catch (MessagingException e) {
			logger.info("DataCollectionUtils->sendEmailOnDuplicateDataEntry->MessagingException");
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (Exception e){
			logger.info("DataCollectionUtils->sendEmailOnDuplicateDataEntry->Exception");
			e.printStackTrace();
		}
	}
	
	/**
	 * This is util method to convert String date in Date format
	 * @param strDate
	 * @return date 
	 * @throws ParseException
	 */
	public static Date convertStringToDate(String strDate,boolean isDateTime) throws ParseException {
		String pattern;
		if(isDateTime) {
			pattern = SysPropUtil.getProperty("default.system.datetimeformat");
		}else {
			pattern = SysPropUtil.getProperty("default.system.dateformat");
		}
		
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date date = formatter.parse(strDate);
		return date;
	}
	
	/**
	 * Method to determine if it is prepopulation question or not
	 * @param af
	 * @return true or false
	 */
	public static boolean prePopulationQuestion(AdministeredForm af){
		boolean prePopulationQuestion = false;
		List resps = af.getResponses();
		Iterator iterR = resps.iterator();
		while(iterR.hasNext()) {
			Response res = (Response)iterR.next();
			Question q = res.getQuestion();
			if(q.getFormQuestionAttributes().isPrepopulation()) {
				prePopulationQuestion = true;
				break;
			}
		}
		
		return prePopulationQuestion;
	}
	
	/**
	 * Method to get Visit Type List for a Study
	 * @param protoMan
	 * @param protocolId
	 * @return
	 * @throws CtdbException
	 */
	public static Map<String, String> getVisitTypeMap(List<Interval> intervals) 
			throws CtdbException{	
		Map<String, String> visitTypes = new LinkedHashMap<String, String>();
		for (Interval p : intervals) {
			visitTypes.put(p.getId() + "", p.getName());
		}
		return visitTypes;
	}

	
	/**
	 * Method to return user based on whether user is available form session or it has to get from database in case of edit
	 * @param request
	 * @param rm
	 * @param sm
	 * @return
	 * @throws UserNotFoundException
	 * @throws CtdbException
	 */
	public static User getUserFromSessionOrEditAssignmentObject(HttpServletRequest request,ResponseManager rm,SecurityManager sm) throws UserNotFoundException, CtdbException{
		User user = null;
		int aformId = Integer.parseInt(request.getParameter(CtdbConstants.AFORM_ID_REQUEST_ATTR));
		if (request.getSession().getAttribute(StrutsConstants.EDITUSER) == null) {
			user = (User) request.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
			request.getSession().removeAttribute(StrutsConstants.EDITUSER);
			return user;
		} else {
			int editUserNum = -1;
			if (request.getParameter(StrutsConstants.EDITUSER) != null) {
				editUserNum = Integer.parseInt(request.getParameter(StrutsConstants.EDITUSER));
			} else {
				editUserNum = Integer.parseInt((String) request.getSession().getAttribute(StrutsConstants.EDITUSER));
			}
			//int editUserNum = Integer.parseInt((String) request.getSession().getAttribute(StrutsConstants.EDITUSER));
			EditAssignment ea = rm.getEditAssignment(aformId,editUserNum);
			int eaId = ea.getCurrentBy();
			user = sm.getUser(eaId);
			request.getSession().setAttribute(StrutsConstants.EDITUSER,String.valueOf(editUserNum));
			return user;
		}
		
	}
	
	
	/**
	 * Will return the next id for given id considering the active form List and
	 * Collection list and skipping the locked list
	 * 
	 * @param fiList
	 * @param currentId
	 * @param aformList
	 * @return
	 */
	public static int returnNextFormId(List<FormInterval> fiList, int currentFormId,List aformList, HttpServletRequest request) {
		logger.info("DataCollectionUtils->returnNextFormId");
		User user = (User) request.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
		// Find the current form index in fiList
		Iterator iter_fi = fiList.iterator();
		int currIndex = 0;
		// get index of current form in interval list
		while (iter_fi.hasNext()) {
			FormInterval fi = (FormInterval) iter_fi.next();
			int iteratorFormId = fi.getId();
			if (currentFormId == iteratorFormId) {
				break;
			}
			currIndex++;
		}
		int nextFormId = -1;
		outerloop: for (int ind = currIndex + 1; ind < fiList.size(); ind++) {
			FormInterval fi = (FormInterval) fiList.get(ind);
			int iteratorFormId = fi.getId();
			boolean found = false;
			Iterator iter = aformList.iterator();
			innerloop: while (iter.hasNext()) {
				AdministeredForm af = (AdministeredForm) iter.next();
				int afFormId = af.getForm().getId();
				if (iteratorFormId == afFormId) {
					if ((af.getDataEntryDraft_LockedDate() != null)|| (!user.isSysAdmin()  && af.getUserOneEntryId() != user.getId())) {
						// it is locked
						found = true;
						break innerloop;

					} else {
						// allNextFoundAndLocked = false;
						nextFormId = iteratorFormId;
						found = true;
						break outerloop;
					}

				} // end if
			} // end innerloop
			if (!found) {
				nextFormId = iteratorFormId;
				break outerloop;
			}
		} // end outerloop

		return nextFormId;

	}

	/**
	 * Will return the next id for given id considering the active form List and
	 * Collection list and skipping the locked list
	 * 
	 * @param fiList
	 * @param currentId
	 * @param aformList
	 * @return
	 */
	public static int returnPreviousFormId(List<FormInterval> fiList,int currentFormId, List aformList, HttpServletRequest request) {
		logger.info("DataCollectionUtils->returnPreviousFormId");
		User user = (User) request.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
		// Find the current form index in fiList
		Iterator iter_fi = fiList.iterator();
		int currIndex = 0;
		while (iter_fi.hasNext()) {
			FormInterval fi = (FormInterval) iter_fi.next();
			int iteratorFormId = fi.getId();
			if (currentFormId == iteratorFormId) {
				break;
			}
			currIndex++;
		}

		int prevFormId = -1;
		outerloop: for (int ind = currIndex - 1; ind >= 0; ind--) {
			FormInterval fi = (FormInterval) fiList.get(ind);
			int iteratorFormId = fi.getId();
			boolean found = false;
			Iterator iter = aformList.iterator();
			innerloop: while (iter.hasNext()) {
				AdministeredForm af = (AdministeredForm) iter.next();
				int afFormId = af.getForm().getId();
				if (iteratorFormId == afFormId) {
					if ((af.getDataEntryDraft_LockedDate() != null)|| (!user.isSysAdmin() && af.getUserOneEntryId() != user.getId())) {
						// it is locked
						found = true;
						break innerloop;
					} else {
						// allNextFoundAndLocked = false;
						prevFormId = iteratorFormId;
						found = true;
						break outerloop;
					}

				} // end if
			} // end innerloop
			if (!found) {
				prevFormId = iteratorFormId;
				break outerloop;
			}
		} // end outerloop

		return prevFormId;
	}
	
	/***
	 * Returns the list of forms in Interval with the right status
	 * 
	 * @param collectedAdminFormsList
	 * @param forms
	 * @return
	 */
	public static List<FormInterval> returnFormsInInterval(List<AdministeredForm> collectedAdminFormsList, List<Form> forms) {
		logger.info("DataCollectionUtils->returnFormsInInterval");
		Iterator iter = forms.iterator();
		Iterator iter2 = null;
		List<FormInterval> formsInInterval = new ArrayList();
		while (iter.hasNext()) {
			Form ft = (Form) iter.next();
			FormInterval fi = new FormInterval();
			fi.setFormId(ft.getId());
			String formNameLeftNav = ft.getName();
			if(formNameLeftNav.length() > CtdbConstants.DATA_COLLECTION_MAX_CHARS_LEFT_NAV_EFORM) {
				formNameLeftNav = formNameLeftNav.substring(0, CtdbConstants.DATA_COLLECTION_MAX_CHARS_LEFT_NAV_EFORM - 1) + "...";
			}
			fi.setFormName(ft.getName());
			fi.setFormNameLink("<a href='javascript:jumpTo(" + ft.getId()+ ")' >" + ft.getName() + "</a>");
			fi.setFormNameLeftNav(formNameLeftNav);
			fi.setFormNameLinkLeftNav("<a href='javascript:jumpTo(" + ft.getId()+ ")' >" + formNameLeftNav + "</a>");
			
			iter2 = collectedAdminFormsList.iterator();
			String status = CtdbConstants.DATACOLLECTION_STATUS_NOTSTARTED;
			while (iter2.hasNext()) {
				AdministeredForm collectedAdminForm = (AdministeredForm) iter2.next();
				Form collectedForm = collectedAdminForm.getForm();
				if (ft.getId() == collectedForm.getId()) {
					fi.setUserId(collectedAdminForm.getUserOneEntryId());
					status = collectedAdminForm.getEntryOneStatus();
					break;
				}
			}
			boolean isRequired = ft.isMandatory();
			String required = CtdbConstants.OPTIONAL;
			if (isRequired) {
				required = CtdbConstants.REQUIRED;
			}
			fi.setRequired(required);
			fi.setDataCollectionStatus(status);
			formsInInterval.add(fi);
		}
		return formsInInterval;
	}
	
	/**
	 * Method to track user session on doing differnet actions in data collection action
	 * @param request
	 * @return user+session for that user
	 */
	public static String getUserIdSessionIdString(HttpServletRequest request) {
		User user = (User) request.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
		return user.getUsername() + ":" + request.getSession().getId() + " ";
	}
	
	public static String getEnvironmentFlag() {
		String envFlag = "";
		String restfulDomain = SysPropUtil.getProperty("webservice.usermgmt.url");
		if(restfulDomain.contains("stage")) {
			envFlag = " - STAGE";
		} else if (restfulDomain.contains("uat")) {
			envFlag = " - UAT";
		} else if (restfulDomain.contains("dev")) {
			envFlag = " - DEV";
		} else if (restfulDomain.contains("demo")) {
			envFlag = " - DEMO";
		} else if (restfulDomain.contains("test")) {
			envFlag = " - TEST";
		} else if (restfulDomain.contains("local")) {
			envFlag = " - LOCAL";
		}
		
		return envFlag;
	}
}
