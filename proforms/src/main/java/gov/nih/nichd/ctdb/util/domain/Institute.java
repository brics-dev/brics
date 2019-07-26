package gov.nih.nichd.ctdb.util.domain;

import gov.nih.nichd.ctdb.common.CtdbLookup;

/**
 * Created by IntelliJ IDEA.
 * User: matt
 * Date: Apr 12, 2011
 * Time: 11:30:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class Institute extends CtdbLookup {
	private static final long serialVersionUID = -5665511414194872856L;
	
	public Institute(int id, String shortName, String longName) {
        super (id, shortName, longName);
    }
    public Institute (int id) {
        super (id);
    }

}
