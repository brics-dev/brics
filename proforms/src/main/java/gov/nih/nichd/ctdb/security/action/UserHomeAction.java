package gov.nih.nichd.ctdb.security.action;

import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.ResultControl;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.security.common.UserResultControl;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;
import gov.nih.nichd.ctdb.security.tag.UserHomeIdtDecorator;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.nichd.ctdb.util.common.LookupResultControl;
import gov.nih.nichd.ctdb.util.domain.LookupType;
import gov.nih.nichd.ctdb.util.manager.LookupManager;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

/**
 * 
 * @author jim3
 */
public class UserHomeAction extends BaseAction {
	private static final Logger logger = Logger.getLogger(UserHomeAction.class);
	private static final long serialVersionUID = -6243779761171735028L;
	
	private String firstName = null;
	private String lastName = null;
	private String email = null;
	private int instituteId = Integer.MIN_VALUE;
	private String staffSearch = "all";
    private String sortBy = UserResultControl.SORT_BY_USERNAME;
    private String sortOrder = ResultControl.SORT_ASC;
	private String sortedBy;
    private String searchSubmitted = "NO";
    List<User> userSearchResult;
	
	public String showUserHome() {
		
		buildLeftNav(LeftNavController.LEFTNAV_ADMIN_USER,
				new int[] {LeftNavController.LEFTNAV_SUBJECTS_MANAGE, LeftNavController.LEFTNAV_COLLECT,
						LeftNavController.LEFTNAV_FORM_HOME, LeftNavController.LEFTNAV_STUDY_HOME});
		User user = getUser();

		try {
            List<CtdbLookup> institutes = new ArrayList<CtdbLookup>();
            CtdbLookup allInstitutes = new CtdbLookup();
            allInstitutes.setId(Integer.MIN_VALUE);
            allInstitutes.setLongName("All");
            institutes.add(allInstitutes);

            LookupManager lm = new LookupManager();
            institutes.addAll(lm.getLookups(LookupType.INSTITUTE, new LookupResultControl()));
            
			if (!user.isSysAdmin()) {
				institutes = removeInstitutes(institutes, user);
                this.setInstituteId(((CtdbLookup)institutes.get(0)).getId());
            }
            session.put("__userSearch_institutes", institutes);
            
			SecurityManager sm = new SecurityManager();
			List<User> allUsers = sm.getUsers(user);
            checkUsersProformsRole(allUsers);
            request.setAttribute("__userSearch_users", allUsers);
            
		}
		catch ( Exception e ) {
			logger.error("Could not show user home.", e);
			
			return StrutsConstants.FAILURE;
		}

		return SUCCESS;
	}


	public String searchUser() {
		
		buildLeftNav(LeftNavController.LEFTNAV_ADMIN_USER,
				new int[] {LeftNavController.LEFTNAV_SUBJECTS_MANAGE, LeftNavController.LEFTNAV_COLLECT,
						LeftNavController.LEFTNAV_FORM_HOME, LeftNavController.LEFTNAV_STUDY_HOME});

		try {
			SecurityManager sm = new SecurityManager();
			UserResultControl rc = new UserResultControl();
			
			updateResultControl(rc);
			userSearchResult = sm.getUsers(rc);
			checkUsersProformsRole(userSearchResult);
			request.setAttribute("__userSearch_users", userSearchResult);
		}
		catch ( Exception e ) {
			logger.error("Could not find user.", e);
			
			return StrutsConstants.FAILURE;
		}

		return SUCCESS;
	}

    private void updateResultControl (UserResultControl rc) {

        if (getSortedBy() != null && getSortedBy().equals(getSortBy())) {
            if (getSortOrder().equals(UserResultControl.SORT_DESC)) {
                setSortOrder(UserResultControl.SORT_ASC);
            } else {
                setSortOrder(UserResultControl.SORT_DESC);
            }
        }
        
        setSortedBy(getSortBy());
        rc.setFirstName(getFirstName());
        rc.setLastName(getLastName());
        rc.setEmail(getEmail());
        rc.setInstituteId(getInstituteId());
        rc.setStaff(getStaffSearch());
        rc.setSortBy(getSortBy());
        rc.setSortOrder(getSortOrder());
    }

    private List<CtdbLookup> removeInstitutes (List<CtdbLookup> institutes, User u) {
        List<CtdbLookup> l = new ArrayList<CtdbLookup>();
        for (Iterator<CtdbLookup> i = institutes.iterator(); i.hasNext(); ) {
            CtdbLookup lu = i.next();
            if (u.getInstituteId() == lu.getId()) {
                l.add(lu);
            }
        }
        return l;
    }

    /**
     * This method will loop through the given list of users and remove the user who 
     * doesn't have Proforms role.
     * 
     * @param users
     * @throws MalformedURLException
     * @throws UnknownHostException 
     * @throws ClientHandlerException 
     * @throws UniformInterfaceException 
     * @throws NoRouteToHostException 
     */
    private void checkUsersProformsRole(List<User> users) 
    		throws MalformedURLException, CtdbException, UnknownHostException, NoRouteToHostException, WebApplicationException, RuntimeException {
        
		SecuritySessionUtil ssu = new SecuritySessionUtil(request);; 
        List<Account> allProformsAccounts = ssu.accountRestWs("ROLE_PROFORMS");

        Set<Long> allProFormsAccountIds = new HashSet<Long>();
        for (Account account : allProformsAccounts) {
     	   if (SecuritySessionUtil.validProformsRole(account)) {
     		   allProFormsAccountIds.add(account.getId());
     	   }
        }
        
        // For each user, if its userId doesn't belong to allProFormsUserIds, remove it from the list.
        Iterator<User> it = users.iterator();
        while (it.hasNext()) {
     	   if (!allProFormsAccountIds.contains(it.next().getBricsUserId())) {
     		  it.remove();
     	   }
        }
    }
    
	// url: http://fitbir-portal-local.cit.nih.gov:8082/proforms/admin/getUsersList.action
	public String getUsersList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			showUserHome();
			searchUser();
			ArrayList<User> outputList = new ArrayList<User>(getUserSearchResult());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new UserHomeIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}


	public String getFirstName() {
		return firstName;
	}


	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}


	public String getLastName() {
		return lastName;
	}


	public void setLastName(String lastName) {
		this.lastName = lastName;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public int getInstituteId() {
		return instituteId;
	}


	public void setInstituteId(int instituteId) {
		this.instituteId = instituteId;
	}


	public String getStaffSearch() {
		return staffSearch;
	}


	public void setStaffSearch(String staffSearch) {
		this.staffSearch = staffSearch;
	}


	public String getSortBy() {
		return sortBy;
	}


	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}


	public String getSortOrder() {
		return sortOrder;
	}


	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}


	public String getSortedBy() {
		return sortedBy;
	}


	public void setSortedBy(String sortedBy) {
		this.sortedBy = sortedBy;
	}
    
    public void setSearchSubmitted(String searchSubmitted) {
		this.searchSubmitted = searchSubmitted;
	}

	public String getSearchSubmitted() {
		return searchSubmitted;
	}
	
	public void setUserSearchResult(List<User> userSearchResult) {
		this.userSearchResult = userSearchResult;
	}
	
	public List<User> getUserSearchResult() {
		return userSearchResult;
	}

}
