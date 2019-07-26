package gov.nih.nichd.ctdb.protocol.util;

import java.util.ArrayList;
import java.util.List;

import gov.nih.nichd.ctdb.security.domain.Role;
import gov.nih.nichd.ctdb.security.domain.User;

/**
 * ProtocolUserRoleElement manages alist of users associated with a role.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ProtocolUserRoleElement
{

    private Role role;
    private List<User> users;

    /**
     * Default constructor
     */
    public ProtocolUserRoleElement()
    {
        users = new ArrayList<User>();
    }

    /**
     * Gets the role this role element represents
     *
     * @return  The role
     */
    public Role getRole()
    {
        return this.role;
    }

    /**
     * Sets the role this roleElement represents
     *
     * @param role The role
     */
    public void setRole(Role role)
    {
        this.role = role;
    }

    /**
     * Gets the users in this RoleElement
     *
     * @return The users
     */
    public List<User> getUsers()
    {
        return this.users;
    }

    /**
     * Assign users to this roleElement
     *
     * @param users The users to assign
     */
    public void setUsers(List<User> users)
    {
        this.users = users;
    }

}
