package gov.nih.nichd.ctdb.form.action;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.form.common.FormResultControl;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.response.common.ResponseConstants;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.LookupResultControl;
import gov.nih.nichd.ctdb.util.domain.LookupType;
import gov.nih.nichd.ctdb.util.manager.LookupManager;

public class FormCopyHomeAction extends BaseAction {

	private static final long serialVersionUID = -2911215155116567059L;

    private String name = null;
    private String status = null;
    private String issued;
    private String updatedDate = null;
    private String sortBy = FormResultControl.SORT_BY_LASTUPDATED;
    private String sortedBy = FormResultControl.SORT_BY_LASTUPDATED;
    private String sortOrder = FormResultControl.SORT_DESC;
    private String clicked = "initial";
    private String protocolName = null;
    private String numResults = null;
    private String numResultsPerPage = null;
    private String searchFormType;
    private String existType = null;
    private String searchSubmitted = "NO";

	public String execute() throws Exception {
        buildLeftNav(LeftNavController.LEFTNAV_FORM_COPY);

        Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
        if (p == null) {
        	return StrutsConstants.SELECTPROTOCOL;
        }
        
        try {
            FormManager fm = new FormManager();
            session.remove(ResponseConstants.FORM_SESSION_KEY);

            //this.retrieveActionMessages(ImportFormAction.ACTION_MESSAGES_KEY);
            this.retrieveActionMessages(FormAction.ACTION_MESSAGES_KEY);
            this.retrieveActionMessages(FormBuildAction.ACTION_MESSAGES_KEY);
       
            @SuppressWarnings("unchecked")
			List<CtdbLookup> formStatus = (List<CtdbLookup>)session.get(FormConstants.FORMSEARCHSTATUS);
            if (formStatus == null) {
                LookupManager lookUp = new LookupManager();

                formStatus = lookUp.getLookups(LookupType.FORM_STATUS, new LookupResultControl());
                //This is for all status value.
                CtdbLookup all = new CtdbLookup(0, "All");
                formStatus.add(0, all);
                session.put(FormConstants.FORMSEARCHSTATUS, formStatus);
            }

            if (getUpdatedDate() != null) {
                String dateString = getUpdatedDate().trim();
                if (dateString.length() != 0) {
                    SimpleDateFormat parser = new SimpleDateFormat("yyy-MM-DD");
                    Date date = parser.parse(dateString, new ParsePosition(0));
                    Date today = new Date();

                    if (date.after(today)) {
                    	addActionError("Please select either today's date or a date in the past");
                        return StrutsConstants.EXCEPTION;
                    }
                }
            }
            
            String existType = request.getParameter("existType");
            if ("YES".equals(searchSubmitted) || (existType != null && existType.equals("Mine"))) {
            	setSearchSubmitted("");
            } else {
            	setSearchSubmitted("NO");
            }
            	
            FormResultControl frc = new FormResultControl();
            updateResultControl(frc);
            frc.setSortBy(getSortBy());
            frc.setSortOrder(getSortOrder());
            frc.setProtocolName(getProtocolName());

            if (searchFormType == null) {
            	searchFormType = "0"; //subject form
            }
            
            if (searchFormType.equals("0")) {  //Subject Forms
            	frc.setFormType(FormResultControl.PatientFormType);
            } else if (searchFormType.equals("1")) {  //Non-Subject Forms
            	frc.setFormType(FormResultControl.NonPatientFormType);
            } else if (searchFormType.equals("2")) {
            	frc.setFormType("all");//
            }
                
			User user = getUser();
            List formList = new ArrayList();
            if (existType != null && existType.equals("Mine")) {
            	formList = fm.getOtherStudyMineForms(p.getId(), user.getId(), frc);
            } else if (existType != null && existType.equals("Public")) {
            	formList = fm.getPublicForms(p.getId(), frc);
            } else if (existType != null && existType.equals("All")) {
            	formList = fm.getOtherStudyAllForms(p.getId(), user.getId(), frc);
            }

            request.setAttribute("formsList", formList);  
            request.setAttribute("mode", "add");
            request.setAttribute("searchFormType", searchFormType);

            List activeForms = fm.getActiveForms(p.getId());
            if (activeForms.size() > 1) {
                request.setAttribute(FormConstants.ACTIVEFORMS, activeForms);
            }
        } catch(CtdbException ce) {
            return StrutsConstants.FAILURE;
        }
        
        return SUCCESS;
    }

    /**
     * Updates a FormResultControl object with the parameters specified
     *
     * @param frc The result control to update
     */
    private void updateResultControl(FormResultControl frc)  {
        if (getName() != null) {
            frc.setName(getName().trim());
        }

        if (getStatus() != null) {
            int formStatus = Integer.parseInt(getStatus());
            if (formStatus != 0) {
                CtdbLookup status = new CtdbLookup();
                status.setId(formStatus);
                frc.setStatus(status);
            }
        }

        if (getIssued() != null) {
            frc.setIsAdministed(Integer.parseInt(getIssued()));
        }

        if (getUpdatedDate() != null) {
            String dateString = getUpdatedDate().trim();

            if (dateString.length() != 0) {
                SimpleDateFormat parser = new SimpleDateFormat("yyy-MM-dd");
                Date date = parser.parse(dateString, new ParsePosition(0));
                frc.setUpdatedDate(date);
            }
        }
    }
    
 	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIssued() {
		return issued;
	}

	public void setIssued(String issued) {
		this.issued = issued;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getSortedBy() {
		return sortedBy;
	}

	public void setSortedBy(String sortedBy) {
		this.sortedBy = sortedBy;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getClicked() {
		return clicked;
	}

	public void setClicked(String clicked) {
		this.clicked = clicked;
	}

	public String getProtocolName() {
		return protocolName;
	}

	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
	}

	public String getNumResults() {
		return numResults;
	}

	public void setNumResults(String numResults) {
		this.numResults = numResults;
	}

	public String getNumResultsPerPage() {
		return numResultsPerPage;
	}

	public void setNumResultsPerPage(String numResultsPerPage) {
		this.numResultsPerPage = numResultsPerPage;
	}

	public String getSearchFormType() {
		return searchFormType;
	}

	public void setSearchFormType(String searchFormType) {
		this.searchFormType = searchFormType;
	}
	
	public String getExistType() {
		return existType;
	}

	public void setExistType(String existType) {
		this.existType = existType;
	}

	public String getSearchSubmitted() {
		return searchSubmitted;
	}

	public void setSearchSubmitted(String searchSubmitted) {
		this.searchSubmitted = searchSubmitted;
	}
}
