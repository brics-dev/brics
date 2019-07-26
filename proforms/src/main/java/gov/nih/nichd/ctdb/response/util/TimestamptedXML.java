package gov.nih.nichd.ctdb.response.util;

import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Booz Allen Hamilton
 * Date: Jul 8, 2004
 * 
 */
public class TimestamptedXML {

    private Date time;
    private Document doc;

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Document getDoc() {
        return doc;
    }

    public void setDoc(Document doc) {
        this.doc = doc;
    }
}
