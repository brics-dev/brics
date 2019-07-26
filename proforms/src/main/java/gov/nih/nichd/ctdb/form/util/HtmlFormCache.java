package gov.nih.nichd.ctdb.form.util;

import gov.nih.nichd.ctdb.common.cache.ObjectCache;
import gov.nih.nichd.ctdb.common.CacheException;
import gov.nih.nichd.ctdb.form.domain.Form;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Apr 9, 2008
 * Time: 10:22:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class HtmlFormCache extends ObjectCache {
        private static HtmlFormCache onlyInstance = null;

        private HtmlFormCache() {
            super(ObjectCache.TTL_1Day);
        }

        public static HtmlFormCache getInstance () {
            if (onlyInstance == null) {
                onlyInstance = new HtmlFormCache();
            }
            return onlyInstance;
        }

        public StringBuffer getHtml (int formId) throws CacheException {
            return (StringBuffer) this.getData(new Integer(formId));
        }


        public synchronized void setHtml (int formId, StringBuffer f) {
            this.setData(new Integer(formId), f);
        }

        public void removeHtml (int formId) {
            this.remove(new Integer(formId));
        }
}
