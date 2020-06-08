package gov.nih.brics.auth.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("application")
public class BlackListCache {
	
	private HashMap<String, Long> cache;
	
	public BlackListCache() {
		cache = new HashMap<>();
	}
	
	public boolean contains(String token) {
		removeExpired();
		return cache.containsKey(token);
	}
	
	public void add(String token, Date expiration) {
		removeExpired();
		cache.put(token, expiration.getTime());
	}
	
	public void remove(String token) {
		cache.remove(token);
	}
	
	public List<String> findExpired() {
		ArrayList<String> expired = new ArrayList<>();
		Long now = new Date().getTime();
		for (Map.Entry<String, Long> entry : cache.entrySet()) {
			// "now" is in the future from the entry's expiration
			if (entry.getValue() < now) {
				expired.add(entry.getKey());
			}
		}
		return expired;
	}
	
	private void removeExpired() {
		List<String> expired = findExpired();
		for (String entry : expired) {
			remove(entry);
		}
	}
}
