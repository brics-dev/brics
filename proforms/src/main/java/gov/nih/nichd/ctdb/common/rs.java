package gov.nih.nichd.ctdb.common;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class rs {
	
	//list of resource bundles
	protected static String[] resourcebundles = {};
	
	/**
	 * Retrieves the value of the property. If the key is not defined the default value is returned.
	 * @param key property key
	 * @return String value of the property
	 */
	public static String getValue(String key,Locale l){
		String value=null;
		ResourceBundle msgRes = ResourceBundle.getBundle("ApplicationResources",l);
		value = msgRes.getString(key);
		return value;
	}	

	/**
	 * Retrieves the value of the property. If the key is not defined the default value is returned.
	 * 
	 * @param key - property key
	 * @param l - Locale
	 * @param arg0 - Replacement argument
	 * @return String value of property
	 */
	public static String getValue(String key, Locale l, Object arg0){
		String value=null;
		ResourceBundle msgRes = ResourceBundle.getBundle("ApplicationResources", l);
		value = MessageFormat.format(msgRes.getString(key), arg0);
		return value;
	}	

    /**
     * @param strings
     */
    public static void setResourcebundles(String[] strings) {
        resourcebundles = strings;
    }

}
