package gov.nih.nichd.ctdb.util.common;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class SysPropUtil extends PropertyPlaceholderConfigurer {
	private static final Logger LOGGER = Logger.getLogger(SysPropUtil.class);
	private static Map<String, String> propertiesMap = new ConcurrentHashMap<String, String>();
	
	private int springSystemPropertiesMode;

	public SysPropUtil() {
		super();
		springSystemPropertiesMode = PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_FALLBACK;
	}

	@Override
	public void setSystemPropertiesMode(int systemPropertiesMode) {
		super.setSystemPropertiesMode(systemPropertiesMode);
		springSystemPropertiesMode = systemPropertiesMode;
	}
	
	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
		super.processProperties(beanFactoryToProcess, props);
		LOGGER.info("Loading property values into the system properties map...");
		
		// Populate the properties map.
		for ( Object key : props.keySet() ) {
			String keyStr = key.toString();
			String value = resolvePlaceholder(keyStr, props, springSystemPropertiesMode);
			
			propertiesMap.put(keyStr, value);
		}
	}
	
	public static synchronized String getProperty(String key) {
		return propertiesMap.get(key);
	}
	
	public void setSystemPropertiesModeName(String constantName) {
		super.setSystemPropertiesModeName(constantName);
	}
}
