
package gov.nih.tbi.account.model;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

public class SiteMinderHeader implements Serializable
{

    private static final long serialVersionUID = -6168322206421454330L;

    public static final String HEADER_SM_USER = "SM_USER";
    public static final String HEADER_USER_EMAIL = "USER_EMAIL";
    public static final String HEADER_USER_AUTHN_LOA = "USER_AUTHN_LOA";
    public static final String HEADER_USER_AUTHN_SOURCE = "USER_AUTHN_SOURCE";
    public static final String HEADER_USER_AUTHZ_SOURCE = "USER_AUTHZ_SOURCE";
    public static final String HEADER_USER_UPN = "USER_UPN";
    public static final String HEADER_USER_DN = "USER_DN";
    public static final String HEADER_USER_UID = "USER_UID";
    public static final String HEADER_USER_FIRSTNAME = "USER_FIRSTNAME";
    public static final String HEADER_USER_LASTNAME = "USER_LASTNAME";
    public static final String HEADER_USER_MIDDLENAME = "USER_MIDDLENAME";
    public static final String HEADER_USER_ADDRESS = "USER_ADDRESS";
    public static final String HEADER_USER_ORG = "USER_ORG";
    public static final String HEADER_USER_TELEPHONE = "USER_TELEPHONE";
    public static final String HEADER_USER_GROUPS = "USER_GROUPS";

    // variable names are lower cased versions of header variable names
    private String sm_user;
    private String user_email;
    private String user_authn_loa;
    private String user_authn_source;
    private String user_authz_source;
    private String user_upn;
    private String user_dn;
    private String user_uid;
    private String user_firstname;
    private String user_lastname;
    private String user_middlename;
    private String user_address;
    private String user_org;
    private String user_telephone;
    private String user_groups;

    public SiteMinderHeader(HttpServletRequest request)
    {

        sm_user = request.getHeader(HEADER_SM_USER);
        user_email = request.getHeader(HEADER_USER_EMAIL);
        user_authn_loa = request.getHeader(HEADER_USER_AUTHN_LOA);
        user_authn_source = request.getHeader(HEADER_USER_AUTHN_SOURCE);
        user_authz_source = request.getHeader(HEADER_USER_AUTHZ_SOURCE);
        user_upn = request.getHeader(HEADER_USER_UPN);
        user_dn = request.getHeader(HEADER_USER_DN);
        user_uid = request.getHeader(HEADER_USER_UID);
        user_firstname = request.getHeader(HEADER_USER_FIRSTNAME);
        user_lastname = request.getHeader(HEADER_USER_LASTNAME);
        user_middlename = request.getHeader(HEADER_USER_MIDDLENAME);
        user_address = request.getHeader(HEADER_USER_ADDRESS);
        user_org = request.getHeader(HEADER_USER_ORG);
        user_telephone = request.getHeader(HEADER_USER_TELEPHONE).replace(".", "");
        user_groups = request.getHeader(HEADER_USER_GROUPS);

    }

    public String getSm_user()
    {

        return sm_user;
    }

    public void setSm_user(String sm_user)
    {

        this.sm_user = sm_user;
    }

    public String getUser_email()
    {

        return user_email;
    }

    public void setUser_email(String user_email)
    {

        this.user_email = user_email;
    }

    public String getUser_authn_loa()
    {

        return user_authn_loa;
    }

    public void setUser_authn_loa(String user_authn_loa)
    {

        this.user_authn_loa = user_authn_loa;
    }

    public String getUser_authn_source()
    {

        return user_authn_source;
    }

    public void setUser_authn_source(String user_authn_source)
    {

        this.user_authn_source = user_authn_source;
    }

    public String getUser_authz_source()
    {

        return user_authz_source;
    }

    public void setUser_authz_source(String user_authz_source)
    {

        this.user_authz_source = user_authz_source;
    }

    public String getUser_upn()
    {

        return user_upn;
    }

    public void setUser_upn(String user_upn)
    {

        this.user_upn = user_upn;
    }

    public String getUser_dn()
    {

        return user_dn;
    }

    public void setUser_dn(String user_dn)
    {

        this.user_dn = user_dn;
    }

    public String getUser_uid()
    {

        return user_uid;
    }

    public void setUser_uid(String user_uid)
    {

        this.user_uid = user_uid;
    }

    public String getUser_firstname()
    {

        return user_firstname;
    }

    public void setUser_firstname(String user_firstname)
    {

        this.user_firstname = user_firstname;
    }

    public String getUser_lastname()
    {

        return user_lastname;
    }

    public void setUser_lastname(String user_lastname)
    {

        this.user_lastname = user_lastname;
    }

    public String getUser_middlename()
    {

        return user_middlename;
    }

    public void setUser_middlename(String user_middlename)
    {

        this.user_middlename = user_middlename;
    }

    public String getUser_address()
    {

        return user_address;
    }

    public void setUser_address(String user_address)
    {

        this.user_address = user_address;
    }

    public String getUser_org()
    {

        return user_org;
    }

    public void setUser_org(String user_org)
    {

        this.user_org = user_org;
    }

    public String getUser_telephone()
    {

        return user_telephone;
    }

    public void setUser_telephone(String user_telephone)
    {

        this.user_telephone = user_telephone;
    }

    public String getUser_groups()
    {

        return user_groups;
    }

    public void setUser_groups(String user_groups)
    {

        this.user_groups = user_groups;
    }
}
