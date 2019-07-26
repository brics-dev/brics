package gov.nih.nichd.ctdb.response.common;

import gov.nih.nichd.ctdb.common.CtdbException;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Feb 6, 2007
 * Time: 9:26:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class VisitDateMismatchException extends CtdbException {
    private String thedate;

    public VisitDateMismatchException(){}
    public VisitDateMismatchException(String s) {
        super (s);
    }

    public VisitDateMismatchException(String s, String ss) {
        super (s);
        this.thedate = ss;
    }


    public String getThedate() {
        return thedate;
    }

    public void setThedate(String thedate) {
        this.thedate = thedate;
    }

}
