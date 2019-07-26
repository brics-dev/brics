/*
 * $Id: ColumnDecorator.java,v 1.2 2003/06/12 17:22:15 pathoss Exp $
 */

package org.apache.taglibs.display;

/**
 * @version $Revision: 1.2 $
 */
public abstract class ColumnDecorator extends Decorator {
    public ColumnDecorator() {
        super();
    }

    public abstract String decorate(Object columnValue);
}