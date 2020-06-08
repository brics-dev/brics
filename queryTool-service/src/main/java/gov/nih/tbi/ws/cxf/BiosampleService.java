package gov.nih.tbi.ws.cxf;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.openrdf.http.protocol.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.exceptions.InstancedDataException;
import gov.nih.tbi.repository.model.CellValue;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.InstancedRecord;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;
import gov.nih.tbi.service.BioSampleManager;
import gov.nih.tbi.service.InstancedDataManager;
import gov.nih.tbi.service.model.DataCart;
import gov.nih.tbi.service.model.PermissionModel;
import gov.nih.tbi.util.DataCartUtil;

@Path("/biosample")
public class BiosampleService extends QueryBaseRestService {
	private static final Logger logger = Logger.getLogger(BiosampleService.class);

	@Autowired
	ApplicationConstants constants;

	@Autowired
	BioSampleManager bioSampleManager;

	@Autowired
	InstancedDataManager instancedDataManager;

	@Autowired
	DataCart dataCart;

	@Autowired
	PermissionModel permissionModel;

	@POST
	@Path("/addToQueue")
	// @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addToQueue(@FormParam("formName") String formName, @FormParam("rowUri") String rowUri,
			@FormParam("nrc") String nrc, @FormParam("isDerived") Boolean isDerived)
			throws WebApplicationException, UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		try {
			Map<String, String> biosampleRow = bioSampleManager.getNonRepeatingRowDataByUri(formName, rowUri, dataCart);
			// check for duplicate biosample in queue
			String biosampleId = DataCartUtil.getBiosampleId(formName, dataCart, rowUri);
			Set<String> biosampleIds = new HashSet<String>();
			biosampleIds.add(biosampleId);
			Set<String> biosamplesFromQueue = bioSampleManager.getBiosamplesFromQueue();
			biosampleIds.retainAll(biosamplesFromQueue);
			if (biosampleIds.isEmpty()) {
				bioSampleManager.addBiosampleToQueue(formName, biosampleRow);
				String successJson = "{\"success\":\"unique\"}";
				return Response.ok(successJson, MediaType.APPLICATION_JSON).build();
			} else {

				String warningJson = "{\"success\":\"duplicate\"}";
				return Response.ok(warningJson, MediaType.APPLICATION_JSON).build();
			}

		} catch (Exception e) {
			// UnsupportedEncodingException is the only one that is explicitly thrown
			// but I wanted to handle all
			String msg = "Couldn't add the sample to the queue.";
			logger.error(msg, e);
			throw new BadRequestException(msg, e);
		}
	}

	// TODO: FC - We really should not be adding a single biosample at a time with webservice calls. Probably should
	// batch them.
	@POST
	@Path("/addManyToQueue")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addManyToQueue(@FormParam("rowUris") String rowUris, @FormParam("selectAll") String selectAll,
			@FormParam("bioFormName") String bioFormName, @FormParam("unselectedRowUris") String unselectedRowUris)
			throws WebApplicationException, UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();

		List<String> optionSet = new ArrayList<String>();
		if (rowUris != null && !rowUris.isEmpty()) {
			String[] optionArr = rowUris.split(",");
			optionSet.addAll(Arrays.asList(optionArr));
		}

		Map<String, String> rowUriFormMap = new HashMap<String, String>();

		for (String key : optionSet) {
			String[] sampleVars = key.split("\\|");
			String rowUri = sampleVars[0];
			String formName = sampleVars[1];
			rowUriFormMap.put(rowUri, formName);
		}

		if (Boolean.valueOf(selectAll)) {
			// get unselected rowUris
			List<String> unselectedOptionSet = new ArrayList<String>();
			if (unselectedRowUris != null && !unselectedRowUris.isEmpty()) {
				String[] unselectedOptionArr = unselectedRowUris.split(",");
				unselectedOptionSet.addAll(Arrays.asList(unselectedOptionArr));
			}

			Map<String, String> unselectedRowUriFormMap = new HashMap<String, String>();

			for (String key : unselectedOptionSet) {
				String[] sampleVars = key.split("\\|");
				String rowUri = sampleVars[0];
				String formName = sampleVars[1];
				unselectedRowUriFormMap.put(rowUri, formName);
			}

			List<InstancedRecord> joinedList = dataCart.getInstancedDataCache().getResultCache();
			for (InstancedRecord ir : joinedList) {
				// filter rows
				// FILTER rows from unselected
				List<InstancedRow> rowList =
						ir.getSelectedRows().stream().filter(x -> unselectedRowUriFormMap.get(x.getRowUri()) == null)
								.collect(Collectors.<InstancedRow>toList());
				for (InstancedRow irow : rowList) {
					if (irow != null) {
						if (irow.getFormShortName().equals(bioFormName)) {
							String rowUri = irow.getRowUri();
							String formName = bioFormName;
							Map<String, String> biosampleRow =
									bioSampleManager.getNonRepeatingRowDataByUri(formName, rowUri, dataCart);
							bioSampleManager.addBiosampleToQueue(formName, biosampleRow);
						}

					}
				}

			}
		} else {
			for (Entry<String, String> rowUriFormEntry : rowUriFormMap.entrySet()) {
				String rowUri = rowUriFormEntry.getKey();
				String formName = rowUriFormEntry.getValue();
				Map<String, String> biosampleRow =
						bioSampleManager.getNonRepeatingRowDataByUri(formName, rowUri, dataCart);
				bioSampleManager.addBiosampleToQueue(formName, biosampleRow);
			}
		}

		String successJson = "{\"success\":\"unique\"}";
		return Response.ok(successJson, MediaType.APPLICATION_JSON).build();
	}

	@POST
	@Path("/validateAddManyToQueue")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateAddManyToQueue(@FormParam("rowUris") String rowUris,
			@FormParam("selectAll") String selectAll, @FormParam("bioFormName") String bioFormName,
			@FormParam("unselectedRowUris") String unselectedRowUris)
			throws WebApplicationException, UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		
		Map<String, String> unselectedRowUriFormMap = null;
		Map<String, String> rowUriFormMap = null;
		if (Boolean.valueOf(selectAll)) {
			// get unselected rowUris
			List<String> unselectedOptionSet = new ArrayList<String>();
			if (unselectedRowUris != null && !unselectedRowUris.isEmpty()) {
				String[] unselectedOptionArr = unselectedRowUris.split(",");
				unselectedOptionSet.addAll(Arrays.asList(unselectedOptionArr));
			}

			unselectedRowUriFormMap = new HashMap<String, String>();

			for (String key : unselectedOptionSet) {
				String[] sampleVars = key.split("\\|");
				String rowUri = sampleVars[0];
				String formName = sampleVars[1];
				unselectedRowUriFormMap.put(rowUri, formName);
			}
		} else {

			// get selected rowUris
			List<String> optionSet = new ArrayList<String>();
			if (rowUris != null && !rowUris.isEmpty()) {
				String[] optionArr = rowUris.split(",");
				optionSet.addAll(Arrays.asList(optionArr));
			}

			rowUriFormMap = new HashMap<String, String>();

			for (String key : optionSet) {
				String[] sampleVars = key.split("\\|");
				String rowUri = sampleVars[0];
				String formName = sampleVars[1];
				rowUriFormMap.put(rowUri, formName);
			}
		}

		Set<String> biosampleIds;
		if (Boolean.valueOf(selectAll)) {

			// get list of all biosample Ids from the ones that we are trying to add that have
			// already been added, also ignoring the ones we don't want
			biosampleIds = getAllBiosampleIdsInverse(dataCart, unselectedRowUriFormMap, bioFormName);
		} else {

			// get list of biosample Ids from the ones that we are trying to add that have
			// already been added
			biosampleIds = DataCartUtil.getBiosampleIds(dataCart, rowUriFormMap);

		}

		Set<String> biosamplesFromQueue = bioSampleManager.getBiosamplesFromQueue();
		biosampleIds.retainAll(biosamplesFromQueue);

		if (!biosampleIds.isEmpty()) {
			List<String> alreadyAddedBiosamples = new ArrayList<String>(biosampleIds);
			String json = new Gson().toJson(alreadyAddedBiosamples);
			String successJson = "{\"success\":\"duplicates\",\"rowUris\":" + json + "}";
			return Response.ok(successJson, MediaType.APPLICATION_JSON).build();

		} else {
			String successJson = "{\"success\":\"unique\"}";
			return Response.ok(successJson, MediaType.APPLICATION_JSON).build();
		}
	}

	/**
	 * Given a list of biosample IDs not selected by the user, this will return all of the biosample IDs that should be
	 * selected.
	 * 
	 * @param dataCart
	 * @param unselectedRowUriFormMap
	 * @param bioFormName
	 * @return
	 */
	protected Set<String> getAllBiosampleIdsInverse(DataCart dataCart, Map<String, String> unselectedRowUriFormMap,
			String bioFormName) {

		if (dataCart.getInstancedDataCache() == null) {
			throw new InstancedDataException("The row cache is empty!");
		}
		
		Set<String> biosampleIds = new HashSet<String>();

		List<InstancedRecord> records = dataCart.getInstancedDataCache().getResultCache();
		for (InstancedRecord ir : records) {

			// FILTER rows from unselected
			List<InstancedRow> rowList =
					ir.getSelectedRows().stream().filter(x -> unselectedRowUriFormMap.get(x.getRowUri()) == null)
							.collect(Collectors.<InstancedRow>toList());
			
			for (InstancedRow irow : rowList) {
				if (irow != null) {
					if (irow.getFormShortName().equals(bioFormName)) {
						for (Entry<DataTableColumn, CellValue> cellValueEntry : irow.getCell().entrySet()) {
							CellValue cellValue = cellValueEntry.getValue();
							// type check the cell value since we only want to add non-repeating cells
							if (cellValue instanceof NonRepeatingCellValue) {
								NonRepeatingCellValue nrc = (NonRepeatingCellValue) cellValue;
									if (DataType.BIOSAMPLE == nrc.getDataElementType()) {
									biosampleIds.add(nrc.getValue());
									break;
								}
							}
						}

					}
				}
			}
		}

		return biosampleIds;
	}
}
