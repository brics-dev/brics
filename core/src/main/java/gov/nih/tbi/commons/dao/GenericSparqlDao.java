
package gov.nih.tbi.commons.dao;

import java.util.List;

public interface GenericSparqlDao<T>
{

    /**
     * Returns a list of all the objects, along with it's nested objects (like an eager get in hibernate)
     * 
     * @return
     */
    public List<T> getAll();
    
    public T get(String uri);

    public T save(T object);
}
