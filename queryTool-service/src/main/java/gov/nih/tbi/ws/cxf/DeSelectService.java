package gov.nih.tbi.ws.cxf;

import gov.nih.tbi.pojo.DeSelectSearch;
import gov.nih.tbi.service.DeSelectResultManager;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.openrdf.http.protocol.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Path("/deSelect/")
public class DeSelectService extends QueryBaseRestService {

	private static final Logger logger = Logger.getLogger(DeSelectService.class);

	@Autowired
	DeSelectResultManager deSelectResultManager;

	/**
	 * Returns the diseases and populations filter options in Json format as following: { "populations": ["population1",
	 * "population2", ...], "diseases": ["disease1", "disease2", ...] }
	 * 
	 * @return filter options for diseases and populations
	 * @throws UnsupportedEncodingException 
	 * @throws UnauthorizedException 
	 */
	@GET
	@Path("deSelectFilterOptions/")
	@Produces(MediaType.APPLICATION_JSON)
	// http://pdbp-portal-local.cit.nih.gov:8080/query/services/query/studyResults/
	public String getDeSelectFilterOptions() throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		JsonObject filterOptionsJson = deSelectResultManager.getDeSelectFilterOptions();
		return filterOptionsJson.toString();
	}

	@POST
	@Path("searchDeSelected/")
	@Produces(MediaType.APPLICATION_JSON)
	public String searchDeSelected(@FormParam("searchPhrase") String searchPhrase,
			@FormParam("wholeWord") String wholeWord, @FormParam("locations[]") List<String> locations,
			@FormParam("elementTypes[]") List<String> elementTypes, @FormParam("diseases[]") List<String> diseases,
			@FormParam("populations[]") List<String> populations, @FormParam("sEcho") Integer sEcho,
			@FormParam("iDisplayStart") String iDisplayStart, @FormParam("iSortCol_0") Integer iSortCol_0,
			@FormParam("sSortDir_0") String sSortDir_0, @FormParam("iDisplayLength") Integer iDisplayLength) throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		DeSelectSearch searchParameters = new DeSelectSearch();

		searchParameters.setSearchPhrase(searchPhrase);
		searchParameters.setWholeWordSearch(wholeWord);
		searchParameters.setSearchLocations(locations);
		searchParameters.setElementTypes(elementTypes);
		searchParameters.setDiseases(diseases);
		searchParameters.setPopulations(populations);

		searchParameters.setsEcho(sEcho);
		searchParameters.setPageOffset(iDisplayStart);
		searchParameters.setSortColumnIndex(iSortCol_0);
		searchParameters.setSortDirection(sSortDir_0);
		searchParameters.setCountPerPage(iDisplayLength);

		JsonArray elementsJson = deSelectResultManager.searchDeSelect(searchParameters);
		int count = deSelectResultManager.countQueryElements(searchParameters);
		logger.debug("countQueryElements returns " + count);

		JsonObject output = new JsonObject();
		output.add("aaData", elementsJson);
		output.addProperty("sEcho", searchParameters.getsEcho());
		output.addProperty("iTotalRecords", count);
		output.addProperty("iTotalDisplayRecords", count);

		return output.toString();
	}

}
