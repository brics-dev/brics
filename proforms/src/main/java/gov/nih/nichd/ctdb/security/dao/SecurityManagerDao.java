package gov.nih.nichd.ctdb.security.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.postgresql.util.PSQLException;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.ResultControl;
import gov.nih.nichd.ctdb.security.common.AuthenticationFailedException;
import gov.nih.nichd.ctdb.security.common.DuplicateUserException;
import gov.nih.nichd.ctdb.security.common.InvalidAssociationException;
import gov.nih.nichd.ctdb.security.common.UserNotFoundException;
import gov.nih.nichd.ctdb.security.domain.AccessAuditLog;
import gov.nih.nichd.ctdb.security.domain.Privilege;
import gov.nih.nichd.ctdb.security.domain.Role;
import gov.nih.nichd.ctdb.security.domain.SiteLink;
import gov.nih.nichd.ctdb.security.domain.SystemRole;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * SecurityManagerDao interacts with the Data Layer for the SecuriyManager.
 * The only job of the DAO is to manipulate the data layer.
 *
 * @author Booz Allen Hamilton
 * @edited CIT
 * @version 1.0
 */
public class SecurityManagerDao extends CtdbDao
{
	private static Logger logger = Logger.getLogger(SecurityManagerDao.class);
	
    /**
     * Private Constructor to hide the instance
     * creation implementation of the SecurityManagerDao object
     * in memory. This will provide a flexible architecture
     * to use a different pattern in the future without
     * refactoring the SecurityManager.
     */
    private SecurityManagerDao() {

    }

    /**
     * Method to retrieve the instance of the SecurityManagerDao.
     *
     * @return SecurityManagerDao data object
     */
    public static synchronized SecurityManagerDao getInstance() {
        return new SecurityManagerDao();
    }

    /**
     * Method to retrieve the instance of the SecurityManagerDao. This method
     * accepts a Database Connection to be used internally by the DAO. All
     * transaction management will be handled at the BusinessManager level.
     *
     * @param conn Database connection to be used within this data object
     * @return SecurityManagerDao data object
     */
    public static synchronized SecurityManagerDao getInstance(Connection conn) {
        SecurityManagerDao dao = new SecurityManagerDao();
        dao.setConnection(conn);
        return dao;
    }
    
    /**
     * Creates a ProFoRMS user based on a BRICS user.
     *
     * @param user The User Object to create and merge data with.
     * @throws DuplicateUserException is thrown if the username already exists in the system
     * @throws CtdbException          thrown if any other errors occur while processing
     */
    public void createUser(User user) throws DuplicateUserException, CtdbException {
        PreparedStatement stmt = null;
        
        try
        {
            String sqlStmt = "insert into usr(usrid, brics_userid, username, sysadminflag, createdby, createddate, updatedby, updateddate, " +
            	"firstname, lastname, middlename, office, phonenumber, xinstituteid, email, isStaff, password, passchanged, cancreatestudy) " +
            	"values(DEFAULT, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP-interval '89.5', ?) ";
            
            stmt = this.conn.prepareStatement(sqlStmt);
            stmt.setLong(1, user.getBricsUserId());
            stmt.setString(2, user.getUsername());
            stmt.setBoolean(3, user.isSysAdmin());
            stmt.setLong(4, user.getCreatedBy());
            stmt.setLong(5, user.getUpdatedBy());
            stmt.setString(6, user.getFirstName());
            stmt.setString(7, user.getLastName());
            stmt.setString(8, user.getMiddleName());
            stmt.setString(9, user.getOffice());
            stmt.setString(10, user.getPhoneNumber());
            stmt.setLong(11, user.getInstituteId());
            stmt.setString(12, user.getEmail());
            stmt.setBoolean(13, user.isStaff());
            stmt.setString(14, user.getPassword());
            stmt.setBoolean(15, user.isCreateStudy());

            stmt.executeUpdate();
            user.setId(getInsertId(conn, "usr_seq"));
        }
        catch (PSQLException e) {
            if (e.getSQLState().equals( SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
                throw new DuplicateUserException("A user with the username " + user.getUsername() + " already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to create new User: " + e.getMessage(), e);
            }
        }
        catch (SQLException e) {
            if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
                throw new DuplicateUserException("A user with the username " + user.getUsername() + " already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to create new User: " + e.getMessage(), e);
            }
        }
        finally {
            this.close(stmt);
        }
    }
    
    
    /**
     * This function determines whether a user alreadyexists in the system with this username
     * @param username
     * @return
     * @throws CtdbException
     */
    public boolean doesUserExistWithThisUsername(String username) throws CtdbException{
    	
    	 PreparedStatement stmt = null;
         ResultSet rs = null;

         try {
             stmt = this.conn.prepareStatement("select * from usr where username = ? ");
             stmt.setString(1, username);
             rs = stmt.executeQuery();
             if(rs.next()) {
            	 return true;
             }else {
            	 return false;
             }


         }
         catch (SQLException e) {

             throw new CtdbException("Failure checking user protocol association " + e.getMessage(), e);
         }
         finally {
             this.close(stmt);
             this.close(rs);
         }
    	
    	
    }
    
    /**
     * Creates the user in the database if it doesn't already exists. For existing users, the user id
     * will be set.
     * 
     * @param userList - List of users to be either added to the database or have their IDs synced
     * @throws DuplicateUserException	when the code tries to insert a duplication user in the database
     * @throws CtdbException	user creation or lookup has failed
     */
    public void createUserOrUpdateId(List<User> userList) throws DuplicateUserException, CtdbException
    {
    	PreparedStatement selectStmt = null;
    	PreparedStatement insertStmt = null;
    	ResultSet rs = null;
        
        try
        {
        	// Prepare the select and insert statements
        	String query = "select usrid from usr where username = ? ";
        	selectStmt = conn.prepareStatement(query);
        	
        	query = "insert into usr(usrid, brics_userid, username, sysadminflag, createdby, createddate, updatedby, updateddate, " +
        		"firstname, lastname, middlename, office, phonenumber, xinstituteid, email, isStaff, password, passchanged, cancreatestudy) " +
                "values(DEFAULT, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP-interval '89.5', ?) ";
        	insertStmt = conn.prepareStatement(query);
        	
        	// Check if each user is in the database. If the user is, set the ID otherwise create the user
        	for ( User u : userList )
        	{
        		// First check if the user is in the database
        		selectStmt.setString(1, u.getUsername());
        		rs = selectStmt.executeQuery();
        		
        		if ( !rs.next() )
        		{
        			// Create the user
        			insertStmt.setLong(1, u.getBricsUserId());
        			insertStmt.setString(2, u.getUsername());
        			insertStmt.setBoolean(3, u.isSysAdmin());
        			insertStmt.setLong(4, u.getCreatedBy());
        			insertStmt.setLong(5, u.getUpdatedBy());
        			insertStmt.setString(6, u.getFirstName());
        			insertStmt.setString(7, u.getLastName());
        			insertStmt.setString(8, u.getMiddleName());
        			insertStmt.setString(9, u.getOffice());
        			insertStmt.setString(10, u.getPhoneNumber());
        			insertStmt.setLong(11, u.getInstituteId());
        			insertStmt.setString(12, u.getEmail());
        			insertStmt.setBoolean(13, u.isStaff());
        			insertStmt.setString(14, u.getPassword());
        			insertStmt.setBoolean(15, u.isCreateStudy());

        			insertStmt.executeUpdate();
                    u.setId(getInsertId(conn, "usr_seq"));
        		}
        		else
        		{
        			// Set the user ID
        			u.setId(rs.getInt("usrid"));
        		}
        	}
        }
        catch (PSQLException e) {
            if (e.getSQLState().equals( SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
                throw new DuplicateUserException("A user already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to create new User: " + e.getMessage(), e);
            }
        }
        catch (SQLException e)
        {
            if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
                throw new DuplicateUserException("A user already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to create new User: " + e.getMessage(), e);
            }
        }
        finally
        {
            close(selectStmt);
            close(insertStmt);
        }
    }
    
    /**
     * Persists changes to the User object to the database.
     * 
     * @param user - The User object to save to the database
     * @throws DuplicateUserException
     * @throws CtdbException
     */
    public void updateUser(User user) throws DuplicateUserException, CtdbException {
        PreparedStatement stmt = null;
        
        try {
            if (user.isStaff() && isUserAssociatedToProtocols(user)) {
                throw new InvalidAssociationException("The user is associated to protocols and cannot be made a site user");

            }
            
            String sqlStmt = "update usr set username = ?, sysadminflag = ?, updatedby = ?, updateddate = CURRENT_TIMESTAMP, " +
            	"firstname = ?, lastname = ?, middlename = ?, office = ?, phonenumber = ?, xinstituteid = ?, email = ?, isStaff = ?, " +
            	"password = ?, cancreatestudy = ?, brics_userid = ? where usrid = ? ";
            
            stmt = this.conn.prepareStatement(sqlStmt);
            stmt.setString(1, user.getUsername());
            stmt.setBoolean(2, user.isSysAdmin());
            stmt.setLong(3, user.getUpdatedBy());
            stmt.setString(4, user.getFirstName());
            stmt.setString(5, user.getLastName());
            stmt.setString(6, user.getMiddleName());
            stmt.setString(7, user.getOffice());
            stmt.setString(8, user.getPhoneNumber());
            stmt.setLong(9, user.getInstituteId());
            stmt.setString(10, user.getEmail());
            stmt.setBoolean(11, user.isStaff());
            stmt.setString(12, user.getPassword());
            stmt.setBoolean(13, user.isCreateStudy());
            stmt.setLong(14, user.getBricsUserId());
            stmt.setLong(15, user.getId());

            stmt.executeUpdate();
        }
        catch (PSQLException e) {
            if (e.getSQLState().equals( SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
                throw new DuplicateUserException("A user with the username " + user.getUsername() + " already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to create new User: " + e.getMessage(), e);
            }
        }
        catch (SQLException e) {
            if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
                throw new DuplicateUserException("A user with the username " + user.getUsername() + " already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to create new User: " + e.getMessage(), e);
            }
        }
        finally {
            this.close(stmt);
        }

    }

    private boolean isUserAssociatedToProtocols(User u) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = this.conn.prepareStatement("select usrid from protocolusrrole where usrid = ? and siteid is null ");
            stmt.setLong(1, u.getId());
            rs = stmt.executeQuery();
            return rs.next();


        }
        catch (SQLException e) {

            throw new CtdbException("Failure checking user protocol association " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
            this.close(rs);
        }


    }

    /**
     * Updates the sysadmin flag for the User.
     *
     * @param username The CTDB User to change the sys admin status for
     * @param sysAdmin True if the user should be made a sys admin, false if the user should not be a sys admin
     * @throws UserNotFoundException is thrown if the user does not exist in the system
     * @throws CtdbException         thrown if any other errors occur while processing
     */
    public void updateSysAdminStatus(String username, boolean sysAdmin) throws UserNotFoundException, CtdbException {
        PreparedStatement stmt = null;

        try {
            String sqlStmt = "update usr set sysadminflag = ? where username = ? ";

            stmt = this.conn.prepareStatement(sqlStmt);
            stmt.setBoolean(1, sysAdmin);
            stmt.setString(2, username);
            
            int recordsUpdated = stmt.executeUpdate();

            if (recordsUpdated == 0) {
                throw new UserNotFoundException("The user with username: " + username + " does not exist in the system.");
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to update Sys Admin Status for user with username " + username + ": " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    public void updateUseTreeview(String username, boolean useTreeview) throws UserNotFoundException, CtdbException {
        PreparedStatement stmt = null;

        try {
            String sqlStmt = "update usr set usetreeview = ? where username = ? ";

            stmt = this.conn.prepareStatement(sqlStmt);
            stmt.setBoolean(1, useTreeview);
            stmt.setString(2, username);

            int recordsUpdated = stmt.executeUpdate();

            if (recordsUpdated == 0) {
                throw new UserNotFoundException("The user with username: " + username + " does not exist in the system.");
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to update use tree view for user with username " + username + ": " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Retrieves a user from the system based on the username. This method
     * will faciliate retrieving additional CTDB information that the
     * web service does not provide.
     *
     * @param user The user object to merge data with
     * @throws UserNotFoundException is thrown if the user cannot
     *                               be found
     * @throws CtdbException         thrown if any other errors occur while processing
     */
    public void getUser(User user) throws AuthenticationFailedException, CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sqlStmt = "select usr.*, password tehPass from usr where username = ? ";

            stmt = this.conn.prepareStatement(sqlStmt);
            stmt.setString(1, user.getUsername());
            rs = stmt.executeQuery();
            
            if (!rs.next()) {
                throw new AuthenticationFailedException("The user with with username: " + user.getUsername() + " could not be found.");
            }
            
            this.rsToUser(rs, user);
            Date d = new Date();
            // 7776000000L milliseconds == 90 days
            user.setPasswordExpired(rs.getDate("passchanged").getTime() + 7776000000L < d.getTime());
            if (rs.getString("CTSSREFRESH").equals("true")) {
                user.setRefreshCtss(true);
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get User: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Retrieves a user from the system based on the usrid. This method
     * will faciliate retrieving additional CTDB information that the
     * web service does not provide.
     *
     * @param usrid surprisingly, the users id
     * @throws UserNotFoundException is thrown if the user cannot
     *                               be found
     * @throws CtdbException         thrown if any other errors occur while processing
     */
    public User getUser(int usrid) throws UserNotFoundException, CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select usr.*, password tehPass from usr where usrid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, usrid);

            rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new UserNotFoundException("The user with with usrId: " + usrid + " could not be found.");
            }
            return this.rsToUser(rs);

        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get User: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    
    /**
     * Retrieves a user from the system based on the usrid. This method
     * will faciliate retrieving additional CTDB information that the
     * web service does not provide.
     *
     * @param usrid surprisingly, the users id
     * @throws UserNotFoundException is thrown if the user cannot
     *                               be found
     * @throws CtdbException         thrown if any other errors occur while processing
     */
    public User getPatientUser() throws UserNotFoundException, CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select usr.*, password tehPass from usr where usrid = -1");

            stmt = this.conn.prepareStatement(sql.toString());


            rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new UserNotFoundException("The patient user could not be found.");
            }
            return this.rsToUser(rs);

        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get User: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    
    
    


    public List<User> getUsers(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select usr.*, 'tehPass' tehPass from  usr ");
            sql.append(rc.getSearchClause());

            stmt = this.conn.prepareStatement(sql.toString());
            rs = stmt.executeQuery();

            List<User> l = new ArrayList<User>();
            while (rs.next()) {
                l.add(rsToUser(rs));
            }
            return l;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get all Users: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Retrieves all roles that this user has across protocols
     *
     * @param userId The user ID for the current user
     * @return A list of all roles that this user has across protocols
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public List<Role> getUserRoles(int userId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select distinct roleid from protocolusrrole where usrid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, userId);

            rs = stmt.executeQuery();

            List<Role> roles = new ArrayList<Role>();

            while (rs.next()) {
                roles.add(this.getSystemRole(rs.getInt("roleid")));
            }

            return roles;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get User Roles: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    /**
     * Retrieves the role that this user has for a defined protocol
     *
     * @param userId The user ID for the current user
     * @param protocolId the ID of the protocol
     * @return the role that this user has in the defined protocol
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public Role getUserRole(int userId, int protocolId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select roleid from protocolusrrole where usrid = ? and protocolid=?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, userId);
            stmt.setLong(2, protocolId);
            rs = stmt.executeQuery();

            Role role = null;
            while (rs.next()) {
            	role = this.getSystemRole(rs.getInt("roleid"));
            }
            return role;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get User Role: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    public int getUserProtocolSite(int userId, int protocolId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select siteid from protocolusrrole where usrid = ? and protocolid=?");

            
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, userId);
            stmt.setLong(2, protocolId);
            rs = stmt.executeQuery();

            int siteid = 0;
            while (rs.next()) {
            	siteid = rs.getInt("siteid");
            }
            return siteid;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get User Role: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public void updateUserRoleSiteInfo(User u) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from protocolusrrole where usrid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, u.getId());

            rs = stmt.executeQuery();

            Map<Integer,Role> roleMap = new HashMap<Integer,Role>();
            Map<Integer,Integer> siteMap = new HashMap<Integer,Integer>();
            while (rs.next()) {
                roleMap.put(new Integer(rs.getInt("protocolid")), this.getSystemRole(rs.getInt("roleid")));
                if (rs.getString("siteid") != null) {
                    siteMap.put(new Integer(rs.getInt("protocolId")), new Integer(rs.getInt("siteId")));
                }
            }
            u.setProtocolSiteMap(siteMap);
            u.setRoleMap(roleMap);
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get user role map: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }

    }

    /**
     * Retrieves protocolId-role map for this user
     *
     * @param userId The user ID for the current user
     * @return A map containing entries with protocol ID as key and role as value.  The
     *         map will be empty if the user is not associated with any protocol.
     * @throws CtdbException Thrown if any errors occur while processing
     */
    public Map<Integer,Role> getRoleMap(int userId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from protocolusrrole where usrid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, userId);

            rs = stmt.executeQuery();

            Map<Integer,Role> roleMap = new HashMap<Integer,Role>();

            while (rs.next()) {
                roleMap.put(new Integer(rs.getInt("protocolid")), this.getSystemRole(rs.getInt("roleid")));
            }

            return roleMap;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get user role map: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Audits all access attempts to the system. Audit attempts track login (both successful and unsuccessful) and
     * logout trails. There is for tracking purposes.
     *
     * @param aal The AccessAuditLog object to log to the database.
     * @throws CtdbException thrown if any errors occur while processing
     */
    public void auditAccessAttempt(AccessAuditLog aal) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(200);
            sql.append("insert into accessauditlog(accessauditlogid, username, attempttype, successflag, accessdate, failurereason ");
            sql.append(" ,remoteAddress, remoteHost, forwardedHeader, allHeaders )");
            sql.append("values(DEFAULT,?,?,?,CURRENT_TIMESTAMP,?, ?, ?, ?, ?)");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setString(1, aal.getUsername());
            stmt.setString(2, aal.getAttemptType());
            stmt.setBoolean(3, aal.isSuccess());
            stmt.setString(4, aal.getFailureReason());
            stmt.setString(5, aal.getRemoteAddress());
            stmt.setString(6, aal.getRemoteHost());
            stmt.setString(7, aal.getXForwardedFor());
            if (aal.getRequestHeaders().length() < 4000){
                stmt.setString(8, aal.getRequestHeaders());
            } else {
                stmt.setString(8, aal.getRequestHeaders().substring(3990));
            }

            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to audit access attempt: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Creates a CTDB system role in the system.
     *
     * @param role The CTDB role to create
     * @throws DuplicateObjectException is thrown if the role already exists in the system
     * @throws CtdbException            thrown if any other errors occur while processing
     */
    public void createSystemRole(Role role) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(200);
            sql.append("insert into role(roleid, name, description, createdby, createddate, updatedby, updateddate) ");
            sql.append("values(DEFAULT,?,?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP)");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setString(1, role.getName());
            stmt.setString(2, role.getDescription());
            stmt.setLong(3, role.getCreatedBy());
            stmt.setLong(4, role.getUpdatedBy());

            stmt.executeUpdate();
            role.setId(getInsertId(conn, "role_seq"));
        }
        catch (PSQLException e) {
            if (e.getSQLState().equals( SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
                throw new DuplicateObjectException("A role with the name " + role.getName() + " already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to create new SystemRole: " + e.getMessage(), e);
            }
        }
        catch (SQLException e) {
            if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
                throw new DuplicateObjectException("A role with the name " + role.getName() + " already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to create new SystemRole: " + e.getMessage(), e);
            }
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Updates a CTDB system role in the system.
     *
     * @param role The CTDB role to update
     * @throws ObjectNotFoundException  is thrown if the role does not exist in the system
     * @throws DuplicateObjectException is thrown if the role already exists in the system
     * @throws CtdbException            thrown if any other errors occur while processing
     */
    public void updateSystemRole(Role role) throws ObjectNotFoundException, DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(200);
            sql.append("update role set name = ?, description = ?, updatedby = ?, updateddate = CURRENT_TIMESTAMP ");
            sql.append("where roleid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setString(1, role.getName());
            stmt.setString(2, role.getDescription());
            stmt.setLong(3, role.getUpdatedBy());
            stmt.setLong(4, role.getId());

            int recordsUpdated = stmt.executeUpdate();

            if (recordsUpdated == 0) {
                throw new ObjectNotFoundException("The role with ID: " + role.getId() + " does not exist in the system.");
            }
        }
        catch (PSQLException e) {
            if (e.getSQLState().equals( SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
                throw new DuplicateObjectException("A role with the name " + role.getName() + " already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to update role with ID " + role.getId() + ": " + e.getMessage(), e);
            }
        }
        catch (SQLException e) {
            if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
                throw new DuplicateObjectException("A role with the name " + role.getName() + " already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to update role with ID " + role.getId() + ": " + e.getMessage(), e);
            }
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Retrieves a role from the system based on the unique identifier
     *
     * @param roleId The Role ID to retrieve
     * @return Role data object
     * @throws ObjectNotFoundException is thrown if the role does not exist in the system
     * @throws CtdbException           thrown if any other errors occur while processing
     */
    public Role getSystemRole(int roleId) throws ObjectNotFoundException, CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from role where roleid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, roleId);

            rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new ObjectNotFoundException("The role with ID: " + roleId + " could not be found.");
            }

            Role role = this.rsToSystemRole(rs);
            role.setPrivList(this.getRolePrivileges(role.getId()));
            return role;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get SystemRole: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Retrieves all System Roles in the CTDB System
     *
     * @return A list of all System Roles in the CTDB System.
     * @throws CtdbException thrown if any errors occur while processing
     */
    public List<Role> getSystemRoles() throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            List<Role> roles = new ArrayList<Role>();

            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from role order by name");

            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();

            Role role;
            while (rs.next()) {
                role = this.rsToSystemRole(rs);
                role.setPrivList(this.getRolePrivileges(role.getId()));
                roles.add(role);
            }

            return roles;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get SystemRoles: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Associates a Role with Privileges in the CTDB System
     *
     * @param roleId   The unique identifier for the Role to associate privileges with.
     * @param privList The list of Privileges to associate with the Role.
     * @throws CtdbException thrown if any errors occur while processing
     */
    public void associateRolePrivileges(int roleId, List<Privilege> privList) throws CtdbException {
        PreparedStatement dStmt = null;
        PreparedStatement iStmt = null;

        try {
            StringBuffer dSql = new StringBuffer(25);
            dSql.append("delete FROM roleprivilege where roleid = ?");

            StringBuffer iSql = new StringBuffer(25);
            iSql.append("insert into roleprivilege(roleid, privilegeid) ");
            iSql.append("values(?,?)");

            dStmt = this.conn.prepareStatement(dSql.toString());
            dStmt.setLong(1, roleId);

            dStmt.executeUpdate();

            iStmt = this.conn.prepareStatement(iSql.toString());
            iStmt.setLong(1, roleId);

            Privilege privilege;
            for (Iterator<Privilege> iterator = privList.iterator(); iterator.hasNext();) {
                privilege = iterator.next();
                iStmt.setLong(2, privilege.getId());
                iStmt.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to associate Privileges with Role ID: " + roleId + ": " + e.getMessage(), e);
        }
        finally {
            this.close(dStmt);
            this.close(iStmt);
        }
    }

    /**
     * Retrieves a Privilege from the system based on the unique identifier
     *
     * @param privilegeId The SiteLink ID to retrieve
     * @return Privilege data object
     * @throws ObjectNotFoundException is thrown if the site link does not exist in the system
     * @throws CtdbException           thrown if any errors occur while processing
     */
    private Privilege getPrivilege(int privilegeId) throws ObjectNotFoundException, CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from privilege where privilegeid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, privilegeId);

            rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new ObjectNotFoundException("The privilege with ID: " + privilegeId + " could not be found.");
            }

            return this.rsToPrivilege(rs);
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get Privilege: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Retrieves all Privileges in the CTDB System
     *
     * @return A list of all Privileges in the CTDB System.
     * @throws CtdbException thrown if any errors occur while processing
     */
    public List<Privilege> getPrivileges() throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            List<Privilege> privileges = new ArrayList<Privilege>();

            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from privilege order by privilegeid");

            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();

            while (rs.next()) {
                privileges.add(this.rsToPrivilege(rs));
            }

            return privileges;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get Privileges: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Retrieves all Privileges associated with a Role in the CTDB System
     *
     * @param roleId The unique identifier for the Role to retrieve privileges for.
     * @return A list of all Privileges associated with the Role in the CTDB System.
     * @throws CtdbException thrown if any errors occur while processing
     */
    private List<Privilege> getRolePrivileges(int roleId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            List<Privilege> privileges = new ArrayList<Privilege>();

            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from roleprivilege where roleid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, roleId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                privileges.add(this.getPrivilege(rs.getInt("privilegeid")));
            }

            return privileges;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get Privileges associated with Role ID: " + roleId + ": " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Creates a CTDB site link in the system. A site link is a URL that can be
     * accessed through the CTDB system.
     *
     * @param siteLink The CTDB site link to create
     * @throws DuplicateObjectException is thrown if a site link with the same name already exists in the system
     * @throws CtdbException            thrown if any other errors occur while processing
     */
    public void createSiteLink(SiteLink siteLink) throws DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(200);
            sql.append("insert into sitelink(sitelinkid, name, description, address, createdby, createddate, updatedby, updateddate, orderval) ");
            sql.append("select ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, coalesce(max(orderval)+1, 1) from sitelink ");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, this.getNextSequenceValue(this.conn, "sitelink_seq"));
            stmt.setString(2, siteLink.getName());
            stmt.setString(3, siteLink.getDescription());
            stmt.setString(4, siteLink.getAddress());
            stmt.setLong(5, siteLink.getCreatedBy());
            stmt.setLong(6, siteLink.getUpdatedBy());

            stmt.executeUpdate();
        }
        catch (PSQLException e) {
            if (e.getSQLState().equals( SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
                throw new DuplicateObjectException("A site link with the name " + siteLink.getName() + " already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to create new site link: " + e.getMessage(), e);
            }
        }
        catch (SQLException e) {
            if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
                throw new DuplicateObjectException("A site link with the name " + siteLink.getName() + " already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to create new site link: " + e.getMessage(), e);
            }
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Updates a CTDB site link in the system. A site link is a URL that can be
     * accessed through the CTDB system.
     *
     * @param siteLink The CTDB SiteLink to update
     * @throws ObjectNotFoundException  is thrown if the site link does not exist in the system
     * @throws DuplicateObjectException is thrown if a site link with the same name already exists in the system
     * @throws CtdbException            thrown if any other errors occur while processing
     */
    public void updateSiteLink(SiteLink siteLink) throws ObjectNotFoundException, DuplicateObjectException, CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(200);
            sql.append("update sitelink set name = ?, description = ?, address = ?, updatedby = ?, updateddate = CURRENT_TIMESTAMP ");
            sql.append("where sitelinkid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setString(1, siteLink.getName());
            stmt.setString(2, siteLink.getDescription());
            stmt.setString(3, siteLink.getAddress());
            stmt.setLong(4, siteLink.getUpdatedBy());
            stmt.setLong(5, siteLink.getId());

            int recordsUpdated = stmt.executeUpdate();

            if (recordsUpdated == 0) {
                throw new ObjectNotFoundException("The site link with ID: " + siteLink.getId() + " does not exist in the system.");
            }
        }
        catch (PSQLException e) {
            if (e.getSQLState().equals( SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
                throw new DuplicateObjectException("A site link with the name " + siteLink.getName() + " already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to update site link with ID " + siteLink.getId() + ": " + e.getMessage(), e);
            }
        }
        catch (SQLException e) {
            if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
                throw new DuplicateObjectException("A site link with the name " + siteLink.getName() + " already exists in the system: " + e.getMessage(), e);
            } else {
                throw new CtdbException("Unable to update site link with ID " + siteLink.getId() + ": " + e.getMessage(), e);
            }
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Updates the ordering of CTDB site links in the system. A site link is a URL that can be
     * accessed through the CTDB system. This ordering will be used when displaying URLs
     * to the end user, but not to the Administrator. If a URL is not ordered, it will be
     * ordered by name.
     *
     * @param siteLinkId The CTDB SiteLink ID to update
     * @param orderVal   The CTDB SiteLink order
     * @throws CtdbException thrown if any other errors occur while processing
     */
    public void updateSiteLinkOrdering(int siteLinkId, int orderVal) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(200);
            sql.append("update sitelink set orderval = ? where sitelinkid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setInt(1, orderVal);
            stmt.setLong(2, siteLinkId);

            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to update site link order with ID " + siteLinkId + ": " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Retrieves a site link from the system based on the unique identifier
     *
     * @param siteLinkId The SiteLink ID to retrieve
     * @return SiteLink data object
     * @throws ObjectNotFoundException is thrown if the site link does not exist in the system
     * @throws CtdbException           thrown if any other errors occur while processing
     */
    public SiteLink getSiteLink(int siteLinkId) throws ObjectNotFoundException, CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from sitelink where sitelinkid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, siteLinkId);

            rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new ObjectNotFoundException("The site link with ID: " + siteLinkId + " could not be found.");
            }

            return this.rsToSiteLink(rs);
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to retrieve site link: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Retrieves all Site Links in the CTDB System
     *
     * @return A list of all Site Links in the CTDB System.
     * @throws CtdbException thrown if any errors occur while processing
     */
    public List<SiteLink> getSiteLinks() throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            List<SiteLink> siteLinks = new ArrayList<SiteLink>();

            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from sitelink order by orderval, name");

            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();

            while (rs.next()) {
                siteLinks.add(this.rsToSiteLink(rs));
            }

            return siteLinks;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to retrieve all site links: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Deletes a site link from the system based on the unique identifier
     *
     * @param siteLinkId The SiteLink ID to delete
     * @throws CtdbException thrown if any other errors occur while processing
     */
    public void deleteSiteLink(int siteLinkId) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("delete FROM sitelink where sitelinkid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, siteLinkId);

            stmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to delete site link: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }
    
    /**
     * Deletes a site link from the system based on a list unique identifiers
     * 
     * @param siteLinkIds - A list of site link IDs to delete
     * @return	The number of site link records that were deleted
     */
    public int deleteSiteLinks(List<Integer> siteLinkIds)
    {
    	PreparedStatement stmt = null;
    	String sqlStm = "";
    	int numDeleted = 0;
    	
    	try
    	{
    		sqlStm = "delete FROM sitelink where sitelinkid = ? ";
    		stmt = this.conn.prepareStatement(sqlStm);
    		
    		for ( Integer id : siteLinkIds )
    		{
    			stmt.setLong(1, id.intValue());
    			stmt.executeUpdate();
    			numDeleted++;
    		}
    	}
    	catch (SQLException e)
    	{
    		logger.error("Unable to delete site link.", e);
        }
        finally
        {
            this.close(stmt);
        }
    	
    	return numDeleted;
    }

    public int getNumLogins() throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select count(*) from accessauditlog where attempttype='LOGIN' and failurereason is null");

            stmt = this.conn.prepareStatement(sql.toString());
            rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get num logins: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * determines if the password has been used by the user in the last three passwords that
     * the user has selected
     * @param u
     * @param newPassword
     * @return
     * @throws CtdbException
     */
    public boolean isPasswordUsed(User u, String newPassword) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("with lastPasses as ( select password from usedPasswords where userid = ? and rownum < 4 order by useddate)");
            sql.append("select 1 from usedPasswords where ? in (select * from lastPasses) ");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setString(2, newPassword);
            stmt.setLong(1, u.getId());

            rs = stmt.executeQuery();

            return rs.next();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get used passwrods: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * records used passwords for a user fo the ctdb
     * @param u
     * @throws CtdbException
     */
    public void archivePassword(User u) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("insert into usedPasswords (select usrid, password, CURRENT_TIMESTAMP from usr where usrid = ?)");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, u.getId());

            stmt.executeUpdate();

        }
        catch (SQLException e) {
            throw new CtdbException("Unable to archive passwrods: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Refreshes the forms used in the CTSS system
     * @throws CtdbException
     */
    public void refreshCtss() throws CtdbException {
        CallableStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("{ call CTSS_REFRESH }");
            stmt = conn.prepareCall(sql.toString());
            stmt.execute();
        }
        catch (SQLException e) {
            throw new CtdbException("Unable refresh Ctss: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * updates the user password when password is expired
     * @param u
     * @throws CtdbException
     */
    public void updatePassword(User u) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            String sqlStmt = "update usr set password = ?, email = ?, passchanged = CURRENT_TIMESTAMP, updateddate = CURRENT_TIMESTAMP  where usrid = ? ";

            stmt = this.conn.prepareStatement(sqlStmt);
            stmt.setString(1, u.getPassword());
            stmt.setString(2, u.getEmail());
            stmt.setLong(3, u.getId());

            stmt.executeUpdate();

        }
        catch (SQLException e) {
            throw new CtdbException("Unable to update passwrods: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Updates the users security questions / answers used for forgotten password retreival.
     * @param u
     * @param securityInfo
     * @throws CtdbException
     */
    public void updateSecurityInfo (User u, HashMap<Integer,String> securityInfo) throws CtdbException {
        PreparedStatement stmt = null;

        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("delete FROM usrsecurityquestion where usrid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, u.getId());
            stmt.execute();
            stmt.close();

            sql = new StringBuffer(" insert into usrsecurityquestion values (?, ?, ?, CURRENT_TIMESTAMP) ");
            stmt = this.conn.prepareStatement(sql.toString());
            for (Iterator<Integer> iter = securityInfo.keySet().iterator(); iter.hasNext(); ) {
                 int key = iter.next();
                stmt.setLong(1, u.getId());
                stmt.setInt(2, key);
                stmt.setString(3, (String)securityInfo.get(key));
                stmt.addBatch();
            }
            stmt.executeBatch();


        }
        catch (SQLException e) {
            throw new CtdbException("Unable to update security Information: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Retreives the security questions and answers that a user has selected for
     *  forgotten passwrod retrevial
     * @param userId
     * @return
     * @throws CtdbException
     */
    public HashMap<Integer,String> getUserSecurityInfo (int userId) throws CtdbException {
    PreparedStatement stmt = null;
        ResultSet rs = null;
        HashMap<Integer,String> results = new HashMap<Integer,String>();
        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select xsecurityquestionid, usranswer from usrsecurityquestion  where usrid = ?");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, userId);

            rs = stmt.executeQuery();
            while (rs.next()) {
                results.put(rs.getInt(1), rs.getString(2));
            }
            return results;

        }
        catch (SQLException e) {
            throw new CtdbException("Unable to update passwrods: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
            this.close(rs);
        }
    }

    /**
     * Retreives the set of security questions that a user has choosen to answer
     *  while retreiving forgottne passwrods
     * @param userId
     * @return  the set of questions
     * @throws CtdbException
     */
    public HashMap<Integer,String> getAnsweredQuestions(int userId) throws CtdbException {
     PreparedStatement stmt = null;
        ResultSet rs = null;
        HashMap<Integer,String> results = new HashMap<Integer,String>();
        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select xsecurityquestions.xsecurityquestionid, question from usrsecurityquestion, xsecurityquestions ");
            sql.append (" where usrid = ? and xsecurityquestions.xsecurityquestionid = usrsecurityquestion.xsecurityquestionid ");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, userId);

            rs = stmt.executeQuery();
            while (rs.next()) {
                results.put(rs.getInt(1), rs.getString(2));
            }
            return results;

        }
        catch (SQLException e) {
            throw new CtdbException("Unable to update passwrods: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
            this.close(rs);
        }
    }

    /**
     * verifies that the user has entered the correct answers to their choosen
     * questions while attempting to retreive a forgotten password
     * @param securityResponses    the set of security question ids and user answers
     * @param userId
     * @return  if the responses are correct or not
     * @throws CtdbException
     */
    public boolean validateSecurityResponses(HashMap<Integer,String> securityResponses, int userId) throws CtdbException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append (" with a as (select 1 from usrsecurityquestion where usrid = ? ");
		    sql.append  ("  and xsecurityquestionid = ? and usranswer = ?), ");
             sql.append  (" b as (select 1 from usrsecurityquestion where usrid = ?   ");
                sql.append  ("and xsecurityquestionid = ? and usranswer = ?),    ");
            sql.append  ("c as (select 1 from usrsecurityquestion where usrid =?        ");
            sql.append  ("	and xsecurityquestionid = ? and usranswer = ?)   ");
            sql.append  ("select a.* , b.*, c.* from a, b, c   ");
            stmt = this.conn.prepareStatement(sql.toString());
            int j = 1;
            for (Iterator<Integer> i = securityResponses.keySet().iterator(); i.hasNext();){
                Integer key = i.next();
                stmt.setLong(j++, userId);
                stmt.setInt(j++, key.intValue());
                stmt.setString(j++, (String)securityResponses.get(key));
            }

            rs = stmt.executeQuery();
            if (rs.next()) {
              return true;
            }
            return false;

        }
        catch (SQLException e) {
            throw new CtdbException("Unable to validate security responses: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
            this.close(rs);
        }
    }

    /**
     * Records each time a user retrieves a forgotten password and an email is sent
     * @param u
     * @throws CtdbException
     */

    public void recordSentPassword (User u) throws CtdbException {
     PreparedStatement stmt = null;
        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("insert into sentPasswords values (?, CURRENT_TIMESTAMP, ?, ?) ");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, u.getId());
            stmt.setString(2, u.getPassword());
            stmt.setString(3, u.getEmail());
            stmt.execute();
        }
        catch (SQLException e) {
            throw new CtdbException("failure recording sent  passwrod: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * records each time a user fails to retreive a forgotten password.
     * @param u
     * @throws CtdbException
     */
    public void recordFailedPasswordAttempt (User u) throws CtdbException {
     PreparedStatement stmt = null;
        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("insert into failedPasswordRetrevialAttempt values (?, CURRENT_TIMESTAMP) ");

            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, u.getId());
           
            stmt.execute();
        }
        catch (SQLException e) {
            throw new CtdbException("failure recording failed retreival of passwrod: " + e.getMessage(), e);
        }
        finally {
            this.close(stmt);
        }
    }

    public boolean userExcededRetrevialAttempts (User u) throws CtdbException {
         PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            StringBuffer sql = new StringBuffer(50);
            sql.append("select count (usrid) from failedPasswordRetrevialAttempt where failDate > (CURRENT_TIMESTAMP - (1/24)) ");
            sql.append( " and usrid = ? ");
            stmt = this.conn.prepareStatement(sql.toString());
            stmt.setLong(1, u.getId());

            rs = stmt.executeQuery();
           if (rs.next() && rs.getInt(1) >= 3) {
               return true;
           }
            return false;

        }
        catch (SQLException e) {
            throw new CtdbException("Unable to determine if failed passwrod attempts exceeded: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    /**
     * Transforms a ResulSet object into a SiteLink object
     *
     * @param rs ResultSet to transform to SiteLink object
     * @return SiteLink data object
     * @throws SQLException thrown if any errors occur while retrieving data from result set
     */
    private SiteLink rsToSiteLink(ResultSet rs) throws SQLException {
        SiteLink siteLink = new SiteLink();
        siteLink.setId(rs.getInt("sitelinkid"));
        siteLink.setName(rs.getString("name"));
        siteLink.setDescription(rs.getString("description"));
        siteLink.setAddress(rs.getString("address"));
        siteLink.setCreatedBy(rs.getInt("createdby"));
        siteLink.setCreatedDate(rs.getDate("createddate"));
        siteLink.setUpdatedBy(rs.getInt("updatedby"));
        siteLink.setUpdatedDate(rs.getDate("updateddate"));
        return siteLink;
    }

    /**
     * Transforms a ResulSet object into a Role object
     *
     * @param rs ResultSet to transform to Role object
     * @return Role data object
     * @throws SQLException thrown if any errors occur while retrieving data from result set
     */
    private Role rsToSystemRole(ResultSet rs) throws SQLException {
        Role role = new SystemRole();
        role.setId(rs.getInt("roleid"));
        role.setName(rs.getString("name"));
        role.setDescription(rs.getString("description"));
        role.setCreatedBy(rs.getInt("createdby"));
        role.setCreatedDate(rs.getDate("createddate"));
        role.setUpdatedBy(rs.getInt("updatedby"));
        role.setUpdatedDate(rs.getDate("updateddate"));
        return role;
    }

    /**
     * Transforms a ResulSet object into a Privilege object
     *
     * @param rs ResultSet to transform to Privilege object
     * @return Privilege data object
     * @throws SQLException thrown if any errors occur while retrieving data from result set
     */
    private Privilege rsToPrivilege(ResultSet rs) throws SQLException {
        Privilege privilege = new Privilege();
        privilege.setId(rs.getInt("privilegeid"));
        privilege.setName(rs.getString("name"));
        privilege.setCode(rs.getString("code"));
        privilege.setDescription(rs.getString("description"));
        return privilege;
    }

    public User rsToUser(ResultSet rs) throws SQLException {
        User u = new User();
        rsToUser(rs, u);
        return u;
    }

    /**
     * Transforms a ResultSet object into a User object
     *
     * @param rs   ResultSet to transform to User object
     * @param user User object to merge data with.
     * @throws SQLException thrown if any errors occur while retrieving data from result set
     */
    private void rsToUser(ResultSet rs, User user) throws SQLException {

        user.setId(rs.getInt("usrid"));
        user.setBricsUserId(rs.getLong("brics_userid"));
        user.setSysAdmin(rs.getBoolean("sysadminflag"));
        user.setActive(rs.getBoolean("activeflag"));

        user.setCreatedBy(rs.getInt("createdby"));
        user.setCreatedDate(rs.getDate("createddate"));
        user.setUpdatedBy(rs.getInt("updatedby"));
        user.setUpdatedDate(rs.getDate("updateddate"));
        user.setUseTreeView(rs.getBoolean("usetreeview"));
        user.setFirstName(rs.getString("firstname"));
        user.setLastName(rs.getString("lastname"));
        user.setMiddleName(rs.getString("middlename"));
        user.setEmail(rs.getString("email"));
        user.setPhoneNumber(rs.getString("phonenumber"));
        user.setUsername(rs.getString("username"));
        user.setOffice(rs.getString("office"));
        user.setInstituteId(rs.getInt("xinstituteId"));
        user.setStaff(rs.getBoolean("isstaff"));
        user.setPassword(rs.getString("tehPass"));
        user.setEditPasswords(Boolean.parseBoolean(rs.getString("editPasswords")));
        user.setVbrAdmin((rs.getString("vbradmin").toLowerCase().equals("true")));
    }
    
    
    public boolean isPDClinicalCoordinatorRole(User user)  {
    	List<Role> roleList = user.getRoleList();
    	
    	for(Role role : roleList) {
    		logger.info("User id :: " + user.getId()  + " Role name " + role.getName());
    		if(role.getName().equalsIgnoreCase(pdClinicalCoordinatorRole)) {
    			return true;
    		}
    	}
    	return false;
    }
}
