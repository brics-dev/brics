package gov.nih.nichd.ctdb.common;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Version implements the Version data for an object. It will take a version
 * number (1,2,3) and convert it into a letter (A,B,C) when the <code>toString()</code>
 * is called.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class Version implements Serializable {
	private static final long serialVersionUID = 5242288947481441113L;
	private int versionNumber = Integer.MIN_VALUE;

    /**
     * Sets a version object's number
     *
     * @param versionNumber The version number
     */
    
	public Version() {}

    public Version(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Version(String version) throws VersionSchemeException {

        /**
         * Use a regular expression to ensure that the version is formatted properly
         */
        Pattern p = Pattern.compile(CtdbConstants.CAP_LETTERS_REGEX);
        Matcher m = p.matcher(version);
        if (m.matches()) {
            this.versionNumber = getIntegerValue(version);
        } else {
            throw (new VersionSchemeException("The version was malformed"));
        }
    }

    /**
     * Gets the version number.
     *
     * @return int   the version number.
     */
    public int getVersionNumber() {
        return versionNumber;
    }

    /**
     * Sets the version number.
     *
     * @param versionNumber the version number
     */
    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    /**
     * Returns the String representation of the current version number.
     * The version number will match to the letter equalivent such that
     * 1=A, 2=B, 3=C, etc. Once the number exceeds the amount of single
     * characters, the string representation will be a combination of
     * letters. For example, 27=AA, 28=AB, 29=AC.
     *
     * @return The String representation of the version number
     */
    public String toString() {
        return convert(versionNumber);

    }

    public String getToString() {
        return this.toString();
    }

    /**
     * Converts the versionNumber (int) to
     * a string representing version in NICHD CTDB versioning format
     *
     * @param ver The version number
     * @return The version in letters
     */
    public String convert(int ver)
    {
    	CharSequence cs = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String alpha = "";
		
		if ( (ver > 0) && (ver <= 26) )
		{
			alpha = String.valueOf(cs.charAt(ver - 1));
		}
		else if ( ver > 26 )
		{
			alpha = "A" + convert(ver - 26);
		}
		
		return alpha;
    }

    /**
     * A helper method to convert the version string into a number.
     * <code>getIntegerValue</code> returns the Integer associated
     * whith the characters
     *
     * @param charRepresentation the character string to convert from
     * @return The Integer Representation
     */
    private int getIntegerValue(String charRepresentation) {
        int i, base, multiplier, position;
        int result = 0;
        int length = charRepresentation.length();

        for (i = 0; i < charRepresentation.length(); i++) {
            position = length - (i + 1);
            base = (int) Math.pow(CtdbConstants.NUM_LETTERS_IN_ALPHABET, (double) i);
            multiplier = (int) (Character.getNumericValue(charRepresentation.charAt(position)) - CtdbConstants.CHAR_TO_INT_OFFSET);
            result += base * multiplier;
        }
        return result;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + versionNumber;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Version other = (Version) obj;
		if (versionNumber != other.versionNumber)
			return false;
		return true;
	}
}
