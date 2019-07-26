package gov.nih.nichd.ctdb.ws;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.web.filter.RequestContextFilter;

public class ProformsApplication extends ResourceConfig {
	private static final Logger logger = Logger.getLogger(ProformsApplication.class);
	
	public ProformsApplication() {
		super();
		logger.info("Registering web serivce classes...");
		register(RequestContextFilter.class);
		register(SubmissionWS.class);
		register(ProtocolRestService.class);
	}
}
