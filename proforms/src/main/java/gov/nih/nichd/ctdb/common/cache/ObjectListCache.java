package gov.nih.nichd.ctdb.common.cache;

import gov.nih.nichd.ctdb.common.CacheException;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jun 20, 2007
 * Time: 8:01:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectListCache extends ObjectCache {

    public ObjectListCache () { super();}

        public List getData (Object key) throws CacheException {
            if (objectCache.get(key) != null) {
                CachedList data = (CachedList)objectCache.get(key);
                if (!data.getSetDirty(CachedList.DIRTY_ACTION_CHECK, CachedObject.unusedParameter)) {
                    // cache is dirty
                    return (List)data;
                }
            }
            throw new CacheException("Data not found - reload");
        }


        public synchronized void setData (Object key, List data) {
            objectCache.remove(key);
            CachedList list = new CachedList();
            list.getSetDirty(CachedList.DIRTY_ACTION_SET, false);
            list.addAll(data);
            objectCache.put(key, list);
        }

        
}
