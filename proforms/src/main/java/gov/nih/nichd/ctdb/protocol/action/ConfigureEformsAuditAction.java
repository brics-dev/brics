package gov.nih.nichd.ctdb.protocol.action;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CasProxyTicketException;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.form.util.FormDataStructureUtility;
import gov.nih.nichd.ctdb.protocol.domain.ConfigureEformAuditDetail;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.protocol.tag.ConfigureEformAuditIdtDecorator;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.response.domain.DataEntryDraft;
import gov.nih.nichd.ctdb.response.tag.ViewEditedAnswerIdtDecorator;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

public class ConfigureEformsAuditAction extends BaseAction {
	private static final Logger logger = Logger.getLogger(ConfigureEformsAuditAction.class);
	private int eformId;
	
	
	
	
	/**
	 * execute method
	 * this will serve up the basic jsp after which datatable makes ajax call to get data for audi
	 */
	public String execute() {

		return BaseAction.SUCCESS;
	}

	
	
	/**
	 * Ajax method to get audit list
	 * @return
	 * @throws Exception
	 */
	public String getConfigureEformAuditList() throws Exception {
			try {
				ArrayList<ConfigureEformAuditDetail> auditDetailList = (ArrayList<ConfigureEformAuditDetail>)getAuditList();
				
				
				IdtInterface idt = new Struts2IdtInterface();

				idt.setList(auditDetailList);
				idt.setTotalRecordCount(auditDetailList.size());
				idt.setFilteredRecordCount(auditDetailList.size());
				idt.decorate(new ConfigureEformAuditIdtDecorator());
				idt.output();
			} catch (InvalidColumnException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		
	/**
	 * Gets the audit list.
	 * Gets the brics eform to get section and queston text
	 * @return
	 */
	private List<ConfigureEformAuditDetail> getAuditList () {
			ProtocolManager pm = new ProtocolManager();
			FormDataStructureUtility fsUtil = new FormDataStructureUtility();
			FormManager formMgr = new FormManager();
			List<ConfigureEformAuditDetail> auditDetailList  = null;
			Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			String eIdStr = request.getParameter("eformId");
			int eId = Integer.parseInt(eIdStr);
			try {
				auditDetailList = pm.getConfigureEformAudit(p.getId(), eId);
				
				String shortName = formMgr.getEFormShortNameByEFormId(eId);
				Form form = fsUtil.getEformFromBrics(request, shortName);
				List<Section> orderedSectionList = form.getOrderedSectionList();	
				for(ConfigureEformAuditDetail auditDetail : auditDetailList)	 {
					int sectionId = auditDetail.getSectionId();
					int questionId = auditDetail.getQuestionId();
					int usrid = auditDetail.getUpdatedBy();
					for(Section s : orderedSectionList) {
						int eformSectionId = s.getId();
						if(sectionId == eformSectionId) {
							String sectionName = s.getName();
							sectionName = sectionName.replaceAll("\\<[^>]*>","");
							auditDetail.setSectionText(sectionName);
							
							if(questionId != -1) {
								List<Question> questionList = s.getQuestionList();
								for(Question q : questionList) {
									int eformQuestionId = q.getId();
									if(questionId == eformQuestionId) {
										String questionText = q.getText();
										questionText = questionText.replaceAll("\\<[^>]*>","");
										auditDetail.setQuestionText(questionText);
										break;
									}
								}
							}
							
							break;
						}
					}
					
				}

				request.setAttribute("auditDetailList", auditDetailList);
			} catch (CtdbException e) {
				logger.error("Error in retrieving config eform audit", e);
				addActionError("Error retrieving configure eform audit");
				//return StrutsConstants.SUCCESS;
			} catch (WebApplicationException e) {
				logger.error("Error in retrieving eform", e);
				addActionError("Error retrieving eform");
				//return StrutsConstants.SUCCESS;
			} catch (CasProxyTicketException e) {
				logger.error("Error in retrieving eform", e);
				addActionError("Error retrieving eform");
			}
			
			return auditDetailList;
			
		}


	public int getEformId() {
		return eformId;
	}




	public void setEformId(int eformId) {
		this.eformId = eformId;
	}
	
	
	
}
