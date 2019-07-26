package gov.nih.nichd.ctdb.response.util;

import java.util.List;
import java.util.Iterator;

/**
 * Created by Booz Allen Hamilton
 * Date: Nov 18, 2004
 * 
 */
public class DataCollectionSummary {

    private String formVersion;
    private String formDescription;
    private int numQuestions;
    private int numCertifiedPatients;
    private List intervalsAdministered;

    public String getFormVersion() {
        return formVersion;
    }

    public void setFormVersion(String formVersion) {
        this.formVersion = formVersion;
    }

    public String getFormDescription() {
        return formDescription;
    }

    public void setFormDescription(String formDescription) {
        this.formDescription = formDescription;
    }

    public int getNumQuestions() {
        return numQuestions;
    }

    public void setNumQuestions(int numQuestions) {
        this.numQuestions = numQuestions;
    }

    public int getNumCertifiedPatients() {
        return numCertifiedPatients;
    }

    public void setNumCertifiedPatients(int numCertifiedPatients) {
        this.numCertifiedPatients = numCertifiedPatients;
    }

    public List getIntervalsAdministered() {
        return intervalsAdministered;
    }

    public void setIntervalsAdministered(List intervalsAdministered) {
        this.intervalsAdministered = intervalsAdministered;
    }

    public String getIntervalsString () {
        String results = "";
        for (Iterator i = intervalsAdministered.iterator(); i.hasNext();){
            results += (String)i.next() + ", ";
        }
        if (! results.equals("")) {
            results = intervalsAdministered.size() + " (" +results.substring(0, results.length() - 2) + ")";
        }
        return results;
    }

}
