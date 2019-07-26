package gov.nih.nichd.ctdb.protocol.domain;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: matt
 * Date: Jan 23, 2009
 * Time: 10:59:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProtocolDefaults implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -5469709576646257790L;
	
	private static HashMap<Integer, String> defaultKey = new HashMap<Integer, String>();
    static {
        defaultKey.put(new Integer(1), "dataQualityMonitoring");
        defaultKey.put(new Integer(2), "patientEditReasons");
        defaultKey.put(new Integer(3), "qaAlertPatientEdits");

    }

    private int[] selectedDefaults = {};
    boolean sampleUncollected = false;
    boolean dataQualityMonitoring = false;
    boolean  patientEditReasons = false;
    boolean generateSampleNames= false;
    boolean  qaAlertPatientEdits = false;


    public boolean isSampleUncollected() {
        return sampleUncollected;
    }

    public void setSampleUncollected(boolean sampleUncollected) {
        this.sampleUncollected = sampleUncollected;
    }

    public boolean isDataQualityMonitoring() {
        return dataQualityMonitoring;
    }

    public void setDataQualityMonitoring(boolean dataQualityMonitoring) {
        this.dataQualityMonitoring = dataQualityMonitoring;
    }

    public boolean isPatientEditReasons() {
        return patientEditReasons;
    }

    public void setPatientEditReasons(boolean patientEditReasons) {
        this.patientEditReasons = patientEditReasons;
    }


    public int[] getSelectedDefaults() {
        if (selectedDefaults == null) {
            return new int[0];
        } else {
            return selectedDefaults;
        }
    }


    public boolean isGenerateSampleNames() {
        return generateSampleNames;
    }

    public void setGenerateSampleNames(boolean generateSampleNames) {
        this.generateSampleNames = generateSampleNames;
    }

    public void setSelectedDefaults(int[] selectedDefaults) {
        if (selectedDefaults == null) {
            selectedDefaults = new int[0]; 
        }

        this.selectedDefaults = selectedDefaults;
        this.initMembers();
    }

    private void initMembers () {
        Class<ProtocolDefaults> cls = ProtocolDefaults.class;

        try {
	        for (int i = 0; i < selectedDefaults.length; i++) {
	            Field f = cls.getDeclaredField(defaultKey.get(new Integer(selectedDefaults[i])));
	            f.setBoolean(this, true);
	        }
        } catch (NoSuchFieldException nsfe) {
            System.err.println ("failure finding field for init protocol defaults");
        } catch (IllegalAccessException iae) {
            System.err.println ("failure setting field for init protocol defaults");
        }
    }
}
