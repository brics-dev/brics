package gov.nih.nichd.ctdb.response.util;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: May 9, 2006
 * Time: 9:14:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExpressDataEntryThreadManager {

    private  static HashMap executingThreads;

    static {
        executingThreads = new HashMap();
    }


    public synchronized static boolean updateThreadManager (int aFormid, boolean starting) throws Exception{
       try {
        if (starting) {
            // the trhead is trying to start, test
            if (executingThreads.get(new Integer(aFormid)) == null) {
                executingThreads.put(new Integer(aFormid), new Boolean(true));
                return true;
            } else {
                return false;
            }
        } else {
            //starting == false, thread is stopping
            executingThreads.remove(new Integer(aFormid));
            return true;
        }
       } catch(Exception e) {
           System.err.println("----------------!!!!!!!!!!!!!!!!!-----------------------------");
           System.err.println (" Error updating ExpressDataEntryThread Manager " );
           System.err.println(" Orignal Msg : " + e.getMessage());
           e.printStackTrace();
           throw (e);
       }
    }



}
