package gov.nih.nichd.ctdb.question.common;

import gov.nih.nichd.ctdb.common.ResultControl;

/**
 * QuestionResultControl handles searching and sorting of question records in the system.
 * 
 * @deprecated This is a rats nest of SQL injection attacks that are waiting to happen. Please do not use any of the methods in this class, 
 * and please take time to removing the usage of this class from your code.
 * @author Booz Allen Hamilton
 * @version 1.0
 * 
 */
public class QuestionResultControl extends ResultControl
{
    /**
     *  QuestionResultControl Constant used to sort by ID
     */
    public static final String SORT_BY_ID = "questionid";

    /**
     *  QuestionResultControl Constant used to sort by name
     */
    public static final String SORT_BY_NAME = "name";

    /**
     *  QuestionResultControl Constant used to sort by text
     */
    public static final String SORT_BY_TEXT = "text";

    /**
     *  QuestionResultControl Constant used to sort by type
     */
    public static final String SORT_BY_TYPE = "type";

    public static final String SEARCH_MODIFIER_CONTAINS = "Contains";
    public static final String SEARCH_MODIFIER_BEGINS_WITH = "Begins With";
    public static final String SEARCH_MODIFIER_NOT = "Not";


    private int questionId = Integer.MIN_VALUE;
    private String name = null;
    private String text = null;
    private int groupId = Integer.MIN_VALUE;
    private int groupIdExclude = Integer.MIN_VALUE;
    private int type = Integer.MIN_VALUE;
    private String createdBy = null;
    private String nameSearchModifier = QuestionResultControl.SEARCH_MODIFIER_CONTAINS;	
    private String textSearchModifier = QuestionResultControl.SEARCH_MODIFIER_CONTAINS;
    private String source = "question";
    private String medicalCodingStatus = null;

    private boolean inCdes = false;


    /**
     * Default constructor for the QuestionResultControl
     */
    public QuestionResultControl()
    {
        // default constructor
    }

    /**
     * Gets ID for question.
     *
     * @return The question id.
     */
    public int getQuestionId()
    {
        return questionId;
    }

    /**
     * Set the question id.
     *
     * @param questionId The question id.
     */
    public void setQuestionId(int questionId)
    {
        this.questionId = questionId;
    }

    /**
     * Gets name of the question.
     *
     * @return The question name.
     */
    public String getName()
    {
        return name;
    }


    public boolean isInCdes() {
        return inCdes;
    }

    public void setInCdes(boolean inCdes) {
        this.inCdes = inCdes;
    }

    /**
     * Set the question name.
     *
     * @param name The question name.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets text of the question.
     *
     * @return The question text.
     */
    public String getText()
    {
        return text;
    }

    /**
     * Set the question text.
     *
     * @param text The question text.
     */
    public void setText(String text)
    {
        this.text = text;
    }

    /**
     * Gets group ID to which this question belongs.
     *
     * @return The question's group Id for exclusion.
     */
    public int getGroupIdExclude()
    {
        return groupIdExclude;
    }

    /**
     * Set the group id for exclusion for this question.
     *
     * @param groupIdExclude The question's group id.
     */
    public void setGroupIdExclude(int groupIdExclude)
    {
        this.groupIdExclude = groupIdExclude;
    }

    /**
     * Gets group ID to which this question will be exculded.
     *
     * @return The question's group Id.
     */
    public int getGroupId()
    {
        return groupId;
    }

    /**
     * Set the group id for this question.
     *
     * @param groupId The question's group id.
     */
    public void setGroupId(int groupId)
    {
        this.groupId = groupId;
    }

    /**
     * Gets the type of this question.
     *
     * @return The question's type.
     */
    public int getType()
    {
        return type;
    }

    /**
     * Set the type of the question.
     *
     * @param type The question type.
     */
    public void setType(int type)
    {
        this.type = type;
    }

    /**
     * Gets the username who created this question.
     *
     * @return The user name who created this question.
     */
    public String getCreatedBy()
    {
        return createdBy;
    }

    /**
     * Set the username who created this question.
     *
     * @param createdBy The user name who created this question.
     */
    public void setCreatedBy(String createdBy)
    {
        this.createdBy = createdBy;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
   
    //***************************************************
    // Get the search clauses
    // **************************************************

    /**
     * Gets question id search string if question id is part of the search data.
     *
     * @return The question search string.
     */
    private String getQuestionIdSearchString()
    {
        if(hasData(questionId))
        {
            return " and "+source+".questionid = " + this.questionId + " ";
        }
        else
        {
            return "";
        }

    }

    /**
     * Gets type search string if question type is part of the search data.
     *
     * @return The question search string.
     */
    private String getTypeSearchString()
    {
        if(hasData(type))
        {
            return " and "+source+".type = " + this.type + " ";
        }
        else
        {
            return "";
        }
    }

    private String getCdeSearchString () {
        if (inCdes) {
            //return " and "+source+".isCde = 1 "; // for oracle
        	return " and "+source+".isCde = true ";
        } else {
            //return " and "+source+".isCde != 1 "; // for oracle
        	return " and "+source+".isCde != true ";
        }
    }

    /**
     * Gets text search string if question text is part of the search data.
     *
     * @return The question search string.
     */
    private String getTextSearchString()
    {
        if(hasData(text))
        {
            String returnStr = " and upper ("+source+".text) ";
            returnStr += this.getModifierStartStr(this.textSearchModifier);

            String text1 = this.replaceQuotes(text);

            if (text1.indexOf("%") != -1)
            {
            	text1 = this.escapePercentageSign(text1);

            	returnStr += text1.toUpperCase() + this.getModifierEndStr(this.textSearchModifier) + "' escape '^' ";
            }
            else {
            	returnStr +=text1.toUpperCase() +this.getModifierEndStr(this.textSearchModifier) + "' ";
            }

            return returnStr;
            }
        else
        {
            return "";
        }
    }

    /**
     * Gets group Id search string if questio group is part of the search data.
     *
     * @return The question search string.
     */
    private String getGroupIdSearchString()
    {
    	if (hasData(groupId) && hasData(groupIdExclude))
    		return " and "+source+".questionid in (select questionid from " +
                    "questiongrpquestion where questiongroupid = '" + this.groupId + "') " +
                    "and "+source+".questionid not in (select questionid from " +
                    "questiongrpquestion where questiongroupid = '" + this.groupIdExclude + "') ";
        else if (hasData(groupId) && !hasData(groupIdExclude))
        {
            return " and "+source+".questionid in (select questionid from " +
                    "questiongrpquestion where questiongroupid = '" + this.groupId + "') ";
        }
        else if (!hasData(groupId) && hasData(groupIdExclude))
        {
            return " and "+source+".questionid not in (select questionid from " +
                    "questiongrpquestion where questiongroupid = '" + this.groupIdExclude + "') ";
        }
        else
        {
            return "";
        }
    }

    /**
     * Gets name search string if question name is part of the search data.
     *
     * @return The question search string.
     */
    private String getNameSearchString()
    {
        if(hasData(name))
        {
            String retStr = " and upper("+source+".name) ";
            retStr += this.getModifierStartStr(this.nameSearchModifier);

            String name1 = this.replaceQuotes(name);
            if (name1.indexOf("%") != -1)
            {
            	name1 = this.escapePercentageSign(name1);

            	retStr += name1.toUpperCase() + this.getModifierEndStr(this.nameSearchModifier) +"' escape '^' ";
            }
            else {
            	retStr += name1.toUpperCase() +  this.getModifierEndStr(this.nameSearchModifier) +"' ";
            }
            return retStr;
        }
        else
        {
            return "";
        }
    }

    /**
     * Gets the Search Clause for this SQL operation to determine the results to be returned.
     *
     * @return The string representation of the search clause for this SQL operation
     */
    public String getSearchClause()
    {
        StringBuffer clause = new StringBuffer(300);
        clause.append(this.getQuestionIdSearchString());
        clause.append(this.getTypeSearchString());
        clause.append(this.getTextSearchString());
        clause.append(this.getGroupIdSearchString());
        clause.append(this.getNameSearchString());
        clause.append(this.getMedicalcodingStatus());
        clause.append(getCdeSearchString());
        return clause.toString();
    };

    /**
     * Gets the Order By SQL operation for the results to be returned
     *
     * @return The string representation of the order by SQL operation
     */
    public String getSortString()
    {
        if(this.getSortBy() != null && !this.getSortBy().equalsIgnoreCase(""))
        {
            if(this.getSortBy().equalsIgnoreCase(QuestionResultControl.SORT_BY_ID))
            {
                return " order by " + this.getSortBy() + " " + this.getSortOrder();
            }
            else
            {
                return " order by upper(" + this.getSortBy() + ") " + this.getSortOrder();
            }
        }
        else
        {
            return " ";
        }
    }

    private String getModifierStartStr (String modifier) {
        if (modifier.equals(QuestionResultControl.SEARCH_MODIFIER_CONTAINS)) {
            return  " like '%";
        } else if (modifier.equals(QuestionResultControl.SEARCH_MODIFIER_NOT)) {
            return " not like '%";
        } else {
            return " like '";
        }
    }

    private String getModifierEndStr (String modifier) {
            return "%";

    }


    public void setMedicalCodingStatus(String medicalCodingStatus){
        this.medicalCodingStatus = medicalCodingStatus;
    }

    public String getMedicalcodingStatus(){
        if(hasData(medicalCodingStatus))
            if(medicalCodingStatus != null && ! medicalCodingStatus.trim().equalsIgnoreCase("all"))
            {
                return " and x.status = '" + this.medicalCodingStatus + "' ";
            }
            else
            {
                return "";
            }
        else
            return "";
}
}
