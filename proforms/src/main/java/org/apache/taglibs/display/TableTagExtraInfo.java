/*
 * $Id: TableTagExtraInfo.java,v 1.3 2003/06/12 17:22:16 pathoss Exp $
 *
 * Todo
 *   - impementation
 *   - documentation (javadoc, examples, etc...)
 *   - junit test cases
 */

package org.apache.taglibs.display;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * Document me!
 *
 * @version $Revision: 1.3 $
 */
public class TableTagExtraInfo extends TagExtraInfo {
    public VariableInfo[] getVariableInfo(TagData data) {
        return new VariableInfo[]{
            new VariableInfo("table_index",
                             "java.lang.Integer",
                             true,
                             VariableInfo.NESTED),

            new VariableInfo("table_item",
                             "java.lang.Object",
                             true,
                             VariableInfo.NESTED),

        };
    }
}
