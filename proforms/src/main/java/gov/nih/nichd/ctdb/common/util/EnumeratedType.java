package gov.nih.nichd.ctdb.common.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class EnumeratedType implements Serializable, Cloneable {
	private static final long serialVersionUID = -2528893003458598688L;
	private static final Map<String, Map<Integer, EnumeratedType>> types = new Hashtable<String, Map<Integer, EnumeratedType>>();

	private int value;
	private String desc;

	protected EnumeratedType(int paramInt, String paramString) {
		this.value = paramInt;
		this.desc = paramString;
		checkForDupes(this);
		storeType(this);
	}

	private void checkForDupes(EnumeratedType paramEnumeratedType) throws RuntimeException {
		String str = paramEnumeratedType.getClass().getName();
		Map<Integer, EnumeratedType> localHashtable = types.get(str);
		
		if ((localHashtable == null) || (localHashtable.get(Integer.valueOf(paramEnumeratedType.getValue())) == null)) {
			return;
		}
		
		System.out.println("No Dupes Allowed: " + str + "=" + paramEnumeratedType);
		throw new RuntimeException();
	}

	private void storeType(EnumeratedType paramEnumeratedType) {
		String str = paramEnumeratedType.getClass().getName();
		Map<Integer, EnumeratedType> localMap;
		
		synchronized (types) {
			localMap = types.get(str);
			if (localMap == null) {
				localMap = new Hashtable<Integer, EnumeratedType>();
				types.put(str, localMap);
			}
		}
		
		localMap.put(Integer.valueOf(paramEnumeratedType.getValue()), paramEnumeratedType);
	}

	public static <T> EnumeratedType getByValue(Class<T> paramClass, int paramInt) {
		EnumeratedType localEnumeratedType = null;
		String str = paramClass.getName();
		Map<Integer, EnumeratedType> localMap = types.get(str);
		
		if (localMap != null) {
			localEnumeratedType = localMap.get(Integer.valueOf(paramInt));
		}
		
		return localEnumeratedType;
	}

	public static <T> Iterator<EnumeratedType> elements(Class<T> paramClass) {
		String str = paramClass.getName();
		Map<Integer, EnumeratedType> localMap = types.get(str);
		
		if (localMap != null) {
			return localMap.values().iterator();
		}
		
		return null;
	}

	public static <T> EnumeratedType[] toArray(Class<T> paramClass) {
		String str = paramClass.getName();
		Map<Integer, EnumeratedType> localMap = types.get(str);
		
		if (localMap != null) {
			Collection<EnumeratedType> values = localMap.values();
			
			return values.toArray(new EnumeratedType[values.size()]);
		}
		
		return null;
	}

	public static <T> int getMaxValue(Class<T> paramClass) {
		int i = -1;
		Iterator<EnumeratedType> localIterator = elements(paramClass);
		
		while (localIterator.hasNext()) {
			EnumeratedType localEnumeratedType = localIterator.next();
			int j = localEnumeratedType.getValue();
			
			if (j <= i) {
				continue;
			}
			
			i = j;
		}
		
		return i;
	}

	public int getValue() {
		return this.value;
	}

	public String getDispValue() {
		return this.desc;
	}

	@Override
	public boolean equals(Object paramObject) {
		if ( paramObject == null ) {
			return false;
		}
		
		if ( this == paramObject ) {
			return true;
		}
		
		if ( paramObject instanceof EnumeratedType ) {
			EnumeratedType other = (EnumeratedType) paramObject;
			
			return this.getValue() == other.getValue();
		}
		
		return false;
	}
}
