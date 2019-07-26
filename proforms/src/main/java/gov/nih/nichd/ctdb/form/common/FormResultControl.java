package gov.nih.nichd.ctdb.form.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.ResultControl;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;


/**
 * FormResultControl handles searching and sorting of form records in the system.
 *
 * @deprecated This is a rats nest of SQL injection attacks that are waiting to happen. Please do not use any of the methods in this class, 
 * and please take time to removing the usage of this class from your code.
 * @author  Booz Allen Hamilton
 * @version 1.0
 */

public class FormResultControl extends ResultControl
{
    /**
     *  FormResultControl Constant used to sort by form name
     */
    public static final String SORT_BY_NAME = "form.name";

    /**
     *  FormResultControl Constant used to sort by protocol name
     */
    public static final String SORT_BY_PROTOCOLNAME = "protocol.name";

    /**
     *  FormResultControl Constant used to sort by number
     */
    public static final String SORT_BY_NUMBER = "form.formid";

    /**
     *  FormResultControl Constant used to sort by status
     */
    public static final String SORT_BY_STATUS = "form.xstatusid";

    /**
     *  FormResultControl Constant used to sort by whether they are administered
     *  or not
     */
    public static final String SORT_BY_ADMINISTERED = "isadministered";

    /**
     *  FormResultControl Constant used to sort by lastupdated
     */
    public static final String SORT_BY_LASTUPDATED = "form.updateddate";

    public static final String PatientFormType = " 10 ";
    public static final String NonPatientFormType = " form.formtypeid != 10 ";


    private String name;
    private CtdbLookup status;
    private CtdbLookup type;
    private int isAdministed;
    private Date updatedDate;
    private String protocolName;
    private Collection notIn;
    private int isType;
    private String formType = PatientFormType;



    /**
     * Default constructor for the FormResultControl
     *
     */
    public FormResultControl()
    {
        // default constructor
        this.setSortBy(FormResultControl.SORT_BY_NUMBER);
    }

    /**
     * Gets the form name as to search form
     *
     * @return The form name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the form name to search for
     *
     * @param name  The form name to search for
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the protocol name as to search form
     *
     * @return The protocol name
     */
    public String getProtocolName()
    {
        return protocolName;
    }

    /**
     * Sets the protocol name to search for
     *
     * @param name  The protocol name to search for
     */
    public void setProtocolName(String name)
    {
        this.protocolName = name;
    }

    /**
     * Gets the form's name search string to be used by the database
     *
     * @return The search string to be used by the database
     */
    public String getNameSearchString()
    {
        String formName = this.name;
        if(formName != null && !formName.equalsIgnoreCase(""))
        {
            String formName1 = this.replaceQuotes(formName);
            
            if (formName1.indexOf("%") != -1)
            {
            	formName1 = this.escapePercentageSign(formName1);

            	return " and upper(form.name) like upper('" + formName1.toUpperCase() + "') escape '^' ";
            }
            else 
	            return " and upper(form.name) like upper('" + formName1 + "') ";
        }
        else
        {
            return " ";
        }
    }

    /**
     * Gets the protocol's name search string to be used by the database
     *
     * @return The search string to be used by the database
     */
    public String getProtocolNameSearchString()
    {
        String protocolName = this.protocolName;
        if(protocolName != null && !protocolName.equalsIgnoreCase(""))
        {
            String protocolName1 = this.replaceQuotes(protocolName);

            if (protocolName1.indexOf("%") != -1)
            {
            	protocolName1 = this.escapePercentageSign(protocolName1);

            	return " and upper(protocol.name) like upper('%" + protocolName1.toUpperCase() + "%') escape '^' ";
            }
            else
	            return " and upper(protocol.name) like upper('%" + protocolName1 + "%') ";
        }
        else 
        {
            return " ";
        }
    }

    /**
     * Gets the form's status
     *
     * @return  Form Status
     */
    public CtdbLookup getStatus()
    {
        return status;
    }

    /**
     * Sets the form's status
     *
     * @param   status  The form status
     */
    public void setStatus(CtdbLookup status)
    {
        this.status = status;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }
    
    public CtdbLookup getType() {
		return type;
	}

	public void setType(CtdbLookup type) {
		this.type = type;
	}

    /**
     * Gets the form status ID search string to be used by the database
     *
     * @return The search string to be used by the database
     */
    private String getStatusSearchString()
    {
        if(this.status != null)
        {
            return " and form.xstatusid = " + this.status.getId() + " ";
        }
        else
        {
            return " ";
        }
    }
    
    
    private String getTypeSearchString()
    {
        if(this.type != null)
        {
            return " and form.formtypeid = " + this.type.getId() + " ";
        }
        else
        {
            return " ";
        }
    }

    /**
     * Gets the isAdministed flag for search
     *
     * @return  The isAdministed flag for search; 0: All; 1: isAdministed; 2: not administed;
     */
    public int getIsAdministed()
    {
        return isAdministed;
    }

    /**
     * Sets the isAdministed flag for search
     *
     * @param   isAdministed  The search flag for search
     */
    public void setIsAdministed(int isAdministed)
    {
        this.isAdministed = isAdministed;
    }

    /**
     * Gets the form administed search string to be used by the database
     *
     * @return The search string to be used by the database
     */
    private String getIsAdministedSearchString()
    {
        if(this.isAdministed == 1)
        {
            return " and decode(intervalid, null, 0, 1) = 1 ";
        }
        else if(this.isAdministed == 2)
        {
            return " and decode(intervalid, null, 0, 1) = 0 ";
        }
        else
        {
            return " ";
        }
    }
    
    
    private String getNpTypeSearchString()
    {
        if(this.isType == 1)
        {
            return "and form.formtypeid = 12 ";//Protocol
        }
        else if(this.isType == 2)
        {
            return "and  form.formtypeid = 13 ";//Project
        }
        else if(this.isType == 3)
        {
        	 return "and  form.formtypeid = 14 ";//Admin
        }
        else if(this.isType == 4)
        {
        	 return "and  form.formtypeid = 15 ";//Other
        }
        else
        {
            return " ";
        }
    }


    /**
     * Gets the updated date for search
     *
     * @return  The form updated date for search
     */
    public Date getUpdatedDate()
    {
        return updatedDate;
    }

    /**
     * Sets the updated date for search
     *
     * @param updatedDate  The form updated date for search
     */
    public void setUpdatedDate(Date updatedDate)
    {
        this.updatedDate = updatedDate;
    }

    public Collection getNotIn() {
        return notIn;
    }

    public void setNotIn(Collection notIn) {
        this.notIn = notIn;
    }

    /**
     * Gets the form updated date search string to be used by the database
     *
     * @return The search string to be used by the database
     */
    public String getUpdatedDateSearchString()
    {
        if(this.updatedDate != null)
        {
            String dateFormat = SysPropUtil.getProperty("default.system.dateformat");
            DateFormat formatter = new SimpleDateFormat(dateFormat);
            String updtDate = formatter.format(this.updatedDate);
            return " and date(form.updateddate) = to_date('" + updtDate + "', '" + dateFormat + "') ";
        }
        else
        {
            return " ";
        }
    }

    public String getNotInClause () {
        if (this.notIn != null && this.notIn.size()> 0) {
            StringBuffer sb = new StringBuffer(" and form.formid not in (");
            for (Iterator i = notIn.iterator(); i.hasNext();) {
                sb.append(i.next()).append(", ");
            }
            sb = sb.deleteCharAt(sb.length()-1);
            sb = sb.deleteCharAt(sb.length()-1);
            sb.append(" ) ");
            return sb.toString();
        } else {
            return " ";
        }
    }

    public String getFormTypeClause () {
        if (formType.equals(NonPatientFormType)) {
            return " and " + formType + " ";
        }else if (formType.equals(PatientFormType)){
            return " and form.formtypeid = " + formType + " ";
        }else{
        	return " ";
        }
    }

    /**
     * Gets the Search Clause for this SQL operation to determine the results
     *
     * @return The string representation of the search clause for this SQL operation
     */
    public String getSearchClause()
    {
        StringBuffer clause = new StringBuffer(100);
        clause.append(this.getNameSearchString());
        clause.append(this.getStatusSearchString());
        clause.append(this.getIsAdministedSearchString());
        clause.append(this.getUpdatedDateSearchString());
        clause.append(this.getFormTypeClause());
        clause.append(this.getTypeSearchString());
        clause.append(this.getNpTypeSearchString());
        return clause.toString();
    }

    /**
     * Gets the Search Clause for this SQL operation to determine the results
     *
     * @return The string representation of the search clause for this SQL operation
     */
    public String getPublicFormSearchClause()
    {
        StringBuffer clause = new StringBuffer(100);
        clause.append(this.getNameSearchString());
        clause.append(this.getStatusSearchString());
        clause.append(this.getProtocolNameSearchString());
        clause.append(this.getUpdatedDateSearchString());
        clause.append(this.getFormTypeClause());
        return clause.toString();
    }

	public int getIsType() {
		return isType;
	}

	public void setIsType(int isType) {
		this.isType = isType;
	}

	
}
