package gov.nih.nichd.ctdb.security.common;

import gov.nih.nichd.ctdb.common.ResultControl;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: May 2, 2006
 * Time: 7:24:41 AM
 * To change this template use File | Settings | File Templates.
 * 
 * @deprecated This is a rats nest of SQL injection attacks that are waiting to happen. Please do not use any of the methods in this class, 
 * and please take time to removing the usage of this class from your code.
 */
public class UserResultControl extends ResultControl {

    public static final String SORT_BY_USERNAME = "username";
    public static final String SORT_BY_FIRSTNAME = "firstname";
    public static final String SORT_BY_LASTNAME = "lastname";
    public static final String SORT_BY_EMAIL = "email";

    private String firstName;
    private String lastName;
    private String email;
    private int instituteId = Integer.MIN_VALUE;
    private String staff;
    private int siteId = Integer.MIN_VALUE;


    public UserResultControl () {
        this.setSortBy(SORT_BY_USERNAME);
        this.setSortOrder(SORT_ASC);
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


    public String getStaff() {
        return staff;
    }

    public void setStaff(String staff) {
        this.staff = staff;
    }


    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public StringBuffer getFirstNameClause () {
        if (this.firstName != null && !this.firstName.trim().equals("")) {
            return new StringBuffer (" and upper(usr.firstname) like '%").append(firstName.toUpperCase().replaceAll("'", "''")).append("%' ");
        } else {
            return new StringBuffer("");
        }
    }

     public StringBuffer getLastNameClause () {
        if (this.lastName != null && !this.lastName.trim().equals("")) {
            return new StringBuffer (" and upper(usr.lastname) like '%").append(lastName.toUpperCase().replaceAll("'", "''")).append("%' ");
        } else {
            return new StringBuffer("");
        }
    }

     public StringBuffer getEmailClause () {
        if (this.email != null && !this.email.trim().equals("")) {
            return new StringBuffer (" and upper(usr.email) like '%").append(email.toUpperCase().replaceAll("'", "''")).append("%' ");
        } else {
            return new StringBuffer("");
        }
    }

     public StringBuffer getInstituteClause () {
        if (this.instituteId != Integer.MIN_VALUE) {
            return new StringBuffer (" and usr.xinstituteId = ").append(instituteId).append(" ");
        } else {
            return new StringBuffer("");
        }
    }

    public StringBuffer getStaffClause() {
        if (this.staff != null && this.staff.length() > 0 && !staff.equals("all")) {
        	if (staff.toUpperCase().equals("FALSE")) {
        		return new StringBuffer(" and usr.isStaff IS FALSE ");
        	}
        	else {
        		return new StringBuffer(" and usr.isStaff IS TRUE ");
        	}
            //return new StringBuffer ("  and usr.isStaff = '").append(staff.toUpperCase()).append( "' ");
        } else {
            return new StringBuffer();
        }
    }

    public StringBuffer getOrderByClause() {
        return new StringBuffer( " order by ").append(this.getSortBy()).append(" ").append(this.getSortOrder());
    }
    

    public String getSearchClause() {
        StringBuffer sb = new StringBuffer();
        sb.append( " where 1 = 1  ");
        sb.append (getFirstNameClause());
        sb.append (getLastNameClause());
        sb.append( getEmailClause());
        sb.append (getInstituteClause());
        sb.append(getStaffClause()); 
        sb.append( getOrderByClause());
        return sb.toString();


    }
}
