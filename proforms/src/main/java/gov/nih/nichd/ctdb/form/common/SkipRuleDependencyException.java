package gov.nih.nichd.ctdb.form.common;

import  gov.nih.nichd.ctdb.common.CtdbException;

/**
 * Exception to be thrown when trying to insert a skip rule question with missing dependent questions
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class SkipRuleDependencyException extends CtdbException
{
    private String name1;
    private String name2;

    /**
     * Default Constructor for the SkipRuleQuestionDependencyException Object
     */
    public SkipRuleDependencyException(String name1, String name2)
    {
        super();
        this.name1 = name1;
        this.name2 = name2;
    }

    /**
     * Get the name2 value.
     * @return the name2 value.
     */
    public String getName2() {
        return name2;
    }

    /**
     * Set the name2 value.
     * @param name2 The new name2 value.
     */
    public void setName2(String name2) {
        this.name2 = name2;
    }

    /**
     * Get the name1 value.
     * @return the name1 value.
     */
    public String getName1() {
        return name1;
    }

    /**
     * Set the name1 value.
     * @param name1 The new name1 value.
     */
    public void setName1(String name1) {
        this.name1 = name1;
    }

}
