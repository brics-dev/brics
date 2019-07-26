package gov.nih.nichd.ctdb.protocol.domain;

import gov.nih.nichd.ctdb.security.domain.User;

/**
 * ProtocolUser DomainObject for the NICHD CTDB Application. The ProtocolUser
 * object is a sub-class of User and allows customization of a user within
 * a protocol.  Each protocol user has a roleId assigned.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ProtocolUser extends User
{
    private int roleId;

    /**
     * Default Constructor for the ProtocolRole Domain Object
     */
    public ProtocolUser()
    {
        // default constructor
    }

    /**
     * Gets the protocol user's role ID in the system
     *
     * @return  The role ID of the user
     */
    public int getRoleId()
    {
        return roleId;
    }

    /**
     * Sets the protocol user's role ID in the system
     *
     * @param  roleId The user's role ID for this Protocol
     */
    public void setRoleId(int roleId)
    {
        this.roleId = roleId;
    }

    /**
     * Initializes a protocoluser object based on a user object
     *
     * @param  user User object
     */
    public void setUser(User user)
    {
        this.setId(user.getId());
        this.setUsername(user.getUsername());
        this.setPassword(user.getPassword());
        this.setFirstName(user.getFirstName());
        this.setMiddleName(user.getMiddleName());
        this.setLastName(user.getLastName());
        this.setOffice(user.getOffice());
        this.setPhoneNumber(user.getPhoneNumber());
        this.setEmail(user.getEmail());
        this.setSysAdmin(user.isSysAdmin());
        this.setPasswordExpired(user.isPasswordExpired());
        this.setRoleList(user.getRoleList());
        this.setRoleMap(user.getRoleMap());
        this.setUseTreeView(user.isUseTreeView());
        this.setDisplayName(user.getDisplayName());
        this.setStaff(user.isStaff());
        this.setProtocolSiteMap(user.getProtocolSiteMap());
        this.setInstituteId(user.getInstituteId());
        this.setEditPasswords(user.isEditPasswords());
        this.setRefreshCtss(user.isRefreshCtss());
        this.setVbrAdmin(user.isVbrAdmin());
        this.setCtdbLookupStringForDisplay(user.getCtdbLookupStringForDisplay());
        this.setCreatedBy(user.getCreatedBy());
        this.setCreatedDate(user.getCreatedDate());
        this.setUpdatedBy(user.getUpdatedBy());
        this.setUpdatedDate(user.getUpdatedDate());
    }
}
