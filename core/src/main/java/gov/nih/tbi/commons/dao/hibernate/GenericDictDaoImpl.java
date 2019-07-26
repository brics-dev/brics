package gov.nih.tbi.commons.dao.hibernate;

import java.io.Serializable;
import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional("dictionaryTransactionManager")
public class GenericDictDaoImpl<T, PK extends Serializable> extends GenericDaoImpl<T, PK> {

	public GenericDictDaoImpl(final Class<T> persistentClass, SessionFactory sessionFactory) {
		super(persistentClass, sessionFactory);
	}
	
	public List<T> getAll() {
		return super.getAll();
	}

	public T get(PK id) {
		return super.get(id);
	}

	public boolean exists(PK id) {
		return super.exists(id);
	}

	public T save(T object) {
		return super.save(object);
	}

	public void remove(PK id) {
		super.remove(id);
	}

	public void removeAll(List<T> removeList) {
		super.removeAll(removeList);
	}
	
}
