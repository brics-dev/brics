package gov.nih.nichd.ctdb.selfreporting.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.util.FormDataStructureUtility;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.selfreporting.form.SelfReportingLandingForm;
import gov.nih.nichd.ctdb.selfreporting.form.SelfReportingProperties;
import gov.nih.nichd.ctdb.selfreporting.manager.SelfReportingManager;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;

public class SelfReportingHomeAction extends BaseAction {

	private static final long serialVersionUID = 3083956466408979259L;

	private static Logger logger = Logger.getLogger(SelfReportingHomeAction.class);

	private String token;
	private List<SelfReportingLandingForm> selfReportingList;
	public static final String PSR_ERROR = "psrError";
	public static final String PSR_HEADER = "psrHeader";
	
	private Date startDate;
	private Date endDate;

	private static final Comparator<SelfReportingLandingForm> selfReportingLandingComparator =
			new Comparator<SelfReportingLandingForm>() {

				@Override
				public int compare(SelfReportingLandingForm form1, SelfReportingLandingForm form2) {

					if (form1 != null && form2 != null && form1.getShortName() != null && form2.getShortName() != null) {
						if(form1.getLastUpdated() != null && form2.getLastUpdated() != null) {
							int compare = form1.getLastUpdated().compareTo(form2.getLastUpdated());
							if(compare == 0) {
								return form1.getShortName().compareToIgnoreCase(form2.getShortName());
							}else {
								return -(form1.getLastUpdated().compareTo(form2.getLastUpdated()));
							}
						}else if(form1.getLastUpdated() != null && form2.getLastUpdated() == null) {
							return -1;
						}else if(form1.getLastUpdated() == null && form2.getLastUpdated() != null) {
							return 1;
						}else {
							return form1.getShortName().compareToIgnoreCase(form2.getShortName());
						}
						

					} else {
						return 0;
					}
				}
			};

	public String execute() {

		if (StringUtils.isBlank(token)) {
			logger.error("Token is empty, please provide a valid token.");
			// error message is static on the error page
			return PSR_ERROR;
		}
		else if (token.equals("sessionExpired")) {
			// clear session
			request.getSession().removeAttribute(CtdbConstants.USER_SESSION_KEY);
			request.getSession().invalidate();
			return "sessionExpired";
		}

		SelfReportingManager srm = new SelfReportingManager();
		ProtocolManager pm = new ProtocolManager();

		try {
			selfReportingList = srm.getSelfReportingList(token);
			
			
			//get eform titles with web service call and set title
			List<String> shortNames = new ArrayList<String>();
			for (SelfReportingLandingForm form : selfReportingList) {
				String shortName = form.getShortName();
				shortNames.add(shortName);
			}
			FormDataStructureUtility fsUtil = new FormDataStructureUtility();
			List<BasicEform> basicEforms = fsUtil.getBasicEforms(request, shortNames);
			
			for(BasicEform beForm : basicEforms) {
				String beShortName = beForm.getShortName();
				String beTitle = beForm.getTitle();
				for (SelfReportingLandingForm form : selfReportingList) {
					String shortName = form.getShortName();
					if(shortName.equalsIgnoreCase(beShortName)) {
						form.setFormTitle(beTitle);
						break;
					}
				}
			}
			
			SelfReportingProperties srp = srm.getSelfReportingDates(token);
			
			// set protocol into session
			int protocolId = srm.getProtocolId(token);
			Protocol protocol = null;
			if (protocolId > 0) {
				protocol = pm.getProtocol(protocolId);
			}

			if (null != protocol.getPsrHeader())
				request.setAttribute(PSR_HEADER, protocol.getPsrHeader());

			session.put(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY, protocol);
			
			Date psrScheduledVisitDate = srm.getScheduledVisitDate(token);
			String psrScheduledVisitDateString = "";
			if(psrScheduledVisitDate != null) {
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				psrScheduledVisitDateString =  dateFormat.format(psrScheduledVisitDate);
				
			}
			session.put(CtdbConstants.PSR_SCHEDULED_VISIT_DATE, psrScheduledVisitDateString);
			
			
			this.setStartDate(srp.getStartDate());
			this.setEndDate(srp.getEndDate());
			
			if (selfReportingList == null || selfReportingList.isEmpty()) {
				logger.error("There are no forms available for this patient.");
				return PSR_ERROR;
			}

		} catch (Exception ce) {
			logger.error("Error occured when fetching self reporting list with token " + token, ce);
			this.addActionError("Error occured when fetching self reporting list with token " +token);
			// error message is static on the error page
			return PSR_ERROR;
		}

		sortSelfReportingList();
		request.getSession().setAttribute(CtdbConstants.USER_SESSION_KEY, true);
		return SUCCESS;
	}

	private void sortSelfReportingList() {

		List<SelfReportingLandingForm> editableList = new ArrayList<SelfReportingLandingForm>();
		List<SelfReportingLandingForm> completeList = new ArrayList<SelfReportingLandingForm>();

		for (SelfReportingLandingForm form : selfReportingList) {
			String status = form.getStatus();
			if (CtdbConstants.DATACOLLECTION_STATUS_COMPLETED.equals(status)
					|| CtdbConstants.DATACOLLECTION_STATUS_LOCKED.equals(status)) {
				completeList.add(form);
			} else {
				editableList.add(form);
			}
		}

		Collections.sort(editableList, selfReportingLandingComparator);
		Collections.sort(completeList, selfReportingLandingComparator);

		selfReportingList.clear();
		selfReportingList.addAll(editableList);
		selfReportingList.addAll(completeList);
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public List<SelfReportingLandingForm> getSelfReportingList() {
		return selfReportingList;
	}


	public Date getStartDate() {
		return startDate;
	}


	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}


	public Date getEndDate() {
		return endDate;
	}


	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
