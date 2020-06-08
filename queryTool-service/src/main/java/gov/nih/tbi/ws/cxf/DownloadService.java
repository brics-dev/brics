package gov.nih.tbi.ws.cxf;

import gov.nih.tbi.commons.util.ValUtil;
import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.exceptions.CSVGenerationException;
import gov.nih.tbi.exceptions.FilterEvaluatorException;
import gov.nih.tbi.pojo.CodeMapping;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.service.DownloadManager;
import gov.nih.tbi.service.cache.InstancedDataCache;
import gov.nih.tbi.service.model.DataCart;
import gov.nih.tbi.service.model.PermissionModel;
import gov.nih.tbi.util.DownloadUtil;
import gov.nih.tbi.util.InstanceSpecificQueryModifier;

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

	@Autowired
	ApplicationConstants constants;
	
	@Autowired
	InstanceSpecificQueryModifier instanceSpecificQueryModifier;

	@POST
	@Path("dataTable/download")
	@Produces(MediaType.APPLICATION_JSON)
	public Response downloadDataTable(@FormParam("packageName") String packageName,
			@FormParam("isNormalCSV") boolean isNormalCSV, @FormParam("filterExpression") String booleanExpression) {
		getAuthenticatedAccount();
		log.debug("Start downloading data table to queue, package name: " + packageName + ", isNormalCSV: "
				+ isNormalCSV);

		// Check if the package name is valid.
		String errorMsg = DownloadUtil.validatePackageName(packageName);
		if (!ValUtil.isBlank(errorMsg)) {
			throw new ForbiddenException(errorMsg);
		}

		// keep the package name here, since it could get overwritten by the UI before the querying finishes.
		// variable needs to be final because it is accessed within an inner class
		final String pkgName = packageName;
		final boolean isNormalCSVClone = isNormalCSV;
		instanceSpecificQueryModifier.modifyDataCart(dataCart);
		final InstancedDataTable instancedDataTable = new InstancedDataTable(dataCart.getInstancedDataTable());
		final List<FormResult> clonedForms = DownloadUtil.cloneFormResults(dataCart.getSelectedForms());
		final CodeMapping codeMapping = dataCart.getCodeMapping();
		final String userName = permissionModel.getUserName();
		final InstancedDataCache cache = new InstancedDataCache(dataCart.getInstancedDataCache());

		// Show AgeYrs in range for PDBP non-admin users
		final boolean showAgeRange = (QueryToolConstants.PDBP_ORG_NAME.equals(constants.getOrgName()))
				&& !permissionModel.isQueryAdmin() && !permissionModel.isSysAdmin();

		Thread downloadThread = new Thread(new Runnable() {
			public void run() {
				try {
					downloadManager.downloadDataTable(pkgName, instancedDataTable, clonedForms, codeMapping, userName,
							isNormalCSVClone, cache, booleanExpression, showAgeRange);
				} catch (CSVGenerationException e) {
					throw new InternalServerErrorException("Error occured while trying to generate the CSV", e);
				} catch (FilterEvaluatorException e) {
					throw new InternalServerErrorException("Error occured when evaluating the filters", e);
				}
			}
		});

		downloadThread.setName("ThreadWithTimeStamp" + System.currentTimeMillis());
		downloadThread.start();

		return respondEmptyOk();
	}


	@POST
	@Path("dataCart/download")
	@Produces(MediaType.APPLICATION_JSON)
	public Response downloadDataCart(@FormParam("packageName") String packageName,
			@FormParam("isNormalCSV") boolean isNormalCSV) {
		getAuthenticatedAccount();

		// Check if the package name is valid.
		String errorMsg = DownloadUtil.validatePackageName(packageName);
		if (!ValUtil.isBlank(errorMsg)) {
			Response errResponse = Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON)
					.entity(errorMsg).build();
			throw new ForbiddenException(errResponse);
		}

		// keep the package name here, since it could get overwritten by the UI before the querying finishes.
		// variable needs to be final because it is accessed within an inner class
		final String pkgName = packageName;
		final boolean isNormalCSVClone = isNormalCSV;
		instanceSpecificQueryModifier.modifyDataCart(dataCart);
		final List<FormResult> clonedForms = DownloadUtil.cloneFormResults(dataCart.getFormsInCart().values());
		final String userName = permissionModel.getUserName();
		final boolean showAgeRange = (QueryToolConstants.PDBP_ORG_NAME.equals(constants.getOrgName()))
				&& !permissionModel.isQueryAdmin() && !permissionModel.isSysAdmin();

		Thread downloadThread = new Thread(new Runnable() {
			public void run() {
				try {
					downloadManager.downloadDataCart(pkgName, clonedForms, userName, isNormalCSVClone, showAgeRange);
				} catch (FilterEvaluatorException e) {
					throw new InternalServerErrorException("Error occured when evaluating the filters", e);
				}
			}
		});

		downloadThread.setName("ThreadWithTimeStamp" + System.currentTimeMillis());
		downloadThread.start();


		return respondEmptyOk();
	}
}
