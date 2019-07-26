package gov.nih.nichd.ctdb.response.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.emailtrigger.domain.SentEmail;
import gov.nih.nichd.ctdb.emailtrigger.manager.EmailTriggerManager;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.form.util.FormDataStructureUtility;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.response.common.ResponseConstants;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.DataEntryDraft;
import gov.nih.nichd.ctdb.response.domain.DataEntryHeader;
import gov.nih.nichd.ctdb.response.domain.EditAnswerDisplay;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.response.tag.ViewEditedAnswerIdtDecorator;
import gov.nih.nichd.ctdb.response.util.MetaDataHistory;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;


/**
 * ViewEditAnswerAction is the Struts action class for viewing the history
 * of editing answers.
 * This class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ViewEditedAnswerAction extends BaseAction {
	private static final Logger logger = Logger.getLogger(ViewEditedAnswerAction.class);
	private static final long serialVersionUID = 7554230083746528112L;
	
	private List<EditAnswerDisplay> editArchives;
	private List<SentEmail> sentEmails;
	private List<AdministeredForm> finalLockList;
	private List<DataEntryDraft> dataEntryList;

	
	
	public String execute() throws Exception {
		
		getListEditedAnswers();
		
        Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);

        String formIdStr = request.getParameter(CtdbConstants.ID_REQUEST_ATTR);
	        try {
	            if (Utils.isBlank(formIdStr)) {
	                throw new Exception("Invalid Id passed to View Audit Action");
	            } else {
	                ResponseManager rm = new ResponseManager();
	                PatientManager pm = new PatientManager();
	                ProtocolManager prm = new ProtocolManager();
	                FormManager fm = new FormManager();

	                int aformId = Integer.parseInt(formIdStr);
	                
	                AdministeredForm admForm = rm.getAdministeredFormForViewAudit(aformId);
	                
	                Patient pt = rm.getPatientForCollection(protocol.getId(), admForm.getPatient().getId());
	                Interval interval = null;
	                if (admForm.getInterval().getId() > 0) {
	                	interval = prm.getInterval(admForm.getInterval().getId());
	                }

	                boolean finalLocked = false;	                
	                
	                String shortName = fm.getEFormShortNameByAFormId(aformId);
	                FormDataStructureUtility fsUtil = new FormDataStructureUtility();
	                Form form = null;
	                try {
	                	form = fsUtil.getEformFromBrics(request, shortName);
	                	
	                	int eformid = admForm.getEformid();

	        			form.setId(eformid);
	        			form.setProtocolId(protocol.getId());
	        			admForm.setForm(form);
	                	
		                if (admForm.getFinalLockDate() != null) {
		                    finalLocked = true;
		                    
		                    AdministeredForm finalLock = rm.getFinalLockedInfo(aformId);

		                } 
	                }
	                catch(Exception e) {
	                	// getting 401: unauthorized
	                	e.printStackTrace();
	                }
	                
	                List<MetaDataHistory> metaDataList = rm.getMetaDataHistory(aformId, finalLocked);
	                if (metaDataList != null && !metaDataList.isEmpty()) {
	                    if (protocol.isUsePatientName()) {
	                    	
	                        for (MetaDataHistory mdh : metaDataList) {
	                            if (mdh.getColname().equals("Patient ID")) {
	                                String valueBefore = mdh.getColvaluebefore();
	                                Patient patient = null;
	                                
	                                //Patient ID is used when changing the patient and need to change to Name when displaying
	                                if (valueBefore.indexOf(',') == -1) {
	                                    mdh.setColname("Patient Name");
	                                    patient = pm.getPatientByNIHNumForAuditLog(mdh.getColvaluebefore().trim());
	                                    mdh.setColvaluebefore(patient.getLastName() + ", " + patient.getFirstName());
	                                    patient = pm.getPatientByNIHNumForAuditLog(mdh.getColvalueafter().trim());
	                                    mdh.setColvalueafter(patient.getLastName() + ", " + patient.getFirstName());
	                                }
	                            }
	                        }
	                        
	                    } else { // Protocol is using Patient ID
	                        for (MetaDataHistory mdh : metaDataList) {
	                            if (mdh.getColname().equals("Patient Name")) {
	                                String valueBefore = mdh.getColvaluebefore();
	                                Patient patient = null;
	                                
	                                //Patient Name is used when changing the patient and need to change to ID when display
	                                if (valueBefore.indexOf(',') != -1) {
	                                    mdh.setColname("Patient ID");
	                                    patient = pm.getPatientByNameForAuditLog(mdh.getColvaluebefore().trim());
	                                    mdh.setColvaluebefore(patient.getSubjectId());
	                                    patient = pm.getPatientByNameForAuditLog(mdh.getColvalueafter().trim());
	                                    mdh.setColvalueafter(patient.getSubjectId());
	                                }
	                            }
	                        }
	                    }
	                    
	                    List<MetaDataHistory> intervals = new ArrayList<MetaDataHistory>();
	                    List<MetaDataHistory> patients = new ArrayList<MetaDataHistory>();
	                    List<MetaDataHistory> visitDates = new ArrayList<MetaDataHistory>();
	                    
	                    for (MetaDataHistory metaData : metaDataList) {
	                        if (metaData.getColname().equalsIgnoreCase("Interval Name")) {
	                            intervals.add(metaData);
	                        } else if (metaData.getColname().trim().equals("Visit Date")) {
	                            visitDates.add(metaData);
	                        } else {
	                            patients.add(metaData);
	                        }
	                    }
	                    request.setAttribute("intervals", intervals);
	                    request.setAttribute("patients", patients);
	                    request.setAttribute("visitDates", visitDates);

	                } else {
	                    request.setAttribute("intervals", new ArrayList());
	                    request.setAttribute("patients", new ArrayList());
	                }
	            
	                DataEntryHeader dataEntryHeader = new DataEntryHeader();

	                dataEntryHeader.setFormDisplay(form.getName());
	                logger.info("------------------------>"+form.getName());

	                dataEntryHeader.setDateDisplay(admForm.getVisitDateStringyyyyMMddHHmm());
	                logger.info("------------------------>"+admForm.getVisitDateStringyyyyMMddHHmm());
	                
	                dataEntryHeader.setScheduledVisitDateDisplay(admForm.getScheduledVisitDateStringyyyyMMddHHmm());
	                logger.info("------------------------>"+admForm.getScheduledVisitDateStringyyyyMMddHHmm());

	                if (interval == null) {
	                	dataEntryHeader.setIntervalDisplay("Other");
	                } else {
	                    dataEntryHeader.setIntervalDisplay(interval.getName());
	                }
	                    
	                // get the right current user(s)
	                String username1 = rm.getDataEntryUserName(admForm.getId(), 1);
	                dataEntryHeader.setEntry(username1);
	                
	                dataEntryHeader.setStudyName(protocol.getName());
	                dataEntryHeader.setStudyNum(protocol.getProtocolNumber());
	                request.setAttribute("comingFromViewAudit", "true");
	                    
	                dataEntryHeader.setPatientDisplay(pt.getDisplayLabel(
	                		protocol.getPatientDisplayType(), protocol.getId()));
	                dataEntryHeader.setGuid(pt.getGuid());
	                dataEntryHeader.setSingleDoubleKeyFlag(CtdbConstants.SINGLE_ENTRY_FORM);

	                session.put(ResponseConstants.DATAENTRYHEADER_SESSION_KEY, dataEntryHeader);
	            }
	        } catch (CtdbException ce) {
	            return StrutsConstants.FAILURE;
	        }		
		
        return SUCCESS;
    }
	
	public String getListEditedAnswers() throws Exception  {
		
        Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);

        String formIdStr = request.getParameter(CtdbConstants.ID_REQUEST_ATTR);

        ResponseManager rm = new ResponseManager();
        PatientManager pm = new PatientManager();
        ProtocolManager prm = new ProtocolManager();
        FormManager fm = new FormManager();

        int aformId = Integer.parseInt(formIdStr);
        
        AdministeredForm admForm = rm.getAdministeredFormForViewAudit(aformId);
        
        Patient pt = rm.getPatientForCollection(protocol.getId(), admForm.getPatient().getId());
        Interval interval = null;
        if (admForm.getInterval().getId() > 0) {
        	interval = prm.getInterval(admForm.getInterval().getId());
        }

        dataEntryList = rm.getDataEntries(aformId, 1);

        boolean finalLocked = false;
        finalLockList = new ArrayList<AdministeredForm>();

        String shortName = fm.getEFormShortNameByAFormId(aformId);
        FormDataStructureUtility fsUtil = new FormDataStructureUtility();
        Form form = null;
        try {
        	form = fsUtil.getEformFromBrics(request, shortName);
        	
        	int eformid = admForm.getEformid();

			form.setId(eformid);
			form.setProtocolId(protocol.getId());
			admForm.setForm(form);
        	
            if (admForm.getFinalLockDate() != null) {
                finalLocked = true;
                
                AdministeredForm finalLock = rm.getFinalLockedInfo(aformId);
                finalLockList.add(finalLock);	                 
				editArchives = rm.getEditArchives(form, aformId, true);

            } else {
				editArchives = rm.getEditArchives(form, aformId, false);
            	//request.setAttribute("responseList", new ArrayList());
            }
        }
        catch(Exception e) {
        	// getting 401: unauthorized
        	e.printStackTrace();
        }

        // getSentEmail records
        EmailTriggerManager em = new EmailTriggerManager();
        sentEmails = em.getSentEmailAudit(admForm);


        return null;
	}	
	
	
	// url: http://fitbir-portal-local.cit.nih.gov:8082/ibis/response/getDataEntryList.action
	public String getDataEntryList() throws Exception {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			getListEditedAnswers();
			ArrayList<DataEntryDraft> outputList = new ArrayList<DataEntryDraft>(getDataEntryDraft());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new ViewEditedAnswerIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// url: http://fitbir-portal-local.cit.nih.gov:8082/ibis/response/getFinalLockedList.action
	public String getFinalLockedList() throws Exception {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			getListEditedAnswers();
			ArrayList<AdministeredForm> outputList = new ArrayList<AdministeredForm>(getFinalLockList());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new ViewEditedAnswerIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// url: http://fitbir-portal-local.cit.nih.gov:8082/ibis/response/getEditArchivesList.action
	public String getEditArchivesList() throws Exception {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			getListEditedAnswers();
			ArrayList<EditAnswerDisplay> outputList = new ArrayList<EditAnswerDisplay>(getEditArchives());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new ViewEditedAnswerIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// url: http://fitbir-portal-local.cit.nih.gov:8082/ibis/response/getSentEmailsList.action
	public String getSentEmailsList() throws Exception {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			getListEditedAnswers();
			ArrayList outputList = new ArrayList(getSentEmails());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new ViewEditedAnswerIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			e.printStackTrace();
		}
		return null;
	}	

	
	public void setEditArchives(List<EditAnswerDisplay> editArchives) {
		this.editArchives = editArchives;
	}
	
	public List<EditAnswerDisplay> getEditArchives() {
		return editArchives;
	}
	
	public void setFinalLockList(List<AdministeredForm> finalLockList) {
		this.finalLockList = finalLockList;
	}
	
	public List<AdministeredForm> getFinalLockList() {
		return finalLockList;
	}
	
	public void setDataEntryDraft(List<DataEntryDraft> dataEntryList) {
		this.dataEntryList = dataEntryList;
	}
	
	public List<DataEntryDraft> getDataEntryDraft() {
		return dataEntryList;
	}
	
	public void setSentEmails(List<SentEmail> sentEmails) {
		this.sentEmails = sentEmails;
	}
	
	public List<SentEmail> getSentEmails() {
		return sentEmails;
	}

	public String getInitialDataEntryList() throws Exception {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			getInitialListEditedAnswers();
			ArrayList<EditAnswerDisplay> outputList = new ArrayList<EditAnswerDisplay>(getEditArchives());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new ViewEditedAnswerIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("Failed to get the initial data entry list.", e);
		}

		return null;
	}

	public String getInitialListEditedAnswers() throws Exception {
		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		String formIdStr = request.getParameter(CtdbConstants.ID_REQUEST_ATTR);

		try {
			ResponseManager rm = new ResponseManager();
			FormManager fm = new FormManager();

			int aformId = Integer.parseInt(formIdStr);

			AdministeredForm admForm = rm.getAdministeredFormForViewAudit(aformId);

			dataEntryList = rm.getDataEntries(aformId, 1);

			String shortName = fm.getEFormShortNameByAFormId(aformId);
			FormDataStructureUtility fsUtil = new FormDataStructureUtility();

			Form form = fsUtil.getEformFromBrics(request, shortName);
			form.setId(admForm.getEformid());
			form.setProtocolId(protocol.getId());
			admForm.setForm(form);

			editArchives = rm.getInitialDataEntries(aformId, admForm);

		} catch (Exception e) {
			logger.error("Failed to get initial list of edited answers.", e);
		}

		return BaseAction.SUCCESS;
	}
}
