package gov.nih.nichd.ctdb.common;

import org.w3c.dom.Document;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Nov 15, 2005
 * Time: 9:38:36 AM
 * To change this template use File | Settings | File Templates.
 */
public interface DomainObject extends java.io.Serializable {

    public Document toXML() throws TransformationException;
}

