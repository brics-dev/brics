package gov.nih.nichd.ctdb.question.form;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbForm;

import java.util.List;
import java.util.Map;

/**
 * Created by Booz Allen Hamilton
 * Date: Aug 18, 2004
 * */
public class QuestionWizardStartForm extends CtdbForm  {

    private String name;
    private String text;
    private int type = 1;
    private boolean calDependent = false;
    private String questionTypeDisplay;
    private String medicalCodingStatus;
    private int editableType = 0;
    
    private String defaultValue;
    private String unansweredValue;
    private int[] questionGroupIds;
    private int[] availableQuestionGroups;
    
    private List currentNames;
    private int imageCount;   // number of images to add
    private Map files;
    private String[] namesToDelete = {};
    
    private int rangeStart = 1;
    private int rangeEnd = 100;
    private int width = 100;
    private String rightText;
    private String leftText;
    private String centerText;
    private boolean showHandle;
    
    private String[] options;
    private String[] origOptions;
    
	private String descriptionUp;
	private String descriptionDown;  
	
	private String htmlText; // added by Josh Park


	/**
     * Get the name from the form
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name on the form
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
 *  get text from form
     * @return
     */
    public String getText() {
        return text;
    }

    /**
     * Set the text on the form
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * get type from form
     * @return
     */
    public int getType() {
        return type;
    }

    /**
     * Set the type on the form
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }
   /**
    * get cal dependant attribute
    * @return
    */
    public boolean isCalDependent() {
        return calDependent;
    }

    /**
     * Set the the cal dependatn attribute on the form
     * @param calDependent
     */
    public void setCalDependent(boolean calDependent) {
        this.calDependent = calDependent;
    }

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
     * get editable type attribute
     * @return
     */
    public int getEditableType() {
        return editableType;
    }

    /**
     * sets editable type attribute
     * @param editableType
     */
    public void setEditableType(int editableType) {
        this.editableType = editableType;
    }

    /**
     * returns a question medicicalcoding status
     * @return String
     */
    public String getMedicalCodingStatus() {
        return medicalCodingStatus;
    }

    /**
     * Set question medicalcoding status
     * @param medicalCodingStatus
     */
    public void setMedicalCodingStatus(String medicalCodingStatus) {
        this.medicalCodingStatus = medicalCodingStatus;
    }   

	/**---------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	 * moved by Ching-Heng from QuestionWizardGroupUmlsForm
     * Get the DefaultValue from the form
     * @return
     */
    public String getDefaultValue() {
		return defaultValue;
	}
    /**QuestionWizardGroupUmlsForm
     * Set the DefaultValue on the form
     * @param name
     */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	/**QuestionWizardGroupUmlsForm
     * Get the UnansweredValue from the form
     * @return
     */
	public String getUnansweredValue() {
		return unansweredValue;
	}
	/**QuestionWizardGroupUmlsForm
     * Set the UnansweredValue on the form
     * @param name
     */
	public void setUnansweredValue(String unansweredValue) {
		this.unansweredValue = unansweredValue;
	}
	/**QuestionWizardGroupUmlsForm**/
    public int[] getQuestionGroupIds() {
		return questionGroupIds;
	}
    /**QuestionWizardGroupUmlsForm**/
	public void setQuestionGroupIds(int[] questionGroupIds) {
		this.questionGroupIds = questionGroupIds;
	}
	/**QuestionWizardGroupUmlsForm**/
    public int[] getAvailableQuestionGroups() {
		return availableQuestionGroups;
	}
    /**QuestionWizardGroupUmlsForm**/
	public void setAvailableQuestionGroups(int[] availableQuestionGroups) {
		this.availableQuestionGroups = availableQuestionGroups;
	}
	
	
	/**--------------------------------------------------------------------------------------------------------------------------
     * moved by Ching-Heng from QuestionImageForm
     * 
     * Return the map for image files.
     * @return The map which holds image files.
     */
    public Map getFiles()
    {
        return files;
    }
    /**QuestionImageForm
     * Return a string array of the image file names for deletion.
     * @return The array of image file names.
     */
    public String[] getNamesToDelete()
    {
        return namesToDelete;
    }

    /**QuestionImageForm
     * Set the image file name array.
     * @param namesToDelete The image file name array to be set for deletion.
     */
    public void setNamesToDelete(String[] namesToDelete)
    {
        this.namesToDelete = namesToDelete;
    }
    /**QuestionImageForm
     * Return the image count.
     *
     * @return The number of image files.
     */
    public int getImageCount() {
		return imageCount;
	}
    /**QuestionImageForm
     * Set the image count.
     *
     * @param imageCount int the image count.
     */
	public void setImageCount(int imageCount) {
		this.imageCount = imageCount;
	}

    /**QuestionImageForm
     * Return a string list of the current image file names.
     * @return The list of image file names.
     */
    public List getCurrentNames()
    {
        return currentNames;
    }

    /**QuestionImageForm
     * Set the image file name list.
     * @param currentNames The list of image file names to be set.
     */
    public void setCurrentNames(List currentNames)
    {
        this.currentNames = currentNames;
    }

    /**--------------------------------------------------------------------------------------------------------------------------
     * moved by Ching-Heng from VisualScaleForm
     * */
   
    /**VisualScaleForm**/
	public int getRangeStart() {
		return rangeStart;
	}
	/**VisualScaleForm**/
	public void setRangeStart(int rangeStart) {
		this.rangeStart = rangeStart;
	}
	/**VisualScaleForm**/
	public int getRangeEnd() {
		return rangeEnd;
	}
	/**VisualScaleForm**/
	public void setRangeEnd(int rangeEnd) {
		this.rangeEnd = rangeEnd;
	}
	/**VisualScaleForm**/
	public int getWidth() {
		return width;
	}
	/**VisualScaleForm**/
	public void setWidth(int width) {
		this.width = width;
	}
	/**VisualScaleForm**/
	public String getRightText() {
		return rightText;
	}
	/**VisualScaleForm**/
	public void setRightText(String rightText) {
		this.rightText = rightText;
	}
	/**VisualScaleForm**/
	public String getLeftText() {
		return leftText;
	}
	/**VisualScaleForm**/
	public void setLeftText(String leftText) {
		this.leftText = leftText;
	}
	/**VisualScaleForm**/
	public String getCenterText() {
		return centerText;
	}
	/**VisualScaleForm**/
	public void setCenterText(String centerText) {
		this.centerText = centerText;
	}
	/**VisualScaleForm**/
	public boolean isShowHandle() {
		return showHandle;
	}
	/**VisualScaleForm**/
	public void setShowHandle(boolean showHandle) {
		this.showHandle = showHandle;
	}
	/**--------------------------------------------------------------------------------------------------------------------------
     * moved by Ching-Heng from QuestionWizardOptionsForm
     * */
	public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public String[] getOrigOptions() {
        return origOptions;
    }

    public void setOrigOptions(String[] origOptions) {
        this.origOptions = origOptions;
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



   
    public String getHtmlText() {
		return htmlText;
	}

	public void setHtmlText(String htmlText) {
		this.htmlText = htmlText;
	}

}
