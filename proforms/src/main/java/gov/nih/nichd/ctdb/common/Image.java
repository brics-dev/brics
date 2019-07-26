package gov.nih.nichd.ctdb.common;

import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * Image object for the NICHD application. This is used to dynamically populate
 * an image tag (&lt;img&gt;) to be used on a JSP. This object is a helper
 * to facilitate creating this tag and concatenating it with the current
 * web context. To create the image tag, create an image object
 * with all attributes specified and then call <code>getTag()</code>.
 * This object only work within a web application using Struts
 * with ApplicationResources properties defined in the configuration.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class Image {
    private String fileName;
    private String altTag;
    private int width;
    private int height;
    private int border;

    /**
     * Default constructor for the image object
     */
    public Image() {
        // default constructor
    }

    /**
     * Overloaded constructor to set all attributes of the image.
     *
     * @param fileName Image file name (Example: icon.gif)
     * @param altTag   Image alternate display
     * @param width    Image width
     * @param height   Image height
     * @param border   Image border
     */
    public Image(String fileName, String altTag, int width, int height, int border) {
        this.fileName = fileName;
        this.altTag = altTag;
        this.width = width;
        this.height = height;
        this.border = border;
    }

    /**
     * Gets the image file name
     *
     * @return The image file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the image file name
     *
     * @param fileName The image file name
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets alternate display for the image
     *
     * @return The alternate display
     */
    public String getAltTag() {
        return altTag;
    }

    /**
     * Sets the alternate display for the image
     *
     * @param altTag The alternate display
     */
    public void setAltTag(String altTag) {
        this.altTag = altTag;
    }

    /**
     * Gets the width of the image
     *
     * @return The image width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width of the image
     *
     * @param width The image width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Gets the height of the image
     *
     * @return The image height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height of the image
     *
     * @param height The image height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Gets the border of the image
     *
     * @return The image border
     */
    public int getBorder() {
        return border;
    }

    /**
     * Sets the border of the image
     *
     * @param border The image border
     */
    public void setBorder(int border) {
        this.border = border;
    }

    /**
     * Returns the HTML image tag for the image
     * (&lt;img src="" height="" width="" alt="" border=""&gt;). This method
     * will concatenate the web application context onto the image tag and
     * is dependent upon being used within a Struts application.
     *
     * @return The HTML &lt;img&gt; tag for the image
     */
    public String getTag() {
        String imageRoot = SysPropUtil.getProperty("app.imageroot");

        return "<img src=\"" + imageRoot + '/' + this.fileName +
                "\" alt=\"" + this.altTag +
                "\" border=\"" + this.border +
                "\" width=\"" + this.width +
                "\" height=\"" + this.height + "\">";
    }
}
