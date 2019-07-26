package gov.nih.nichd.ctdb.question.form;

import gov.nih.nichd.ctdb.common.CtdbForm;
import gov.nih.nichd.ctdb.question.common.QuestionResultControl;

/**
 * The QuestionSearchForm represents the Java class behind the HTML
 * to search for a Question
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class QuestionSearchForm extends CtdbForm
{
    private String questionName = null;
    private int questionGroup = Integer.MIN_VALUE;
    private String questionText = null;
    private int questionType = Integer.MIN_VALUE;
    private String questionId = null;
    private String createdBy = null;
    private String clicked = "initial";  
    private String sortBy = QuestionResultControl.SORT_BY_NAME;
    private String sortedBy = null;
    private String sortOrder = QuestionResultControl.SORT_ASC;
    private String numResults = null;
    private String numResultsPerPage = null;
    private String medicalCodingStatus = null;
    private String nameSearchModifier = QuestionResultControl.SEARCH_MODIFIER_CONTAINS;
    private String textSearchModifier = QuestionResultControl.SEARCH_MODIFIER_CONTAINS;
    private boolean inCdes = false;
    // added for do not show the duplicate questions in form creation
    private String duplicateQuestions;
    
	private String descriptionUp;//add by sunny
	private String descriptionDown; //add by sunny 
    
	public String getDuplicateQuestions() {
		return duplicateQuestions;
	}

	public void setDuplicateQuestions(String duplicateQuestions) {
		this.duplicateQuestions = duplicateQuestions;
	}  
    
    
    /**
     * Get the NumResultsPerPage value.
     * @return the NumResultsPerPage value.
     */
    public String getNumResultsPerPage() {
        return numResultsPerPage;
    }

    /**
     * Set the NumResultsPerPage value.
     * @param newNumResultsPerPage The new NumResultsPerPage value.
     */
    public void setNumResultsPerPage(String newNumResultsPerPage) {
        this.numResultsPerPage = newNumResultsPerPage;
    }

    /**
     * Get the NumResults value.
     * @return the NumResults value.
     */
    public String getNumResults() {
        return numResults;
    }

    /**
     * Set the NumResults value.
     * @param newNumResults The new NumResults value.
     */
    public void setNumResults(String newNumResults) {
        this.numResults = newNumResults;
    }

    /**
     * Return the questionName
     *
     * @return The question name
     */
    public String getQuestionName()
    {
        return questionName;
    }

    /**
     * Set the questionName
     *
     * @param questionName The name entered
     */
    public void setQuestionName(String questionName)
    {
        this.questionName = questionName;
    }

    /**
     * Return the question ID
     *
     * @return The question ID
     */
    public String getQuestionId()
    {
        return questionId;
    }

    /**
     * Set the questionId
     *
     * @param questionId The question Id entered
     */
    public void setQuestionId(String questionId)
    {
        this.questionId = questionId;
    }

    /**
     * Return the questionGroup
     *
     * @return The questionGroup
     */
    public int getQuestionGroup()
    {
        return questionGroup;
    }

    /**
     * Set the questionGroup
     *
     * @param questionGroup The questionGroup entered
     */
    public void setQuestionGroup(int questionGroup)
    {
        this.questionGroup = questionGroup;
    }

    /**
     * Return the questionText
     *
     * @return The questionText
     */
    public String getQuestionText()
    {
        return questionText;
    }

    /**
     * Set the questionText
     *
     * @param questionText The questionText number entered.
     */
    public void setQuestionText(String questionText)
    {
        this.questionText = questionText;
    }

    /**
     * Return the questionType
     *
     * @return The questionType
     */
    public int getQuestionType()
    {
        return questionType;
    }

    /**
     * Set the questionType
     *
     * @param questionType
     */
    public void setQuestionType(int questionType)
    {
        this.questionType = questionType;
    }

    /**
     * Gets the sort by for the search/listing
     *
     * @return The sort by for the search/listing
     */
    public String getSortBy()
    {
        return sortBy;
    }

    /**
     * Set the sort by for the search/listing
     *
     * @param sortBy The sort by for the search/listing
     */
    public void setSortBy(String sortBy)
    {
        this.sortBy = sortBy;
    }

    /**
     * Gets the sorted by for the search/listing
     *
     * @return The sorted by for the search/listing
     */
    public String getSortedBy()
    {
        return sortedBy;
    }

    /**
     * Set the sorted by for the search/listing
     *
     * @param sortedBy The sorted by for the search/listing
     */
    public void setSortedBy(String sortedBy)
    {
        this.sortedBy = sortedBy;
    }

    /**
     * Gets the sort order for this search/listing
     *
     * @return The sort order for this search/listing
     */
    public String getSortOrder()
    {
        return sortOrder;
    }

    /**
     * Sets the sort order for this search/listing
     *
     * @param sortOrder The sort order for this search/listing
     */
    public void setSortOrder(String sortOrder)
    {
        this.sortOrder = sortOrder;
    }


    /**
     * Sets the medicalcode for the questions to search for
     * @param medicalCodingStatus
     */
    public void setMedicalCodingStatus(String medicalCodingStatus){this.medicalCodingStatus = medicalCodingStatus;}


    /**
     * Returns the selected medical coding status
     * @return
     */
    public String getMedicalCodingStatus(){return medicalCodingStatus;}


    /**
     * Gets the value created by
     *
     * @return The created by to search for
     */
    public String getCreatedBy()
    {
        return createdBy;
    }

    /**
     * Sets the createdBy search term
     *
     * @param createdBy The value entered
     */
    public void setCreatedBy(String createdBy)
    {
        this.createdBy = createdBy;
    }

    /**
     * Returns the value of clicked, determines if this list should be sorted or searched on.
     *
     * @return The clicked value
     */
    public String getClicked()
    {
        return clicked;
    }

    /**
     * Sets the value of clicked
     *
     * @param clicked The clicked value
     */
    public void setClicked(String clicked)
    {
        this.clicked = clicked;
    }
   /**
    *  get the search modifier for questoin name
    * @return
    */
    public String getNameSearchModifier() {
        return nameSearchModifier;
    }
    /**
    *  set the search modifier for questoin name
    */
    public void setNameSearchModifier(String nameSearchModifier) {
        this.nameSearchModifier = nameSearchModifier;
    }
    /**
    *  get the search modifier for questoin text
    * @return
    */
    public String getTextSearchModifier() {
        return textSearchModifier;
    }
    /**
    *  set the search modifier for questoin text
    */
    public void setTextSearchModifier(String textSearchModifier) {
        this.textSearchModifier = textSearchModifier;
    }

    public boolean isInCdes() {
        return inCdes;
    }

    public void setInCdes(boolean inCdes) {
        this.inCdes = inCdes;
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

	/**
     * Sets the properties based upon an existing questionSearchForm
     *
     */
    public void clone(QuestionSearchForm qsf)
    {
        this.questionName = qsf.questionName;
        this.questionGroup = qsf.questionGroup;
        this.questionText = qsf.questionText;
        this.questionType  = qsf.questionType;
        this.questionId = qsf.questionId;
        this.createdBy = qsf.createdBy;
        this.clicked = qsf.clicked;
        this.sortBy = qsf.sortBy;
        this.sortedBy = qsf.sortedBy;
        this.sortOrder = qsf.sortOrder;
        this.numResults = qsf.numResults;
        this.numResultsPerPage = qsf.numResultsPerPage;
        super.clone(qsf);
    }
}
