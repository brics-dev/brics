package gov.nih.nichd.ctdb.form.util;

import gov.nih.nichd.ctdb.common.cache.ObjectCache;
import gov.nih.nichd.ctdb.common.cache.CachedObject;
import gov.nih.nichd.ctdb.common.cache.CachedList;
import gov.nih.nichd.ctdb.common.CacheException;
import gov.nih.nichd.ctdb.form.domain.Form;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jan 3, 2008
 * Time: 3:27:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class FormCache extends ObjectCache {
        private static FormCache onlyInstance = null;

        private FormCache() {                                                                                        
            super(ObjectCache.TTL_1Day);
        }

        public static FormCache getInstance () {
            if (onlyInstance == null) {
                onlyInstance = new FormCache();
            }
            return onlyInstance;
        }

        public Form getForm (int formId) throws CacheException {
            return (Form) this.getData(new Integer(formId));
        }


        public synchronized void setForm (int formId, Form f) {
            this.setData(new Integer(formId), f);
        }

        public void removeForm (int formId) {
            this.remove(new Integer(formId));
        }
}
