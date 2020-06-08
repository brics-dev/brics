package gov.nih.nichd.ctdb.protocol.domain;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import gov.nih.tbi.commons.service.ServiceConstants;

public class CsvImportUtil {
	private static final long serialVersionUID = 6360470100879672871L;
	private static final Logger logger = Logger.getLogger(CsvImportUtil.class);
	
	private Map<String, Integer> headersMap = new HashMap<String, Integer>();;
	
	public CsvImportUtil(String[] headers) {
		initializeHeaders(headers);
	}

	private void initializeHeaders(String[] headers) {
		// the headers could be out of order but still valid, so handle that
		for (int i = 0; i < headers.length; i++) {
			headersMap.put(headers[i].toLowerCase().trim(), i);
		}
	}
	
	public Map<String, Integer> getCsvHeaders() {
		return this.headersMap;
	}

	public String getValueByHeader(String[] line, String headerName) {
		// The different cases in header should not affect
		Integer mappingIndex = headersMap.get(headerName.toLowerCase());

		if (mappingIndex == null || line.length <= mappingIndex) {
			return null;
		} else {
			return line[mappingIndex].trim();
		}
	}

	public String generateRowHash(String[] rowElements) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < rowElements.length; i++) {
			String currentElement = rowElements[i];
			sb.append(currentElement).append("_");
		}

		if (sb.length() > 0) {
			sb.replace(sb.length() - 1, sb.length(), ServiceConstants.EMPTY_STRING);
		}

		return sb.toString();
	}
}
