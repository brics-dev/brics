package gov.nih.tbi.util;

import gov.nih.tbi.pojo.BeanField;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class CollectionCache<T>
{
    //key, beanfield, collection
    private HashMap<String, HashMap<BeanField, ArrayList<Object>>> cacheMap;
    private BeanField key;
    private Class<?> type;
    
    public CollectionCache(BeanField key, Class<?> type)
    {
        this.type = type;
        this.key = key;
        cacheMap = new HashMap<String, HashMap<BeanField, ArrayList<Object>>>();
    }

    public List<T> InsertCacheCollections(List<T> resultList) throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {

        // set the collection fields in our result list using the collection cache
        for (String key : cacheMap.keySet())
        {
            HashMap<BeanField, ArrayList<Object>> collectionMap = cacheMap.get(key);
            for (T result : resultList)
            {
                Method getKeyMethod = ResultSetToBean.getGetMethod(type, this.key.getName());
                Object resultKeyObj = getKeyMethod.invoke(result);
                String resultKey = null;

                // checking if the key exists
                if (resultKeyObj == null)
                {
                    continue;
                }
                else
                {
                    resultKey = getKeyMethod.invoke(result).toString();
                }

                if (resultKey.equals(key))
                {
                    for (BeanField field : collectionMap.keySet())
                    {
                        List<Object> collection = collectionMap.get(field);

                        if (collection != null)
                        {

                            Method setMethod = ResultSetToBean.getSetMethod(type, field.getName(), List.class);
                            setMethod.invoke(result, collection);
                        }
                    }
                }
            }
        }

        return resultList;
    }
    
    /**
     * Adds the value into the collection cache
     * @param key
     * @param field
     * @param value
     */
    public void addToCache(String key, BeanField field, Object value)
    {

        HashMap<BeanField, ArrayList<Object>> collectionMap = cacheMap.get(key);
        
        if(value == null)
        {
            return;
        }
        
        if (collectionMap != null) // if key exists in the map
        {
            ArrayList<Object> collection = collectionMap.get(field);

            if (collection != null) // if field exists for the specified key
            {
                if (!collection.contains(value))
                {
                    collection.add(value);
                }
            }
            else
            // this is a newly discovered field
            {
                ArrayList<Object> newCollection = new ArrayList<Object>();
                newCollection.add(value);
                collectionMap.put(field, newCollection);
            }
        }
        else
        // key not yet exists
        {
            collectionMap = new HashMap<BeanField, ArrayList<Object>>();
            cacheMap.put(key, collectionMap);
            addToCache(key, field, value);
        }
    }
}
