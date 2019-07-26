package gov.nih.tbi.commons;

import java.util.Properties;
import java.util.Random;

import gov.nih.tbi.commons.model.exceptions.ImmutableStateException;
import gov.nih.tbi.commons.model.exceptions.DataIsolationException;

/**
 * Created by amakar on 7/11/2017.
 */


public class AppConfig {

    private static AppConfig singleton;

    private static boolean isSet;
    private Properties props;
    private String securKey;

    private AppConfig() {

        isSet = false;
        this.props = new Properties();
    }

    public static AppConfig getInstance() {

        if(singleton == null) {
            singleton = new AppConfig();
        }

        return singleton;
    }

    public synchronized String init() throws DataIsolationException {

        if(!isSet && this.securKey == null) {
            this.securKey = getRandomHexString(50);
            return this.securKey;
        } else {
            throw new DataIsolationException();
        }
    }

    public void commit(String _key) {
        if(this.validateInitialization(_key)) {
            isSet = true;
        }
    }

    public void setProperty(String _propName, String _propVal, String _key) {

        if(this.validateInitialization(_key)) {

            this.props.setProperty(_propName, _propVal);
        } else {
            throw new ImmutableStateException("Attempted to reset AppConfig value " + _propName);
        }
    }

    private boolean validateInitialization(String _key) {
        boolean result = !isSet && this.securKey != null && this.securKey.equals(_key);
        return result;
    }

    public String getProperty(String _propName) {

        String result = isSet ? this.props.getProperty(_propName) : null;

        return result;
    }

    private static String getRandomHexString(int numchars){
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < numchars){
            sb.append(Integer.toHexString(r.nextInt()));
        }

        return sb.toString().substring(0, numchars);
    }

    public static boolean getIsSet() {
        return isSet;
    }
}
