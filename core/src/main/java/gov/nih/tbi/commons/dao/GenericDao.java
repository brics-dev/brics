
package gov.nih.tbi.commons.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface GenericDao<T, PK extends Serializable>
{

    public List<T> getAll();

    public T get(PK id);

    public boolean exists(PK id);

    public T save(T object);

    public void remove(PK id);

    public void removeAll(List<T> removeList);
    
    public void batchSave(Set<T> objects);
}
