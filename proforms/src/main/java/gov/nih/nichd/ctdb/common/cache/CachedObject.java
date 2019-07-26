package gov.nih.nichd.ctdb.common.cache;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jun 20, 2007
 * Time: 8:05:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class CachedObject {

     public static final int DIRTY_ACTION_CHECK = 0;
    public static final int DIRTY_ACTION_SET = 1;
    public static final boolean unusedParameter = false;

    private Object objectCached;
    private long ttl;
    private boolean dirty;
    private long birthTime;

    public CachedObject() {
        this.birthTime = System.currentTimeMillis();
    }


    public synchronized boolean getSetDirty (int action, boolean setValue){
        if (action == DIRTY_ACTION_CHECK) {
            return dirty;
        } else if (action == DIRTY_ACTION_SET) {
            dirty = setValue;
            return dirty;
        } else {
            return dirty;
        }
    }


    public Object getObjectCached() {
        return objectCached;
    }

    public void setObjectCached(Object objectCached) {
        this.objectCached = objectCached;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }


    public long getBirthTime() {
        return birthTime;
    }

    public void setBirthTime(long birthTime) {
        this.birthTime = birthTime;
    }


}
