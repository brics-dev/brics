package gov.nih.nichd.ctdb.common.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ResponseProcessingException;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.CasProxyTicketException;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.protocol.domain.PrepopDataElement;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.ws.clients.DictionaryWSProvider;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;

public class DictionaryUtil {
	private static final Logger logger = Logger.getLogger(DictionaryUtil.class);

	public DictionaryUtil() {}
	
	/**
	 * Creates a map of data elements that are associated with all pre-population data elements in the system.
	 * The data element objects are queried from the Dictionary web service.
	 * 
	 * @param request - The HTTP request object from the calling action class.
	 * @return A map of all DataElements that exist in the Dictionary that corresponds to all supported pre-population
	 * data element data in the system.
	 * @throws CtdbException When there is an error while looking up the list of all pre-population data elements.
	 */
	public Map<String, DataElement> getDeMapForAllPrePopDes(HttpServletRequest request) throws CtdbException {
		Map<String, DataElement> deCache = new HashMap<String, DataElement>();
		ProtocolManager pm = new ProtocolManager();
		List<PrepopDataElement> prePopDeList = pm.getAllPrepopDEs(false);
		DictionaryWSProvider dictClient = new DictionaryWSProvider(request);
		
		logger.info("Creating the data element cache...");
		
		// Add the DataElement object associated to each pre-pop data element in the system in the cache.
		for ( PrepopDataElement pde : prePopDeList ) {
			try {
				DataElement de = dictClient.getDataElementByName(pde.getShortName());
				
				if ( de != null ) {
					logger.debug("Adding data element " + pde.getShortName() + " to the cache.");
    				deCache.put(pde.getShortName(), de);
				}
				else {
					logger.warn("Couldn't find the pre-pop DE, " + pde.getShortName() + ". It will be ignored.");
				}
			}
			catch ( CasProxyTicketException | WebApplicationException | ResponseProcessingException e ) {
				logger.warn("Couldn't get data element from the dictionary web service because \"" + 
						e.getMessage() + ".\" Ignoring the " + pde.getShortName() + " pre-pop DE.");
			}
		}
		
		return deCache;
	}

}
