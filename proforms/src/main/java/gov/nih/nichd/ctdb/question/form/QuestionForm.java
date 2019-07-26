package gov.nih.nichd.ctdb.question.form;

import gov.nih.nichd.ctdb.common.CtdbForm;

/**
 * The QuestionForm represents the Java class behind the HTML question library form used on the system
 * to add/edit questions.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class QuestionForm extends CtdbForm
{
    private String name;
    private String text;
    private int type = 1;
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
    private int[] questionGroups;
    private int[] availableQuestionGroups;
    private int skipRuleType = Integer.MIN_VALUE;
    private String skipRuleEquals;
    private int skipRuleOperatorType = Integer.MIN_VALUE;
    //private int calculationType = Integer.MIN_VALUE;
    private int[] questionsToSkip;
    private int[] availableQuestionsToSkip;
    //private int[] questionsToCalculate;
    //private int[] availableQuestionsToCalculate;
    private String defaultValue;
    private String[] options;

    private String rangeOperator;
    private String rangeValue1;
    private String rangeValue2;

    private boolean calDependent = false;
    private boolean skipRuleDependent = false;
    private String answerTypeDisplay;
    private String questionTypeDisplay;

    private int editableType = 0;

    /**
     * Return the name.
     *
     * @return The question name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the question name.
     *
     * @param name String the name for question.
     */
    public void setName(String name)
    {
        if(name != null && !name.trim().equalsIgnoreCase(""))
        {
            this.name = name.trim();
        }
        else
        {
            this.name = null;
        }
    }

    /**
     * Return the text.
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
     * @param text The text for question.
     */
    public void setText(String text)
    {
        if(text != null && !text.trim().equalsIgnoreCase(""))
        {
            this.text = text.trim();
        }
        else
        {
            this.text = null;
        }
    }

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
    public String getvAlign()
    {
        return vAlign;
    }

    /**
     * Set the question vAlign property.
     *
     * @param vAlign String the vAlign for question.
     */
    public void setvAlign(String vAlign)
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
     * Return the type.
     *
     * @return The type property of the question.
     */
    public int getType()
    {
        return type;
    }

    /**
     * Set the question type property.
     *
     * @param type The type for question.
     */
    public void setType(int type)
    {
        this.type = type;
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
    public int[] getQuestionGroups()
    {
        return questionGroups;
    }

    /**
     * Set the question groups for this question.
     *
     * @param questionGroups int[] the question groups for the question.
     */
    public void setQuestionGroups(int[] questionGroups)
    {
        this.questionGroups = questionGroups;
    }

    /**
     * Return the availableQuestionGroups.
     *
     * @return The question groups available in the question library.
     */
    public int[] getAvailableQuestionGroups()
    {
        return availableQuestionGroups;
    }

    /**
     * Sets the available question groups.
     *
     * @param availableQuestionGroups The question groups available from
     *                                question library.
     */
    public void setAvailableQuestionGroups(int[] availableQuestionGroups)
    {
        this.availableQuestionGroups = availableQuestionGroups;
    }

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

    public boolean isSkipRuleDependent() {
		return skipRuleDependent;
	}

	public void setSkipRuleDependent(boolean skipRuleDependent) {
		this.skipRuleDependent = skipRuleDependent;
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
    public void setQuestionTypeDisplay(String display)
    {
        this.questionTypeDisplay = display;
    }

    /**
     * Return the question type display.
     *
     * @return The questionTypeDisplay property of the question.
     */
    public String getQuestionTypeDisplay()
    {
        return questionTypeDisplay;
    }

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

}