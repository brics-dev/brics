package gov.nih.nichd.ctdb.question.domain;

import java.util.List;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Group DomainObject for the NICHD CTDB Applicatio
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class Group extends CtdbDomainObject
{
	private static final long serialVersionUID = 344293791230373037L;
	
	private String name;
    private String description;
    private List questions;
    private List images;

    /**
     * Default Constructor for the Group Domain Object
     */
    public Group()
    {
        //default constructor
    }

    /**
     * To get the group name.
     *
     * @return The group name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the group name.
     *
     * @param name String the group name to set to.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the group description text.
     *
     * @return The group description.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the description of the group.
     *
     * @param description String the description of the group.
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Gets the list of questions that belongs to this group.
     *
     * @return The list of questions that belongs to this group.
     */
    public List getQuestions()
    {
        return questions;
    }

    /**
     * Save this list of questions under this group.
     *
     * @param questions List the list of questions to be set under this group.
     */
    public void setQuestions(List questions)
    {
        this.questions = questions;
    }

    /**
     * Gets a list of image file names assigned in this group.
     *
     * @return The list of image file names for this group.
     */
    public List getImages()
    {
        return images;
    }

    /**
     * Sets the images of this group to this list of image file names.
     *
     * @param images List the list of image file names to be set to this group.
     */
    public void setImages(List images)
    {
        this.images = images;
    }

    /**
     * This method allows the transformation of a Group into an XML Document.
     * If no implementation is available at this time,
     * an UnsupportedOperationException will be thrown.
     *
     * @return XML Document
     * @throws TransformationException is thrown if there is an error during the XML tranformation
     * @throws UnsupportedOperationException is thrown if this method is currently unsupported and not implemented.
     */
    public Document toXML() throws TransformationException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in Group.");
    }

}