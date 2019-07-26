package gov.nih.nichd.ctdb.question.domain;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * QuestionImage DomainObject for the NICHD CTDB Application.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class QuestionImage extends CtdbDomainObject
{
	private static final long serialVersionUID = 4093420582829058375L;
	
	private List<String> names;
    private String path;
    private List<File> files;
    private List<BufferedImage> bufferedImage;
    private List<String> namesToDelete;
    
    /**
     * Gets the image file save path
     *
     * @return The image file save path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Sets the image file save path
     *
     * @param path The image file save path
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * Get the list of image file names
     * @return List the list of image file names.
     */
    public List<String> getNames()
    {
        return names;
    }

    /**
     * Set the image file names.
     *
     * @param names List the list of image file names to be set to.
     */
    public void setNames(List<String> names)
    {
        this.names = names;
    }

    /**
     * Get the list of image files.
     *
     * @return The list of image files.
     */
    public List<File> getFiles()
    {
        return files;
    }

    /**
     * Set the image files.
     *
     * @param files List the list of image files to be set.
     */
    public void setFiles(List<File> files)
    {
        this.files = files;
    }

    /**
     * Get the list of image file names to be deleted.
     *
     * @return The list of image file names.
     */
    public List<String> getNamesToDelete()
    {
        return namesToDelete;
    }

    /**
     * Set the image file names that will be deleted.
     *
     * @param namesToDelete List the list of image file names.
     */
    public void setNamesToDelete(List<String> namesToDelete)
    {
        this.namesToDelete = namesToDelete;
    }

	/**
	 * This method allows the transformation of a QuestionImage into an XML
	 * Document. If no implementation is available at this time, an
	 * UnsupportedOperationException will be thrown.
	 *
	 * @return XML Document
	 * @throws TransformationException is thrown if there is an error during the XML tranformation
	 * @throws UnsupportedOperationException is thrown if this method is currently unsupported and not implemented.
	 */
	public Document toXML() throws TransformationException, UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"No Implementation at this time for the method toXML() in QuestionImage.");
	}

	public List<BufferedImage> getBufferedImage() {
		return bufferedImage;
	}

	public void setBufferedImage(List<BufferedImage> bufferedImage) {
		this.bufferedImage = bufferedImage;
	}
}
