package gov.nih.tbi.ws.cxf;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;
import org.openrdf.http.protocol.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.semantic.model.QueryPermissions;
import gov.nih.tbi.semantic.model.QueryPermissions.FormResultPermission;
import gov.nih.tbi.semantic.model.QueryPermissions.StudyResultPermission;
import gov.nih.tbi.service.model.PermissionModel;
import gov.nih.tbi.util.ReportingRestProviderUtils;
import gov.nih.tbi.ws.provider.RestReportingAccountProvider;

public class ReportingInitializeService extends ReportingBaseRestService {

	private final static Logger logger = Logger.getLogger(ReportingInitializeService.class);

	@Autowired
	PermissionModel permissionModel;

	@GET
	@Path("initialize/")
	@Produces(MediaType.APPLICATION_XML)
	public Response initialize(@QueryParam("savedQueryId") String savedQueryId)
			throws UnsupportedEncodingException, UnauthorizedException {

		logger.info("Initializing PermissionModel ...");
		Account account = getAuthenticatedAccount();

		if (account == null || ANONYMOUS_USER_NAME.equals(account.getUserName())) {
			URI uri = UriBuilder.fromUri("../cas/logout").build();
			return Response.temporaryRedirect(uri).build();
		}

		URI uri = UriBuilder.fromUri("../reporting/studyReportingListAction!list.action").build();
		return Response.temporaryRedirect(uri).build();
	}

	@SuppressWarnings("unused")
	private void initializePermission(Account account)
			throws UnsupportedEncodingException, WebApplicationException, UnauthorizedException {
		getAuthenticatedAccount();
		permissionModel.setAccount(account);

		String accUrl = applicationConstants.getModulesAccountURL();
		logger.error("This is the accUrl var: " + accUrl);
		RestReportingAccountProvider accountProvider = new RestReportingAccountProvider(accUrl,
				ReportingRestProviderUtils.getProxyTicket(accUrl));

		QueryPermissions queryPermissions = accountProvider.getQueryPermission(account,
				applicationConstants.getPermissionsWebserviceURL());

		for (StudyResultPermission srp : queryPermissions.getStudyResultPermissions()) {
			permissionModel.addStudyResultPermission(srp);
		}
		for (FormResultPermission frp : queryPermissions.getFormResultPermissions()) {
			permissionModel.addFormResultPermission(frp);
		}

	}

}
