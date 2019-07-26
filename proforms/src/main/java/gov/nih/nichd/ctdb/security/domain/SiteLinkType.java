package gov.nih.nichd.ctdb.security.domain;

/**
 * Created by IntelliJ IDEA.
 * User: matt
 * Date: Apr 27, 2011
 * Time: 9:31:34 AM
 * To change this template use File | Settings | File Templates.
 */
public enum SiteLinkType {
    PROTOCOL(1), REGULATION(2);
    private int id;

    private SiteLinkType (int _id){
        id = _id;
    }
    public int getId(){
        return id;
    }
}
