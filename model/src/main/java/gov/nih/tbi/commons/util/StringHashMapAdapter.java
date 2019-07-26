package gov.nih.tbi.commons.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class StringHashMapAdapter extends XmlAdapter<StringHashMapAdapter.HashMapEntries, HashMap<String, String>> {
	protected static class HashMapEntries {
		public Set<HashMapEntry> mapEntry = new HashSet<HashMapEntry>();
	}

	protected static class HashMapEntry {

		public HashMapEntry() {

		}

		public HashMapEntry(String key, String value) {
			this.key = key;
			this.value = value;
		}

		@XmlAttribute
		public String key;

		@XmlAttribute
		public String value;
	}

	@Override
	public HashMap<String, String> unmarshal(HashMapEntries entries) throws Exception {
		HashMap<String, String> unmarshalledMap = new HashMap<String, String>();
		Set<HashMapEntry> entrySet = entries.mapEntry;
		for (HashMapEntry hashMapEntry : entrySet) {
			unmarshalledMap.put(hashMapEntry.key, hashMapEntry.value);
		}

		return unmarshalledMap;
	}

	@Override
	public HashMapEntries marshal(HashMap<String, String> map) throws Exception {
		HashMapEntries entries = new HashMapEntries();
		for (Entry<String, String> currentEntry : map.entrySet()) {
			entries.mapEntry.add(new HashMapEntry(currentEntry.getKey(), currentEntry.getValue()));
		}
		return entries;
	}
}
