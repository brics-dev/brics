package gov.nih.nichd.ctdb.util.domain;

public class LookupType
{
	private int value = -1;
	private String name = "";
	
	/** Lookup types for ProFoRMS.
     */
    public static final LookupType STATE = new LookupType(1, "xstate");
    public static final LookupType INSTITUTE = new LookupType(2, "xinstitute");
    public static final LookupType FORM_STATUS = new LookupType(3, "xformstatus");
    public static final LookupType PROTOCOL_STATUS = new LookupType(4, "xprotocolstatus");
    public static final LookupType COUNTRY = new LookupType(5, "xcountry");
    public static final LookupType QUESTIONRANGEOPERATOR = new LookupType (8, "questionrangeoperator");
    public static final LookupType RESOLUTIONS = new LookupType(13, "resolutions");
    public static final LookupType PATIENT_SEX = new LookupType(14, "xsex");
    public static final LookupType PATIENT_RACE = new LookupType(15, "xrace");
    public static final LookupType PATIENT_RELIGION = new LookupType(16, "xreligion");
    public static final LookupType PATIENT_MARITAL_STATUS = new LookupType(17, "xmaritalstatus");
    public static final LookupType PATIENT_PREADMIT = new LookupType(18, "xpreadmit");
    public static final LookupType PATIENT_PRIMARY_PROTOCOL = new LookupType(19, "xprimaryprotocol");
    public static final LookupType PATIENT_SECONDARY_PROTOCOL = new LookupType(20, "xsecondaryprotocol");
    public static final LookupType PATIENT_ETHNICITY = new LookupType(21, "xethnicity");
    public static final LookupType PATIENT_EDUCATION = new LookupType(22, "xeducationlevel");
    public static final LookupType PATIENT_OCCUPATION = new LookupType(23, "xoccupation");
    public static final LookupType ATTACHMENT_TYPE = new LookupType(24, "xattachmenttype");
    public static final LookupType CONTACT_TYPES = new LookupType(29, "xexternalcontacttype");
    public static final LookupType PROTOCOL_DEFAULTS = new LookupType(32, "protocoldefaults");
    public static final LookupType SECURITY_QUESTIONS = new LookupType(33, "securityquestions");
    public static final LookupType FORMTYPES = new LookupType(35, "xformtype");
    public static final LookupType BTRIS_ACCESS = new LookupType(38, "btrisaccess");
    public static final LookupType IRB_STATUS = new LookupType(43, "xirbstatus");
    public static final LookupType PUBLICATION_TYPE = new LookupType(44, "publicationtype");
    public static final LookupType QA_QUERY_TYPE = new LookupType(45, "qaquerytype");
    public static final LookupType QA_QUERY_STATUS = new LookupType(46, "qaquerystatus");
    public static final LookupType QA_QUERY_RESOLUTION = new LookupType(47, "qaqueryresolution");
    public static final LookupType QA_QUERY_CLASS = new LookupType(48, "qaqueryclass");
    public static final LookupType QA_QUERY_PRIORITY = new LookupType(49, "qaquerypriority");
    public static final LookupType USER_NAME = new LookupType(51, "username");
    public static final LookupType INTERVAL_TYPE = new LookupType(52, "xintervaltype");
    public static final LookupType EBINDER_TYPE = new LookupType(53, "xbindertype");

    /**
     * Protected Constructor to populate default
     * enumerated types that are set as final variables
     * in the LookupType class.
     *
     * @param value - The int value for the type
     * @param name - The display name for the type
     */
    protected LookupType(int value, String name)
    {
        this.setValue(value);
        this.name = name;
    }

    /**
     * Returns the display value for the question type
     *
     * @return The display value for the LookupType
     */
    public String toString()
    {
        return this.name;
    }

	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(int value) {
		this.value = value;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
