package gov.nih.nichd.ctdb.ebinder.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Created by IntelliJ IDEA.
 * User: matt
 * Date: Apr 27, 2011
 * Time: 9:26:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class Ebinder extends CtdbDomainObject
{
	private static final long serialVersionUID = 1410283452367770403L;
	
	private String name = "";
    private long studyId = Integer.MIN_VALUE;
    private CtdbLookup type = null;
    private String jsonTree = "{}";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStudyId() {
        return studyId;
    }

    public void setStudyId(long studyId) {
        this.studyId = studyId;
    }

    public Document toXML() throws TransformationException, UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented");
    }

	/**
	 * @return the jsonTree
	 */
	public String getJsonTree() {
		return jsonTree;
	}

	/**
	 * @param jsonTree the jsonTree to set
	 */
	public void setJsonTree(String jsonTree) {
		this.jsonTree = jsonTree;
	}

	/**
	 * @return the type
	 */
	public CtdbLookup getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(CtdbLookup type) {
		this.type = type;
	}
}
