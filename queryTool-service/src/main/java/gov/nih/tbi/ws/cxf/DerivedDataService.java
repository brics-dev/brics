package gov.nih.tbi.ws.cxf;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.query.model.DerivedDataContainer;
import gov.nih.tbi.query.model.DerivedDataKey;
import gov.nih.tbi.query.model.DerivedDataRequest;
import gov.nih.tbi.query.model.DerivedDataRow;
import gov.nih.tbi.query.model.RepeatableGroupDataElement;
import gov.nih.tbi.service.DerivedDataManager;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.openrdf.http.protocol.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;

public class DerivedDataService extends QueryBaseRestService {

	@Autowired
	private DerivedDataManager derivedDataManager;

	/**
	 * Webservice primarily used by the import-RESTful to get derived data from the query tool.
	 * 
	 * @param request
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws UnauthorizedException 
	 */
	@POST
	@Path("/search")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public DerivedDataContainer getDerivedData(DerivedDataRequest request) throws UnauthorizedException, UnsupportedEncodingException {
		// break up the request first into separate arguements to be passed to derived data manager
		NameAndVersion formNameAndVersion = request.getFormNameAndVersion();
		List<RepeatableGroupDataElement> repeatableGroupDataElements = request.getRepeatableGroupDataElementRequests();
		Set<String> guids = request.getGuids();

		// get the derived data map
		HashMap<DerivedDataKey, DerivedDataRow> derivedDataMap =
				(HashMap<DerivedDataKey, DerivedDataRow>) derivedDataManager.getDerivedData(formNameAndVersion,
						repeatableGroupDataElements, guids);

		// put it into the JAXB container to send back to provider
		DerivedDataContainer derivedDataContainer = new DerivedDataContainer();
		derivedDataContainer.setDataMap(derivedDataMap);
		return derivedDataContainer;
	}
}
