
package gov.nih.tbi.util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.QuerySolution;

import gov.nih.tbi.pojo.BeanField;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.PermissibleValue;
import gov.nih.tbi.pojo.QueryResult;
import gov.nih.tbi.pojo.StudyResult;

/**
 * Generates SPARQL queries and converts ResultSet to Java Objects
 * 
 * @author Francis Chen
 * 
 * @param <T>
 */
public abstract class ResultSetToBean<T> implements Serializable {

	private static final long serialVersionUID = -8888501444542022840L;

	private Class<T> type;
	private BeanField key;

	public abstract List<BeanField> getFields();

	public BeanField getKey() {

		if (key == null) {
			key = getFields().get(0);
		}

		return key;
	}

	/**
	 * Parses ResultSet into Java Object. Mode specifies whether or not to include java beans into the final result
	 * 
	 * @param rs
	 * @param mode
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public List<T> getBeans(QueryResult rs) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException {

		// Hashmap of the field name to result key and arrayList of items
		CollectionCache<T> collectionCache = new CollectionCache<T>(getKey(), type);

		// map of the key to a list of the key of the nested object, so if the primary
		// object is a form result then the
		// map would store a map of form result shortnames to a list of study titles
		Map<String, ArrayList<String>> secondaryKeyTracker = new HashMap<String, ArrayList<String>>();
		List<String> keyTracker = new ArrayList<String>(); // tracks the keys that have already been added
		List<BeanField> fields = getFields();
		List<T> resultList = new ArrayList<T>();

		for (QuerySolution row : rs.getQueryData()) {

			if (row.get(getKey().getName()) == null) {
				return null;
			}

			String key = row.get(getKey().getName()).toString();
			T newResult = null;

			// if the result with the specified key has not been added yet, create new
			// object and add the key to the
			// tracker
			if (!keyTracker.contains(key)) {
				newResult = getInstanceOfT();
				keyTracker.add(key);
				// log.info("Creating new object with key: " + key);
			}

			// iterate through each field
			for (BeanField currentField : fields) {
				// log.info("Current Field: " + currentField.getName());
				// skip the field if it is empty in the resultset
				if (row.get(currentField.getName()) == null) {
					continue;
				}

				// parse the resultset value into the object we want
				Object value = null;

				if (FormResult.class.equals(currentField.getType())) {
					ArrayList<String> secondKeyList = secondaryKeyTracker.get(key);
					String formUri = row.get("forms").toString();

					if (secondKeyList == null || !secondKeyList.contains(formUri)) {

						value = new FormResult();
						((FormResult) value).setUri(formUri);

						if (secondKeyList == null) {
							secondKeyList = new ArrayList<String>();
							secondKeyList.add(formUri);
							secondaryKeyTracker.put(key, secondKeyList);
						} else {
							secondKeyList.add(formUri);
						}
					}
				} else if (StudyResult.class.equals(currentField.getType())) {
					ArrayList<String> secondKeyList = secondaryKeyTracker.get(key);
					String studyUri = row.get("studies").toString();

					if (secondKeyList == null || !secondKeyList.contains(studyUri)) {
						value = new StudyResult();
						((StudyResult) value).setUri(studyUri);

						if (secondKeyList == null) {
							secondKeyList = new ArrayList<String>();
							secondKeyList.add(studyUri);
							secondaryKeyTracker.put(key, secondKeyList);
						} else {
							secondKeyList.add(studyUri);
						}
					}
				} else if (PermissibleValue.class.equals(currentField.getType())) {
					ArrayList<String> secondKeyList = secondaryKeyTracker.get(key);
					String pvUri = row.get("permissibleValues").toString();

					if (secondKeyList == null || !secondKeyList.contains(pvUri)) {
						value = new PermissibleValue();
						((PermissibleValue) value).setUri(pvUri);

						if (secondKeyList == null) {
							secondKeyList = new ArrayList<String>();
							secondKeyList.add(pvUri);
							secondaryKeyTracker.put(key, secondKeyList);
						} else {
							secondKeyList.add(pvUri);
						}
					}
				} else {
					value = row.get(currentField.getName()).toString(); // get the value of the row as a string
					value = ((String) value).split("\\^\\^")[0]; // get rid of any xsd typing
					value = ObjectConverter.convert(value, currentField.getType()); // attempt to convert the value into
																					 // the type we want
				}

				if (currentField.isCollection()) // if current field is a collection
				{
					collectionCache.addToCache(key, currentField, value);
				} else if (newResult != null && value != null) // not a collection and is a new result
				{
					Method setMethod = getSetMethod(type, currentField.getName(), currentField.getType());
					setMethod.invoke(newResult, value);
				}
				// ignore non-collection fields because that implies the field is a duplicate

			}

			// add the new result into the result list
			if (newResult != null) {
				resultList.add(newResult);
			}
		}

		return collectionCache.InsertCacheCollections(resultList);
	}

	/**
	 * Gets the set method a field
	 * 
	 * @param instanceBean
	 * @param field
	 * @param type
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static Method getSetMethod(Class<?> type, String field, Class<?> methodType)
			throws SecurityException, NoSuchMethodException {

		String setMethodName = "set" + field.substring(0, 1).toUpperCase() + field.substring(1);

		return type.getMethod(setMethodName, methodType);
	}

	public static Method getGetMethod(Class<?> type, String field) throws SecurityException, NoSuchMethodException {

		String getMethodName = "get" + field.substring(0, 1).toUpperCase() + field.substring(1);

		return type.getMethod(getMethodName);
	}

	private T getInstanceOfT() throws InstantiationException, IllegalAccessException {

		return type.newInstance();
	}

	public void setType(Class<T> type) {

		this.type = type;
	}
}
