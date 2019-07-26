package gov.nih.nichd.ctdb.form.common;

import  gov.nih.nichd.ctdb.form.common.CalculatedDependencyException;

/**
 * Exception to be thrown when trying to delete a section which contains child of calculated questions.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class SectionDependencyException extends CalculatedDependencyException
{
    private String sectionName;

    /**
     * Get the SectionName value.
     * @return the SectionName value.
     */
    public String getSectionName() {
        return sectionName;
    }

    /**
     * Set the SectionName value.
     * @param newSectionName The new SectionName value.
     */
    public void setSectionName(String newSectionName) {
        this.sectionName = newSectionName;
    }

    /**
     * Default Constructor for the SectionQuestionDependencyException Object
     */
    public SectionDependencyException(String name1, String name2, String sectionName)
    {
        super(name1,name2);
        this.sectionName = sectionName;
    }
}
