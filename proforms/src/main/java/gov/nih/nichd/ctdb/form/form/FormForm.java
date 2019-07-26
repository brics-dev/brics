package gov.nih.nichd.ctdb.form.form;


import gov.nih.nichd.ctdb.common.CtdbForm;
import gov.nih.nichd.ctdb.response.domain.DataEntryWorkflowType;

/**
 * The FormForm is the JAVA class to represent the data
 * users enter to create or change forms assigned to a protocol
 * in nichd ctdb form module.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */

/**
 * @author pandyan
 *
 */
public class FormForm extends CtdbForm
{
	private static final long serialVersionUID = 7874247916575567768L;
	private String name = null;
    private String status = null;
    private String description = null;
    private String formborder = "yes";
    private String sectionborder = "yes";
    private String formfont = "arial";
    private String formcolor = "black";
    private String sectionfont = "arial";
    private String sectioncolor = "black";
    private int dataEntryFlag = 1;
    private int accessFlag = 1;
    private int isAdministered = 0;
    private String formHeader = null;
    private String formFooter = null;

    private int[]  formGroups = null;
    private int[]  availiableFormGroups = null;

    private int fontSize = 10;
    private int dataEntryWorkflowType = DataEntryWorkflowType.EXPRESS.getValue();
    private int dataEntryFlagNo = 1;

    private int cellpadding = 2;

    private boolean attachFiles = false; //this flag is the old way of uploading filer attachments...now that we have a question type called File, this will always
    									//be false because that way the Manage Attachments link will not show up on data collection

    private boolean dataSpring = false;

    private boolean tabdisplay = false; // display using tabs !?

    private int formtypeid = 10;
    private int formid;
    
    private int nonpatientformtypeid = 0;
    
    // added by Ching Heng for adding question
    private String questionTypeDisplay;
    private String[] options;

    private String dataStructureName = "none";
    private String dataStructureVersion = "";
   
	//add for copyright
	private boolean copyRight = false;  
	
	//for allowing multiple instances of data collections for same form
	private boolean allowMultipleCollectionInstances = false;
    
	private String descriptionUp;
	private String descriptionDown;  
	
	private String statusHidden = null;
    
    public String getStatusHidden() {
		return statusHidden;
	}

	public void setStatusHidden(String statusHidden) {
		this.statusHidden = statusHidden;
	}

	public String getDataStructureName() {
		return dataStructureName;
	}

	public void setDataStructureName(String dataStructureName) {
		this.dataStructureName = dataStructureName;
	}


	
	public String getDataStructureVersion() {
		return dataStructureVersion;
	}

	public void setDataStructureVersion(String dataStructureVersion) {
		this.dataStructureVersion = dataStructureVersion;
	}

	public int getFormid() {
		return formid;
	}

	public void setFormid(int formid) {
		this.setId(formid);
		this.formid = formid;
	}

	/**
     * Get the isAdministered flag.
     * @return the isAdministered flag
     */
    public int getIsAdministered()
    {
        return isAdministered;
    }

    /**
     * Set the isAdministered flag.
     * @param isAdministered the new isAdministered flag
     */
    public void setIsAdministered(int isAdministered)
    {
        this.isAdministered = isAdministered;
    }

    /**
     * Get the DataEntryFlag value.
     * @return the DataEntryFlag value.
     */
    public int getDataEntryFlag() {

        return dataEntryFlag;
    }

    /*public String getFormType() {
    	
		return formType;
	}

	public void setFormType(String formType) {
		this.formType = formType;
	}*/

	/**
     * Set the DataEntryFlag value.
     * @param newDataEntryFlag The new DataEntryFlag value.
     */
    public void setDataEntryFlag(int newDataEntryFlag) {
        this.dataEntryFlag = newDataEntryFlag;
    }

    /**
     * Get the accessFlag value.
     * @return the accessFlag value.
     */
    public int getAccessFlag() {

        return accessFlag;
    }

    public int getNonpatientformtypeid() {
		return nonpatientformtypeid;
	}

	public void setNonpatientformtypeid(int nonpatientformtypeid) {
		this.nonpatientformtypeid = nonpatientformtypeid;
	}

	/**
     * Set the accessFlag value.
     * @param newAccessFlag The new accessFlag value.
     */
    public void setAccessFlag(int newAccessFlag) {
        this.accessFlag = newAccessFlag;
    }

    /**
     * Sets the form name.
     *
     * @param name The form name entered.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the form name.
     *
     * @return The form name
     */
    public String getName()
    {
    	
        return name;
    }

    /**
     * Sets the form status.
     *
     * @param status The form status entered.
     */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * Gets the form status.
     *
     * @return The form status
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * Sets the description about this form
     *
     * @param description The form's description user entered.
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Gets the form's description.
     *
     * @return The form's description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets whether the form has border.
     *
     * @param formborder The form has border if equal to "yes".
     */
    public void setFormborder(String formborder)
    {
        this.formborder = formborder;
    }

    /**
     * Gets the form border property.
     *
     * @return The form's border property
     */
    public String getFormborder()
    {
        return formborder;
    }

    /**
     * Sets whether the section has border.
     *
     * @param sectionborder The section has border if equal to "yes".
     */
    public void setSectionborder(String sectionborder)
    {
        this.sectionborder = sectionborder;
    }

    /**
     * Gets the section border property.
     *
     * @return The section's border property
     */
    public String getSectionborder()
    {
        return sectionborder;
    }

    public boolean isAllowMultipleCollectionInstances() {
		return allowMultipleCollectionInstances;
	}

	public void setAllowMultipleCollectionInstances(
			boolean allowMultipleCollectionInstances) {
		this.allowMultipleCollectionInstances = allowMultipleCollectionInstances;
	}

	/**
     * Sets form's font property for display.
     *
     * @param formfont the form's display font.
     */
    public void setFormfont(String formfont)
    {
        this.formfont = formfont;
    }

    /**
     * Gets the form's font property.
     *
     * @return The form's display font property
     */
    public String getFormfont()
    {
        return formfont;
    }

    /**
     * Sets form's color property for display.
     *
     * @param formcolor the form's display color.
     */
    public void setFormcolor(String formcolor)
    {
        this.formcolor = formcolor;
    }

    /**
     * Gets the form's color property.
     *
     * @return The form's display color property
     */
    public String getFormcolor()
    {
        return formcolor;
    }

    /**
     * Sets section's font property for display.
     *
     * @param sectionfont the section's display font.
     */
    public void setSectionfont(String sectionfont)
    {
        this.sectionfont = sectionfont;
    }

    /**
     * Gets the section's font property.
     *
     * @return The section's display font property
     */
    public String getSectionfont()
    {
        return sectionfont;
    }

    /**
     * Sets section's color property for display.
     *
     * @param sectioncolor the section's display color.
     */
    public void setSectioncolor(String sectioncolor)
    {
        this.sectioncolor = sectioncolor;
    }

    /**
     * Gets the section's color property.
     *
     * @return The section's display color property
     */
    public String getSectioncolor()
    {
        return sectioncolor;
    }

    /**
     * Sets the form header.
     *
     * @param header The form header entered.
     */
    public void setFormHeader(String header)
    {
        this.formHeader = header;
    }

    /**
     * Gets the form header.
     *
     * @return The form header
     */
    public String getFormHeader()
    {

        return formHeader;
    }

    /**
     * Sets the form footer.
     *
     * @param footer The form footer entered.
     */
    public void setFormFooter(String footer)
    {
        this.formFooter = footer;
    }

    /**
     * Gets the form footer.
     *
     * @return The form footer
     */
    public String getFormFooter()
    {
        return formFooter;
    }

    public int[] getFormGroups() {
        return formGroups;
    }

    public void setFormGroups(int[] formGroups) {
        this.formGroups = formGroups;
    }

    public int[] getAvailiableFormGroups() {
        return availiableFormGroups;
    }

    public void setAvailiableFormGroups(int[] availiableFormGroups) {
        this.availiableFormGroups = availiableFormGroups;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getDataEntryWorkflowType() {
        return dataEntryWorkflowType;
    }

    public void setDataEntryWorkflowType(int dataEntryWorkflowType) {
        this.dataEntryWorkflowType = dataEntryWorkflowType;
    }

    public int getDataEntryFlagNo() {
        return dataEntryFlagNo;
    }

    public void setDataEntryFlagNo(int dataEntryFlagNo) {
        this.dataEntryFlagNo = dataEntryFlagNo;
    }

    public int getCellpadding() {
        return cellpadding;
    }

    public void setCellpadding(int cellpadding) {
        this.cellpadding = cellpadding;
    }

    public boolean isAttachFiles() {
        return attachFiles;
    }

    public void setAttachFiles(boolean attachFiles) {
        this.attachFiles = attachFiles;
    }


    public boolean isDataSpring() {
        return dataSpring;
    }

    public void setDataSpring(boolean dataSpring) {
        this.dataSpring = dataSpring;
    }


    public boolean isTabdisplay() {
        return tabdisplay;
    }

    public void setTabdisplay(boolean tabdisplay) {
        this.tabdisplay = tabdisplay;
    }

    public int getFormtypeid() {
        return formtypeid;
    }

    public void setFormtypeid(int formtypeid) {
        this.formtypeid = formtypeid;
    }
    //  added by Ching Heng for adding question =================================
    /**
     * get display for questiojn type
     * @return
     */
    public String getQuestionTypeDisplay() {
        return questionTypeDisplay;
    }

    /**
     * sets the question type display
     * @param questionTypeDisplay
     */
    public void setQuestionTypeDisplay(String questionTypeDisplay) {
        this.questionTypeDisplay = questionTypeDisplay;
    }
    
    /**
     * Return the options.
     *
     * @return The options for the question.
     */
    public String[] getOptions()
    {
        return options;
    }

    /**
     * Set the options for this question.
     *
     * @param options The options for the question.
     */
    public void setOptions(String[] options)
    {
        this.options = options;
    }

    
    public boolean isCopyRight() {
		return copyRight;
	}

	public void setCopyRight(boolean copyRight) {
		this.copyRight = copyRight;
	}   
    
	public String getDescriptionUp() {
		return descriptionUp;
	}
	public void setDescriptionUp(String descriptionUp) {
		this.descriptionUp = descriptionUp;
	}
	public String getDescriptionDown() {
		return descriptionDown;
	}
	public void setDescriptionDown(String descriptionDown) {
		this.descriptionDown = descriptionDown;
	}      
}
