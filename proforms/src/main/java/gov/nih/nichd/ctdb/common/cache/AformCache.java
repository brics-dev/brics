package gov.nih.nichd.ctdb.common.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import gov.nih.nichd.ctdb.response.domain.AdministeredForm;


@Component
@ApplicationScope
public class AformCache {
	
	private Map<String,AdministeredForm> aformCache;
	
	public AformCache() {
		aformCache = new ConcurrentHashMap<>();
	}
	
	public void add(String key, AdministeredForm aform) {
		aformCache.put(key, aform);
	}
	
	public void remove(String key) {
		aformCache.remove(key);
	}
	
	public void clear() {
		aformCache.clear();
	}
	
	public AdministeredForm get(String key) {
		return aformCache.get(key);
	}

}
