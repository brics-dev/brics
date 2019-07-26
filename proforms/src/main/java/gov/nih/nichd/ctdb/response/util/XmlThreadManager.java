package gov.nih.nichd.ctdb.response.util;

import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;

import gov.nih.nichd.ctdb.form.domain.Form;

/**
 * Created by Booz Allen Hamilton
 * Date: Jul 8, 2004
 * 
 */
public class XmlThreadManager {

    private static HashMap threadsExecuting;
    private static HashMap formXml;
    private static Date lastCleanup;

    static {
        threadsExecuting = new HashMap();
        formXml = new HashMap();
        lastCleanup = new Date();

    }

    public void addXml (String key, TimestamptedXML xml) {
        if (formXml.get(key) != null) {
            formXml.remove(key);
        }
        formXml.put(key, xml);
    }

    synchronized public void cleanUp () {
        ArrayList toRemove = new ArrayList();
        Date d = new Date(System.currentTimeMillis() - 1500000);
        if (lastCleanup.before (d)) {
            for (Iterator iter = formXml.keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                 Date age = ((TimestamptedXML) formXml.get(key)).getTime();
                 if (age.before(d)) {
                 // xml is 15 min old, remove it
                     toRemove.add(key);
                }
            }
            for (Iterator it = toRemove.iterator(); it.hasNext();){
                formXml.remove(it.next());
            }
        }
    }

    synchronized public void removeXml (String key) {
        formXml.remove(key);
    }

    public synchronized Document getXml (Form f) {
        Document doc = null;
        String key = f.getId() + f.getVersion().getToString();
        while (true) {

            if (formXml.get(key) == null) {
                try {
                    if (threadsExecuting.get(key) != null) {
                    // the thread is still executing, wait.
                        wait (200);
                    } else {
                        // form requested is not availiable and is not loading
                        System.err.println (" INVALID FORM XML REQUESTED FROM THREAD.... LOADING");
                        XmlRetrevialThread xrt = new XmlRetrevialThread(f);
                        xrt.start();
                        wait (2000);
                    }
                } catch (InterruptedException ie) {
                }
                continue;
            } else if (((TimestamptedXML)formXml.get(key)).getTime().before(f.getUpdatedDate())) {
                // xml is out of date get new one
                try {
                    formXml.remove(key);
                    XmlRetrevialThread xrt = new XmlRetrevialThread(f);
                    xrt.start();
                    wait (2000);
                 } catch (InterruptedException ie) {
                }
                continue;
            }
             doc = ((TimestamptedXML)formXml.get(key)).getDoc();
            break;
        }
        cleanUp();
        return doc;
    }

    public void setLoading (String sessionId) {
          threadsExecuting.put (sessionId, Boolean.valueOf(true));
        // remove any form that exists but has not been claimed
        formXml.remove(sessionId);
    }

    public boolean isLoading (String key) {
        if (threadsExecuting.get(key) != null) {
            return true;
        }
        else {
            return false;
        }
    }

    public void finishedLoading (String sessionId) {
        threadsExecuting.remove(sessionId);
    }

    public boolean hasXml (String key) {
        if (formXml.get(key) != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isUpToDate (String key, Date formModified) {
        if (((TimestamptedXML)formXml.get (key)).getTime().after(formModified)) {
            return true;
        } else {
            return false;
        }
    }


}


