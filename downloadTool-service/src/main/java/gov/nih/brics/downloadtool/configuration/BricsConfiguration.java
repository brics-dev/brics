package gov.nih.brics.downloadtool.configuration;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BricsConfiguration {
	public static final String MICROSERVICES_DIRECTORY = "microservices";
	public static final String DATASOURCES_DIRECTORY = "datasources";
	
	@Value("${conf.dir}")
	private String confDir;
	
	@Value("${spring.application.name}")
	private String applicationName;
	
	public void load(String tenantName) throws IOException {
		/*
		 * If properties need to be set, uncomment the below and follow the pattern
		 * 
		Resource serviceProperties = new FileSystemResource(confDir + File.separator + MICROSERVICES_DIRECTORY + File.separator + tenantName +  File.separator + applicationName + ".properties");
		Properties props = PropertiesLoaderUtils.loadProperties(serviceProperties);
		accountExpirationDays = Integer.valueOf(props.getProperty("brics.security.account.expirationDays"));
		 */
	}

	public String getConfDir() {
		return confDir;
	}

	public void setConfDir(String confDir) {
		this.confDir = confDir;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
}
