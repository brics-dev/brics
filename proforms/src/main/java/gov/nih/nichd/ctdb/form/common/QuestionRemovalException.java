package gov.nih.nichd.ctdb.form.common;

import gov.nih.nichd.ctdb.common.CtdbException;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jul 10, 2006
 * Time: 11:30:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class QuestionRemovalException  extends CtdbException {
	private static final long serialVersionUID = -2232796226412501187L;

	public QuestionRemovalException (String msg ) {
        super (msg);
    }

    public QuestionRemovalException (String msg, Throwable ex) {
        super (msg, ex);
    }
}
