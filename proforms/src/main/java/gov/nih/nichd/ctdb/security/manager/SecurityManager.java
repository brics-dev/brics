package gov.nih.nichd.ctdb.security.manager;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.ResultControl;
import gov.nih.nichd.ctdb.security.common.AuthenticationFailedException;
import gov.nih.nichd.ctdb.security.common.DuplicateUserException;
import gov.nih.nichd.ctdb.security.common.UserNotFoundException;
import gov.nih.nichd.ctdb.security.common.UserResultControl;
import gov.nih.nichd.ctdb.security.dao.SecurityManagerDao;
import gov.nih.nichd.ctdb.security.domain.AccessAuditLog;
import gov.nih.nichd.ctdb.security.domain.Privilege;
import gov.nih.nichd.ctdb.security.domain.Role;
import gov.nih.nichd.ctdb.security.domain.SiteLink;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.nichd.ctdb.site.dao.SiteDao;
import gov.nih.nichd.ctdb.site.domain.Site;
import gov.nih.tbi.account.model.hibernate.Account;

/**
 * SecurityManager is a business layer object which interacts with the SecurityManagerDao. The
 * role of the SecurityManager is to enforce business rule logic and delegate data layer manipulation
 * to the SecurityManagerDao.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class SecurityManager extends CtdbManager
{

	public static final String PRINCIPAL_INVESTIGATOR_ROLE = "Principal Investigator";
	public static final String ASSOCIATE_INVESTIGATOR_ROLE = "Associate Investigator";
	public static final String RESEARCH_ASSOCIATE_ROLE = "Research Associate";
	public static final String DATA_MANAGER_ROLE = "Data Manager";
	public static final String DATA_ENTRY_ROLE = "Data Entry";
	public static final String CLINICAL_RESEARCH_ASSOCIATE_ROLE = "Clinical Research Associate";
	
	public User ssoAuthenticate(String username, HttpServletRequest request) throws AuthenticationFailedException, CtdbException  {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);
			
			User user = new User();
            user.setUsername(username);
            // this method will fail with an AuthenticationFailedException if the user cannot be found
            dao.getUser(user);
            if (! user.isActive()) {
                throw new AuthenticationFailedException("The user account is inactive");
            }

            List<Role> roles = dao.getUserRoles(user.getId());
            user.setRoleList(roles);
            dao.updateUserRoleSiteInfo(user);

            this.login(user, request);
            return user;
		}
		catch(AuthenticationFailedException e) {
			System.out.println("User failed login");
            this.failedLogin(username, request);
            throw e;
		}
		finally {
			this.close(conn);
		}
}
    
    /**
     * Sets the roles and rolesiteinfo for the given user u
     * 
     * @param u the User to update roles for
     * @throws Exception upon DAO exception
     */
    public void setRoles(User u) throws Exception {
    	Connection conn = null;
    	try {
			conn = CtdbManager.getConnection();
			SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);
	        List<Role> roles = dao.getUserRoles(u.getId());
	        u.setRoleList(roles);
	        dao.updateUserRoleSiteInfo(u);
    	}
    	finally {
    		conn.close();
    	}
    }

    public void createUser (User u) throws CtdbException , DuplicateUserException{
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);
            dao.createUser(u);
        }
         finally
        {
            this.close(conn);
        }

    }

    /**
     * Saves changes to a user to the database
     * 
     * @param u - The user information to be saved
     * @throws CtdbException	If there are any failures during save
     */
    public void updateUser (User u) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);
            dao.updateUser(u);
        }
         finally
        {
            this.close(conn);
        }

    }
        /**
     * Retrieves all roles that this user has across protocols
     *
     * @param userId The user ID for the current user
     * @return A list of all roles that this user has across protocols
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public Map<Integer,Role> getRoleMap(int userId) throws CtdbException    {
        Connection conn = null;
        try
        {
            conn = CtdbManager.getConnection();
            SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);
            return dao.getRoleMap(userId);
        }
        finally
        {
            this.close(conn);
        }
    }

    public List<Role> getUserRoles(int userId) throws CtdbException
    {
        Connection conn = null;
        try
        {
            conn = CtdbManager.getConnection();
            SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);
            return dao.getUserRoles(userId);
        }
        finally
        {
            this.close(conn);
        }
    }
    
    public Role getUserRole(int userId, int protocolId) throws CtdbException
    {
        Connection conn = null;
        try
        {
            conn = CtdbManager.getConnection();
            SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);
            return dao.getUserRole(userId, protocolId);
        }
        finally
        {
            this.close(conn);
        }
    }
    
    /**
     * Checks if the current user has a certain role for the given study
     * 
     * @param userId - ID of the current user
     * @param protocolId - ID of the current study
     * @param roleName - The role name to check for
     * @return	True if the current user has the given role for the current study, or false otherwise
     * @throws CtdbException	Failure occurred while retrieving data from the database.
     */
    public boolean doesUserHaveRoleForStudy(int userId, int protocolId, String roleName) throws CtdbException
    {
    	boolean haveRole = false;
    	Role userRole = getUserRole(userId, protocolId);
    	
    	if ( userRole != null )
    	{
    		haveRole = roleName.equalsIgnoreCase(userRole.getName());
    	}
    	
    	return haveRole;
    }
    
    public Site getUserProtocolSite(int userId, int protocolId) throws CtdbException {
        Connection conn = null;
        try
        {
            conn = CtdbManager.getConnection();
            SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);
            SiteDao sitedao = SiteDao.getInstance(conn);
            int siteId = dao.getUserProtocolSite(userId, protocolId);
            if (siteId == 0) {
            	return null;
            }
            return sitedao.getSite(siteId);
        }
        finally
        {
            this.close(conn);
        }
    }
    
    /**
     * Logs a user out of the system. This will facilitate updating the audit
     * log of this access attempt, where the access attempt type = LOGOUT.
     *
     * @param   user The user to log out of the system.
     * @throws  CtdbException thrown if any errors occur while processing
     */
    public void logout(User user) throws CtdbException
    {
        AccessAuditLog aal = new AccessAuditLog();
        aal.setRemoteAddress(" ");
        aal.setRemoteHost(" ");
        aal.setRequestHeaders( " ");
        aal.setXForwardedFor(" ");
        aal.setUsername(user.getUsername());
        aal.setSuccess(true);
        aal.setAccessDate(new Date());
        aal.setAttemptType(AccessAuditLog.LOGOUT_ATTEMPT_TYPE);
        this.auditAccessAttempt(aal);
    }

    /**
     * Logs a user into of the system. This will facilitate updating the audit
     * log of this access attempt, where the access attempt type = LOGIN.
     *
     * @param   user The user to log into the system.
     * @throws  CtdbException thrown if any errors occur while processing
     */
    private void login(User user, HttpServletRequest request) throws CtdbException
    {
        AccessAuditLog aal = new AccessAuditLog();
        aal.setRemoteAddress(request.getRemoteAddr());
        aal.setRemoteHost(request.getRemoteHost());
        aal.setXForwardedFor(request.getHeader("X-Forwarded-For"));
        aal.setRequestHeaders(this.getHeaders(request));
        aal.setUsername(user.getUsername());
        aal.setSuccess(true);
        aal.setAccessDate(new Date());
        aal.setAttemptType(AccessAuditLog.LOGIN_ATTEMPT_TYPE);
        this.auditAccessAttempt(aal);
    }

    /**
     * Logs a failed attempt to log nto of the system. This will facilitate updating the audit
     * log of this access attempt, where the access attempt type = LOGIN.
     *
     * @param   username The username trying to log into the system
     * @throws  CtdbException thrown if any errors occur while processing
     */
    private void failedLogin(String username, HttpServletRequest request) throws CtdbException
    {
        AccessAuditLog aal = new AccessAuditLog();
        aal.setRemoteAddress(request.getRemoteAddr());
        aal.setRemoteHost(request.getRemoteHost());
        aal.setXForwardedFor(request.getHeader("X-Forwarded-For"));
        aal.setRequestHeaders(this.getHeaders(request));
        aal.setUsername(username);
        aal.setSuccess(true);
        aal.setAccessDate(new Date());
        aal.setAttemptType(AccessAuditLog.LOGIN_ATTEMPT_TYPE);
        aal.setFailureReason("Invalid Username and/or Password");
        this.auditAccessAttempt(aal);
    }


    private String getHeaders (HttpServletRequest request) {
        StringBuffer heads = new StringBuffer();
        Enumeration<String> headerNames = request.getHeaderNames();
            while(headerNames.hasMoreElements()) {
              String headerName = (String)headerNames.nextElement();
                heads.append(" [").append(headerName.toUpperCase()).append("] = ");
                heads.append(" (").append(request.getHeader(headerName)).append(")   .   ");
            }
        return heads.toString();

    }
    /**
     * Updates the sysadmin flag for the User.
     *
     * @param   username The CTDB User to change the sys admin status for
     * @param   sysAdmin True if the user should be made a sys admin, false if the user should not be a sys admin
     * @throws  CtdbException thrown if any other errors occur while processing
     */
    public void updateSysAdminStatus(String username, boolean sysAdmin) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);

                dao.updateSysAdminStatus(username, sysAdmin);


        }
        finally
        {
            this.close(conn);
        }
    }

     public void updateUseTreeview(String username, boolean useTreeview) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);

                dao.updateUseTreeview(username, useTreeview);

        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Gets a User in the system.
     *
     * @param   username The CTDB User to get
     * @throws  CtdbException thrown if any other errors occur while processing
     */
    public User getUser(String username) throws CtdbException, UserNotFoundException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);
            User user = new User();
            user.setUsername(username);
            dao.getUser(user);
            return user;
        }
        finally
        {
            this.close(conn);
        }
    }

        /**
     * Gets a User in the system.
     *
     * @param   usrid The CTDB User to get
     * @throws  CtdbException thrown if any other errors occur while processing
     */
    public User getUser(int usrid) throws CtdbException, UserNotFoundException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);
            User user  = dao.getUser(usrid);
            return user;
        }
        finally
        {
            this.close(conn);
        }
    }
    
    
    
    
    /**
    * Gets a User in the system.
    *
    * @param   usrid The CTDB User to get
    * @throws  CtdbException thrown if any other errors occur while processing
    */
   public User getPatientUser() throws CtdbException, UserNotFoundException
   {
       Connection conn = null;
       

       try
       {
           conn = CtdbManager.getConnection();
           SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);
           User user  = dao.getPatientUser();
           return user;
       }
       finally
       {
           this.close(conn);
       }
   }


    public List<User> getUsers (ResultControl rc) throws CtdbException, UserNotFoundException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);
            return dao.getUsers(rc);
        }
        finally
        {
            this.close(conn);
        }
    }

    public List<User> getUsers (User currentUser, UserResultControl rc) throws CtdbException, UserNotFoundException  {
            if (! currentUser.isSysAdmin()) {
              rc.setInstituteId(currentUser.getInstituteId());
        }
        return this.getUsers(rc);
    }

    public List<User> getUsers (User currentUser) throws CtdbException, UserNotFoundException  {
        UserResultControl rc = new UserResultControl();
        return this.getUsers(currentUser, rc);
    }
	
	/**
	 * Converts BRICS user objects to ProFoRMS user objects.  If a BRICS user is not in the database,
	 * it is then created
	 * 
	 * @param bricsUsers - a list of BRICS users
	 * @return	A list of ProFoRMS users created from the BRICS user list
	 * @throws CtdbException	the user conversion has failed
	 */
	public List<User> convertBricsUsers(List<Account> bricsUsers) throws CtdbException
	{
		Connection conn = null;
		List<User> proformsUsers = new ArrayList<User>(bricsUsers.size());
		
		try
		{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);
			
			for ( Account act : bricsUsers )
			{
	        	// if the role is not VALID, don't add it
	        	if ( SecuritySessionUtil.validProformsRole(act) )
	        	{
	        		proformsUsers.add(User.userFromBricsAccount(act));
	        	}
	        }
			
			// Create any user and get IDs for existing users
			dao.createUserOrUpdateId(proformsUsers);
		}
		catch ( UnsupportedEncodingException uee )
		{
			throw new CtdbException("Error occured during password encoding.", uee);
		}
		catch ( DuplicateUserException due )
		{
			throw new CtdbException("A duplicate user was attempted to be added to the database.: " + due.getMessage(), due);
		}
		finally
        {
            this.close(conn);
        }
		
		return proformsUsers;
	}

    public int getNumLogins() throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return SecurityManagerDao.getInstance(conn).getNumLogins();
        }
        finally
        {
            this.close(conn);
        }
    }

    public Map<String,User> getUsersMap (User u) throws CtdbException {
        return this.getUsersMap(u, new UserResultControl());
    }

    public Map<String,User> getUsersMap (User u, UserResultControl rc) throws CtdbException {


       Connection conn = null;

        try
        {
            List<User> users = this.getUsers(u, rc);
            Map<String,User> usersMap = new HashMap<String,User>();

            for(Iterator<User> iterator = users.iterator(); iterator.hasNext();)
            {
                User user = iterator.next();
                usersMap.put(user.getUsername(), user);
            }

            return usersMap;
        }
        finally
        {
            this.close(conn);
        }
    }
    /**
     * Audits all access attempts to the system. Audit attempts track login (both successful and unsuccessful) and
     * logout trails. There is for tracking purposes.
     *
     * @param   aal The AccessAuditLog object to log to the database.
     * @throws  CtdbException thrown if any errors occur while processing
     */
    private void auditAccessAttempt(AccessAuditLog aal) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            SecurityManagerDao.getInstance(conn).auditAccessAttempt(aal);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Creates a CTDB system role in the system.
     *
     * @param   role The CTDB role to create
     * @throws  DuplicateObjectException is thrown if the role already exists in the system
     * @throws  CtdbException thrown if any other errors occur while processing
     */
    public void createSystemRole(Role role) throws DuplicateObjectException, CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            conn.setAutoCommit(false);

            SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);
            dao.createSystemRole(role);
            dao.associateRolePrivileges(role.getId(), role.getPrivList());

            conn.commit();
        }
        catch(SQLException e)
        {
            this.rollback(conn);
            throw new CtdbException("Unable to create SystemRole: " + e.getMessage(), e);
        }
        catch(DuplicateObjectException doe)
        {
            this.rollback(conn);
            throw doe;
        }
        catch(CtdbException ce)
        {
            this.rollback(conn);
            throw ce;
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Updates a CTDB system role in the system.
     *
     * @param   role The CTDB role to update
     * @throws  ObjectNotFoundException is thrown if the role does not exist in the system
     * @throws  DuplicateObjectException is thrown if the role already exists in the system
     * @throws  CtdbException thrown if any other errors occur while processing
     */
    public void updateSystemRole(Role role) throws ObjectNotFoundException, DuplicateObjectException, CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            conn.setAutoCommit(false);

            SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);
            dao.updateSystemRole(role);
            dao.associateRolePrivileges(role.getId(), role.getPrivList());

            conn.commit();
        }
        catch(SQLException e)
        {
            this.rollback(conn);
            throw new CtdbException("Unable to update SystemRole: " + e.getMessage(), e);
        }
        catch(DuplicateObjectException doe)
        {
            this.rollback(conn);
            throw doe;
        }
        catch(CtdbException ce)
        {
            this.rollback(conn);
            throw ce;
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves a role from the system based on the unique identifier
     *
     * @param   roleId The Role ID to retrieve
     * @return  Role data object
     * @throws  ObjectNotFoundException is thrown if the role does not exist in the system
     * @throws  CtdbException thrown if any other errors occur while processing
     */
    public Role getSystemRole(int roleId) throws ObjectNotFoundException, CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return SecurityManagerDao.getInstance(conn).getSystemRole(roleId);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves all System Roles in the CTDB System
     *
     * @return  A list of all System Roles in the CTDB System.
     * @throws  CtdbException thrown if any errors occur while processing
     */
    public List<Role> getSystemRoles() throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return SecurityManagerDao.getInstance(conn).getSystemRoles();
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves all Privileges in the CTDB System
     *
     * @return  A list of all Privileges in the CTDB System.
     * @throws  CtdbException thrown if any errors occur while processing
     */
    public List<Privilege> getPrivileges() throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return SecurityManagerDao.getInstance(conn).getPrivileges();
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Creates a CTDB site link in the system. A site link is a URL that can be
     * accessed through the CTDB system.
     *
     * @param   siteLink The CTDB site link to create
     * @throws  DuplicateObjectException is thrown if a site link with the same name already exists in the system
     * @throws  CtdbException thrown if any other errors occur while processing
     */
    public void createSiteLink(SiteLink siteLink) throws DuplicateObjectException, CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            SecurityManagerDao.getInstance(conn).createSiteLink(siteLink);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Updates a CTDB site link in the system. A site link is a URL that can be
     * accessed through the CTDB system.
     *
     * @param   siteLink The CTDB SiteLink to update
     * @throws  ObjectNotFoundException is thrown if the site link does not exist in the system
     * @throws  DuplicateObjectException is thrown if a site link with the same name already exists in the system
     * @throws  CtdbException thrown if any other errors occur while processing
     */
    public void updateSiteLink(SiteLink siteLink) throws ObjectNotFoundException, DuplicateObjectException, CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            SecurityManagerDao.getInstance(conn).updateSiteLink(siteLink);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Updates the ordering of CTDB site links in the system. A site link is a URL that can be
     * accessed through the CTDB system. This ordering will be used when displaying URLs
     * to the end user, but not to the Administrator. If a URL is not ordered, it will be
     * ordered by name.
     *
     * @param   orderedIds The CTDB SiteLink IDs in order of display
     * @throws  CtdbException thrown if any other errors occur while processing
     */
    public void updateSiteLinkOrdering(String[] orderedIds) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            conn.setAutoCommit(false);
            SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);
            for(int idx = 0; idx < orderedIds.length; idx++)
            {
                int siteLinkId = Integer.parseInt(orderedIds[idx]);
                dao.updateSiteLinkOrdering(siteLinkId, idx);
            }

            conn.commit();
        }
        catch(SQLException e)
        {
            this.rollback(conn);
            throw new CtdbException("Unable to update SiteLink: " + e.getMessage(), e);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves a site link from the system based on the unique identifier
     *
     * @param   siteLinkId The SiteLink ID to retrieve
     * @return  SiteLink data object
     * @throws  ObjectNotFoundException is thrown if the site link does not exist in the system
     * @throws  CtdbException thrown if any other errors occur while processing
     */
    public SiteLink getSiteLink(int siteLinkId) throws ObjectNotFoundException, CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return SecurityManagerDao.getInstance(conn).getSiteLink(siteLinkId);
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Retrieves all Site Links in the CTDB System
     *
     * @return  A list of all Site Links in the CTDB System.
     * @throws  CtdbException thrown if any errors occur while processing
     */
    public List<SiteLink> getSiteLinks() throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            return SecurityManagerDao.getInstance(conn).getSiteLinks();
        }
        finally
        {
            this.close(conn);
        }
    }

    /**
     * Deletes a site link from the system based on the unique identifier
     *
     * @param   siteLinkId The SiteLink ID to delete
     * @throws  CtdbException thrown if any other errors occur while processing
     */
    public void deleteSiteLink(int siteLinkId) throws CtdbException
    {
        Connection conn = null;

        try
        {
            conn = CtdbManager.getConnection();
            SecurityManagerDao.getInstance(conn).deleteSiteLink(siteLinkId);
        }
        finally
        {
            this.close(conn);
        }
    }
    
    /**
     * Deletes a site link from the system based on a list unique identifiers
     * 
     * @param siteLinkIds - A list of site link IDs to delete
     * @return	The number of site link records deleted
     * @throws CtdbException	thrown if any other errors occur while processing
     */
    public int deleteSiteLinks(List<Integer> siteLinkIds) throws CtdbException
    {
    	Connection conn = null;
    	int numDeleted = 0;
    	
    	try
    	{
    		conn = CtdbManager.getConnection();
    		numDeleted = SecurityManagerDao.getInstance(conn).deleteSiteLinks(siteLinkIds);
    	}
    	finally
        {
            this.close(conn);
        }
    	
    	return numDeleted;
    }

    public boolean isPasswordUsed (User u, String newPassword) throws CtdbException {
     Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
           return SecurityManagerDao.getInstance(conn).isPasswordUsed ( u,  newPassword);
        }
        finally{
            this.close(conn);
        }
    }

    public void refreshCtss () throws CtdbException {
     Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
            SecurityManagerDao.getInstance(conn).refreshCtss ();
        }
        finally{
            this.close(conn);
        }
    }


    public void updatePassword (User u, HashMap<Integer,String> securityInfo) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            SecurityManagerDao dao = SecurityManagerDao.getInstance(conn);
            dao.archivePassword (u);
            dao.updatePassword ( u);
            dao.updateSecurityInfo (u, securityInfo);
            conn.commit();
        } catch (SQLException sqle) {
            throw new CtdbException("Failure committing while updating user passwrod : " + sqle.getMessage(), sqle);
        }
        finally{
            this.close(conn);
        }
    }

    public HashMap<Integer,String> getUserSecurityInfo (int userId) throws CtdbException {
        Connection conn = null;

        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            return  SecurityManagerDao.getInstance(conn).getUserSecurityInfo (userId);
        }
        finally{
            this.close(conn);
        }
    }

    public HashMap<Integer,String> getAnsweredQuestions(int userId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            return  SecurityManagerDao.getInstance(conn).getAnsweredQuestions (userId);
        }
        finally{
            this.close(conn);
        }
    }


    public boolean validateSecurityResponses(HashMap<Integer,String> securityResponses, int userId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            return  SecurityManagerDao.getInstance(conn).validateSecurityResponses (securityResponses, userId);
        }
        finally{
            this.close(conn);
        }
    }

     public void recordSentPassword (User u) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
            SecurityManagerDao.getInstance(conn).recordSentPassword (u);
        }
        finally{
            this.close(conn);
        }
    }

     public void recordFailedPasswordAttempt (User u) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
            SecurityManagerDao.getInstance(conn).recordFailedPasswordAttempt (u);
        }
        finally{
            this.close(conn);
        }
    }


     public boolean userExcededRetrevialAttempts (User u) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            return SecurityManagerDao.getInstance(conn).userExcededRetrevialAttempts (u);
        }
        finally{
            this.close(conn);
        }
    }
}
