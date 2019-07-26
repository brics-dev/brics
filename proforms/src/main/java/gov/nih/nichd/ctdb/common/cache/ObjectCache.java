package gov.nih.nichd.ctdb.common.cache;

import gov.nih.nichd.ctdb.common.CacheException;
import gov.nih.nichd.ctdb.common.cache.CachedList;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jun 20, 2007
 * Time: 7:44:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectCache {

    public static long TTL_2Hr = 7200000;
    public static final long TTL_1Hr = 3600000;
    public static long TTL_1Day = 86400000;
    public static final long TTL_HALF_DAY = 43200000;
    public static final long TTL_10Min = 600000;

        protected Map objectCache = new HashMap();

        public ObjectCache() {
            super();
           CacheCleanUpThread cleaner = new CacheCleanUpThread();
            cleaner.start();
        }

        public ObjectCache(long ttl) {
            super();
           CacheCleanUpThread cleaner = new CacheCleanUpThread(ttl);
            cleaner.start();
        }

        public Object getData (Object key) throws CacheException {
            if (objectCache.get(key) != null) {
                CachedObject data = (CachedObject)objectCache.get(key);
                if (!data.getSetDirty(CachedObject.DIRTY_ACTION_CHECK, false)) {
                    // cache is dirty
                    return data.getObjectCached();
                }
            }
            throw new CacheException("Data not found - reload");
        }


        public synchronized void setData (Object key, Object data) {
            objectCache.remove(key);
            CachedObject co = new CachedObject();
            co.setObjectCached(data);
            co.getSetDirty(CachedList.DIRTY_ACTION_SET, false);
            objectCache.put(key, co);
        }

    public void remove (Object key) {
        objectCache.remove(key);
    }

    public void getSetDirty (int action, Object key, boolean setValue) throws CacheException {
        if (this.getData(key) != null) {
            ((CachedObject)getData(key)).getSetDirty(CachedObject.DIRTY_ACTION_SET, setValue);
        }
    }



        private class CacheCleanUpThread extends Thread {

            private long TTL = TTL_2Hr;
            public CacheCleanUpThread() {}

            public CacheCleanUpThread (long ttl) {
                this.TTL = ttl;
            }


            public void run () {
                try {
                   // this.setDaemon(true);
                while (true) {
                   sleep(1800000);   // half an hour
                    //sleep (180000);
                   // System.err.println(".............cleaning");
                    List toRemove = new ArrayList();
                    for (Iterator i = objectCache.keySet().iterator(); i.hasNext(); ) {
                        Object key = i.next();
                        CachedObject cl = (CachedObject) objectCache.get(key);
                        if (cl.getBirthTime() + TTL < System.currentTimeMillis()) {
                           // System.err.println ("----------removing");
                            toRemove.add(key);
                        }
                    }
                    for (Iterator i = toRemove.iterator(); i.hasNext();){
                        Object removeKey = i.next();
                        objectCache.remove(removeKey);
                    }
                }
                } catch (InterruptedException ie) {
                    System.err.println (" FAILURE IN Object CACHE CLEANUP THREAD, CLEANING STOPPED");
                    ie.printStackTrace();
                    return;
                }
            }
        }




}
