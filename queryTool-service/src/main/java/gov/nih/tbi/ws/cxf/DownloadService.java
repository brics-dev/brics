package gov.nih.tbi.ws.cxf;

import gov.nih.tbi.commons.util.ValUtil;
import gov.nih.tbi.exceptions.CSVGenerationException;
import gov.nih.tbi.pojo.CodeMapping;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.service.DownloadManager;
import gov.nih.tbi.service.cache.InstancedDataCache;
import gov.nih.tbi.service.model.DataCart;
import gov.nih.tbi.service.model.PermissionModel;
import gov.nih.tbi.util.DownloadUtil;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.FormParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.openrdf.http.protocol.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;

@Path("/download/")
public class DownloadService extends QueryBaseRestService {

	private static final Logger log = Logger.getLogger(DownloadService.class);

	@Autowired
	DataCart dataCart;

	@Autowired
	DownloadManager downloadManager;

	@Autowired
	PermissionModel permissionModel;

	@POST
	@Path("dataTable/download")
	@Produces(MediaType.APPLICATION_JSON)
	public Response downloadDataTable(@FormParam("packageName") String packageName,
			@FormParam("isNormalCSV") boolean isNormalCSV) throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		log.debug("Start downloading data table to queue, package name: " + packageName + ", isNormalCSV: "
				+ isNormalCSV);

		// Check if the package name is valid.
		String errorMsg = DownloadUtil.validatePackageName(packageName);
		if (!ValUtil.isBlank(errorMsg)) {
			Response errResponse = Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON)
					.entity(errorMsg).build();
			throw new ForbiddenException(errResponse);
		}

		try {
			// keep the package name here, since it could get overwritten by the UI before the querying finishes.
			// variable needs to be final because it is accessed within an inner class
			final String pkgName = packageName;
			final boolean isNormalCSVClone = isNormalCSV;
			final InstancedDataTable instancedDataTable = new InstancedDataTable(dataCart.getInstancedDataTable());
			final List<FormResult> clonedForms = DownloadUtil.cloneFormResults(dataCart.getSelectedForms());
			final CodeMapping codeMapping = dataCart.getCodeMapping();
			final String userName = permissionModel.getUserName();
			final InstancedDataCache cache = dataCart.getInstancedDataCache();

			Thread downloadThread = new Thread(new Runnable() {
				public void run() {
					try {
						downloadManager.downloadDataTable(pkgName, instancedDataTable, clonedForms, codeMapping,
								userName, isNormalCSVClone, cache);
					} catch (CSVGenerationException e) {
						log.error("Error occured when trying to triggering QT download", e);
					}
				}
			});

			downloadThread.setName("ThreadWithTimeStamp" + System.currentTimeMillis());
			downloadThread.start();

		} catch (Exception e) {
			e.printStackTrace();
			errorMsg = "Exception thrown when downloading to queue " + e.getMessage();
			Response errResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.type(MediaType.APPLICATION_JSON).entity(errorMsg).build();
			throw new InternalServerErrorException(errResponse);
		}

		return respondEmptyOk();
	}


	@POST
	@Path("dataCart/download")
	@Produces(MediaType.APPLICATION_JSON)
	public Response downloadDataCart(@FormParam("packageName") String packageName,
			@FormParam("isNormalCSV") boolean isNormalCSV) throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		// Check if the package name is valid.
		String errorMsg = DownloadUtil.validatePackageName(packageName);
		if (!ValUtil.isBlank(errorMsg)) {
			Response errResponse = Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON)
					.entity(errorMsg).build();
			throw new ForbiddenException(errResponse);
		}

		try {
			// keep the package name here, since it could get overwritten by the UI before the querying finishes.
			// variable needs to be final because it is accessed within an inner class
			final String pkgName = packageName;
			final boolean isNormalCSVClone = isNormalCSV;
			final List<FormResult> clonedForms = DownloadUtil.cloneFormResults(dataCart.getFormsInCart().values());
			final String userName = permissionModel.getUserName();

			Thread downloadThread = new Thread(new Runnable() {
				public void run() {
					downloadManager.downloadDataCart(pkgName, clonedForms, userName, isNormalCSVClone);
				}
			});

			downloadThread.setName("ThreadWithTimeStamp" + System.currentTimeMillis());
			downloadThread.start();

		} catch (Exception e) {
			e.printStackTrace();
			errorMsg = "Exception thrown when downloading data cart to queue " + e.getMessage();
			Response errResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.type(MediaType.APPLICATION_JSON).entity(errorMsg).build();
			throw new InternalServerErrorException(errResponse);
		}

		return respondEmptyOk();
	}

	// @GET
	// @Path("executeRScript")
	// public byte[] executeRScript() throws CSVGenerationException{
	//
	// final List<FormResult> clonedForms = DownloadUtil.cloneFormResults(dataCart.getSelectedForms());
	// final String userName = permissionModel.getUserName();
	//
	// InstancedDataTable dataTable = downloadManager.generateInstancedDataTable(clonedForms, userName);
	//
	// byte[] dataBytes = CSVGenerator.generateCSV(dataTable);
	//
	// //Use this line for flattened csv format
	//// byte[] dataBytes = CSVGenerator.generateFlattenedCSV(dataTable);
	//
	// return dataBytes;
	//
	// }

}
