package gov.nih.tbi.service.impl;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.exceptions.InstancedDataException;
import gov.nih.tbi.repository.model.CellValue;
import gov.nih.tbi.repository.model.DataTableColumn;
import gov.nih.tbi.repository.model.InstancedRow;
import gov.nih.tbi.repository.model.NonRepeatingCellValue;
import gov.nih.tbi.service.BioSampleManager;
import gov.nih.tbi.service.cache.InstancedDataCache;
import gov.nih.tbi.service.model.DataCart;
import gov.nih.tbi.util.QueryRestProviderUtils;
import gov.nih.tbi.ws.provider.OrderManagerWebserviceProvider;

@Component
@Scope("application")
public class BioSampleManagerImpl implements BioSampleManager, Serializable {
	private static final long serialVersionUID = 7075162431015654191L;

	private static final Logger logger = Logger.getLogger(BioSampleManagerImpl.class);

	@Autowired
	ApplicationConstants constants;

	/**
	 * Returns a list of Coriell IDs from the current user's biosample queue
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Set<String> getBiosamplesFromQueue() throws UnsupportedEncodingException {
		OrderManagerWebserviceProvider orderManagerWebserviceProvider =
				new OrderManagerWebserviceProvider(constants.getQueryOrderQueueURL(),
						QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));
		return orderManagerWebserviceProvider.getBiosampleFromQueue(constants.getQueryOrderQueueURL());
	}

	public void addBiosampleToQueue(String formName, Map<String, String> biosampleRow)
			throws UnsupportedEncodingException {
		OrderManagerWebserviceProvider orderManagerWebserviceProvider =
				new OrderManagerWebserviceProvider(constants.getQueryOrderQueueURL(),
						QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));
		orderManagerWebserviceProvider.addBiosampleToQueue(formName, biosampleRow, constants.getQueryOrderManagreURL());
	}

	public Map<String, String> getNonRepeatingRowDataByUri(String formName, String rowUri, DataCart dataCart) {
		InstancedDataCache instancedDataCache = dataCart.getInstancedDataCache();
		if (instancedDataCache == null) {
			logger.info("The row cache is empty!");
			throw new InstancedDataException("The row cache is empty!");
		}

		Map<String, String> rowFieldValueMap = new LinkedHashMap<String, String>();

		InstancedRow row = instancedDataCache.getByFormName(formName).getByRowUri(rowUri);

		if (row != null) {
			for (Entry<DataTableColumn, CellValue> cellValueEntry : row.getCell().entrySet()) {
				CellValue cellValue = cellValueEntry.getValue();
				String columnName = cellValueEntry.getKey().toString();

				// type check the cell value since we only want to add non-repeating cells
				if (cellValue instanceof NonRepeatingCellValue) {
					NonRepeatingCellValue nrc = (NonRepeatingCellValue) cellValue;

					rowFieldValueMap.put(columnName, nrc.getValue());
				}
			}
		}

		return rowFieldValueMap;
	}
}
