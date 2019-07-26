
package gov.nih.tbi.portal;

import gov.nih.tbi.account.model.SessionAccount;

import java.io.Serializable;
import java.util.Iterator;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;

public class TbiInterceptor extends EmptyInterceptor
{

    /**
	 * 
	 */
    private static final long serialVersionUID = -695323034329443321L;

    /**
     * Session's Current User: used to record the user who last edited an object.
     */
    @Autowired
    SessionAccount sessionAccount;

    /**
     * Delete Entity
     */
    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
    {

        super.onDelete(entity, id, state, propertyNames, types);
    }

    /**
     * After Loading information from database
     */
    @Override
    public void postFlush(Iterator entities)
    {

        super.postFlush(entities);
    }

    /**
     * Update Entity
     */
    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
            String[] propertyNames, Type[] types)
    {

        return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    }

    /**
     * Create Entity
     */
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
    {

        return super.onSave(entity, id, state, propertyNames, types);
    }

    /**
     * Iterates over propertyNames in order to set currentStates's corresponding value
     * 
     * @param currentState
     * @param propertyNames
     * @param propertyToSet
     * @param value
     */
    private void setValue(Object[] currentState, String[] propertyNames, String propertyToSet, Object value)
    {

        for (int i = 0; i < propertyNames.length; i++)
        {
            if (propertyNames[i].equalsIgnoreCase(propertyToSet))
            {
                currentState[i] = value;
            }
        }

    }
}
