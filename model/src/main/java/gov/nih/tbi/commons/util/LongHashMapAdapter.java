package gov.nih.tbi.commons.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LongHashMapAdapter extends XmlAdapter<LongHashMapAdapter.HashMapEntries, HashMap<Long, Long>> {
	protected static class HashMapEntries {
		public Set<HashMapEntry> mapEntry = new HashSet<HashMapEntry>();
	}

	protected static class HashMapEntry {

		public HashMapEntry() {

		}

		public HashMapEntry(Long key, Long value) {
			this.key = key;
			this.value = value;
		}

		@XmlAttribute
		public Long key;

		@XmlAttribute
		public Long value;
	}

	@Override
	public HashMap<Long, Long> unmarshal(HashMapEntries entries) throws Exception {
		HashMap<Long, Long> unmarshalledMap = new HashMap<Long, Long>();
		Set<HashMapEntry> entrySet = entries.mapEntry;
		for (HashMapEntry hashMapEntry : entrySet) {
			unmarshalledMap.put(hashMapEntry.key, hashMapEntry.value);
		}

		return unmarshalledMap;
	}

	@Override
	public HashMapEntries marshal(HashMap<Long, Long> map) throws Exception {
		HashMapEntries entries = new HashMapEntries();
		for (Entry<Long, Long> currentEntry : map.entrySet()) {
			entries.mapEntry.add(new HashMapEntry(currentEntry.getKey(), currentEntry.getValue()));
		}
		return entries;
	}
}
