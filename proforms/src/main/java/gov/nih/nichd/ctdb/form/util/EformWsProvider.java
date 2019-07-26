package gov.nih.nichd.ctdb.form.util;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

public class EformWsProvider {
	private static final Logger logger = Logger.getLogger(EformWsProvider.class);
	
	public EformWsProvider() {}
	
	public InputStream saveEFormToDictForMigration(HttpServletRequest request, String eFormJsonStr) 
			throws WebApplicationException, IOException, RuntimeException {
		String ddtRootURL = SysPropUtil.getProperty("webservice.restful.ddt.domain");
		String wsUrl = ddtRootURL + SysPropUtil.getProperty("webservice.restful.eform.ddt.url") + "/import/migration";
		SecuritySessionUtil ssu = new SecuritySessionUtil(request);
		String proxyTicket = ssu.getProxyTicket(ddtRootURL);
		
		if ( proxyTicket == null ) {
			throw new RuntimeException("No proxy ticket was created.");
		}
		
		Client client = ClientBuilder.newClient();
		WebTarget wt = client.target(wsUrl).queryParam("ticket", proxyTicket);
		
		logger.info("Save eForm URL: " + wt.getUri().toString());
		
		// Add in form values.
		Form httpForm = new Form();
		httpForm.param("eformJson", eFormJsonStr);
		
		Entity<Form> postEntity = Entity.form(httpForm);
		
		InputStream in = wt.request(MediaType.APPLICATION_XML).post(postEntity, InputStream.class);
		
		return in;
	}
}
