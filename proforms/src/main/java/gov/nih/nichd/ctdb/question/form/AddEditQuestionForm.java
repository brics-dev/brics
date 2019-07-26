package gov.nih.nichd.ctdb.question.form;

import gov.nih.nichd.ctdb.common.CtdbForm;
import gov.nih.nichd.ctdb.emailtrigger.domain.EmailTrigger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ching Heng
 * Date: Apir 24, 2012
 * */
public class AddEditQuestionForm extends CtdbForm  {

    private String questionName;
    private String text;
    private int type = 1;
    private boolean calDependent = false;
	private boolean skipRuleDependent= false;
    private String questionTypeDisplay;
    private String medicalCodingStatus;
    private int editableType = 0;
    
    // moved by Ching-Heng from  QuestionGroupUmlsForm
    private String defaultValue;
    private String unansweredValue;
    private int[] questionGroupIds;
    private int[] availableQuestionGroups;
    
    // moved by Ching-Heng Lin from QuestionImageForm
    
    private List currentNames;
    private int imageCount;   // number of images to add
    private Map files;
    private String[] namesToDelete = {};
    
    
    // moved by Ching-Heng from VisualScaleForm
    private int rangeStart = 1;
    private int rangeEnd = 100;
    private int width = 100;
    private String rightText;
    private String leftText;
    private String centerText;
    private boolean showHandle;
    
    // moved by Ching-Heng from QuestionWizardOptionsForm
    private String[] options;
    private String[] origOptions;
    
    private List<QuestionOption> questionOptionsObjectList = new ArrayList();;
    private String questionOptionsJSON;
    

	// moved by Ching Heng from QuestionAttributesForm
    private boolean required = false;
    private int answerType = 1;
    private String minCharacters;
    private String maxCharacters;
    private String align = "left";
    private String vAlign = "top";
    private String color = "black";
    
    private String fontFace = "arial";
    private String fontSize;
    private int indent = 0;
    private int skipRuleType = Integer.MIN_VALUE;
    private String skipRuleEquals;
    private int skipRuleOperatorType = Integer.MIN_VALUE;
    private int[] questionsToSkip;
    private int[] availableQuestionsToSkip;

    private String rangeOperator;
    private String rangeValue1;
    private String rangeValue2;

    private boolean calculations = false;
    private String answerTypeDisplay;

    private boolean childSkipRules = false;
    private boolean horizontalDisplay = false;
    private boolean horizDisplayBreak = true;
    private int textareaHeight = 60;
    private int textareaWidth = 100;
    private int textboxLength = 20;

    private EmailTrigger emailTrigger = new EmailTrigger();
    private boolean deleteTrigger = false;
    private boolean dataSpring = false;

    private String htmlText;
    
	private String descriptionUp;
    private String descriptionDown;
    
    // added by Ching Heng
	private boolean includeOtherOption = false;
	private boolean prepopulation = false;
	private String prepopulationValue;
	
	private String decimalPrecision = "-1";
	
    public String getDecimalPrecision() {
		return decimalPrecision;
	}

	public void setDecimalPrecision(String decimalPrecision) {
		this.decimalPrecision = decimalPrecision;
	}

	public boolean isPrepopulation() {
		return prepopulation;
	}

	public void setPrepopulation(boolean prepopulation) {
		this.prepopulation = prepopulation;
	}

	public String getPrepopulationValue() {
		return prepopulationValue;
	}

	public void setPrepopulationValue(String prepopulationValue) {
		this.prepopulationValue = prepopulationValue;
	}

	public boolean isIncludeOtherOption() {
		return includeOtherOption;
	}

	public void setIncludeOtherOption(boolean includeOtherOption) {
		this.includeOtherOption = includeOtherOption;
	}
	
	
	
	
	/**
     * Get the name from the form
     * @return
     */
    public String getQuestionName() {
        return questionName;
    }

    /**
     * Set the name on the form
     * @param name
     */
    public void setQuestionName(String name) {
        this.questionName = name;
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
    public void setAtrCalDependent(boolean calDependent) {
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
    public int getArtEditableType() {
        return editableType;
    }

    /**
     * sets editable type attribute
     * @param editableType
     */
    public void setArtEditableType(int editableType) {
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
    public String getArtDefaultValue() {
		return defaultValue;
	}
    /**QuestionWizardGroupUmlsForm
     * Set the DefaultValue on the form
     * @param name
     */
	public void setArtDefaultValue(String defaultValue) {
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
	public String[] getArtOptions() {
        return options;
    }

    public void setArtOptions(String[] options) {
        this.options = options;
    }

    public String[] getOrigOptions() {
        return origOptions;
    }

    public void setOrigOptions(String[] origOptions) {
        this.origOptions = origOptions;
    }
    
	//
	/**
	 * Return required property of the question.
	 *
	 * @return The required property of the question.
	 */
	public boolean isRequired()
	{
		return required;
	}
	
	/**
	 * Set the question required property.
	 *
	 * @param required The required property for this question.
	 */
	public void setRequired(boolean required)
	{
		this.required = required;
	}

	/**
     * Return the answerType.
     *
     * @return The answerType property of the question.
     */
    public int getArtAnswerType()
    {
        return answerType;
    }

    /**
     * Set the question answerType property.
     * @param answerType int the answerType for question.
     */
    public void setArtAnswerType(int answerType)
    {
        this.answerType = answerType;
    }
    
    /**
     * Return the minCharacters.
     *
     * @return The minCharacters for the question.
     */
    public String getMinCharacters()
    {
        return minCharacters;
    }

    /**
     * Set the question minCharacters property.
     *
     * @param minCharacters String the minCharacters for question.
     */
    public void setMinCharacters(String minCharacters)
    {
        if(minCharacters != null && !minCharacters.trim().equalsIgnoreCase(""))
        {
            this.minCharacters = minCharacters.trim();
        }
        else
        {
            this.minCharacters = null;
        }
    }
    
    /**
     * Return the maxCharacters.
     *
     * @return The maxCharacters for the question.
     */
    public String getMaxCharacters()
    {
        return maxCharacters;
    }

    /**
     * Set the question maxCharacters property.
     *
     * @param maxCharacters String the maxCharacters for question.
     */
    public void setMaxCharacters(String maxCharacters)
    {
        if(maxCharacters != null && !maxCharacters.trim().equalsIgnoreCase(""))
        {
            this.maxCharacters = maxCharacters.trim();
        }
        else
        {
            this.maxCharacters = null;
        }
    }
    
    /**
     * Return the align.
     *
     * @return The align property of the question.
     */
    public String getAlign()
    {
        return align;
    }

    /**
     * Set the question align property.
     *
     * @param align String the align for question.
     */
    public void setAlign(String align)
    {
        this.align = align;
    }

    /**
     * Return the vAlign.
     *
     * @return The vAlign property of the question.
     */
    public String getVAlign()
    {
        return vAlign;
    }

    /**
     * Set the question vAlign property.
     *
     * @param vAlign String the vAlign for question.
     */
    public void setVAlign(String vAlign)
    {
        this.vAlign = vAlign;
    }
    
    /**
     * Return the color.
     *
     * @return The color property of the question.
     */
    public String getColor()
    {
        return color;
    }

    /**
     * Set the question color property.
     *
     * @param color String the color for question.
     */
    public void setColor(String color)
    {
        this.color = color;
    }
    
    /**
     * Return the fontFace.
     *
     * @return The fontFace property of the question.
     */
    public String getFontFace()
    {
        return fontFace;
    }

    /**
     * Set the question fontFace property.
     *
     * @param fontFace String the fontFace for question.
     */
    public void setFontFace(String fontFace)
    {
        this.fontFace = fontFace;
    }

    /**
     * Return the fontSize.
     *
     * @return The fontSize property of the question.
     */
    public String getFontSize()
    {
        return fontSize;
    }

    /**
     * Set the question fontSize property.
     *
     * @param fontSize String the fontSize for question.
     */
    public void setFontSize(String fontSize)
    {
        this.fontSize = fontSize;
    }

    /**
     * Return the indent.
     *
     * @return The indent property of the question.
     */
    public int getIndent()
    {
        return indent;
    }

    /**
     * Set the question indent property.
     *
     * @param indent int the indent for question.
     */
    public void setIndent(int indent)
    {
        this.indent = indent;
    }


    /**
     * Return the answerType.
     *
     * @return The answerType property of the question.
     */
    public int getAnswerType()
    {
        return answerType;
    }

    /**
     * Set the question answerType property.
     * @param answerType int the answerType for question.
     */
    public void setAnswerType(int answerType)
    {
        this.answerType = answerType;
    }

    /**
     * Return the questionGroups.
     *
     * @return The question groups for the question.
     */
   ////public int[] getQuestionGroups()
    ////{
        ////return questionGroups;
    ////}

    /**
     * Set the question groups for this question.
     *
     * @param questionGroups int[] the question groups for the question.
     */
    ////public void setQuestionGroups(int[] questionGroups)
    ////{
        ////this.questionGroups = questionGroups;
    ////}

    /**
     * Return the availableQuestionGroups.
     *
     * @return The question groups available in the question library.
     */
    ////public int[] getAvailableQuestionGroups()
    ////{
        ////return availableQuestionGroups;
    ////}

    /**
     * Sets the available question groups.
     *
     * @param availableQuestionGroups The question groups available from
     *                                question library.
     */
    ////public void setAvailableQuestionGroups(int[] availableQuestionGroups)
    ////{
        ////this.availableQuestionGroups = availableQuestionGroups;
    ////}

    /**
     * Return the skipRuleType.
     *
     * @return The skipRuleType of the question.
     */
    public int getSkipRuleType()
    {
        return skipRuleType;
    }

    /**
     * Set the skipRuleType for this question.
     *
     * @param skipRuleType The skipRuleType for the question.
     */
    public void setSkipRuleType(int skipRuleType)
    {
        this.skipRuleType = skipRuleType;
    }

    /**
     * Return the skipRuleEquals.
     *
     * @return The skipRuleEquals of the question.
     */
    public String getSkipRuleEquals()
    {
        return skipRuleEquals;
    }

    /**
     * Set the skipRuleEquals for this question.
     *
     * @param skipRuleEquals The skipRuleEquals for the question.
     */
    public void setSkipRuleEquals(String skipRuleEquals)
    {
        if(skipRuleEquals != null && !skipRuleEquals.trim().equalsIgnoreCase(""))
        {
            this.skipRuleEquals = skipRuleEquals.trim();
        }
        else
        {
            this.skipRuleEquals = null;
        }
    }

    /**
     * Return the skipRuleOperatorType.
     *
     * @return The skipRuleOperatorType of the question.
     */
    public int getSkipRuleOperatorType()
    {
        return skipRuleOperatorType;
    }

    /**
     * Set the skipRuleOperatorType for this question.
     *
     * @param skipRuleOperatorType The skipRuleOperatorType for the question.
     */
    public void setSkipRuleOperatorType(int skipRuleOperatorType)
    {
        this.skipRuleOperatorType = skipRuleOperatorType;
    }

    /**
     * Return the calculationType.
     *
     * @return The calculationType of the question.
     */
    //public int getCalculationType()
    //{
    //    return calculationType;
    //}

    /**
     * Set the calculationType for this question.
     *
     * @param calculationType The calculationType for the question.
     */
   // public void setCalculationType(int calculationType)
   // {
   //     this.calculationType = calculationType;
   // }

    /**
     * Return the questionsToSkip.
     *
     * @return The questionsToSkip for the question.
     */
    public int[] getQuestionsToSkip()
    {
        return questionsToSkip;
    }

    /**
     * Set the questionsToSkip for this question.
     *
     * @param questionsToSkip The questionsToSkip for the question.
     */
    public void setQuestionsToSkip(int[] questionsToSkip)
    {
        this.questionsToSkip = questionsToSkip;
    }

    /**
     * Return the availableQuestionsToSkip.
     *
     * @return The availableQuestionsToSkip for the question.
     */
    public int[] getAvailableQuestionsToSkip()
    {
        return availableQuestionsToSkip;
    }

    /**
     * Set the availableQuestionsToSkip for this question.
     *
     * @param availableQuestionsToSkip The availableQuestionsToSkip for the question.
     */
    public void setAvailableQuestionsToSkip(int[] availableQuestionsToSkip)
    {
        this.availableQuestionsToSkip = availableQuestionsToSkip;
    }

    /**
     * Return the questionsToCalculate.
     *
     * @return The questionsToCalculate for the question.
     */
   // public int[] getQuestionsToCalculate()
   // {
   //     return questionsToCalculate;
   // }

    /**
     * Set the questionsToCalculate for this question.
     *
     * @param questionsToCalculate The questionsToCalculate for the question.
     */
  //  public void setQuestionsToCalculate(int[] questionsToCalculate)
  //  {
  //      this.questionsToCalculate = questionsToCalculate;
  //  }

    /**
     * Return the availableQuestionsToCalculate.
     *
     * @return The availableQuestionsToCalculate for the question.
     */
   // public int[] getAvailableQuestionsToCalculate()
   // {
    //    return availableQuestionsToCalculate;
   // }

    /**
     * Set the availableQuestionsToCalculate for this question.
     *
     * @param availableQuestionsToCalculate The availableQuestionsToCalculate for the question.
     */
   // public void setAvailableQuestionsToCalculate(int[] availableQuestionsToCalculate)
   // {
   //     this.availableQuestionsToCalculate = availableQuestionsToCalculate;
    //}

    /**
     * Return the defaultValue.
     *
     * @return The defaultValue for the question.
     */
    public String getDefaultValue()
    {
        return defaultValue;
    }

    /**
     * Set the defaultValue for this question.
     *
     * @param defaultValue The defaultValue for the question.
     */
    public void setDefaultValue(String defaultValue)
    {
        if(defaultValue != null && !defaultValue.trim().equalsIgnoreCase(""))
        {
            this.defaultValue = defaultValue.trim();
        }
        else
        {
            this.defaultValue = null;
        }
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

        /** Getter for property rangeValue2.
     * @return Value of property rangeValue2.
     */
    public String getRangeValue2()
    {
        return rangeValue2;
    }

    /** Setter for property rangeValue2.
     * @param rangeValue2 New value of property rangeValue2.
     */
    public void setRangeValue2(String rangeValue2)
    {
        this.rangeValue2 = rangeValue2;
    }

    /** Getter for property rangeValue1.
     * @return Value of property rangeValue1.
     */
    public String getRangeValue1()
    {
        return rangeValue1;
    }

    /** Setter for property rangeValue1.
     * @param rangeValue1 New value of property rangeValue1.
     */
    public void setRangeValue1(String rangeValue1)
    {
        this.rangeValue1 = rangeValue1;
    }

    /** Getter for property rangeOperator.
     * @return Value of property rangeOperator.
     */
    public String getRangeOperator()
    {
        return rangeOperator;
    }

    /** Setter for property rangeOperator.
     * @param rangeOperator New value of property rangeOperator.
     */
    public void setRangeOperator(String rangeOperator)
    {
        this.rangeOperator = rangeOperator;
    }

    /**
     * Return childSkipRules property of the question.
     *
     * @return The childSkipRules property of the question.
     */
    public boolean getChildSkipRules()
    {
        return childSkipRules;
    }

    /**
     * Set the question childSkipRules property.
     *
     * @param childSkipRules The childSkipRules property for this question.
     */
    public void setChildSkipRules(boolean childSkipRules)
    {
        this.childSkipRules = childSkipRules;
    }

    /**
     * Return calDependent property of the question.
     *
     * @return The calDependent property of the question.
     */
    public boolean getCalDependent()
    {
        return calDependent;
    }

    /**
     * Set the question calDependent property.
     *
     * @param calDependent The calDependent property for this question.
     */
    public void setCalDependent(boolean calDependent)
    {
        this.calDependent = calDependent;
    }
    // add by sunny
    public boolean isSkipRuleDependent() {
		return skipRuleDependent;
	}

	public void setSkipRuleDependent(boolean skipRuleDependent) {
		this.skipRuleDependent = skipRuleDependent;
	}
   
    
    
    /**
     * Return calculations property of the question.
     *
     * @return The calculation property of the question.
     */
    public boolean getCalculations()
    {
        return calculations;
    }

    /**
     * Set the question calculation property.
     *
     * @param calculations The calculation property for this question.
     */
    public void setCalculations(boolean calculations)
    {
        this.calculations = calculations;
    }

    /**
     * Set the question answer type property.
     *
     * @param display String the answer type for question.
     */
    public void setAnswerTypeDisplay(String display)
    {
        this.answerTypeDisplay = display;
    }

    /**
     * Return the answer type display.
     *
     * @return The answerTypeDisplay property of the question.
     */
    public String getAnswerTypeDisplay()
    {
        return answerTypeDisplay;
    }

    /**
     * Set the question type property.
     *
     * @param display String the type for question.
     */
   //// public void setQuestionTypeDisplay(String display)
    ////{
        ////this.questionTypeDisplay = display;
    ////}

    /**
     * Return the question type display.
     *
     * @return The questionTypeDisplay property of the question.
     */
   //// public String getQuestionTypeDisplay()
    ////{
        ////return questionTypeDisplay;
    ////}

    /**
     * Get the editableType flag.
     * @return the editableType flag
     */
    public int getEditableType()
    {
        return editableType;
    }

    /**
     * Set the editableType flag.
     * @param editableType the new editableType flag
     */
    public void setEditableType(int editableType)
    {
        this.editableType = editableType;
    }

    public boolean isHorizontalDisplay() {
        return horizontalDisplay;
    }

    public void setHorizontalDisplay(boolean horizontalDisplay) {
        this.horizontalDisplay = horizontalDisplay;
    }

    public int getTextareaHeight() {
        return textareaHeight;
    }

    public void setTextareaHeight(int textareaHeight) {
        this.textareaHeight = textareaHeight;
    }

    public int getTextareaWidth() {
        return textareaWidth;
    }

    public void setTextareaWidth(int textareaWidth) {
        this.textareaWidth = textareaWidth;
    }

    public int getTextboxLength() {
        return textboxLength;
    }

    public void setTextboxLength(int textboxLength) {
        this.textboxLength = textboxLength;
    }

    public boolean isHorizDisplayBreak() {
        return horizDisplayBreak;
    }

    public void setHorizDisplayBreak(boolean horizDisplayBreak) {
        this.horizDisplayBreak = horizDisplayBreak;
    }

    public EmailTrigger getEmailTrigger() {
        return emailTrigger;
    }

    public void setEmailTrigger(EmailTrigger emailTrigger) {
        this.emailTrigger = emailTrigger;
    }

    public boolean isDeleteTrigger() {
        return deleteTrigger;
    }

    public void setDeleteTrigger(boolean deleteTrigger) {
        this.deleteTrigger = deleteTrigger;
    }

    public boolean isDataSpring() {
        return dataSpring;
    }

    public void setDataSpring(boolean dataSpring) {
        this.dataSpring = dataSpring;
    }
    /**
     * Return the options.
     *
     * @return The options for the question.
     */
   //public List getOptions()
    //{
        //return options;
    //}

    /**
     * Set the options for this question.
     *
     * @param options The options for the question.
     */
   // public void setOptions(List options)
   // {
        //this.options = options;
   // }

    public String getHtmlText()
    {
        return htmlText;
    }

    public void setHtmlText(String htmlText)
    {
        this.htmlText = htmlText;
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

	public List<QuestionOption> getQuestionOptionsObjectList() {
		return questionOptionsObjectList;
	}

	public void setQuestionOptionsObjectList(List<QuestionOption> questionOptionsObjectList) {
		this.questionOptionsObjectList = questionOptionsObjectList;
	}
	
	public void setQuestionOptionsObjectList(JSONArray questionOptionsJSON) throws JSONException {

		for(int i=0;i<questionOptionsJSON.length();i++) {
			JSONObject obj = (JSONObject)questionOptionsJSON.get(i);
			this.questionOptionsObjectList.add(new QuestionOption(obj));
			
		}
	
	}

	public String getQuestionOptionsJSON() {
		return questionOptionsJSON;
	}

	public void setQuestionOptionsJSON(String questionOptionsJSON) {
		this.questionOptionsJSON = questionOptionsJSON;
	}
   
}
